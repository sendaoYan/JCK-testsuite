/*
 * @test
 * @summary java.util.AbstractMap.SimpleImmutableEntry conformance: immutable entry.
 * @run main SimpleImmutableEntryTest
 */

import java.util.AbstractMap;
import java.util.Map;

public class SimpleImmutableEntryTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testSimpleImmutableEntry() {
        AbstractMap.SimpleImmutableEntry<String, Integer> e = new AbstractMap.SimpleImmutableEntry<>("k", 1);
        check("k".equals(e.getKey()), "getKey");
        check(e.getValue() == 1, "getValue");
        check("k=1".equals(e.toString()), "toString");
        boolean threw = false;
        try {
            e.setValue(2);
        } catch (UnsupportedOperationException expected) {
            threw = true;
        }
        check(threw, "setValue throws UnsupportedOperationException");
        Map.Entry<String, Integer> src = new AbstractMap.SimpleEntry<>("x", 9);
        AbstractMap.SimpleImmutableEntry<String, Integer> copy = new AbstractMap.SimpleImmutableEntry<>(src);
        check("x".equals(copy.getKey()) && copy.getValue() == 9, "copy constructor");
    }

    // ---- immutability contract -----------------------------------------------

    static void testImmutability() {
        AbstractMap.SimpleImmutableEntry<String, Integer> e =
                new AbstractMap.SimpleImmutableEntry<>("k", 1);
        boolean threw = false;
        try {
            e.setValue(2);
        } catch (UnsupportedOperationException expected) {
            threw = true;
        }
        check(threw, "setValue throws UnsupportedOperationException (recheck)");
        check(e.getValue() == 1, "value unchanged after failed setValue");
    }

    // ---- equals contract: type-agnostic across Map.Entry implementations -----

    static void testEqualsContract() {
        AbstractMap.SimpleImmutableEntry<String, Integer> e =
                new AbstractMap.SimpleImmutableEntry<>("k", 1);

        // equals a SimpleEntry with the same key/value (Map.Entry contract)
        AbstractMap.SimpleEntry<String, Integer> se = new AbstractMap.SimpleEntry<>("k", 1);
        check(e.equals(se) && se.equals(e), "equals SimpleEntry, symmetric");

        // equals a live HashMap entry view
        java.util.HashMap<String, Integer> hm = new java.util.HashMap<>();
        hm.put("k", 1);
        Map.Entry<String, Integer> view = hm.entrySet().iterator().next();
        check(e.equals(view) && view.equals(e), "equals HashMap entry view, symmetric");

        // negative branches
        check(!e.equals(null), "equals(null) false");
        check(!e.equals("k=1"), "equals(non-entry) false");
        check(!e.equals(new AbstractMap.SimpleImmutableEntry<>("k", 2)), "different value not equal");
        check(!e.equals(new AbstractMap.SimpleImmutableEntry<>("j", 1)), "different key not equal");

        // null key/value permitted, compare correctly, toString well-formed
        AbstractMap.SimpleImmutableEntry<String, Integer> n1 =
                new AbstractMap.SimpleImmutableEntry<>(null, 1);
        AbstractMap.SimpleImmutableEntry<String, Integer> n2 =
                new AbstractMap.SimpleImmutableEntry<>(null, 1);
        check(n1.equals(n2) && n1.hashCode() == n2.hashCode(), "null-key entries equal");
        check(n1.toString().equals("null=1"), "toString null key");
        check(new AbstractMap.SimpleImmutableEntry<String, Integer>(null, null).toString()
                      .equals("null=null"), "toString null=null");
    }

    // ---- hashCode formula: key.hashCode() ^ value.hashCode() ------------------

    static void testHashCodeFormula() {
        check(new AbstractMap.SimpleImmutableEntry<>("k", 1).hashCode() == 106,
              "hashCode (k,1) == 106");
        check(new AbstractMap.SimpleImmutableEntry<String, Integer>(null, 1).hashCode() == 1,
              "hashCode (null,1) == 1");
        check(new AbstractMap.SimpleImmutableEntry<>("k", null).hashCode() == 107,
              "hashCode (k,null) == 107");
        check(new AbstractMap.SimpleImmutableEntry<String, Integer>(null, null).hashCode() == 0,
              "hashCode (null,null) == 0");
        check(new AbstractMap.SimpleImmutableEntry<>("k", 1).hashCode()
                  == new AbstractMap.SimpleEntry<>("k", 1).hashCode(),
              "hashCode matches SimpleEntry");
    }

    // ---- copy constructor snapshots a mutable source ---------------------------

    static void testCopyConstructorSnapshot() {
        AbstractMap.SimpleEntry<String, Integer> src = new AbstractMap.SimpleEntry<>("x", 9);
        AbstractMap.SimpleImmutableEntry<String, Integer> copy =
                new AbstractMap.SimpleImmutableEntry<>(src);
        src.setValue(100);
        check(copy.getValue() == 9, "copy ctor snapshots value at construction");
        check(!copy.equals(src), "mutating source does not affect immutable copy");
        check(copy.getKey().equals("x"), "copy ctor keeps key");
    }

    // ---- serialization round-trip -----------------------------------------------

    static void testSerialization() throws Exception {
        AbstractMap.SimpleImmutableEntry<String, Integer> e =
                new AbstractMap.SimpleImmutableEntry<>("k", 1);
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos)) {
            oos.writeObject(e);
        }
        Object back;
        try (java.io.ObjectInputStream ois =
                     new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray()))) {
            back = ois.readObject();
        }
        check(back.getClass() == AbstractMap.SimpleImmutableEntry.class,
              "round-trip class SimpleImmutableEntry");
        check(back.equals(e), "round-trip equal");
        boolean threw = false;
        try {
            ((AbstractMap.SimpleImmutableEntry<String, Integer>) back).setValue(2);
        } catch (UnsupportedOperationException expected) {
            threw = true;
        }
        check(threw, "deserialized entry still immutable");
    }

    public static void main(String[] args) throws Exception {
        testSimpleImmutableEntry();
        testImmutability();
        testEqualsContract();
        testHashCodeFormula();
        testCopyConstructorSnapshot();
        testSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
