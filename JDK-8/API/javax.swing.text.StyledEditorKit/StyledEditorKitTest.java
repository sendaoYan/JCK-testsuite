/*
 * @test
 * @summary javax.swing.text.StyledEditorKit conformance: type shape via reflection (headless-safe).
 * @run main StyledEditorKitTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class StyledEditorKitTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.text.StyledEditorKit", false, StyledEditorKitTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.text.DefaultEditorKit"), "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getInputAttributes"), "method getInputAttributes");
        check(names.contains("getCharacterAttributeRun"), "method getCharacterAttributeRun");
        check(names.contains("getActions"), "method getActions");
        check(names.contains("createDefaultDocument"), "method createDefaultDocument");
        check(names.contains("install"), "method install");
        check(names.contains("deinstall"), "method deinstall");
        check(names.contains("getViewFactory"), "method getViewFactory");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
