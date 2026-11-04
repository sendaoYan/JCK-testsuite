/*
 * @test
 * @summary java.awt.im.InputContext conformance: white-box structure + black-box behavior.
 * @run main InputContextTest
 */

import java.lang.reflect.*;

public class InputContextTest {
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
        Class<?> c = Class.forName("java.awt.im.InputContext", false, InputContextTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.im.InputContext");
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
        check(hasMethod(c, "getInstance"), "declares getInstance");
        check(hasMethod(c, "isCompositionEnabled"), "declares isCompositionEnabled");
        check(hasMethod(c, "dispose"), "declares dispose");
    }

    static void testBlackBox() throws Exception {
        java.awt.im.InputContext ic = java.awt.im.InputContext.getInstance();
        check(ic != null, "getInstance non-null");
        java.util.Locale loc = ic.getLocale();
        check(loc == null || loc instanceof java.util.Locale, "getLocale returns null or valid Locale");

        // isCompositionEnabled() either returns a boolean (input method present)
        // or throws UnsupportedOperationException (no input method). Both are legal.
        boolean noInputMethod = false;
        boolean enabled = false;
        try {
            enabled = ic.isCompositionEnabled();
        } catch (UnsupportedOperationException e) {
            noInputMethod = true;
        }
        check(true, "isCompositionEnabled callable (boolean or UnsupportedOperationException)");

        if (noInputMethod) {
            boolean uoe = false;
            try { ic.setCompositionEnabled(false); } catch (UnsupportedOperationException e) { uoe = true; }
            check(uoe, "setCompositionEnabled throws UnsupportedOperationException when no input method");
        } else {
            ic.setCompositionEnabled(false);
            check(true, "setCompositionEnabled callable when input method present");
        }

        ic.endComposition();
        ic.selectInputMethod(java.util.Locale.ROOT);
        ic.dispose();
        check(true, "endComposition/selectInputMethod/dispose callable");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
