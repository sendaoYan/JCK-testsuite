/*
 * @test
 * @summary com.sun.jmx.mbeanserver.MBeanSupport conformance: hierarchy and declared members via reflection.
 * @run main MBeanSupportTest
 */

public class MBeanSupportTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.jmx.mbeanserver.MBeanSupport", false, null);
        check(c.getName().equals("com.sun.jmx.mbeanserver.MBeanSupport"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("com.sun.jmx.mbeanserver.DynamicMBean2"), "implements com.sun.jmx.mbeanserver.DynamicMBean2");
        check(in.contains("javax.management.MBeanRegistration"), "implements javax.management.MBeanRegistration");
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("getAttribute"), "declares getAttribute");
        check(ms.contains("getAttributes"), "declares getAttributes");
        check(ms.contains("getClassName"), "declares getClassName");
        check(ms.contains("getCookie"), "declares getCookie");
        check(ms.contains("getMBeanInfo"), "declares getMBeanInfo");
        check(ms.contains("getMBeanInterface"), "declares getMBeanInterface");
        check(ms.contains("getMBeanIntrospector"), "declares getMBeanIntrospector");
        check(ms.contains("getResource"), "declares getResource");
        check(ms.contains("invoke"), "declares invoke");
        check(ms.contains("isMXBean"), "declares isMXBean");
        check(ms.contains("postDeregister"), "declares postDeregister");
        check(ms.contains("postRegister"), "declares postRegister");
        check(ms.contains("preDeregister"), "declares preDeregister");
        check(ms.contains("preRegister"), "declares preRegister");
        check(ms.contains("preRegister2"), "declares preRegister2");
        check(ms.contains("register"), "declares register");
        check(ms.contains("registerFailed"), "declares registerFailed");
        check(ms.contains("setAttribute"), "declares setAttribute");
        check(ms.contains("setAttributes"), "declares setAttributes");
        check(ms.contains("unregister"), "declares unregister");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
