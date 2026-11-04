/*
 * @test
 * @summary java.awt.dnd.DropTargetContext conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main DropTargetContextTest
 */

import java.lang.reflect.*;

public class DropTargetContextTest {
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

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.dnd.DropTargetContext", false, DropTargetContextTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.dnd.DropTargetContext");
        int m = c.getModifiers();
        check(Modifier.isPublic(m) || Modifier.isProtected(m) || c.isMemberClass(), "public/protected/member");
        if (c.isEnum()) check(c.getEnumConstants() != null, "enum has constants");
        else check(c.getEnumConstants() == null, "non-enum returns null constants");
        check(c.getDeclaredConstructors() != null, "constructors array non-null");
        check(c.getDeclaredMethods() != null, "methods array non-null");
        check(c.getDeclaredFields() != null, "fields array non-null");
        if (c.isInterface()) check(Modifier.isInterface(m), "interface modifier consistent");
        else if (c != Object.class) check(c.getSuperclass() != null, "class superclass non-null");
        check(Modifier.isFinal(c.getModifiers()), "final class");
        check(hasMethod(c, "getDropTarget"), "declares getDropTarget");
        check(hasMethod(c, "getComponent"), "declares getComponent");
    }

    static void testBlackBox() throws Exception {
        java.awt.dnd.DropTarget dt = new java.awt.dnd.DropTarget();
        java.awt.dnd.DropTargetContext ctx = dt.getDropTargetContext();
        check(ctx != null, "getDropTargetContext non-null");
        check(ctx.getDropTarget() == dt, "getDropTarget");
        check(ctx.getComponent() == null, "getComponent null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
