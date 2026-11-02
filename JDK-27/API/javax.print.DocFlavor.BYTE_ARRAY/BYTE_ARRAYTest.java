/*
 * @test
 * @summary javax.print.DocFlavor.BYTE_ARRAY conformance: doc flavor nested class.
 * @run main BYTE_ARRAYTest
 */

import java.lang.reflect.*;


public class BYTE_ARRAYTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("javax.print.DocFlavor$" + "BYTE_ARRAY");
        check(c.getSuperclass() == javax.print.DocFlavor.class, "extends DocFlavor");
        javax.print.DocFlavor f_PDF = javax.print.DocFlavor.BYTE_ARRAY.PDF;
        check("application/pdf".equals(f_PDF.getMimeType()), "PDF mimeType");
        check("[B".equals(f_PDF.getRepresentationClassName()), "PDF representation");
        javax.print.DocFlavor f_TEXT_PLAIN_UTF_8 = javax.print.DocFlavor.BYTE_ARRAY.TEXT_PLAIN_UTF_8;
        check("text/plain; charset=\"utf-8\"".equals(f_TEXT_PLAIN_UTF_8.getMimeType()), "TEXT_PLAIN_UTF_8 mimeType");
        check("[B".equals(f_TEXT_PLAIN_UTF_8.getRepresentationClassName()), "TEXT_PLAIN_UTF_8 representation");
        javax.print.DocFlavor f_AUTOSENSE = javax.print.DocFlavor.BYTE_ARRAY.AUTOSENSE;
        check("application/octet-stream".equals(f_AUTOSENSE.getMimeType()), "AUTOSENSE mimeType");
        check("[B".equals(f_AUTOSENSE.getRepresentationClassName()), "AUTOSENSE representation");
        javax.print.DocFlavor made = new javax.print.DocFlavor.BYTE_ARRAY("application/pdf");
        check(made instanceof javax.print.DocFlavor.BYTE_ARRAY, "ctor instanceof");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
