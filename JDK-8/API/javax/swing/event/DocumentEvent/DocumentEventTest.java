/*
 * @test
 * @summary javax.swing.event.DocumentEvent conformance: type shape via reflection (headless-safe).
 * @run main DocumentEventTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class DocumentEventTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.event.DocumentEvent", false, DocumentEventTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface(), "is interface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "interface has no superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getOffset"), "method getOffset");
        check(names.contains("getLength"), "method getLength");
        check(names.contains("getDocument"), "method getDocument");
        check(names.contains("getType"), "method getType");
        check(names.contains("getChange"), "method getChange");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
