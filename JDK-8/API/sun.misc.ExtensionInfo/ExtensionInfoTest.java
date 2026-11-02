/*
 * @test
 * @summary sun.misc.ExtensionInfo conformance: class and members.
 * @run main ExtensionInfoTest
 */

public class ExtensionInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.misc.ExtensionInfo", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.misc.ExtensionInfo"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("compareExtensionVersion"), "declares method compareExtensionVersion");
        check(mnames.contains("convertToken"), "declares method convertToken");
        check(mnames.contains("isCompatibleWith"), "declares method isCompatibleWith");
        check(mnames.contains("strictCompareExtensionVersion"), "declares method strictCompareExtensionVersion");
        check(mnames.contains("toString"), "declares method toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
