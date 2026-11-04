/*
 * @test
 * @summary sun.reflect.generics.visitor.Reifier conformance: class and members.
 * @run main ReifierTest
 */

public class ReifierTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.reflect.generics.visitor.Reifier", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.reflect.generics.visitor.Reifier"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        check(ifaces.contains("sun.reflect.generics.visitor.TypeTreeVisitor"), "interface sun.reflect.generics.visitor.TypeTreeVisitor");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("getFactory"), "declares method getFactory");
        check(mnames.contains("getResult"), "declares method getResult");
        check(mnames.contains("make"), "declares method make");
        check(mnames.contains("reifyTypeArguments"), "declares method reifyTypeArguments");
        check(mnames.contains("visitArrayTypeSignature"), "declares method visitArrayTypeSignature");
        check(mnames.contains("visitBooleanSignature"), "declares method visitBooleanSignature");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
