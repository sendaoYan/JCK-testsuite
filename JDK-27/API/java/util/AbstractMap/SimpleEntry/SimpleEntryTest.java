/*
 * @test
 * @summary java.util.AbstractMap.SimpleEntry conformance: key/value/setValue/equals.
 * @run main SimpleEntryTest
 */

import java.util.AbstractMap;
import java.util.Map;

public class SimpleEntryTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testSimpleEntry() {
        AbstractMap.SimpleEntry<String, Integer> e = new AbstractMap.SimpleEntry<>("k", 1);
        check("k".equals(e.getKey()), "getKey");
        check(e.getValue() == 1, "getValue");
        check(e.setValue(2) == 1, "setValue returns old");
        check(e.getValue() == 2, "setValue applied");
        check("k=2".equals(e.toString()), "toString");
        Map.Entry<String, Integer> src = new AbstractMap.SimpleEntry<>("x", 9);
        AbstractMap.SimpleEntry<String, Integer> copy = new AbstractMap.SimpleEntry<>(src);
        check("x".equals(copy.getKey()) && copy.getValue() == 9, "copy constructor");
        AbstractMap.SimpleEntry<String, Integer> same = new AbstractMap.SimpleEntry<>("k", 2);
        check(e.equals(same), "equals");
        check(e.hashCode() == same.hashCode(), "hashCode");
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

    // ---- equals contract: type-agnostic across Map.Entry implementations -----

    static void testEqualsContract() {
        AbstractMap.SimpleEntry<String, Integer> e = new AbstractMap.SimpleEntry<>("k", 1);

        // equals a SimpleImmutableEntry with the same key/value (Map.Entry contract)
        AbstractMap.SimpleImmutableEntry<String, Integer> sie =
                new AbstractMap.SimpleImmutableEntry<>("k", 1);
        check(e.equals(sie) && sie.equals(e), "equals SimpleImmutableEntry, symmetric");

        // equals a live HashMap entry view
        java.util.HashMap<String, Integer> hm = new java.util.HashMap<>();
        hm.put("k", 1);
        Map.Entry<String, Integer> view = hm.entrySet().iterator().next();
        check(e.equals(view) && view.equals(e), "equals HashMap entry view, symmetric");

        // negative branches
        check(!e.equals(null), "equals(null) false");
        check(!e.equals("k=1"), "equals(non-entry) false");
        check(!e.equals(new AbstractMap.SimpleEntry<>("k", 2)), "different value not equal");
        check(!e.equals(new AbstractMap.SimpleEntry<>("j", 1)), "different key not equal");
        check(!e.equals(new AbstractMap.SimpleEntry<>(null, 1)), "null vs non-null key not equal");
        check(!e.equals(new AbstractMap.SimpleEntry<>("k", null)), "null vs non-null value not equal");

        // null key and null value are permitted and compare correctly
        AbstractMap.SimpleEntry<String, Integer> n1 = new AbstractMap.SimpleEntry<>(null, 1);
        AbstractMap.SimpleEntry<String, Integer> n2 = new AbstractMap.SimpleEntry<>(null, 1);
        check(n1.equals(n2) && n2.equals(n1), "null-key entries equal");
        check(n1.hashCode() == n2.hashCode(), "null-key entries same hashCode");
    }

    // ---- hashCode formula: key.hashCode() ^ value.hashCode() ------------------

    static void testHashCodeFormula() {
        // "k".hashCode() == 107, Integer.hashCode(1) == 1 -> 107 ^ 1 == 106
        check(new AbstractMap.SimpleEntry<>("k", 1).hashCode() == 106, "hashCode (k,1) == 106");
        // null key contributes 0: 0 ^ 1 == 1
        check(new AbstractMap.SimpleEntry<String, Integer>(null, 1).hashCode() == 1,
              "hashCode (null,1) == 1");
        // null value contributes 0: 107 ^ 0 == 107
        check(new AbstractMap.SimpleEntry<>("k", null).hashCode() == 107,
              "hashCode (k,null) == 107");
        // both null: 0
        check(new AbstractMap.SimpleEntry<String, Integer>(null, null).hashCode() == 0,
              "hashCode (null,null) == 0");
        // consistent with equal entries of the other flavor
        check(new AbstractMap.SimpleEntry<>("k", 1).hashCode()
                  == new AbstractMap.SimpleImmutableEntry<>("k", 1).hashCode(),
              "hashCode matches SimpleImmutableEntry");
    }

    // ---- copy constructor takes a snapshot ------------------------------------

    static void testCopyConstructorSnapshot() {
        AbstractMap.SimpleEntry<String, Integer> src = new AbstractMap.SimpleEntry<>("x", 9);
        AbstractMap.SimpleEntry<String, Integer> copy = new AbstractMap.SimpleEntry<>(src);
        src.setValue(100);
        check(copy.getValue() == 9, "copy ctor snapshots value at construction");
        check(copy.getKey().equals("x"), "copy ctor keeps key");
        check(!copy.equals(src), "mutating source does not affect copy");

        // copying a SimpleImmutableEntry works too
        AbstractMap.SimpleImmutableEntry<String, Integer> src2 =
                new AbstractMap.SimpleImmutableEntry<>("y", 8);
        AbstractMap.SimpleEntry<String, Integer> copy2 = new AbstractMap.SimpleEntry<>(src2);
        check(copy2.getKey().equals("y") && copy2.getValue() == 8, "copy ctor from immutable entry");
        copy2.setValue(80);
        check(src2.getValue() == 8, "mutating copy leaves immutable source untouched");
    }

    // ---- setValue chain + toString with nulls ----------------------------------

    static void testSetValueChainAndToString() {
        AbstractMap.SimpleEntry<String, Integer> e = new AbstractMap.SimpleEntry<>("k", 1);
        check(e.setValue(2) == 1, "setValue returns previous");
        check(e.setValue(3) == 2, "setValue chain returns previous each time");
        check(e.getValue() == 3, "final value applied");
        check(e.toString().equals("k=3"), "toString after updates");

        AbstractMap.SimpleEntry<String, Integer> n =
                new AbstractMap.SimpleEntry<>(null, null);
        check(n.toString().equals("null=null"), "toString null=null");
        AbstractMap.SimpleEntry<String, Integer> half =
                new AbstractMap.SimpleEntry<>(null, 5);
        check(half.toString().equals("null=5"), "toString null key");
    }

    // ---- serialization round-trip -----------------------------------------------

    static void testSerialization() throws Exception {
        AbstractMap.SimpleEntry<String, Integer> e = new AbstractMap.SimpleEntry<>("k", 1);
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos)) {
            oos.writeObject(e);
        }
        Object back;
        try (java.io.ObjectInputStream ois =
                     new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray()))) {
            back = ois.readObject();
        }
        check(back.getClass() == AbstractMap.SimpleEntry.class, "round-trip class SimpleEntry");
        check(back.equals(e), "round-trip equal");
        check(((AbstractMap.SimpleEntry<String, Integer>) back).setValue(9) == 1,
              "deserialized entry still mutable");
    }

    public static void main(String[] args) throws Exception {
        testSimpleEntry();
        testEqualsContract();
        testHashCodeFormula();
        testCopyConstructorSnapshot();
        testSetValueChainAndToString();
        testSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
