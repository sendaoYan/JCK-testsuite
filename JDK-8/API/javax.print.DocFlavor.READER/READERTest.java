/*
 * @test
 * @summary javax.print.DocFlavor.READER conformance: doc flavor nested class.
 * @run main READERTest
 */

import java.lang.reflect.*;


public class READERTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("javax.print.DocFlavor$" + "READER");
        check(c.getSuperclass() == javax.print.DocFlavor.class, "extends DocFlavor");
        javax.print.DocFlavor f_TEXT_PLAIN = javax.print.DocFlavor.READER.TEXT_PLAIN;
        check("text/plain; charset=\"utf-16\"".equals(f_TEXT_PLAIN.getMimeType()), "TEXT_PLAIN mimeType");
        check("java.io.Reader".equals(f_TEXT_PLAIN.getRepresentationClassName()), "TEXT_PLAIN representation");
        javax.print.DocFlavor f_TEXT_HTML = javax.print.DocFlavor.READER.TEXT_HTML;
        check("text/html; charset=\"utf-16\"".equals(f_TEXT_HTML.getMimeType()), "TEXT_HTML mimeType");
        check("java.io.Reader".equals(f_TEXT_HTML.getRepresentationClassName()), "TEXT_HTML representation");
        javax.print.DocFlavor made = new javax.print.DocFlavor.READER("text/plain; charset=\"utf-16\"");
        check(made instanceof javax.print.DocFlavor.READER, "ctor instanceof");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
