/*
 * @test
 * @summary java.awt.Event conformance: white-box structure + black-box behavior.
 * @run main EventTest
 */

import java.lang.reflect.*;

public class EventTest {
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
        Class<?> c = Class.forName("java.awt.Event", false, EventTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.Event");
        int m = c.getModifiers();
        check(Modifier.isPublic(m) || Modifier.isProtected(m) || c.isMemberClass(), "public/protected/member");
        if (c.isEnum()) check(c.getEnumConstants() != null, "enum has constants");
        else check(c.getEnumConstants() == null, "non-enum returns null constants");
        check(c.getDeclaredConstructors() != null, "constructors array non-null");
        check(c.getDeclaredMethods() != null, "methods array non-null");
        check(c.getDeclaredFields() != null, "fields array non-null");
        if (c.isInterface()) check(Modifier.isInterface(m), "interface modifier consistent");
        else if (c != Object.class) check(c.getSuperclass() != null, "class superclass non-null");
        check(!c.isInterface(), "class not interface");
        check(hasInterface(c, "java.io.Serializable"), "implements Serializable");
        check(hasMethod(c, "translate"), "declares translate");
        check(hasMethod(c, "shiftDown"), "declares shiftDown");
        check(hasMethod(c, "controlDown"), "declares controlDown");
        check(hasMethod(c, "metaDown"), "declares metaDown");
        check(hasMethod(c, "toString"), "declares toString");
    }

    static void testBlackBox() throws Exception {
        java.awt.Event e = new java.awt.Event("src", java.awt.Event.ACTION_EVENT, "arg");
        check(e.id == java.awt.Event.ACTION_EVENT, "id field set");
        check("src".equals(e.target), "target field set");
        check("arg".equals(e.arg), "arg field set");
        e.translate(3, 4);
        check(e.x == 3 && e.y == 4, "translate shifts x/y");
        java.awt.Event mods = new java.awt.Event("src", 0L, 0, 0, 0, 0, java.awt.Event.SHIFT_MASK, null);
        check(mods.shiftDown(), "shiftDown true");
        check(!mods.controlDown(), "controlDown false");
        check(!mods.metaDown(), "metaDown false");
        check(e.toString() != null, "toString non-null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
