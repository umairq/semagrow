package org.semagrow.evaluation.rxjava;

import org.semagrow.evaluation.EvaluationStrategy;
import org.semagrow.evaluation.IterationPublisher;
import org.semagrow.evaluation.util.QueryEvaluationUtil;
import org.semagrow.evaluation.util.SimpleBindingSetOps;
import org.semagrow.evaluation.BindingSetOps;
import org.eclipse.rdf4j.common.iteration.Iteration;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.Dataset;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.algebra.*;
import org.eclipse.rdf4j.query.algebra.evaluation.QueryBindingSet;
import org.eclipse.rdf4j.query.algebra.evaluation.TripleSource;
import org.eclipse.rdf4j.query.algebra.evaluation.federation.FederatedServiceResolverImpl;
import org.eclipse.rdf4j.query.algebra.evaluation.impl.ExternalSet;
import org.eclipse.rdf4j.query.algebra.evaluation.util.OrderComparator;
import org.eclipse.rdf4j.query.algebra.evaluation.util.ValueComparator;
import org.eclipse.rdf4j.util.iterators.Iterators;
import org.reactivestreams.Publisher;
import rx.Observable;
import rx.RxReactiveStreams;

import java.util.*;

/**
 * Created by angel on 11/22/14.
 */
public class EvaluationStrategyImpl implements EvaluationStrategy
{

    private org.eclipse.rdf4j.query.algebra.evaluation.EvaluationStrategy evalStrategy;
    protected BindingSetOps bindingSetOps = SimpleBindingSetOps.getInstance();

    public EvaluationStrategyImpl(TripleSource tripleSource, Dataset dataset) {

        evalStrategy = new org.eclipse.rdf4j.query.algebra.evaluation.impl.SimpleEvaluationStrategy(tripleSource, dataset, new FederatedServiceResolverImpl());
    }

    public EvaluationStrategyImpl(TripleSource tripleSource) {

        evalStrategy = new org.eclipse.rdf4j.query.algebra.evaluation.impl.SimpleEvaluationStrategy(tripleSource, new FederatedServiceResolverImpl());

    }

    public Publisher<BindingSet> evaluate(TupleExpr expr, BindingSet bindings)
        throws QueryEvaluationException
    {
        return RxReactiveStreams.toPublisher(evaluateReactiveInternal(expr, bindings));
    }

    public boolean isTrue(ValueExpr expr, BindingSet bindings)
            throws QueryEvaluationException
    {
        return evalStrategy.isTrue(expr, bindings);
    }

    public Value evaluate(ValueExpr expr, BindingSet bindings)
            throws QueryEvaluationException
    {
        return evalStrategy.evaluate(expr, bindings);
    }

    public Observable<BindingSet> evaluateReactiveInternal(TupleExpr expr, BindingSet bindings)
            throws QueryEvaluationException
    {
        if (expr instanceof StatementPattern) {
            return evaluateReactiveInternal((StatementPattern) expr, bindings);
        }
        else if (expr instanceof UnaryTupleOperator) {
            return evaluateReactiveInternal((UnaryTupleOperator) expr, bindings);
        }
        else if (expr instanceof BinaryTupleOperator) {
            return evaluateReactiveInternal((BinaryTupleOperator) expr, bindings);
        }
        else if (expr instanceof SingletonSet) {
            return evaluateReactiveInternal((SingletonSet) expr, bindings);
        }
        else if (expr instanceof EmptySet) {
            return evaluateReactiveInternal((EmptySet) expr, bindings);
        }
        else if (expr instanceof ExternalSet) {
            return evaluateReactiveInternal((ExternalSet) expr, bindings);
        }
        else if (expr instanceof ZeroLengthPath) {
            return evaluateReactiveInternal((ZeroLengthPath) expr, bindings);
        }
        else if (expr instanceof ArbitraryLengthPath) {
            return evaluateReactiveInternal((ArbitraryLengthPath) expr, bindings);
        }
        else if (expr instanceof BindingSetAssignment) {
            return evaluateReactiveInternal((BindingSetAssignment) expr, bindings);
        }
        else if (expr == null) {
            throw new IllegalArgumentException("expr must not be null");
        }
        else {
            throw new QueryEvaluationException("Unsupported tuple expr type: " + expr.getClass());
        }
    }


