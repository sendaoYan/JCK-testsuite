/*
 * @test
 * @summary javax.swing.text.html.AccessibleHTML.TextElementInfo.TextAccessibleContext conformance: type shape via reflection (headless-safe).
 * @run main TextAccessibleContextTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class TextAccessibleContextTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.text.html.AccessibleHTML$TextElementInfo$TextAccessibleContext", false, TextAccessibleContextTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.text.html.AccessibleHTML$HTMLAccessibleContext"), "superclass");
        check(hasInterface(c, "javax.accessibility.AccessibleText"), "implements javax.accessibility.AccessibleText");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getAccessibleText"), "method getAccessibleText");
        check(names.contains("getAccessibleName"), "method getAccessibleName");
        check(names.contains("getAccessibleDescription"), "method getAccessibleDescription");
        check(names.contains("getAccessibleRole"), "method getAccessibleRole");
        check(names.contains("getIndexAtPoint"), "method getIndexAtPoint");
        check(names.contains("getCharacterBounds"), "method getCharacterBounds");
        check(names.contains("getCharCount"), "method getCharCount");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
