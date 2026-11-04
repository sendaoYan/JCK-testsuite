/*
 * @test
 * @summary javax.print.DocFlavor.STRING conformance: doc flavor nested class.
 * @run main STRINGTest
 */

import java.lang.reflect.*;


public class STRINGTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("javax.print.DocFlavor$" + "STRING");
        check(c.getSuperclass() == javax.print.DocFlavor.class, "extends DocFlavor");
        javax.print.DocFlavor f_TEXT_PLAIN = javax.print.DocFlavor.STRING.TEXT_PLAIN;
        check("text/plain; charset=\"utf-16\"".equals(f_TEXT_PLAIN.getMimeType()), "TEXT_PLAIN mimeType");
        check("java.lang.String".equals(f_TEXT_PLAIN.getRepresentationClassName()), "TEXT_PLAIN representation");
        javax.print.DocFlavor f_TEXT_HTML = javax.print.DocFlavor.STRING.TEXT_HTML;
        check("text/html; charset=\"utf-16\"".equals(f_TEXT_HTML.getMimeType()), "TEXT_HTML mimeType");
        check("java.lang.String".equals(f_TEXT_HTML.getRepresentationClassName()), "TEXT_HTML representation");
        javax.print.DocFlavor made = new javax.print.DocFlavor.STRING("text/plain; charset=\"utf-16\"");
        check(made instanceof javax.print.DocFlavor.STRING, "ctor instanceof");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
