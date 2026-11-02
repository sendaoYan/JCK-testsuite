/*
 * @test
 * @summary sun.reflect.generics.tree.TypeTree conformance: interface and members.
 * @run main TypeTreeTest
 */

public class TypeTreeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.reflect.generics.tree.TypeTree", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.reflect.generics.tree.TypeTree"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        check(ifaces.contains("sun.reflect.generics.tree.Tree"), "interface sun.reflect.generics.tree.Tree");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("accept"), "declares method accept");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
