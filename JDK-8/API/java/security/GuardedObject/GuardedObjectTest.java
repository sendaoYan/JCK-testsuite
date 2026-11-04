/*
 * @test
 * @summary java.security.GuardedObject conformance: construct with guard and getObject.
 *          Appended: null guard permits unconditionally, guard denial propagates the
 *          guard's own SecurityException, guard consulted on every getObject,
 *          non-Security unchecked exceptions propagate unwrapped, Serializable
 *          round-trip with a permissive serializable guard, serialization itself
 *          invokes the guard (denying guard blocks writeObject), structural
 *          Serializable + single-arg getObject surface.
 * @run main GuardedObjectTest
 */

public class GuardedObjectTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface ThrowingRunnable { void run() throws Throwable; }

    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    static class CountingGuard implements java.security.Guard, java.io.Serializable {
        private static final long serialVersionUID = 1L;
        int calls;
        public void checkGuard(Object o) { calls++; }
    }

    static class DenyingGuard implements java.security.Guard, java.io.Serializable {
        private static final long serialVersionUID = 1L;
        public void checkGuard(Object o) { throw new SecurityException("guarded-denied"); }
    }

    static <T> T roundtrip(java.io.Serializable o) throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(o);
        oos.close();
        java.io.ObjectInputStream ois =
                new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray()));
        @SuppressWarnings("unchecked")
        T back = (T) ois.readObject();
        return back;
    }

    public static void main(String[] args) throws Exception {
        java.security.Guard g = new java.security.Guard() { public void checkGuard(Object o) {} };
        java.security.GuardedObject go = new java.security.GuardedObject("x", g);
        check("x".equals(go.getObject()), "getObject");
        testContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- appended contract dimensions ----
    static void testContract() throws Exception {
        // structural: concrete, Serializable, only getObject()
        Class<?> c = java.security.GuardedObject.class;
        check(java.io.Serializable.class.isAssignableFrom(c), "implements Serializable");
        java.lang.reflect.Method[] ms = c.getDeclaredMethods();
        int getObjectCount = 0;
        for (java.lang.reflect.Method m : ms) if (m.getName().equals("getObject")) getObjectCount++;
        check(getObjectCount == 1, "single getObject method");

        // null guard: unconditional access (javadoc: guard==null means no guard)
        java.security.GuardedObject unguarded = new java.security.GuardedObject("free", null);
        check("free".equals(unguarded.getObject()), "null guard permits");
        check("free".equals(unguarded.getObject()), "null guard repeatable");

        // null object with null guard: storing null is legal
        java.security.GuardedObject nullObj = new java.security.GuardedObject(null, null);
        check(nullObj.getObject() == null, "null object retrievable");

        // denial: the guard's SecurityException propagates verbatim from getObject
        java.security.GuardedObject denied = new java.security.GuardedObject("secret", new DenyingGuard());
        checkThrows(SecurityException.class, () -> denied.getObject(), "guard denial propagates");
        try {
            denied.getObject();
        } catch (SecurityException e) {
            check("guarded-denied".equals(e.getMessage()), "denial message verbatim");
        }

        // guard consulted on EVERY getObject call
        CountingGuard cg = new CountingGuard();
        java.security.GuardedObject counted = new java.security.GuardedObject(Integer.valueOf(7), cg);
        check(Integer.valueOf(7).equals(counted.getObject()), "1st access ok");
        check(Integer.valueOf(7).equals(counted.getObject()), "2nd access ok");
        check(Integer.valueOf(7).equals(counted.getObject()), "3rd access ok");
        check(cg.calls == 3, "guard called exactly 3 times");

        // non-Security unchecked exceptions propagate unwrapped (not converted)
        java.security.GuardedObject ise = new java.security.GuardedObject("v", new java.security.Guard() {
            public void checkGuard(Object o) { throw new IllegalStateException("boom"); }
        });
        checkThrows(IllegalStateException.class, () -> ise.getObject(), "ISE propagates unwrapped");

        // guard may inspect the guarded payload: checkGuard receives the object, not the GuardedObject
        final Object[] seen = new Object[1];
        java.security.GuardedObject self = new java.security.GuardedObject("payload", new java.security.Guard() {
            public void checkGuard(Object o) { seen[0] = o; }
        });
        self.getObject();
        check("payload".equals(seen[0]), "checkGuard receives the guarded payload");

        // serialization: permissive serializable guard -> payload survives round-trip
        java.security.GuardedObject ser = new java.security.GuardedObject("round", new CountingGuard());
        java.security.GuardedObject back = roundtrip(ser);
        check(back != ser, "deserialized is a new instance");
        check("round".equals(back.getObject()), "payload survives round-trip");

        // serialization invokes the guard: denying guard blocks writeObject with SecurityException
        java.security.GuardedObject blocked = new java.security.GuardedObject("x", new DenyingGuard());
        checkThrows(SecurityException.class, () -> roundtrip(blocked), "denying guard blocks serialization");
    }
}
