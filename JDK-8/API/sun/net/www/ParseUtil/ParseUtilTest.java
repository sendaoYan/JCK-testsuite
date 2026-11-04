/*
 * @test
 * @summary sun.net.www.ParseUtil conformance: class and members.
 * @run main ParseUtilTest
 */

public class ParseUtilTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.net.www.ParseUtil", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.net.www.ParseUtil"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("appendAuthority"), "declares method appendAuthority");
        check(mnames.contains("appendEncoded"), "declares method appendEncoded");
        check(mnames.contains("appendEscape"), "declares method appendEscape");
        check(mnames.contains("appendFragment"), "declares method appendFragment");
        check(mnames.contains("appendSchemeSpecificPart"), "declares method appendSchemeSpecificPart");
        check(mnames.contains("canonizeString"), "declares method canonizeString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
