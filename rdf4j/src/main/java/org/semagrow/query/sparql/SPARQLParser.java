package org.semagrow.query.sparql;


import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semagrow.model.SemagrowValueFactory;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.query.Dataset;
import org.eclipse.rdf4j.query.IncompatibleOperationException;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.algebra.TupleExpr;
import org.eclipse.rdf4j.query.algebra.UpdateExpr;
import org.eclipse.rdf4j.query.parser.ParsedBooleanQuery;
import org.eclipse.rdf4j.query.parser.ParsedDescribeQuery;
import org.eclipse.rdf4j.query.parser.ParsedGraphQuery;
import org.eclipse.rdf4j.query.parser.ParsedQuery;
import org.eclipse.rdf4j.query.parser.ParsedTupleQuery;
import org.eclipse.rdf4j.query.parser.ParsedUpdate;
import org.eclipse.rdf4j.query.parser.QueryParser;
import org.eclipse.rdf4j.query.parser.sparql.*;
import org.eclipse.rdf4j.query.parser.sparql.ast.ASTAskQuery;
import org.eclipse.rdf4j.query.parser.sparql.ast.ASTConstructQuery;
import org.eclipse.rdf4j.query.parser.sparql.ast.ASTDescribeQuery;
import org.eclipse.rdf4j.query.parser.sparql.ast.ASTInsertData;
import org.eclipse.rdf4j.query.parser.sparql.ast.ASTPrefixDecl;
import org.eclipse.rdf4j.query.parser.sparql.ast.ASTQuery;
import org.eclipse.rdf4j.query.parser.sparql.ast.ASTQueryContainer;
import org.eclipse.rdf4j.query.parser.sparql.ast.ASTSelectQuery;
import org.eclipse.rdf4j.query.parser.sparql.ast.ASTUpdate;
import org.eclipse.rdf4j.query.parser.sparql.ast.ASTUpdateContainer;
import org.eclipse.rdf4j.query.parser.sparql.ast.ASTUpdateSequence;
import org.eclipse.rdf4j.query.parser.sparql.ast.Node;
import org.eclipse.rdf4j.query.parser.sparql.ast.ParseException;
import org.eclipse.rdf4j.query.parser.sparql.ast.SyntaxTreeBuilder;
import org.eclipse.rdf4j.query.parser.sparql.ast.TokenMgrError;
import org.eclipse.rdf4j.query.parser.sparql.ast.VisitorException;

public class SPARQLParser implements QueryParser {

    private ValueFactory valueFactory;

    protected SPARQLParser() {
       valueFactory = SemagrowValueFactory.getInstance();
    }

    public void setValueFactory(ValueFactory vf) { valueFactory = vf; }

    public ValueFactory getValueFactory() { return valueFactory; }

