/*
 * @test
 * @summary com.sun.org.glassfish.external.amx.MBeanListener.Callback conformance: reflection.
 * @run main CallbackTest
 */

public class CallbackTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.glassfish.external.amx.MBeanListener$Callback", false, CallbackTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("Callback"), "simple name Callback");
        check(c.isInterface(), "is interface");
        check(!c.isEnum(), "not enum");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "mbeanRegistered"), "method mbeanRegistered");
        check(has(c, "mbeanUnregistered"), "method mbeanUnregistered");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