    public Observable<BindingSet> evaluateReactiveInternal(UnaryTupleOperator expr, BindingSet bindings)
            throws QueryEvaluationException
    {

        if (expr instanceof Projection) {
            return evaluateReactiveInternal((Projection) expr, bindings);
        }
        else if (expr instanceof MultiProjection) {
            return evaluateReactiveInternal((MultiProjection) expr, bindings);
        }
        else if (expr instanceof Filter) {
            return evaluateReactiveInternal((Filter) expr, bindings);
        }
        else if (expr instanceof Extension) {
            return evaluateReactiveInternal((Extension) expr, bindings);
        }
        else if (expr instanceof Group) {
            return evaluateReactiveInternal((Group) expr, bindings);
        }
        else if (expr instanceof Order) {
            return evaluateReactiveInternal((Order) expr, bindings);
        }
        else if (expr instanceof Slice) {
            return evaluateReactiveInternal((Slice) expr, bindings);
        }
        else if (expr instanceof Distinct) {
            return evaluateReactiveInternal((Distinct) expr, bindings);
        }
        else if (expr instanceof Reduced) {
            return evaluateReactiveInternal((Reduced) expr, bindings);
        }
        else if (expr instanceof Service) {
            return evaluateReactiveInternal((Service) expr, bindings);
        }
        else if (expr instanceof QueryRoot) {
            return evaluateReactiveInternal(expr.getArg(), bindings);
        }
        else if (expr instanceof DescribeOperator) {
            return evaluateReactiveInternal((DescribeOperator) expr, bindings);
        }
        else if (expr == null) {
            throw new IllegalArgumentException("expr must not be null");
        }
        else {
            throw new QueryEvaluationException("Unsupported tuple expr type: " + expr.getClass());
        }
    }

    public Observable<BindingSet> evaluateReactiveInternal(BinaryTupleOperator expr, BindingSet bindings)
            throws QueryEvaluationException
    {
        if (expr instanceof Union) {
            return evaluateReactiveInternal((Union) expr, bindings);
        }
        else if (expr instanceof Join) {
            return evaluateReactiveInternal((Join) expr, bindings);
        }
        else if (expr instanceof LeftJoin) {
            return evaluateReactiveInternal((LeftJoin) expr, bindings);
        }
        else if (expr instanceof Intersection) {
            return evaluateReactiveInternal((Intersection) expr, bindings);
        }
        else if (expr instanceof Difference) {
            return evaluateReactiveInternal((Difference) expr, bindings);
        }
        else if (expr == null) {
            throw new IllegalArgumentException("expr must not be null");
        }
        else {
            throw new QueryEvaluationException("Unsupported tuple expr type: " + expr.getClass());
        }
    }

    public Observable<BindingSet> evaluateReactiveInternal(SingletonSet expr, BindingSet bindings)
            throws QueryEvaluationException
    {
        return Observable.just(bindings);
    }

    public Observable<BindingSet> evaluateReactiveInternal(EmptySet expr, BindingSet bindings)
            throws QueryEvaluationException
    {
        return Observable.empty();
    }

    public Observable<BindingSet> evaluateReactiveInternal(StatementPattern expr, BindingSet bindings)
            throws QueryEvaluationException
    {
        return fromIteration(evalStrategy.evaluate(expr, bindings));
    }

    public Observable<BindingSet> evaluateReactiveInternal(BindingSetAssignment expr, BindingSet bindings)
            throws QueryEvaluationException
    {
        final Iterator<BindingSet> iter = expr.getBindingSets().iterator();

        final List<BindingSet> blist = new LinkedList<BindingSet>();
        Iterators.addAll(iter, blist);

        return Observable.from(blist)
                .map((b) -> {
                    QueryBindingSet bb = new QueryBindingSet(bindings);
                    bb.addAll(b);
                    return bb;
                });
    }

    public Observable<BindingSet> evaluateReactiveInternal(ExternalSet expr, BindingSet bindings)
            throws QueryEvaluationException
    {
        return fromIteration(expr.evaluate(bindings));
    }


    public Observable<BindingSet> evaluateReactiveInternal(ZeroLengthPath expr, BindingSet bindings)
            throws QueryEvaluationException
    {
        return fromIteration(evalStrategy.evaluate(expr, bindings));
    }


    public Observable<BindingSet> evaluateReactiveInternal(ArbitraryLengthPath expr, BindingSet bindings)
            throws QueryEvaluationException
    {
        return fromIteration(evalStrategy.evaluate(expr, bindings));
    }

    public Observable<BindingSet> evaluateReactiveInternal(Filter expr, BindingSet bindings)
            throws QueryEvaluationException
    {
        QueryBindingSet scopeBindings = new QueryBindingSet(bindings);

        return evaluateReactiveInternal(expr.getArg(), bindings)
                    .filter((b) -> {
                        try {
                            return this.isTrue(expr.getCondition(), scopeBindings);
                        } catch (QueryEvaluationException /*| ValueExprEvaluationException */ e) {
                            return false;
                        }
                    });
    }

