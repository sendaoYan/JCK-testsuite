/*
 * @test
 * @summary java.awt.datatransfer.ClipboardOwner conformance: white-box structure + black-box behavior.
 * @run main ClipboardOwnerTest
 */

import java.lang.reflect.*;

public class ClipboardOwnerTest {
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
        Class<?> c = Class.forName("java.awt.datatransfer.ClipboardOwner", false, ClipboardOwnerTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.datatransfer.ClipboardOwner");
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
        java.awt.datatransfer.ClipboardOwner o = (c, t) -> {};
        o.lostOwnership(null, null);
        check(true, "lostOwnership callable");
        check(!(o instanceof java.util.EventListener), "ClipboardOwner is NOT an EventListener (standalone interface)");

        // ---- callback receives exact clipboard/transferable instances ----
        final java.util.List<String> calls = new java.util.ArrayList<>();
        final java.awt.datatransfer.Clipboard cb = new java.awt.datatransfer.Clipboard("own");
        final java.awt.datatransfer.Transferable tr = new java.awt.datatransfer.StringSelection("data");
        java.awt.datatransfer.ClipboardOwner recording = (c, t) -> {
            calls.add("lost");
            if (c != null) { check(c == cb, "callback clipboard identity"); check(t == tr, "callback transferable identity"); }
        };
        recording.lostOwnership(cb, tr);
        check(calls.size() == 1 && calls.get(0).equals("lost"), "lostOwnership dispatched once");
        // null arguments are legal per contract (no focus/clipboard left)
        recording.lostOwnership(null, null);
        check(calls.size() == 2, "lostOwnership(null,null) callable");
        // exactly one abstract method
        check(java.awt.datatransfer.ClipboardOwner.class.getDeclaredMethods().length == 1, "single abstract method");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
