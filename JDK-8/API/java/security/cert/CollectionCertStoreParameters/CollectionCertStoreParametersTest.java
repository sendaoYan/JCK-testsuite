/*
 * @test
 * @summary java.security.cert.CollectionCertStoreParameters conformance: constructors and getCollection.
 * @run main CollectionCertStoreParametersTest
 */

public class CollectionCertStoreParametersTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.util.Collection<Object> col = new java.util.ArrayList<Object>();
        java.security.cert.CollectionCertStoreParameters p = new java.security.cert.CollectionCertStoreParameters(col);
        check(col.equals(p.getCollection()), "getCollection");
        java.security.cert.CollectionCertStoreParameters dflt = new java.security.cert.CollectionCertStoreParameters();
        check(dflt.getCollection() != null, "default collection");
        check(p.clone() instanceof java.security.cert.CollectionCertStoreParameters, "clone");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
