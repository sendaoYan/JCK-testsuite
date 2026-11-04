/*
 * @test
 * @summary javax.swing.text.Highlighter conformance: type shape via reflection (headless-safe).
 * @run main HighlighterTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class HighlighterTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.text.Highlighter", false, HighlighterTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface(), "is interface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "interface has no superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("install"), "method install");
        check(names.contains("deinstall"), "method deinstall");
        check(names.contains("paint"), "method paint");
        check(names.contains("addHighlight"), "method addHighlight");
        check(names.contains("removeHighlight"), "method removeHighlight");
        check(names.contains("removeAllHighlights"), "method removeAllHighlights");
        check(names.contains("changeHighlight"), "method changeHighlight");
        check(names.contains("getHighlights"), "method getHighlights");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
