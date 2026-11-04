/*
 * @test
 * @summary sun.nio.cs.ext.ExtendedCharsets conformance: class and members.
 * @run main ExtendedCharsetsTest
 */

public class ExtendedCharsetsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.nio.cs.ext.ExtendedCharsets", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.nio.cs.ext.ExtendedCharsets"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.nio.cs.AbstractCharsetProvider"), "superclass sun.nio.cs.AbstractCharsetProvider");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("aliasesFor"), "declares method aliasesFor");
        check(mnames.contains("init"), "declares method init");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
