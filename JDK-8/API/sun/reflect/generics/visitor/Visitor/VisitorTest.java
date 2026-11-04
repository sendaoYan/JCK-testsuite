/*
 * @test
 * @summary sun.reflect.generics.visitor.Visitor conformance: interface and members.
 * @run main VisitorTest
 */

public class VisitorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.reflect.generics.visitor.Visitor", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.reflect.generics.visitor.Visitor"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        check(ifaces.contains("sun.reflect.generics.visitor.TypeTreeVisitor"), "interface sun.reflect.generics.visitor.TypeTreeVisitor");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("visitClassSignature"), "declares method visitClassSignature");
        check(mnames.contains("visitMethodTypeSignature"), "declares method visitMethodTypeSignature");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
