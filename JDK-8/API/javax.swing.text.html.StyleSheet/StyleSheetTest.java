/*
 * @test
 * @summary javax.swing.text.html.StyleSheet conformance: type shape via reflection (headless-safe).
 * @run main StyleSheetTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class StyleSheetTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.text.html.StyleSheet", false, StyleSheetTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.text.StyleContext"), "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getRule"), "method getRule");
        check(names.contains("getRule"), "method getRule");
        check(names.contains("addRule"), "method addRule");
        check(names.contains("getDeclaration"), "method getDeclaration");
        check(names.contains("loadRules"), "method loadRules");
        check(names.contains("getViewAttributes"), "method getViewAttributes");
        check(names.contains("removeStyle"), "method removeStyle");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
