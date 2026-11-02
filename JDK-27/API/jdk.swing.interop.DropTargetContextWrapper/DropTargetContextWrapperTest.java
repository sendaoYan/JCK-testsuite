/*
 * @test
 * @summary jdk.swing.interop.DropTargetContextWrapper conformance: abstract class.
 * @run main DropTargetContextWrapperTest
 */

import java.lang.reflect.*;


public class DropTargetContextWrapperTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    static boolean hasInterface(Class<?> c, String n) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.swing.interop.DropTargetContextWrapper");
        check(Modifier.isAbstract(c.getModifiers()), "isAbstract");
        check(!c.isInterface(), "not interface");
        check(c.getSuperclass() == java.lang.Object.class, "extends Object");
        check(hasMethod(c, "setDropTargetContext"), "declares setDropTargetContext");
        check(hasMethod(c, "reset"), "declares reset");
        check(hasMethod(c, "setTargetActions"), "declares setTargetActions");
        check(hasMethod(c, "getTargetActions"), "declares getTargetActions");
        check(hasMethod(c, "getDropTarget"), "declares getDropTarget");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
