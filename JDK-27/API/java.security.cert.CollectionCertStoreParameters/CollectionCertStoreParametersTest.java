/*
 * @test
 * @summary java.security.cert.CollectionCertStoreParameters conformance: constructors, getCollection, clone.
 * @run main CollectionCertStoreParametersTest
 */

import java.security.cert.CollectionCertStoreParameters;
import java.util.ArrayList;
import java.util.Collection;

public class CollectionCertStoreParametersTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        Collection<Object> coll = new ArrayList<>();
        coll.add("x");
        CollectionCertStoreParameters p = new CollectionCertStoreParameters(coll);
        check(coll.equals(p.getCollection()), "getCollection");
        CollectionCertStoreParameters d = new CollectionCertStoreParameters();
        check(d.getCollection().isEmpty(), "default empty collection");
        Object clone = p.clone();
        check(clone instanceof CollectionCertStoreParameters, "clone type");
        check(coll.equals(((CollectionCertStoreParameters) clone).getCollection()), "clone getCollection equals");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
