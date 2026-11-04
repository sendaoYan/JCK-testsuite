/*
 * @test
 * @summary java.awt.datatransfer.SystemFlavorMap conformance: white-box structure + black-box behavior.
 * @run main SystemFlavorMapTest
 */

import java.lang.reflect.*;

public class SystemFlavorMapTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    static boolean hasInterface(Class<?> c, String n) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true;
        return false;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.datatransfer.SystemFlavorMap", false, SystemFlavorMapTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.datatransfer.SystemFlavorMap");
        int m = c.getModifiers();
        check(Modifier.isPublic(m) || Modifier.isProtected(m) || c.isMemberClass(), "public/protected/member");
        if (c.isEnum()) check(c.getEnumConstants() != null, "enum has constants");
        else check(c.getEnumConstants() == null, "non-enum returns null constants");
        check(c.getDeclaredConstructors() != null, "constructors array non-null");
        check(c.getDeclaredMethods() != null, "methods array non-null");
        check(c.getDeclaredFields() != null, "fields array non-null");
        if (c.isInterface()) check(Modifier.isInterface(m), "interface modifier consistent");
        else if (c != Object.class) check(c.getSuperclass() != null, "class superclass non-null");
        check(Modifier.isFinal(c.getModifiers()), "final class");
        check(hasInterface(c, "java.awt.datatransfer.FlavorMap"), "implements FlavorMap");
        check(hasInterface(c, "java.awt.datatransfer.FlavorTable"), "implements FlavorTable");
    }

    static void testBlackBox() throws Exception {
        java.awt.datatransfer.FlavorMap fm = java.awt.datatransfer.SystemFlavorMap.getDefaultFlavorMap();
        check(fm != null, "getDefaultFlavorMap non-null");
        check(fm instanceof java.awt.datatransfer.FlavorTable, "is FlavorTable");
        java.awt.datatransfer.FlavorTable ft = (java.awt.datatransfer.FlavorTable) fm;
        java.util.List<String> natives = ft.getNativesForFlavor(java.awt.datatransfer.DataFlavor.stringFlavor);
        check(natives != null && !natives.isEmpty(), "natives for stringFlavor non-empty");
        java.util.List<java.awt.datatransfer.DataFlavor> flavors = ft.getFlavorsForNative("STRING");
        check(flavors != null && !flavors.isEmpty(), "flavors for STRING non-empty");
        java.util.Map<java.awt.datatransfer.DataFlavor,String> nf = fm.getNativesForFlavors(new java.awt.datatransfer.DataFlavor[]{java.awt.datatransfer.DataFlavor.stringFlavor});
        check(nf != null && nf.containsKey(java.awt.datatransfer.DataFlavor.stringFlavor), "getNativesForFlavors has stringFlavor");
        check(!java.awt.datatransfer.SystemFlavorMap.isJavaMIMEType("application/x-java-serialized-object"), "raw serialized mime not java mime");
        check(!java.awt.datatransfer.SystemFlavorMap.isJavaMIMEType("text/plain"), "text/plain not java mime");
        String enc = java.awt.datatransfer.SystemFlavorMap.encodeJavaMIMEType("application/x-java-serialized-object");
        check(java.awt.datatransfer.SystemFlavorMap.isJavaMIMEType(enc), "encoded still java mime");
        String dec = java.awt.datatransfer.SystemFlavorMap.decodeJavaMIMEType(enc);
        check("application/x-java-serialized-object".equals(dec), "decode roundtrip");
        String e2 = java.awt.datatransfer.SystemFlavorMap.encodeDataFlavor(java.awt.datatransfer.DataFlavor.stringFlavor);
        java.awt.datatransfer.DataFlavor df = java.awt.datatransfer.SystemFlavorMap.decodeDataFlavor(e2);
        check(java.awt.datatransfer.DataFlavor.stringFlavor.equals(df), "encode/decode DataFlavor roundtrip");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
