/*
 * @test
 * @summary javax.print.StreamPrintServiceFactory conformance: stream print service factory.
 * @run main StreamPrintServiceFactoryTest
 */

import java.lang.reflect.*;


public class StreamPrintServiceFactoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    static boolean hasIf(Class<?> c, String n) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true;
        return false;
    }

    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("javax.print.StreamPrintServiceFactory");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "isAbstract");
        check(hasMethod(c, "getOutputFormat"), "declares getOutputFormat");
        check(hasMethod(c, "getSupportedDocFlavors"), "declares getSupportedDocFlavors");
        check(hasMethod(c, "getPrintService"), "declares getPrintService");
        javax.print.StreamPrintServiceFactory[] f = javax.print.StreamPrintServiceFactory.lookupStreamPrintServiceFactories(javax.print.DocFlavor.BYTE_ARRAY.AUTOSENSE, null);
        check(f != null, "lookupStreamPrintServiceFactories non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
