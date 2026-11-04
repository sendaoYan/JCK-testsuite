/*
 * @test
 * @summary java.text.Format.Field conformance: base class equality/hashCode/toString via
 *          concrete subclasses.
 * @run main FieldTest
 */

import java.text.DateFormat;
import java.text.Format;
import java.text.NumberFormat;

public class FieldTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testField() {
        Format.Field era = DateFormat.Field.ERA;
        Format.Field integer = NumberFormat.Field.INTEGER;
        check(era != null, "ERA instance");
        check(integer != null, "INTEGER instance");
        check(era.equals(DateFormat.Field.ERA), "equals same field");
        check(!era.equals(integer), "distinct fields not equal");
        check(era.hashCode() == DateFormat.Field.ERA.hashCode(), "hashCode");
        check(era.toString() != null, "toString");
    }


    // ---- deepening helper (contract-level additions) ----
    interface TestBlock { void go() throws Exception; }

    static void expectX(Class<? extends Throwable> expected, TestBlock r, String m) {
        try {
            r.go();
        } catch (Throwable x) {
            if (expected.isInstance(x)) { passed++; return; }
            throw new AssertionError(m + ": expected " + expected.getSimpleName()
                    + " but got " + x.getClass().getName(), x);
        }
        throw new AssertionError(m + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }


    // ================= contract deepening =================

    static void testContractDeepening() {
        // getFields() resolves inherited members only, so subclass constants must be
        // enumerated on the subclasses themselves; every constant is a distinct singleton
        java.util.Set<Object> seen = new java.util.HashSet<>();
        int count = 0;
        for (Class<?> sub : new Class<?>[] {java.text.DateFormat.Field.class,
                java.text.NumberFormat.Field.class, java.text.MessageFormat.Field.class}) {
            check(java.text.Format.Field.class.isAssignableFrom(sub),
                  sub.getName() + " extends Format.Field");
            for (java.lang.reflect.Field f : sub.getFields()) {
                if (f.getType() == sub) {
                    Object v;
                    try { v = f.get(null); } catch (IllegalAccessException e) { throw new RuntimeException(e); }
                    check(v.equals(v) && v.hashCode() == v.hashCode(), sub.getSimpleName() + "." + f.getName() + " reflexive");
                    check(seen.add(v), sub.getSimpleName() + "." + f.getName() + " distinct identity");
                    count++;
                }
            }
        }
        check(count >= 6, "at least 6 constants across the three subclasses (" + count + ")");
        check(java.text.Format.Field.class.getDeclaredFields().length > 0,
              "base class declares its own (serialVersionUID etc.) members");
        check(java.text.AttributedCharacterIterator.Attribute.class
                      .isAssignableFrom(java.text.Format.Field.class),
              "Format.Field extends AttributedCharacterIterator.Attribute");
        check(!java.text.Format.Field.class.equals(java.text.DateFormat.Field.class)
              && !java.text.Format.Field.class.equals(java.text.NumberFormat.Field.class),
              "Format.Field is the shared abstract-ish base, distinct from concrete subclasses");
        check(java.text.Format.Field.class.getSuperclass()
                      .equals(java.text.AttributedCharacterIterator.Attribute.class),
              "direct superclass is Attribute");
    }
    public static void main(String[] args) {
        testContractDeepening();
        testField();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
