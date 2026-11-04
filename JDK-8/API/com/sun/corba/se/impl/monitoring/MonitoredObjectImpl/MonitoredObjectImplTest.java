/*
 * @test
 * @summary com.sun.corba.se.impl.monitoring.MonitoredObjectImpl conformance: type shape via reflection (headless-safe).
 * @run main MonitoredObjectImplTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class MonitoredObjectImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.monitoring.MonitoredObjectImpl", false, MonitoredObjectImplTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        Set<String> in = new HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("com.sun.corba.se.spi.monitoring.MonitoredObject"), "interface com.sun.corba.se.spi.monitoring.MonitoredObject");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 13, "public declared method count");
        check(mn.contains("addAttribute"), "method addAttribute");
        check(mn.contains("addChild"), "method addChild");
        check(mn.contains("clearState"), "method clearState");
        check(mn.contains("getAttribute"), "method getAttribute");
        check(mn.contains("getAttributes"), "method getAttributes");
        check(mn.contains("getChild"), "method getChild");
        check(mn.contains("getChildren"), "method getChildren");
        check(mn.contains("getDescription"), "method getDescription");
        check(mn.contains("getName"), "method getName");
        check(mn.contains("getParent"), "method getParent");
        check(mn.contains("removeAttribute"), "method removeAttribute");
        check(mn.contains("removeChild"), "method removeChild");
        check(mn.contains("setParent"), "method setParent");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
