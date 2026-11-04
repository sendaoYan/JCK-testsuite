/*
 * @test
 * @summary java.awt.datatransfer.FlavorEvent conformance: white-box structure + black-box behavior.
 * @run main FlavorEventTest
 */

import java.io.*;
import java.lang.reflect.*;

public class FlavorEventTest {
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
        Class<?> c = Class.forName("java.awt.datatransfer.FlavorEvent", false, FlavorEventTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.datatransfer.FlavorEvent");
        int m = c.getModifiers();
        check(Modifier.isPublic(m) || Modifier.isProtected(m) || c.isMemberClass(), "public/protected/member");
        if (c.isEnum()) check(c.getEnumConstants() != null, "enum has constants");
        else check(c.getEnumConstants() == null, "non-enum returns null constants");
        check(c.getDeclaredConstructors() != null, "constructors array non-null");
        check(c.getDeclaredMethods() != null, "methods array non-null");
        check(c.getDeclaredFields() != null, "fields array non-null");
        if (c.isInterface()) check(Modifier.isInterface(m), "interface modifier consistent");
        else if (c != Object.class) check(c.getSuperclass() != null, "class superclass non-null");
        check(c.getSuperclass().getName().equals("java.util.EventObject"), "extends EventObject");
        check(java.io.Serializable.class.isAssignableFrom(c), "Serializable (via EventObject)");
        check(Modifier.isPublic(c.getConstructor(java.awt.datatransfer.Clipboard.class).getModifiers()), "public Clipboard ctor");
    }

    static void testBlackBox() throws Exception {
        java.awt.datatransfer.Clipboard cb = new java.awt.datatransfer.Clipboard("evt");
        java.awt.datatransfer.FlavorEvent e = new java.awt.datatransfer.FlavorEvent(cb);
        check(e.getSource() == cb, "getSource is clipboard");
        check(e.toString() != null, "toString non-null");
        check(e.toString().contains("FlavorEvent"), "toString has class name");

        // null source is rejected by EventObject's constructor
        boolean iae = false;
        try { new java.awt.datatransfer.FlavorEvent(null); }
        catch (IllegalArgumentException x) { iae = true; }
        check(iae, "FlavorEvent(null) throws IAE");

        // each event keeps its own source identity
        java.awt.datatransfer.Clipboard cb2 = new java.awt.datatransfer.Clipboard("evt2");
        java.awt.datatransfer.FlavorEvent e2 = new java.awt.datatransfer.FlavorEvent(cb2);
        check(e2.getSource() == cb2 && e.getSource() == cb, "independent sources");

        // serialization round-trip (Clipboard is not serializable, source drops to null)
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        new ObjectOutputStream(b).writeObject(e);
        java.awt.datatransfer.FlavorEvent back =
            (java.awt.datatransfer.FlavorEvent) new ObjectInputStream(new ByteArrayInputStream(b.toByteArray())).readObject();
        check(back != null, "serialization round-trip returns FlavorEvent");
        check(back instanceof java.util.EventObject, "round-trip keeps EventObject supertype");

        // listener dispatch: FlavorListener receives the exact event instance
        final java.util.List<java.awt.datatransfer.FlavorEvent> seen = new java.util.ArrayList<>();
        java.awt.datatransfer.FlavorListener l = seen::add;
        l.flavorsChanged(e);
        check(seen.size() == 1 && seen.get(0) == e, "flavorsChanged receives exact event instance");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
