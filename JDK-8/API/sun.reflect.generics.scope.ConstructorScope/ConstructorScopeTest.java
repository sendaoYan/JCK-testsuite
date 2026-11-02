/*
 * @test
 * @summary sun.reflect.generics.scope.ConstructorScope conformance: class and members.
 * @run main ConstructorScopeTest
 */

public class ConstructorScopeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.reflect.generics.scope.ConstructorScope", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.reflect.generics.scope.ConstructorScope"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.reflect.generics.scope.AbstractScope"), "superclass sun.reflect.generics.scope.AbstractScope");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("computeEnclosingScope"), "declares method computeEnclosingScope");
        check(mnames.contains("getEnclosingClass"), "declares method getEnclosingClass");
        check(mnames.contains("make"), "declares method make");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
