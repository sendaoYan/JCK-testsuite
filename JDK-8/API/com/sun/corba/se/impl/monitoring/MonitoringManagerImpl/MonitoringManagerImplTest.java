/*
 * @test
 * @summary com.sun.corba.se.impl.monitoring.MonitoringManagerImpl conformance: type shape via reflection (headless-safe).
 * @run main MonitoringManagerImplTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class MonitoringManagerImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.monitoring.MonitoringManagerImpl", false, MonitoringManagerImplTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        Set<String> in = new HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("com.sun.corba.se.spi.monitoring.MonitoringManager"), "interface com.sun.corba.se.spi.monitoring.MonitoringManager");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 3, "public declared method count");
        check(mn.contains("clearState"), "method clearState");
        check(mn.contains("close"), "method close");
        check(mn.contains("getRootMonitoredObject"), "method getRootMonitoredObject");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
