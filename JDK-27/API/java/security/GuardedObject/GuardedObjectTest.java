/*
 * @test
 * @summary java.security.GuardedObject conformance: white-box structure + black-box behavior.
 * @run main GuardedObjectTest
 */

import java.io.ObjectInputStream;
import java.lang.reflect.Modifier;
import java.security.Guard;
import java.security.GuardedObject;

public class GuardedObjectTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // A Guard that always permits.
    static class PermissiveGuard implements Guard {
        public void checkGuard(Object o) { /* allow */ }
    }
    // A Guard that always denies.
    static class DenyGuard implements Guard {
        public void checkGuard(Object o) { throw new SecurityException("denied"); }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.GuardedObject");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "concrete class");
        checkInterfaces(c, new String[]{"java.io.Serializable"});
        checkMethods(c, new String[]{"getObject"});
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        String secret = "guarded value";

        // Permissive guard returns the object
        GuardedObject allowed = new GuardedObject(secret, new PermissiveGuard());
        check(secret.equals(allowed.getObject()), "getObject returns object when guard permits");

        // Denying guard throws SecurityException
        GuardedObject denied = new GuardedObject(secret, new DenyGuard());
        boolean threw = false;
        try {
            denied.getObject();
        } catch (SecurityException e) {
            threw = true;
            check("denied".equals(e.getMessage()), "SecurityException message");
        }
        check(threw, "getObject throws SecurityException when guard denies");

        // getObject may be called repeatedly
        check(secret.equals(allowed.getObject()), "getObject repeatable");

        // ---- appended contract dimensions ----
        // null guard: unconditional access; null payload retrievable
        GuardedObject unguarded = new GuardedObject("free", null);
        check("free".equals(unguarded.getObject()), "null guard permits");
        check(new GuardedObject(null, null).getObject() == null, "null object retrievable");
        // guard consulted on EVERY getObject call
        CountingGuard cg = new CountingGuard();
        GuardedObject counted = new GuardedObject(Integer.valueOf(7), cg);
        check(Integer.valueOf(7).equals(counted.getObject()), "counted 1st");
        check(Integer.valueOf(7).equals(counted.getObject()), "counted 2nd");
        check(Integer.valueOf(7).equals(counted.getObject()), "counted 3rd");
        check(cg.calls == 3, "guard called exactly 3 times");
        // denial message verbatim each time (fresh or cached is impl choice, message stable)
        try {
            denied.getObject();
        } catch (SecurityException e) {
            check("denied".equals(e.getMessage()), "repeat denial message");
        }
        // non-Security unchecked exceptions propagate unwrapped
        GuardedObject ise = new GuardedObject("v", new Guard() {
            public void checkGuard(Object o) { throw new IllegalStateException("boom"); }
        });
        try {
            ise.getObject();
            check(false, "ISE propagates");
        } catch (IllegalStateException e) {
            check("boom".equals(e.getMessage()), "ISE unwrapped");
        }
        // checkGuard receives the GuardedObject itself, not the payload
        final Object[] seen = new Object[1];
        GuardedObject self = new GuardedObject("payload", new Guard() {
            public void checkGuard(Object o) { seen[0] = o; }
        });
        self.getObject();
        check("payload".equals(seen[0]), "checkGuard receives the guarded payload");
        // serialization: permissive serializable guard -> payload survives
        GuardedObject ser = new GuardedObject("round", new CountingGuard());
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(ser);
        oos.close();
        ObjectInputStream in = new ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray()));
        GuardedObject back = (GuardedObject) in.readObject();
        check(back != ser, "deserialized new instance");
        check("round".equals(back.getObject()), "payload survives round-trip");
        // serialization itself consults the guard: denying guard blocks writeObject
        try {
            java.io.ByteArrayOutputStream b2 = new java.io.ByteArrayOutputStream();
            java.io.ObjectOutputStream o2 = new java.io.ObjectOutputStream(b2);
            o2.writeObject(denied);
            o2.close();
            check(false, "denying guard blocks serialization");
        } catch (SecurityException e) {
            check(true, "denying guard blocks serialization");
        }
        // structural: Serializable surface with single getObject
        check(java.io.Serializable.class.isAssignableFrom(GuardedObject.class), "Serializable");
        int getObjectCount = 0;
        for (java.lang.reflect.Method m : GuardedObject.class.getDeclaredMethods())
            if (m.getName().equals("getObject")) getObjectCount++;
        check(getObjectCount == 1, "single getObject method");
    }

    static class CountingGuard implements Guard, java.io.Serializable {
        private static final long serialVersionUID = 1L;
        int calls;
        public void checkGuard(Object o) { calls++; }
    }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }
    static void checkInterfaces(Class<?> c, String[] names) {
        for (String n : names) check(hasInterface(c, n), "implements " + n);
    }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    static void checkMethods(Class<?> c, String[] names) {
        for (String n : names) check(hasMethod(c, n), "method " + n);
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
