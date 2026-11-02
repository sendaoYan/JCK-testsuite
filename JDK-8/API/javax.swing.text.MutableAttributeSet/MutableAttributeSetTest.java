/*
 * @test
 * @summary javax.swing.text.MutableAttributeSet conformance: type shape via reflection (headless-safe).
 * @run main MutableAttributeSetTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class MutableAttributeSetTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.text.MutableAttributeSet", false, MutableAttributeSetTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface(), "is interface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(hasInterface(c, "javax.swing.text.AttributeSet"), "implements javax.swing.text.AttributeSet");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("addAttribute"), "method addAttribute");
        check(names.contains("addAttributes"), "method addAttributes");
        check(names.contains("removeAttribute"), "method removeAttribute");
        check(names.contains("removeAttributes"), "method removeAttributes");
        check(names.contains("removeAttributes"), "method removeAttributes");
        check(names.contains("setResolveParent"), "method setResolveParent");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
