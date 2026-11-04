/*
 * @test
 * @summary com.sun.org.glassfish.external.amx.MBeanListener conformance: reflection.
 * @run main MBeanListenerTest
 */

public class MBeanListenerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.glassfish.external.amx.MBeanListener", false, MBeanListenerTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("MBeanListener"), "simple name MBeanListener");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 1, "interfaces count 1");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("javax.management.NotificationListener"), "interface javax.management.NotificationListener");
        check(has(c, "toString"), "method toString");
        check(has(c, "getType"), "method getType");
        check(has(c, "getName"), "method getName");
        check(has(c, "getMBeanServer"), "method getMBeanServer");
        check(has(c, "getCallback"), "method getCallback");
        check(has(c, "startListening"), "method startListening");
        check(has(c, "stopListening"), "method stopListening");
        check(has(c, "handleNotification"), "method handleNotification");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
