/*
 * @test
 * @summary sun.reflect.generics.repository.ConstructorRepository conformance: class and members.
 * @run main ConstructorRepositoryTest
 */

public class ConstructorRepositoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.reflect.generics.repository.ConstructorRepository", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.reflect.generics.repository.ConstructorRepository"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.reflect.generics.repository.GenericDeclRepository"), "superclass sun.reflect.generics.repository.GenericDeclRepository");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("getExceptionTypes"), "declares method getExceptionTypes");
        check(mnames.contains("getParameterTypes"), "declares method getParameterTypes");
        check(mnames.contains("make"), "declares method make");
        check(mnames.contains("parse"), "declares method parse");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
