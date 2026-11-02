/*
 * @test
 * @summary sun.nio.cs.Surrogate.Parser conformance: class and members.
 * @run main ParserTest
 */

public class ParserTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.nio.cs.Surrogate$Parser", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.nio.cs.Surrogate$Parser"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("character"), "declares method character");
        check(mnames.contains("error"), "declares method error");
        check(mnames.contains("increment"), "declares method increment");
        check(mnames.contains("isPair"), "declares method isPair");
        check(mnames.contains("parse"), "declares method parse");
        check(mnames.contains("unmappableResult"), "declares method unmappableResult");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
