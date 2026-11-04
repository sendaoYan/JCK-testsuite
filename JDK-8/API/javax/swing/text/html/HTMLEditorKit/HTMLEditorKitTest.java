/*
 * @test
 * @summary javax.swing.text.html.HTMLEditorKit conformance: type shape via reflection (headless-safe).
 * @run main HTMLEditorKitTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class HTMLEditorKitTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.text.html.HTMLEditorKit", false, HTMLEditorKitTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.text.StyledEditorKit"), "superclass");
        check(hasInterface(c, "javax.accessibility.Accessible"), "implements javax.accessibility.Accessible");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getContentType"), "method getContentType");
        check(names.contains("getViewFactory"), "method getViewFactory");
        check(names.contains("createDefaultDocument"), "method createDefaultDocument");
        check(names.contains("read"), "method read");
        check(names.contains("insertHTML"), "method insertHTML");
        check(names.contains("write"), "method write");
        check(names.contains("install"), "method install");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
