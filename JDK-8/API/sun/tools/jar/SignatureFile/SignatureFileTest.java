/*
 * @test
 * @summary sun.tools.jar.SignatureFile conformance: class and members.
 * @run main SignatureFileTest
 */

public class SignatureFileTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.tools.jar.SignatureFile", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.tools.jar.SignatureFile"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("add"), "declares method add");
        check(mnames.contains("computeEntry"), "declares method computeEntry");
        check(mnames.contains("debug"), "declares method debug");
        check(mnames.contains("entries"), "declares method entries");
        check(mnames.contains("entryAt"), "declares method entryAt");
        check(mnames.contains("getBlock"), "declares method getBlock");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
