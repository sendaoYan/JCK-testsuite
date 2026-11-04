/*
 * @test
 * @summary sun.net.www.HeaderParser conformance: class and members.
 * @run main HeaderParserTest
 */

public class HeaderParserTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.net.www.HeaderParser", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.net.www.HeaderParser"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("findInt"), "declares method findInt");
        check(mnames.contains("findKey"), "declares method findKey");
        check(mnames.contains("findValue"), "declares method findValue");
        check(mnames.contains("keys"), "declares method keys");
        check(mnames.contains("parse"), "declares method parse");
        check(mnames.contains("subsequence"), "declares method subsequence");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
