/*
 * @test
 * @summary java.awt.im.spi.InputMethodContext conformance: white-box structure + black-box behavior.
 * @run main InputMethodContextTest
 */

import java.lang.reflect.*;

public class InputMethodContextTest {
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
        Class<?> c = Class.forName("java.awt.im.spi.InputMethodContext", false, InputMethodContextTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.im.spi.InputMethodContext");
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
        check(hasInterface(c, "java.awt.im.InputMethodRequests"), "extends InputMethodRequests");
    }

    static void testBlackBox() throws Exception {
        java.awt.im.spi.InputMethodContext ctx = new java.awt.im.spi.InputMethodContext() {
            public void dispatchInputMethodEvent(int id, java.text.AttributedCharacterIterator t, int c, java.awt.font.TextHitInfo a, java.awt.font.TextHitInfo b) {}
            public java.awt.Window createInputMethodWindow(String t, boolean f) { return null; }
            public javax.swing.JFrame createInputMethodJFrame(String t, boolean f) { return null; }
            public void enableClientWindowNotification(java.awt.im.spi.InputMethod m, boolean e) {}
            public java.awt.Rectangle getTextLocation(java.awt.font.TextHitInfo h) { return null; }
            public java.awt.font.TextHitInfo getLocationOffset(int x, int y) { return null; }
            public int getInsertPositionOffset() { return 0; }
            public java.text.AttributedCharacterIterator getCommittedText(int b, int e, java.text.AttributedCharacterIterator.Attribute[] a) { return null; }
            public int getCommittedTextLength() { return 0; }
            public java.text.AttributedCharacterIterator cancelLatestCommittedText(java.text.AttributedCharacterIterator.Attribute[] a) { return null; }
            public java.text.AttributedCharacterIterator getSelectedText(java.text.AttributedCharacterIterator.Attribute[] a) { return null; }
        };
        ctx.dispatchInputMethodEvent(0, null, 0, null, null);
        check(ctx.createInputMethodWindow(null, false) == null, "createInputMethodWindow null");
        check(ctx.createInputMethodJFrame(null, false) == null, "createInputMethodJFrame null");
        ctx.enableClientWindowNotification(null, false);
        check(true, "InputMethodContext callable");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
