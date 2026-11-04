/*
 * @test
 * @summary javax.swing.text.ZoneView conformance: type shape via reflection (headless-safe).
 * @run main ZoneViewTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class ZoneViewTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.text.ZoneView", false, ZoneViewTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.text.BoxView"), "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getMaximumZoneSize"), "method getMaximumZoneSize");
        check(names.contains("setMaximumZoneSize"), "method setMaximumZoneSize");
        check(names.contains("getMaxZonesLoaded"), "method getMaxZonesLoaded");
        check(names.contains("setMaxZonesLoaded"), "method setMaxZonesLoaded");
        check(names.contains("insertUpdate"), "method insertUpdate");
        check(names.contains("removeUpdate"), "method removeUpdate");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