    public Observable<BindingSet> evaluateReactiveInternal(Projection expr, BindingSet bindings)
            throws QueryEvaluationException
    {
        return evaluateReactiveInternal(expr.getArg(), bindings)
                .map((b) -> QueryEvaluationUtil.project(expr.getProjectionElemList(), b, bindings));
    }

    public Observable<BindingSet> evaluateReactiveInternal(Extension expr, BindingSet bindings)
            throws QueryEvaluationException
    {
        return evaluateReactiveInternal(expr.getArg(), bindings)
                .concatMap((b) -> {
                    try {
                        return Observable.just(QueryEvaluationUtil.extend(this, expr.getElements(), b));
                    } catch (Exception e) {
                        return Observable.error(e);
                    }
                }).onErrorResumeNext(Observable::error);
    }

    public Observable<BindingSet> evaluateReactiveInternal(Union expr, BindingSet bindings)
            throws QueryEvaluationException
    {
        return Observable.merge(
                this.evaluateReactiveInternal(expr.getLeftArg(), bindings),
                this.evaluateReactiveInternal(expr.getRightArg(), bindings));
    }

    public Observable<BindingSet> evaluateReactiveInternal(Join expr, BindingSet bindings)
            throws QueryEvaluationException
    {
        return evaluateReactiveInternal(expr.getLeftArg(), bindings)
                    .concatMap((b) -> {
                        try {
                            return this.evaluateReactiveInternal(expr.getRightArg(), b);
                        } catch (Exception e) {
                            return Observable.error(e);
                        }
                    });
    }

    public Observable<BindingSet> evaluateReactiveInternal(LeftJoin expr, BindingSet bindings)
            throws QueryEvaluationException
    {
        Observable<BindingSet> r = evaluateReactiveInternal(expr.getRightArg(), bindings);

        Set<String> joinAttributes = expr.getLeftArg().getBindingNames();
        joinAttributes.retainAll(expr.getRightArg().getBindingNames());

        return evaluateReactiveInternal(expr.getLeftArg(), bindings)
                .concatMap((b) -> {
                    try {
                        return this.evaluateReactiveInternal(expr.getRightArg(), b).defaultIfEmpty(b);
                    } catch (Exception e) {
                        return Observable.error(e);
                    }
                });
    }

    public Observable<BindingSet> evaluateReactiveInternal(Group expr, BindingSet bindings)
            throws QueryEvaluationException
    {
        return null;
    }

    public Observable<BindingSet> evaluateReactiveInternal(Order expr, BindingSet bindings)
            throws QueryEvaluationException
    {
        ValueComparator vcmp = new ValueComparator();
        OrderComparator cmp = new OrderComparator(evalStrategy, expr, vcmp);
        return evaluateReactiveInternal(expr.getArg(), bindings)
                .toSortedList(cmp::compare)
                .flatMap(Observable::from);
    }

    public Observable<BindingSet> evaluateReactiveInternal(Slice expr, BindingSet bindings)
            throws QueryEvaluationException {
        Observable<BindingSet> result = evaluateReactiveInternal(expr.getArg(), bindings);

        if (expr.hasOffset())
            result = result.skip((int) expr.getOffset());

        if (expr.hasLimit())
            result = result.take((int) expr.getLimit());

        return result;
    }

    public Observable<BindingSet> evaluateReactiveInternal(Distinct expr, BindingSet bindings)
            throws QueryEvaluationException {

        return evaluateReactiveInternal(expr.getArg(), bindings).distinct();
    }

    public Observable<BindingSet> evaluateReactiveInternal(Reduced expr, BindingSet bindings)
            throws QueryEvaluationException {

        return evaluateReactiveInternal(expr.getArg(), bindings).distinctUntilChanged();
    }

    public Observable<BindingSet> evaluateReactiveInternal(DescribeOperator expr, BindingSet bindings)
            throws QueryEvaluationException
    {
        return fromIteration(evalStrategy.evaluate(expr, bindings));
    }

    public Observable<BindingSet> evaluateReactiveInternal(Intersection expr, BindingSet bindings)
            throws QueryEvaluationException
    {
        return fromIteration(evalStrategy.evaluate(expr, bindings));
    }


    public Observable<BindingSet> evaluateReactiveInternal(Difference expr, BindingSet bindings)
            throws QueryEvaluationException
    {
        return fromIteration(evalStrategy.evaluate(expr, bindings));
    }


    public Observable<BindingSet> evaluateReactiveInternal(Service expr, BindingSet bindings)
            throws QueryEvaluationException
    {
        return fromIteration(evalStrategy.evaluate(expr, bindings));
    }

    protected <T> Observable<T> fromIteration(Iteration<T, ? extends Exception> it) {
        return RxReactiveStreams.toObservable(new IterationPublisher<T>(it));
    }

}
