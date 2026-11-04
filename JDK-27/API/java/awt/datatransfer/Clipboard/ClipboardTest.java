/*
 * @test
 * @summary java.awt.datatransfer.Clipboard conformance: white-box structure + black-box behavior.
 * @run main ClipboardTest
 */

import java.util.concurrent.atomic.AtomicInteger;
import java.lang.reflect.*;

public class ClipboardTest {
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
        Class<?> c = Class.forName("java.awt.datatransfer.Clipboard", false, ClipboardTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.datatransfer.Clipboard");
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
        check(hasMethod(c, "setContents"), "declares setContents");
        check(hasMethod(c, "getData"), "declares getData");
        check(hasMethod(c, "addFlavorListener"), "declares addFlavorListener");
    }

    static void testBlackBox() throws Exception {
        java.awt.datatransfer.Clipboard cb = new java.awt.datatransfer.Clipboard("testClip");
        check("testClip".equals(cb.getName()), "getName");
        java.awt.datatransfer.StringSelection sel = new java.awt.datatransfer.StringSelection("hello");
        java.awt.datatransfer.ClipboardOwner owner = (c, t) -> {};
        cb.setContents(sel, owner);
        check(cb.getContents(null) == sel, "getContents returns set Transferable");
        check("hello".equals(cb.getData(java.awt.datatransfer.DataFlavor.stringFlavor)), "getData stringFlavor");
        java.awt.datatransfer.DataFlavor[] fl = cb.getAvailableDataFlavors();
        boolean hasText = false;
        for (java.awt.datatransfer.DataFlavor f : fl) if (java.awt.datatransfer.DataFlavor.stringFlavor.equals(f)) hasText = true;
        check(hasText, "available flavors include stringFlavor");
        check(cb.isDataFlavorAvailable(java.awt.datatransfer.DataFlavor.stringFlavor), "isDataFlavorAvailable stringFlavor");
        check(!cb.isDataFlavorAvailable(java.awt.datatransfer.DataFlavor.imageFlavor), "imageFlavor not available");
        java.util.concurrent.atomic.AtomicInteger called = new java.util.concurrent.atomic.AtomicInteger();
        java.awt.datatransfer.FlavorListener l = e -> called.incrementAndGet();
        cb.addFlavorListener(l);
        check(cb.getFlavorListeners() != null && cb.getFlavorListeners().length == 1, "one flavor listener");
        cb.removeFlavorListener(l);
        check(cb.getFlavorListeners().length == 0, "listener removed");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
