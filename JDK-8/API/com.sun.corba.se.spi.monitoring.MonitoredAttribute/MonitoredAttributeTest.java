/*
 * @test
 * @summary com.sun.corba.se.spi.monitoring.MonitoredAttribute conformance: type shape via reflection (headless-safe).
 * @run main MonitoredAttributeTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class MonitoredAttributeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.spi.monitoring.MonitoredAttribute", false, MonitoredAttributeTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "interface superclass null");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 5, "public declared method count");
        check(mn.contains("clearState"), "method clearState");
        check(mn.contains("getAttributeInfo"), "method getAttributeInfo");
        check(mn.contains("getName"), "method getName");
        check(mn.contains("getValue"), "method getValue");
        check(mn.contains("setValue"), "method setValue");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
