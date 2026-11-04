/*
 * @test
 * @summary javax.swing.text.html.parser.TagElement conformance: type shape via reflection (headless-safe).
 * @run main TagElementTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class TagElementTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.text.html.parser.TagElement", false, TagElementTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("breaksFlow"), "method breaksFlow");
        check(names.contains("isPreformatted"), "method isPreformatted");
        check(names.contains("getElement"), "method getElement");
        check(names.contains("getHTMLTag"), "method getHTMLTag");
        check(names.contains("fictional"), "method fictional");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
