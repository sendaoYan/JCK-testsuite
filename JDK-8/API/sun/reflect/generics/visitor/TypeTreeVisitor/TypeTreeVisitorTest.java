/*
 * @test
 * @summary sun.reflect.generics.visitor.TypeTreeVisitor conformance: interface and members.
 * @run main TypeTreeVisitorTest
 */

public class TypeTreeVisitorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.reflect.generics.visitor.TypeTreeVisitor", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.reflect.generics.visitor.TypeTreeVisitor"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("getResult"), "declares method getResult");
        check(mnames.contains("visitArrayTypeSignature"), "declares method visitArrayTypeSignature");
        check(mnames.contains("visitBooleanSignature"), "declares method visitBooleanSignature");
        check(mnames.contains("visitBottomSignature"), "declares method visitBottomSignature");
        check(mnames.contains("visitByteSignature"), "declares method visitByteSignature");
        check(mnames.contains("visitCharSignature"), "declares method visitCharSignature");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
