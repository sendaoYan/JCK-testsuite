/*
 * @test
 * @summary javax.swing.text.AttributeSet conformance: type shape via reflection (headless-safe).
 * @run main AttributeSetTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class AttributeSetTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.text.AttributeSet", false, AttributeSetTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface(), "is interface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "interface has no superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getAttributeCount"), "method getAttributeCount");
        check(names.contains("isDefined"), "method isDefined");
        check(names.contains("isEqual"), "method isEqual");
        check(names.contains("copyAttributes"), "method copyAttributes");
        check(names.contains("getAttribute"), "method getAttribute");
        check(names.contains("getAttributeNames"), "method getAttributeNames");
        check(names.contains("containsAttribute"), "method containsAttribute");
        check(names.contains("containsAttributes"), "method containsAttributes");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
