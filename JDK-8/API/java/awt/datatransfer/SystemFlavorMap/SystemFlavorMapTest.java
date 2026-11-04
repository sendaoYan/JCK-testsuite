/*
 * @test
 * @summary java.awt.datatransfer.SystemFlavorMap conformance: class reflection.
 * @run main SystemFlavorMapTest
 */

public class SystemFlavorMapTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getMethods())
            if (m.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.awt.datatransfer.SystemFlavorMap");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"java.awt.datatransfer.FlavorMap", "java.awt.datatransfer.FlavorTable"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "getDefaultFlavorMap"), "method getDefaultFlavorMap");
        check(hasMethod(c, "getNativesForFlavor"), "method getNativesForFlavor");
        check(hasMethod(c, "getFlavorsForNative"), "method getFlavorsForNative");
        check(hasMethod(c, "getNativesForFlavors"), "method getNativesForFlavors");
        check(hasMethod(c, "getFlavorsForNatives"), "method getFlavorsForNatives");
        check(hasMethod(c, "addUnencodedNativeForFlavor"), "method addUnencodedNativeForFlavor");
        check(hasMethod(c, "setNativesForFlavor"), "method setNativesForFlavor");
        check(hasMethod(c, "addFlavorForUnencodedNative"), "method addFlavorForUnencodedNative");
        check(hasMethod(c, "setFlavorsForNative"), "method setFlavorsForNative");
        check(hasMethod(c, "encodeJavaMIMEType"), "method encodeJavaMIMEType");
        check(hasMethod(c, "encodeDataFlavor"), "method encodeDataFlavor");
        check(hasMethod(c, "isJavaMIMEType"), "method isJavaMIMEType");
        check(hasMethod(c, "decodeJavaMIMEType"), "method decodeJavaMIMEType");
        check(hasMethod(c, "decodeDataFlavor"), "method decodeDataFlavor");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
