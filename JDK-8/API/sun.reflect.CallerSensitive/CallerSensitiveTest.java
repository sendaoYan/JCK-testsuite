/*
 * @test
 * @summary sun.reflect.CallerSensitive conformance: annotation type and elements.
 * @run main CallerSensitiveTest
 */

public class CallerSensitiveTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.reflect.CallerSensitive", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.reflect.CallerSensitive"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(c.isAnnotation(), "is annotation");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        check(ifaces.contains("java.lang.annotation.Annotation"), "interface java.lang.annotation.Annotation");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
