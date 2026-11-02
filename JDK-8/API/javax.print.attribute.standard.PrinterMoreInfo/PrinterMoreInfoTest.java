/*
 * @test
 * @summary javax.print.attribute.standard.PrinterMoreInfo conformance: URI attribute.
 * @run main PrinterMoreInfoTest
 */

import java.lang.reflect.*;
import javax.print.attribute.standard.*;

public class PrinterMoreInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] a) throws Exception {
        PrinterMoreInfo v = new PrinterMoreInfo(new java.net.URI("http://example.com/x"));
        check("http://example.com/x".equals(v.getURI().toString()), "getURI");
        check(v.getCategory() == PrinterMoreInfo.class, "getCategory");
        check(v.getName() != null && v.getName().length() > 0, "getName non-empty");
        check(v.equals(new PrinterMoreInfo(new java.net.URI("http://example.com/x"))), "equals");
        check(v.hashCode() == new PrinterMoreInfo(new java.net.URI("http://example.com/x")).hashCode(), "hashCode");
        check(v.toString() != null, "toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
