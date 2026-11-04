/*
 * @test
 * @summary javax.swing.text.html.HTMLDocument conformance: type shape via reflection (headless-safe).
 * @run main HTMLDocumentTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class HTMLDocumentTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.text.html.HTMLDocument", false, HTMLDocumentTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.text.DefaultStyledDocument"), "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getReader"), "method getReader");
        check(names.contains("getReader"), "method getReader");
        check(names.contains("getBase"), "method getBase");
        check(names.contains("setBase"), "method setBase");
        check(names.contains("setParagraphAttributes"), "method setParagraphAttributes");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
