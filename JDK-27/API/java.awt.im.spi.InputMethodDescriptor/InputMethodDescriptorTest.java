/*
 * @test
 * @summary java.awt.im.spi.InputMethodDescriptor conformance: white-box structure + black-box behavior.
 * @run main InputMethodDescriptorTest
 */

import java.lang.reflect.*;

public class InputMethodDescriptorTest {
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
        Class<?> c = Class.forName("java.awt.im.spi.InputMethodDescriptor", false, InputMethodDescriptorTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.im.spi.InputMethodDescriptor");
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
        java.awt.im.spi.InputMethodDescriptor d = new java.awt.im.spi.InputMethodDescriptor() {
            public java.util.Locale[] getAvailableLocales() { return new java.util.Locale[0]; }
            public boolean hasDynamicLocaleList() { return false; }
            public String getInputMethodDisplayName(java.util.Locale l1, java.util.Locale l2) { return "IM"; }
            public java.awt.Image getInputMethodIcon(java.util.Locale l) { return null; }
            public java.awt.im.spi.InputMethod createInputMethod() { return null; }
        };
        check(d.getAvailableLocales() != null && d.getAvailableLocales().length == 0, "empty locales");
        check(d.hasDynamicLocaleList() == false, "hasDynamicLocaleList false");
        check("IM".equals(d.getInputMethodDisplayName(java.util.Locale.ROOT, java.util.Locale.US)), "display name");
        check(d.getInputMethodIcon(null) == null, "icon null");
        check(d.createInputMethod() == null, "createInputMethod null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
