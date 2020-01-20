import junit.framework.TestCase;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.semagrow.connector.sparql.execution.SPARQLRepository;

public class SemagrowTest extends TestCase {

    public void testThematicStoreQuery() {

        String queryStr = "" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "PREFIX ex: <http://example.org/>\n" +
                "SELECT ?x ?y {\n" +
                "  ?x rdf:type ex:point .\n" +
                "  ?y rdf:type ex:point .\n" +
                "  ?x ex:color ex:blue .\n" +
                "  ?y ex:color ex:red .\n" +
                "}";

        Repository repo = new SPARQLRepository("http://localhost:30200/sparql");
        repo.initialize();

        RepositoryConnection conn = repo.getConnection();

        TupleQuery query = conn.prepareTupleQuery(queryStr);
        TupleQueryResult result = query.evaluate();

        int count=0;
        while (result.hasNext()) {
            System.out.println(result.next());
            count++;
        }
        System.out.println("");
        conn.close();

        assertEquals(1122, count);
    }
}
