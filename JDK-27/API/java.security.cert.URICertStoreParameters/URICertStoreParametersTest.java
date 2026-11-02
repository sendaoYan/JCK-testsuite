/*
 * @test
 * @summary java.security.cert.URICertStoreParameters conformance: constructor, getURI, clone, equals.
 * @run main URICertStoreParametersTest
 */

import java.security.cert.URICertStoreParameters;
import java.net.URI;

public class URICertStoreParametersTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        URI uri = new URI("ldap://example.com");
        URICertStoreParameters p = new URICertStoreParameters(uri);
        check(uri.equals(p.getURI()), "getURI");
        URICertStoreParameters clone = p.clone();
        check(p.equals(clone), "clone equals");
        check(p.hashCode() == clone.hashCode(), "hashCode");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
