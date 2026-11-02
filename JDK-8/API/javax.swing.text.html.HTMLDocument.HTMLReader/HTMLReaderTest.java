/*
 * @test
 * @summary javax.swing.text.html.HTMLDocument.HTMLReader conformance: type shape via reflection (headless-safe).
 * @run main HTMLReaderTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class HTMLReaderTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.text.html.HTMLDocument$HTMLReader", false, HTMLReaderTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.text.html.HTMLEditorKit$ParserCallback"), "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("flush"), "method flush");
        check(names.contains("handleText"), "method handleText");
        check(names.contains("handleStartTag"), "method handleStartTag");
        check(names.contains("handleComment"), "method handleComment");
        check(names.contains("handleEndTag"), "method handleEndTag");
        check(names.contains("handleSimpleTag"), "method handleSimpleTag");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
