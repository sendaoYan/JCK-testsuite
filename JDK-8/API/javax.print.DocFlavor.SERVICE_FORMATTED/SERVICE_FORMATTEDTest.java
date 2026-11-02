/*
 * @test
 * @summary javax.print.DocFlavor.SERVICE_FORMATTED conformance: doc flavor nested class.
 * @run main SERVICE_FORMATTEDTest
 */

import java.lang.reflect.*;


public class SERVICE_FORMATTEDTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("javax.print.DocFlavor$" + "SERVICE_FORMATTED");
        check(c.getSuperclass() == javax.print.DocFlavor.class, "extends DocFlavor");
        javax.print.DocFlavor f_PAGEABLE = javax.print.DocFlavor.SERVICE_FORMATTED.PAGEABLE;
        check("application/x-java-jvm-local-objectref".equals(f_PAGEABLE.getMimeType()), "PAGEABLE mimeType");
        check("java.awt.print.Pageable".equals(f_PAGEABLE.getRepresentationClassName()), "PAGEABLE representation");
        javax.print.DocFlavor f_PRINTABLE = javax.print.DocFlavor.SERVICE_FORMATTED.PRINTABLE;
        check("application/x-java-jvm-local-objectref".equals(f_PRINTABLE.getMimeType()), "PRINTABLE mimeType");
        check("java.awt.print.Printable".equals(f_PRINTABLE.getRepresentationClassName()), "PRINTABLE representation");
        javax.print.DocFlavor made = new javax.print.DocFlavor.SERVICE_FORMATTED("application/x-java-jvm-local-objectref");
        check(made instanceof javax.print.DocFlavor.SERVICE_FORMATTED, "ctor instanceof");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
