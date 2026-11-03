/*
 * @test
 * @summary java.awt.im.InputMethodHighlight conformance: white-box structure + black-box behavior.
 * @run main InputMethodHighlightTest
 */

import java.lang.reflect.*;

public class InputMethodHighlightTest {
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
        Class<?> c = Class.forName("java.awt.im.InputMethodHighlight", false, InputMethodHighlightTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.im.InputMethodHighlight");
        int m = c.getModifiers();
        check(Modifier.isPublic(m) || Modifier.isProtected(m) || c.isMemberClass(), "public/protected/member");
        if (c.isEnum()) check(c.getEnumConstants() != null, "enum has constants");
        else check(c.getEnumConstants() == null, "non-enum returns null constants");
        check(c.getDeclaredConstructors() != null, "constructors array non-null");
        check(c.getDeclaredMethods() != null, "methods array non-null");
        check(c.getDeclaredFields() != null, "fields array non-null");
        if (c.isInterface()) check(Modifier.isInterface(m), "interface modifier consistent");
        else if (c != Object.class) check(c.getSuperclass() != null, "class superclass non-null");
        check(!c.isInterface(), "class");
        check(hasMethod(c, "isSelected"), "declares isSelected");
        check(hasMethod(c, "getState"), "declares getState");
    }

    static void testBlackBox() throws Exception {
        check(java.awt.im.InputMethodHighlight.RAW_TEXT == 0, "RAW_TEXT 0");
        check(java.awt.im.InputMethodHighlight.CONVERTED_TEXT == 1, "CONVERTED_TEXT 1");
        check(java.awt.im.InputMethodHighlight.UNSELECTED_RAW_TEXT_HIGHLIGHT != null, "constant non-null");
        check(java.awt.im.InputMethodHighlight.SELECTED_CONVERTED_TEXT_HIGHLIGHT != null, "constant non-null");
        java.awt.im.InputMethodHighlight h = new java.awt.im.InputMethodHighlight(true, java.awt.im.InputMethodHighlight.CONVERTED_TEXT);
        check(h.isSelected(), "isSelected true");
        check(h.getState() == java.awt.im.InputMethodHighlight.CONVERTED_TEXT, "getState");
        check(h.getVariation() == 0, "default variation 0");
        java.awt.im.InputMethodHighlight h2 = new java.awt.im.InputMethodHighlight(false, java.awt.im.InputMethodHighlight.RAW_TEXT, 3);
        check(!h2.isSelected(), "isSelected false");
        check(h2.getVariation() == 3, "getVariation 3");
        java.awt.im.InputMethodHighlight h3 = new java.awt.im.InputMethodHighlight(true, java.awt.im.InputMethodHighlight.CONVERTED_TEXT, 0, java.util.Collections.emptyMap());
        check(h3.getStyle() != null && h3.getStyle().isEmpty(), "style empty map");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
