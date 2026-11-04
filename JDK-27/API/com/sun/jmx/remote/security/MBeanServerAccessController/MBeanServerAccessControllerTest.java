/*
 * @test
 * @summary com.sun.jmx.remote.security.MBeanServerAccessController conformance: internal package reflection.
 * @modules java.management/com.sun.jmx.remote.security:+open
 * @run main MBeanServerAccessControllerTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MBeanServerAccessControllerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static String[] methods(Class<?> c) {
        List<String> l = new ArrayList<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) l.add(m.getName());
        }
        String[] a = l.toArray(new String[0]); Arrays.sort(a); return a;
    }

    static String[] fields(Class<?> c) {
        List<String> l = new ArrayList<>();
        for (Field f : c.getDeclaredFields()) {
            int mod = f.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) l.add(f.getName());
        }
        String[] a = l.toArray(new String[0]); Arrays.sort(a); return a;
    }

    static String[] enumConstants(Class<?> c) {
        List<String> l = new ArrayList<>();
        for (Field f : c.getDeclaredFields()) {
            if (f.isEnumConstant()) l.add(f.getName());
        }
        String[] a = l.toArray(new String[0]); Arrays.sort(a); return a;
    }

    static String[] interfaces(Class<?> c) {
        List<String> l = new ArrayList<>();
        for (Class<?> i : c.getInterfaces()) l.add(i.getName());
        String[] a = l.toArray(new String[0]); Arrays.sort(a); return a;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.jmx.remote.security.MBeanServerAccessController", false, MBeanServerAccessControllerTest.class.getClassLoader());
        check(c.getName().equals("com.sun.jmx.remote.security.MBeanServerAccessController"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(Arrays.equals(interfaces(c), new String[]{"javax.management.remote.MBeanServerForwarder"}), "interfaces");
        check(Arrays.equals(methods(c), new String[]{"addNotificationListener","addNotificationListener","checkCreate","checkRead","checkUnregister","checkWrite","createMBean","createMBean","createMBean","createMBean","deserialize","deserialize","deserialize","getAttribute","getAttributes","getClassLoader","getClassLoaderFor","getClassLoaderRepository","getDefaultDomain","getDomains","getMBeanCount","getMBeanInfo","getMBeanServer","getObjectInstance","instantiate","instantiate","instantiate","instantiate","invoke","isInstanceOf","isRegistered","queryMBeans","queryNames","registerMBean","removeNotificationListener","removeNotificationListener","removeNotificationListener","removeNotificationListener","setAttribute","setAttributes","setMBeanServer","unregisterMBean"}), "methods");
        check(Arrays.equals(fields(c), new String[]{}), "fields");
        check(Arrays.equals(enumConstants(c), new String[]{}), "enumConstants");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
