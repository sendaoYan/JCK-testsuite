/*
 * @test
 * @summary javax.swing.undo.UndoableEdit conformance: type shape via reflection (headless-safe).
 * @run main UndoableEditTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class UndoableEditTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.undo.UndoableEdit", false, UndoableEditTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface(), "is interface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "interface has no superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("undo"), "method undo");
        check(names.contains("canUndo"), "method canUndo");
        check(names.contains("redo"), "method redo");
        check(names.contains("canRedo"), "method canRedo");
        check(names.contains("die"), "method die");
        check(names.contains("addEdit"), "method addEdit");
        check(names.contains("replaceEdit"), "method replaceEdit");
        check(names.contains("isSignificant"), "method isSignificant");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
