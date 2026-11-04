/*
 * @test
 * @summary sun.misc.FloatingDecimal conformance: class and members.
 * @run main FloatingDecimalTest
 */

public class FloatingDecimalTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.misc.FloatingDecimal", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.misc.FloatingDecimal"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("appendTo"), "declares method appendTo");
        check(mnames.contains("getBinaryToASCIIBuffer"), "declares method getBinaryToASCIIBuffer");
        check(mnames.contains("getBinaryToASCIIConverter"), "declares method getBinaryToASCIIConverter");
        check(mnames.contains("getHexDigit"), "declares method getHexDigit");
        check(mnames.contains("parseDouble"), "declares method parseDouble");
        check(mnames.contains("parseFloat"), "declares method parseFloat");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
