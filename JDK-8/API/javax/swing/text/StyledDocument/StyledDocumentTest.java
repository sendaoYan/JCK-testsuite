/*
 * @test
 * @summary javax.swing.text.StyledDocument conformance: type shape via reflection (headless-safe).
 * @run main StyledDocumentTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class StyledDocumentTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.text.StyledDocument", false, StyledDocumentTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface(), "is interface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(hasInterface(c, "javax.swing.text.Document"), "implements javax.swing.text.Document");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("addStyle"), "method addStyle");
        check(names.contains("removeStyle"), "method removeStyle");
        check(names.contains("getStyle"), "method getStyle");
        check(names.contains("setCharacterAttributes"), "method setCharacterAttributes");
        check(names.contains("setParagraphAttributes"), "method setParagraphAttributes");
        check(names.contains("setLogicalStyle"), "method setLogicalStyle");
        check(names.contains("getLogicalStyle"), "method getLogicalStyle");
        check(names.contains("getParagraphElement"), "method getParagraphElement");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
