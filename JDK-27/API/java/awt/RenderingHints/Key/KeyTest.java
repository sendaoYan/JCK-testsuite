/*
 * @test
 * @summary java.awt.RenderingHints.Key conformance: white-box structure + black-box behavior.
 * @run main KeyTest
 */

import java.lang.reflect.*;

public class KeyTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // A concrete Key for exercising the abstract API surface.
    static class TestKey extends java.awt.RenderingHints.Key {
        TestKey(int privatekey) { super(privatekey); }
        public boolean isCompatibleValue(Object val) { return val instanceof String; }
        public int exposeKey() { return intKey(); }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.RenderingHints$Key", false, KeyTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.RenderingHints.Key");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public class");
        check(Modifier.isAbstract(m), "abstract class");
        check(!c.isInterface(), "not interface");

        java.lang.reflect.Constructor<?> ctor = c.getDeclaredConstructor(int.class);
        check(ctor != null, "declared ctor (int)");
        check(Modifier.isProtected(ctor.getModifiers()), "ctor is protected");

        Method hc = c.getMethod("hashCode");
        check(Modifier.isFinal(hc.getModifiers()), "hashCode is final");
        Method eq = c.getMethod("equals", Object.class);
        check(Modifier.isFinal(eq.getModifiers()), "equals is final");
        Method icv = c.getMethod("isCompatibleValue", Object.class);
        check(Modifier.isAbstract(icv.getModifiers()), "isCompatibleValue is abstract");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        TestKey k1 = new TestKey(1001);
        TestKey k2 = new TestKey(1002);

        // intKey via subclass getter
        check(k1.exposeKey() == 1001, "intKey returns private key");
        check(k2.exposeKey() == 1002, "intKey distinct keys");

        // equals is identity (==)
        check(k1.equals(k1), "Key equals itself");
        check(!k1.equals(k2), "distinct instances not equal even for subclass");
        check(!k1.equals(null), "not equal null");
        check(!k1.equals("1001"), "not equal unrelated type");

        // hashCode is identity hash
        check(k1.hashCode() == System.identityHashCode(k1), "hashCode is identity hash");

        // isCompatibleValue from subclass
        check(k1.isCompatibleValue("valid"), "subclass isCompatibleValue accepts String");
        check(!k1.isCompatibleValue(Integer.valueOf(5)), "subclass rejects Integer");
        check(!k1.isCompatibleValue(null), "subclass rejects null");

        // duplicate privatekey in same subclass throws
        boolean iae = false;
        try { new TestKey(1001); } catch (IllegalArgumentException e) { iae = true; }
        check(iae, "duplicate privatekey in same subclass throws IllegalArgumentException");

        // built-in key compatibility
        java.awt.RenderingHints.Key aa = java.awt.RenderingHints.KEY_ANTIALIASING;
        check(aa.isCompatibleValue(java.awt.RenderingHints.VALUE_ANTIALIAS_ON),
              "KEY_ANTIALIASING accepts VALUE_ANTIALIAS_ON");
        check(aa.isCompatibleValue(java.awt.RenderingHints.VALUE_ANTIALIAS_OFF),
              "KEY_ANTIALIASING accepts VALUE_ANTIALIAS_OFF");
        check(aa.isCompatibleValue(java.awt.RenderingHints.VALUE_ANTIALIAS_DEFAULT),
              "KEY_ANTIALIASING accepts VALUE_ANTIALIAS_DEFAULT");
        check(!aa.isCompatibleValue("bogus"), "KEY_ANTIALIASING rejects bogus value");
        check(!aa.isCompatibleValue(null), "KEY_ANTIALIASING rejects null");

        java.awt.RenderingHints.Key rend = java.awt.RenderingHints.KEY_RENDERING;
        check(rend.isCompatibleValue(java.awt.RenderingHints.VALUE_RENDER_SPEED),
              "KEY_RENDERING accepts VALUE_RENDER_SPEED");
        check(!rend.isCompatibleValue(java.awt.RenderingHints.VALUE_ANTIALIAS_ON),
              "KEY_RENDERING rejects antialias value");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
