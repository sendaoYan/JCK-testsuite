/*
 * @test
 * @summary java.awt.datatransfer.FlavorListener conformance: white-box structure + black-box behavior.
 * @run main FlavorListenerTest
 */

import java.lang.reflect.*;

public class FlavorListenerTest {
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
        Class<?> c = Class.forName("java.awt.datatransfer.FlavorListener", false, FlavorListenerTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.datatransfer.FlavorListener");
        int m = c.getModifiers();
        check(Modifier.isPublic(m) || Modifier.isProtected(m) || c.isMemberClass(), "public/protected/member");
        if (c.isEnum()) check(c.getEnumConstants() != null, "enum has constants");
        else check(c.getEnumConstants() == null, "non-enum returns null constants");
        check(c.getDeclaredConstructors() != null, "constructors array non-null");
        check(c.getDeclaredMethods() != null, "methods array non-null");
        check(c.getDeclaredFields() != null, "fields array non-null");
        if (c.isInterface()) check(Modifier.isInterface(m), "interface modifier consistent");
        else if (c != Object.class) check(c.getSuperclass() != null, "class superclass non-null");
        check(hasInterface(c, "java.util.EventListener"), "extends EventListener");
    }

    static void testBlackBox() throws Exception {
        java.awt.datatransfer.FlavorListener l = e -> {};
        l.flavorsChanged(null);
        check(true, "flavorsChanged callable");
        check(l instanceof java.util.EventListener, "FlavorListener instanceof EventListener");
        check(java.awt.datatransfer.FlavorListener.class.getDeclaredMethods().length == 1, "single abstract method");
        java.lang.reflect.Method m = java.awt.datatransfer.FlavorListener.class.getMethod("flavorsChanged", java.awt.datatransfer.FlavorEvent.class);
        check(java.lang.reflect.Modifier.isPublic(m.getModifiers()) && java.lang.reflect.Modifier.isAbstract(m.getModifiers()), "flavorsChanged public abstract");
        check(m.getReturnType() == void.class, "flavorsChanged returns void");
        // exact event instance delivery
        final java.util.List<java.awt.datatransfer.FlavorEvent> seen = new java.util.ArrayList<>();
        java.awt.datatransfer.FlavorListener rec = seen::add;
        java.awt.datatransfer.FlavorEvent ev = new java.awt.datatransfer.FlavorEvent(new java.awt.datatransfer.Clipboard("l"));
        rec.flavorsChanged(ev);
        check(seen.size() == 1 && seen.get(0) == ev, "exact event instance delivered");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
