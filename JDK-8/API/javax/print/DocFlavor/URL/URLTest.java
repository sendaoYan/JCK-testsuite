/*
 * @test
 * @summary javax.print.DocFlavor.URL conformance: doc flavor nested class.
 * @run main URLTest
 */

import java.lang.reflect.*;


public class URLTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("javax.print.DocFlavor$" + "URL");
        check(c.getSuperclass() == javax.print.DocFlavor.class, "extends DocFlavor");
        javax.print.DocFlavor f_PDF = javax.print.DocFlavor.URL.PDF;
        check("application/pdf".equals(f_PDF.getMimeType()), "PDF mimeType");
        check("java.net.URL".equals(f_PDF.getRepresentationClassName()), "PDF representation");
        javax.print.DocFlavor f_AUTOSENSE = javax.print.DocFlavor.URL.AUTOSENSE;
        check("application/octet-stream".equals(f_AUTOSENSE.getMimeType()), "AUTOSENSE mimeType");
        check("java.net.URL".equals(f_AUTOSENSE.getRepresentationClassName()), "AUTOSENSE representation");
        javax.print.DocFlavor made = new javax.print.DocFlavor.URL("application/pdf");
        check(made instanceof javax.print.DocFlavor.URL, "ctor instanceof");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
