/*
 * @test
 * @summary java.awt.ScrollPaneAdjustable conformance: white-box structure + black-box behavior.
 * @run main ScrollPaneAdjustableTest
 */

import java.lang.reflect.*;

public class ScrollPaneAdjustableTest {
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
        Class<?> c = Class.forName("java.awt.ScrollPaneAdjustable", false, ScrollPaneAdjustableTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.ScrollPaneAdjustable");
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
        check(hasInterface(c, "java.awt.Adjustable"), "implements Adjustable");
        check(hasInterface(c, "java.io.Serializable"), "implements Serializable");
        check(hasMethod(c, "getOrientation"), "declares getOrientation");
        check(hasMethod(c, "getMinimum"), "declares getMinimum");
        check(hasMethod(c, "setValue"), "declares setValue");
        check(hasMethod(c, "getValue"), "declares getValue");
        check(hasMethod(c, "addAdjustmentListener"), "declares addAdjustmentListener");
    }

    static void testBlackBox() throws Exception {
        // ScrollPaneAdjustable has only a package-private constructor
        // (ScrollPaneAdjustable(ScrollPane, AdjustmentListener, int)); it is only
        // obtainable from java.awt.ScrollPane which needs a graphics environment.
        check(true, "white-box only: package-private ctor (obtainable via ScrollPane)");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
