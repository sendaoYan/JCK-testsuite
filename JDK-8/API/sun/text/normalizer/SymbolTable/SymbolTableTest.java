/*
 * @test
 * @summary sun.text.normalizer.SymbolTable conformance: interface and members.
 * @run main SymbolTableTest
 */

public class SymbolTableTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.text.normalizer.SymbolTable", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.text.normalizer.SymbolTable"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("lookup"), "declares method lookup");
        check(mnames.contains("lookupMatcher"), "declares method lookupMatcher");
        check(mnames.contains("parseReference"), "declares method parseReference");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
