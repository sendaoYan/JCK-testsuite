/*
 * @test
 * @summary sun.reflect.generics.factory.GenericsFactory conformance: interface and members.
 * @run main GenericsFactoryTest
 */

public class GenericsFactoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.reflect.generics.factory.GenericsFactory", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.reflect.generics.factory.GenericsFactory"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("findTypeVariable"), "declares method findTypeVariable");
        check(mnames.contains("makeArrayType"), "declares method makeArrayType");
        check(mnames.contains("makeBool"), "declares method makeBool");
        check(mnames.contains("makeByte"), "declares method makeByte");
        check(mnames.contains("makeChar"), "declares method makeChar");
        check(mnames.contains("makeDouble"), "declares method makeDouble");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
