/*
 * @test
 * @summary sun.reflect.generics.tree.TypeArgument conformance: interface and members.
 * @run main TypeArgumentTest
 */

public class TypeArgumentTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.reflect.generics.tree.TypeArgument", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.reflect.generics.tree.TypeArgument"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        check(ifaces.contains("sun.reflect.generics.tree.TypeTree"), "interface sun.reflect.generics.tree.TypeTree");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
