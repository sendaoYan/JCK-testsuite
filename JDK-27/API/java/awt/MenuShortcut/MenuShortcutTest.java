/*
 * @test
 * @summary java.awt.MenuShortcut conformance: white-box structure + black-box behavior.
 * @run main MenuShortcutTest
 */

import java.lang.reflect.*;
import java.awt.MenuShortcut;
import java.awt.event.KeyEvent;

public class MenuShortcutTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name) {
        for (Method m : c.getMethods())
            if (m.getName().equals(name)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.MenuShortcut", false, MenuShortcutTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.MenuShortcut");
        int m = c.getModifiers();
        check(Modifier.isPublic(m) && !Modifier.isAbstract(m), "public concrete class");
        check(c.getEnumConstants() == null, "non-enum");
        check(java.io.Serializable.class.isAssignableFrom(c), "implements Serializable");
        check(c.getConstructor(int.class) != null, "ctor (int)");
        check(c.getConstructor(int.class, boolean.class) != null, "ctor (int,boolean)");
        for (String n : new String[] {"getKey", "usesShiftModifier", "equals", "hashCode", "toString"})
            check(hasMethod(c, n), "method present: " + n);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        MenuShortcut plain = new MenuShortcut(KeyEvent.VK_A);
        check(plain.getKey() == KeyEvent.VK_A, "getKey == VK_A");
        check(!plain.usesShiftModifier(), "plain not using shift");

        MenuShortcut shifted = new MenuShortcut(KeyEvent.VK_A, true);
        check(shifted.getKey() == KeyEvent.VK_A, "shifted getKey == VK_A");
        check(shifted.usesShiftModifier(), "shifted uses shift modifier");

        // hashCode: key when no shift, ~key when shift
        check(plain.hashCode() == KeyEvent.VK_A, "plain hashCode == key");
        check(shifted.hashCode() == ~KeyEvent.VK_A, "shifted hashCode == ~key");

        // equals
        check(plain.equals(new MenuShortcut(KeyEvent.VK_A)), "equal shortcuts");
        check(!plain.equals(new MenuShortcut(KeyEvent.VK_B)), "different key not equal");
        check(!plain.equals(shifted), "shift flag differs -> not equal");
        check(!plain.equals(null), "not equal null");
        check(!plain.equals("A"), "not equal unrelated type");
        check(plain.equals(plain), "reflexive");

        // toString ends with the localized key text
        check(plain.toString().endsWith("A"), "toString ends with 'A': " + plain);
        check(shifted.toString().endsWith("A"), "shifted toString ends with 'A': " + shifted);
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
