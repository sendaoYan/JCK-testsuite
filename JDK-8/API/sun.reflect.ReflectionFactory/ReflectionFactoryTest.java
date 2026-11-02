/*
 * @test
 * @summary sun.reflect.ReflectionFactory conformance: class and members.
 * @run main ReflectionFactoryTest
 */

public class ReflectionFactoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.reflect.ReflectionFactory", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.reflect.ReflectionFactory"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("checkInitted"), "declares method checkInitted");
        check(mnames.contains("copyConstructor"), "declares method copyConstructor");
        check(mnames.contains("copyField"), "declares method copyField");
        check(mnames.contains("copyMethod"), "declares method copyMethod");
        check(mnames.contains("findReadWriteObjectForSerialization"), "declares method findReadWriteObjectForSerialization");
        check(mnames.contains("generateConstructor"), "declares method generateConstructor");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
