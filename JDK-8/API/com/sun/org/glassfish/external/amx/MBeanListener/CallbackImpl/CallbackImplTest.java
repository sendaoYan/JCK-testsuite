/*
 * @test
 * @summary com.sun.org.glassfish.external.amx.MBeanListener.CallbackImpl conformance: reflection.
 * @run main CallbackImplTest
 */

public class CallbackImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.glassfish.external.amx.MBeanListener$CallbackImpl", false, CallbackImplTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("CallbackImpl"), "simple name CallbackImpl");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 1, "interfaces count 1");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("com.sun.org.glassfish.external.amx.MBeanListener$Callback"), "interface com.sun.org.glassfish.external.amx.MBeanListener$Callback");
        check(has(c, "getRegistered"), "method getRegistered");
        check(has(c, "getUnregistered"), "method getUnregistered");
        check(has(c, "await"), "method await");
        check(has(c, "mbeanRegistered"), "method mbeanRegistered");
        check(has(c, "mbeanUnregistered"), "method mbeanUnregistered");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
