/*
 * @test
 * @summary javax.print.DocFlavor.INPUT_STREAM conformance: doc flavor nested class.
 * @run main INPUT_STREAMTest
 */

import java.lang.reflect.*;


public class INPUT_STREAMTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("javax.print.DocFlavor$" + "INPUT_STREAM");
        check(c.getSuperclass() == javax.print.DocFlavor.class, "extends DocFlavor");
        javax.print.DocFlavor f_TEXT_PLAIN_US_ASCII = javax.print.DocFlavor.INPUT_STREAM.TEXT_PLAIN_US_ASCII;
        check("text/plain; charset=\"us-ascii\"".equals(f_TEXT_PLAIN_US_ASCII.getMimeType()), "TEXT_PLAIN_US_ASCII mimeType");
        check("java.io.InputStream".equals(f_TEXT_PLAIN_US_ASCII.getRepresentationClassName()), "TEXT_PLAIN_US_ASCII representation");
        javax.print.DocFlavor f_AUTOSENSE = javax.print.DocFlavor.INPUT_STREAM.AUTOSENSE;
        check("application/octet-stream".equals(f_AUTOSENSE.getMimeType()), "AUTOSENSE mimeType");
        check("java.io.InputStream".equals(f_AUTOSENSE.getRepresentationClassName()), "AUTOSENSE representation");
        javax.print.DocFlavor made = new javax.print.DocFlavor.INPUT_STREAM("text/plain; charset=\"us-ascii\"");
        check(made instanceof javax.print.DocFlavor.INPUT_STREAM, "ctor instanceof");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
