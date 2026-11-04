/*
 * @test
 * @summary javax.swing.text.html.HTMLDocument.Iterator conformance: type shape via reflection (headless-safe).
 * @run main IteratorTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class IteratorTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.text.html.HTMLDocument$Iterator", false, IteratorTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getAttributes"), "method getAttributes");
        check(names.contains("getStartOffset"), "method getStartOffset");
        check(names.contains("getEndOffset"), "method getEndOffset");
        check(names.contains("next"), "method next");
        check(names.contains("isValid"), "method isValid");
        check(names.contains("getTag"), "method getTag");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
