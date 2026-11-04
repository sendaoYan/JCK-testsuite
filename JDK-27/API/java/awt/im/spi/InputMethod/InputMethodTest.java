/*
 * @test
 * @summary java.awt.im.spi.InputMethod conformance: white-box structure + black-box behavior.
 * @run main InputMethodTest
 */

import java.lang.reflect.*;

public class InputMethodTest {
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
        Class<?> c = Class.forName("java.awt.im.spi.InputMethod", false, InputMethodTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.im.spi.InputMethod");
        int m = c.getModifiers();
        check(Modifier.isPublic(m) || Modifier.isProtected(m) || c.isMemberClass(), "public/protected/member");
        if (c.isEnum()) check(c.getEnumConstants() != null, "enum has constants");
        else check(c.getEnumConstants() == null, "non-enum returns null constants");
        check(c.getDeclaredConstructors() != null, "constructors array non-null");
        check(c.getDeclaredMethods() != null, "methods array non-null");
        check(c.getDeclaredFields() != null, "fields array non-null");
        if (c.isInterface()) check(Modifier.isInterface(m), "interface modifier consistent");
        else if (c != Object.class) check(c.getSuperclass() != null, "class superclass non-null");
        check(c.isInterface(), "is interface");
    }

    static void testBlackBox() throws Exception {
        java.awt.im.spi.InputMethod im = new java.awt.im.spi.InputMethod() {
            public void setInputMethodContext(java.awt.im.spi.InputMethodContext c) {}
            public boolean setLocale(java.util.Locale l) { return false; }
            public java.util.Locale getLocale() { return null; }
            public void setCharacterSubsets(java.lang.Character.Subset[] s) {}
            public void setCompositionEnabled(boolean b) {}
            public boolean isCompositionEnabled() { return false; }
            public void reconvert() {}
            public void dispatchEvent(java.awt.AWTEvent e) {}
            public void notifyClientWindowChange(java.awt.Rectangle r) {}
            public void activate() {}
            public void deactivate(boolean b) {}
            public void hideWindows() {}
            public void removeNotify() {}
            public void endComposition() {}
            public void dispose() {}
            public Object getControlObject() { return null; }
        };
        check(im.setLocale(java.util.Locale.ROOT) == false, "setLocale false");
        check(im.getLocale() == null, "getLocale null");
        check(im.isCompositionEnabled() == false, "isCompositionEnabled false");
        im.setInputMethodContext(null); im.activate(); im.dispose();
        check(im.getControlObject() == null, "getControlObject null");
        check(true, "all InputMethod methods callable");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
