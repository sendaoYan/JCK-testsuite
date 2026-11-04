/*
 * @test
 * @summary com.sun.corba.se.spi.monitoring.StatisticMonitoredAttribute conformance: type shape via reflection (headless-safe).
 * @run main StatisticMonitoredAttributeTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class StatisticMonitoredAttributeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.spi.monitoring.StatisticMonitoredAttribute", false, StatisticMonitoredAttributeTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.corba.se.spi.monitoring.MonitoredAttributeBase"), "superclass com.sun.corba.se.spi.monitoring.MonitoredAttributeBase");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 3, "public declared method count");
        check(mn.contains("clearState"), "method clearState");
        check(mn.contains("getStatisticsAccumulator"), "method getStatisticsAccumulator");
        check(mn.contains("getValue"), "method getValue");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
