/*
 * @test
 * @summary java.security.URIParameter conformance: constructor and accessor.
 * @run main URIParameterTest
 */

import java.security.URIParameter;
import java.net.URI;

public class URIParameterTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        URI uri = new URI("file:///tmp/policy");
        URIParameter p = new URIParameter(uri);
        check(uri.equals(p.getURI()), "getURI");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
