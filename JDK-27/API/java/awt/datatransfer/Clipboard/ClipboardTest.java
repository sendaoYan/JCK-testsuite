/*
 * @test
 * @summary java.awt.datatransfer.Clipboard conformance: white-box structure + black-box behavior.
 * @run main ClipboardTest
 */

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
        // setContents/getContents are synchronized per javadoc contract of a thread-safe clipboard
        check(Modifier.isSynchronized(c.getMethod("setContents", java.awt.datatransfer.Transferable.class, java.awt.datatransfer.ClipboardOwner.class).getModifiers()), "setContents synchronized");
        check(Modifier.isSynchronized(c.getMethod("getContents", Object.class).getModifiers()), "getContents synchronized");
        // setContents returns void (owner notification is via lostOwnership callback)
        check(c.getMethod("setContents", java.awt.datatransfer.Transferable.class, java.awt.datatransfer.ClipboardOwner.class).getReturnType() == void.class, "setContents returns void");
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

        // ---- empty clipboard semantics ----
        java.awt.datatransfer.Clipboard empty = new java.awt.datatransfer.Clipboard("empty");
        check(empty.getContents(null) == null, "empty clipboard getContents null");
        check(empty.getContents(empty) == null, "empty clipboard getContents(requestor) null");
        check(empty.getAvailableDataFlavors() != null && empty.getAvailableDataFlavors().length == 0, "empty clipboard no flavors");
        check(!empty.isDataFlavorAvailable(java.awt.datatransfer.DataFlavor.stringFlavor), "empty clipboard flavor unavailable");
        boolean emptyUfe = false;
        try { empty.getData(java.awt.datatransfer.DataFlavor.stringFlavor); }
        catch (java.awt.datatransfer.UnsupportedFlavorException x) { emptyUfe = true; }
        check(emptyUfe, "empty clipboard getData throws UnsupportedFlavorException");

        // ---- setContents null semantics: null contents clears the clipboard ----
        java.awt.datatransfer.Clipboard cb2 = new java.awt.datatransfer.Clipboard("c2");
        cb2.setContents(new java.awt.datatransfer.StringSelection("one"), null);
        check(cb2.getContents(null) != null, "setContents with null owner still stores contents");
        check("one".equals(cb2.getData(java.awt.datatransfer.DataFlavor.stringFlavor)), "getData after null-owner setContents");
        cb2.setContents(null, null);
        check(cb2.getContents(null) == null, "setContents(null) clears clipboard");
        check(!cb2.isDataFlavorAvailable(java.awt.datatransfer.DataFlavor.stringFlavor), "cleared clipboard flavor unavailable");
        // replacement
        java.awt.datatransfer.Transferable first = new java.awt.datatransfer.StringSelection("first");
        java.awt.datatransfer.Transferable second = new java.awt.datatransfer.StringSelection("second");
        cb2.setContents(first, null);
        cb2.setContents(second, null);
        check(cb2.getContents(null) == second, "second setContents replaces first");
        // requestor argument is ignored for local clipboard
        check(cb2.getContents(new Object()) == second, "getContents ignores requestor");

        // ---- getData negative: unsupported flavor ----
        boolean ufe = false;
        try { cb2.getData(java.awt.datatransfer.DataFlavor.imageFlavor); }
        catch (java.awt.datatransfer.UnsupportedFlavorException x) { ufe = true; }
        check(ufe, "getData unsupported flavor throws UnsupportedFlavorException");
        // available flavors reflect the current contents
        java.awt.datatransfer.DataFlavor[] avail = cb2.getAvailableDataFlavors();
        boolean hasString = false;
        for (java.awt.datatransfer.DataFlavor f : avail) if (java.awt.datatransfer.DataFlavor.stringFlavor.equals(f)) hasString = true;
        check(hasString, "available flavors after StringSelection include stringFlavor");

        // ---- FlavorListener set semantics ----
        java.awt.datatransfer.Clipboard cb3 = new java.awt.datatransfer.Clipboard("c3");
        check(cb3.getFlavorListeners() != null && cb3.getFlavorListeners().length == 0, "fresh clipboard no listeners");
        java.awt.datatransfer.FlavorListener l1 = e -> {};
        java.awt.datatransfer.FlavorListener l2 = e -> {};
        cb3.addFlavorListener(l1);
        cb3.addFlavorListener(l1); // duplicate registration
        // JDK-9+ Clipboard deduplicates listener registration (JDK-8 stored it twice)
        check(cb3.getFlavorListeners().length == 1, "duplicate addFlavorListener deduplicated");
        cb3.addFlavorListener(l2);
        check(cb3.getFlavorListeners().length == 2, "two distinct listeners registered");
        check(cb3.getFlavorListeners() != cb3.getFlavorListeners(), "getFlavorListeners returns fresh array");
        java.awt.datatransfer.FlavorListener[] got = cb3.getFlavorListeners();
        boolean found1 = false, found2 = false;
        for (java.awt.datatransfer.FlavorListener x : got) { if (x == l1) found1 = true; if (x == l2) found2 = true; }
        check(found1 && found2, "getFlavorListeners returns registered instances");
        cb3.removeFlavorListener(l1);
        check(cb3.getFlavorListeners().length == 1, "remove leaves one");
        cb3.removeFlavorListener(l1); // removing a non-registered listener is a no-op
        check(cb3.getFlavorListeners().length == 1, "removing non-registered listener is no-op");
        cb3.removeFlavorListener(l2);
        check(cb3.getFlavorListeners().length == 0, "all removed");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
