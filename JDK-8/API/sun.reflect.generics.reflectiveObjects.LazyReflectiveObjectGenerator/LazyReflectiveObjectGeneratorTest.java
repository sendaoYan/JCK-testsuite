/*
 * @test
 * @summary sun.reflect.generics.reflectiveObjects.LazyReflectiveObjectGenerator conformance: abstract class and members.
 * @run main LazyReflectiveObjectGeneratorTest
 */

public class LazyReflectiveObjectGeneratorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.reflect.generics.reflectiveObjects.LazyReflectiveObjectGenerator", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.reflect.generics.reflectiveObjects.LazyReflectiveObjectGenerator"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("getFactory"), "declares method getFactory");
        check(mnames.contains("getReifier"), "declares method getReifier");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