    @Override
    public ParsedUpdate parseUpdate(String updateStr, String baseURI)
            throws MalformedQueryException
    {
        try {

            ParsedUpdate update = new ParsedUpdate(updateStr);

            ASTUpdateSequence updateSequence = SyntaxTreeBuilder.parseUpdateSequence(updateStr);

            List<ASTUpdateContainer> updateOperations = updateSequence.getUpdateContainers();

            List<ASTPrefixDecl> sharedPrefixDeclarations = null;

            Node node = updateSequence.jjtGetChild(0);

            Set<String> globalUsedBNodeIds = new HashSet<String>();
            for (int i = 0; i < updateOperations.size(); i++) {

                ASTUpdateContainer uc = updateOperations.get(i);

                if (uc.jjtGetNumChildren() == 0 && i > 0 && i < updateOperations.size() - 1) {
                    // empty update in the middle of the sequence
                    throw new MalformedQueryException("empty update in sequence not allowed");
                }

                StringEscapesProcessor.process(uc);
                BaseDeclProcessor.process(uc, baseURI);

                // do a special dance to handle prefix declarations in sequences: if
                // the current
                // operation has its own prefix declarations, use those. Otherwise,
                // try and use
                // prefix declarations from a previous operation in this sequence.
                List<ASTPrefixDecl> prefixDeclList = uc.getPrefixDeclList();
                if (prefixDeclList == null || prefixDeclList.size() == 0) {
                    if (sharedPrefixDeclarations != null) {
                        for (ASTPrefixDecl prefixDecl : sharedPrefixDeclarations) {
                            uc.jjtAppendChild(prefixDecl);
                        }
                    }
                }
                else {
                    sharedPrefixDeclarations = prefixDeclList;
                }

                PrefixDeclProcessor.process(uc);
                Set<String> usedBNodeIds = BlankNodeVarProcessor.process(uc);

                if (uc.getUpdate() instanceof ASTInsertData || uc.getUpdate() instanceof ASTInsertData) {
                    if (Collections.disjoint(usedBNodeIds, globalUsedBNodeIds)) {
                        globalUsedBNodeIds.addAll(usedBNodeIds);
                    }
                    else {
                        throw new MalformedQueryException(
                                "blank node identifier may not be shared across INSERT/DELETE DATA operations");
                    }
                }

                UpdateExprBuilder updateExprBuilder = new UpdateExprBuilder(getValueFactory());

                ASTUpdate updateNode = uc.getUpdate();
                if (updateNode != null) {
                    UpdateExpr updateExpr = (UpdateExpr)updateNode.jjtAccept(updateExprBuilder, null);

                    // add individual update expression to ParsedUpdate sequence
                    // container
                    update.addUpdateExpr(updateExpr);

                    // associate updateExpr with the correct dataset (if any)
                    Dataset dataset = DatasetDeclProcessor.process(uc);
                    update.map(updateExpr, dataset);
                }
            } // end for

            return update;
        }
        catch (ParseException e) {
            throw new MalformedQueryException(e.getMessage(), e);
        }
        catch (TokenMgrError e) {
            throw new MalformedQueryException(e.getMessage(), e);
        }
        catch (VisitorException e) {
            throw new MalformedQueryException(e.getMessage(), e);
        }

    }

    @Override
    public ParsedQuery parseQuery(String queryStr, String baseURI)
            throws MalformedQueryException
    {
        try {
            ASTQueryContainer qc = SyntaxTreeBuilder.parseQuery(queryStr);
            StringEscapesProcessor.process(qc);
            BaseDeclProcessor.process(qc, baseURI);
            Map<String, String> prefixes = PrefixDeclProcessor.process(qc);
            WildcardProjectionProcessor.process(qc);
            BlankNodeVarProcessor.process(qc);

            if (qc.containsQuery()) {

                // handle query operation

                TupleExpr tupleExpr = buildQueryModel(qc);

                ParsedQuery query;

                ASTQuery queryNode = qc.getQuery();
                if (queryNode instanceof ASTSelectQuery) {
                    query = new ParsedTupleQuery(queryStr, tupleExpr);
                }
                else if (queryNode instanceof ASTConstructQuery) {
                    query = new ParsedGraphQuery(queryStr, tupleExpr, prefixes);
                }
                else if (queryNode instanceof ASTAskQuery) {
                    query = new ParsedBooleanQuery(queryStr, tupleExpr);
                }
                else if (queryNode instanceof ASTDescribeQuery) {
                    query = new ParsedDescribeQuery(queryStr, tupleExpr, prefixes);
                }
                else {
                    throw new RuntimeException("Unexpected query type: " + queryNode.getClass());
                }

                // Handle dataset declaration
                Dataset dataset = DatasetDeclProcessor.process(qc);
                if (dataset != null) {
                    query.setDataset(dataset);
                }

                return query;
            }
            else {
                throw new IncompatibleOperationException("supplied string is not a query operation");
            }
        }
        catch (ParseException e) {
            throw new MalformedQueryException(e.getMessage(), e);
        }
        catch (TokenMgrError e) {
            throw new MalformedQueryException(e.getMessage(), e);
        }
    }

    private TupleExpr buildQueryModel(Node qc)
            throws MalformedQueryException
    {
        TupleExprBuilder tupleExprBuilder = new TupleExprBuilder(getValueFactory());
        try {
            return (TupleExpr)qc.jjtAccept(tupleExprBuilder, null);
        }
        catch (VisitorException e) {
            throw new MalformedQueryException(e.getMessage(), e);
        }
    }

}