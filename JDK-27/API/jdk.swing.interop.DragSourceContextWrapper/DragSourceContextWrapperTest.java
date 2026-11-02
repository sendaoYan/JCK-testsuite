/*
 * @test
 * @summary jdk.swing.interop.DragSourceContextWrapper conformance: abstract class.
 * @run main DragSourceContextWrapperTest
 */

import java.lang.reflect.*;


public class DragSourceContextWrapperTest {
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
        Class<?> c = Class.forName("jdk.swing.interop.DragSourceContextWrapper");
        check(Modifier.isAbstract(c.getModifiers()), "isAbstract");
        check(!c.isInterface(), "not interface");
        check(c.getSuperclass() == java.lang.Object.class, "extends Object");
        check(hasMethod(c, "getPeer"), "declares getPeer");
        check(hasMethod(c, "convertModifiersToDropAction"), "declares convertModifiersToDropAction");
        check(hasMethod(c, "setNativeCursor"), "declares setNativeCursor");
        check(hasMethod(c, "startDrag"), "declares startDrag");
        check(hasMethod(c, "startSecondaryEventLoop"), "declares startSecondaryEventLoop");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
