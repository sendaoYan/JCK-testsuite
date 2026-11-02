/*
 * @test
 * @summary javax.print.attribute.standard.Destination conformance: URI attribute.
 * @run main DestinationTest
 */

import java.lang.reflect.*;
import javax.print.attribute.standard.*;

public class DestinationTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] a) throws Exception {
        Destination v = new Destination(new java.net.URI("http://example.com/x"));
        check("http://example.com/x".equals(v.getURI().toString()), "getURI");
        check(v.getCategory() == Destination.class, "getCategory");
        check(v.getName() != null && v.getName().length() > 0, "getName non-empty");
        check(v.equals(new Destination(new java.net.URI("http://example.com/x"))), "equals");
        check(v.hashCode() == new Destination(new java.net.URI("http://example.com/x")).hashCode(), "hashCode");
        check(v.toString() != null, "toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
