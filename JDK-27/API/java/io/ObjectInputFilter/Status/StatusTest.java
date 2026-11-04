/*
 * @test
 * @summary java.io.ObjectInputFilter.Status conformance: white-box structure
 *          (final enum with the three constants) + black-box behavior (values
 *          order, ordinals, compareTo, name, valueOf case sensitivity, and the
 *          real filtering semantics: REJECTED aborts deserialization with
 *          InvalidClassException, ALLOWED and UNDECIDED let it proceed).
 * @run main StatusTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputFilter.Status;
import java.io.Serializable;
import java.lang.reflect.Modifier;

public class StatusTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class Payload implements Serializable {
        private static final long serialVersionUID = 1L;
    }

    static byte[] stream() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(new Payload());
        }
        return bos.toByteArray();
    }

    static Object readWith(Status s, byte[] bytes) throws Exception {
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes));
        ois.setObjectInputFilter(info -> s);
        return ois.readObject();
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.io.ObjectInputFilter$Status");
        check(c.isEnum(), "is an enum");
        check(Modifier.isFinal(c.getModifiers()), "final enum");
        check(c.getSuperclass() == java.lang.Enum.class, "extends Enum");
        check(c == Status.class, "class identity");
        check(c.getEnumConstants().length == 3, "three constants");
        check(java.util.EnumSet.allOf(Status.class).size() == 3, "EnumSet sees all three");
        check(c.getMethod("values").getReturnType() == Status[].class, "values() type");
        check(c.getMethod("valueOf", String.class).getReturnType() == Status.class,
              "valueOf(String) type");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // 1. declaration order and enum plumbing
        Status[] vals = Status.values();
        check(vals.length == 3, "three values");
        check(vals[0] == Status.UNDECIDED && vals[1] == Status.ALLOWED
                  && vals[2] == Status.REJECTED, "declaration order UNDECIDED, ALLOWED, REJECTED");
        check(Status.UNDECIDED.ordinal() == 0, "UNDECIDED ordinal 0");
        check(Status.ALLOWED.ordinal() == 1, "ALLOWED ordinal 1");
        check(Status.REJECTED.ordinal() == 2, "REJECTED ordinal 2");
        check("UNDECIDED".equals(Status.UNDECIDED.name()), "name UNDECIDED");
        check(Status.ALLOWED.compareTo(Status.UNDECIDED) > 0
                  && Status.ALLOWED.compareTo(Status.REJECTED) < 0, "compareTo ordering");
        check(Status.values() != Status.values(), "values() returns a fresh array each call");

        // 2. valueOf exact and case-sensitive
        check(Status.valueOf("REJECTED") == Status.REJECTED, "valueOf exact");
        try {
            Status.valueOf("rejected");
            check(false, "valueOf must be case sensitive");
        } catch (IllegalArgumentException iae) {
            passed++;
        }
        try {
            Status.valueOf("NOPE");
            check(false, "unknown name must throw");
        } catch (IllegalArgumentException iae) {
            passed++;
        }

        // 3. REJECTED aborts deserialization of a custom class
        byte[] bytes = stream();
        try {
            readWith(Status.REJECTED, bytes);
            check(false, "REJECTED must abort readObject");
        } catch (InvalidClassException ice) {
            check(ice.getMessage() != null
                      && ice.getMessage().endsWith("filter status: REJECTED"),
                  "message: " + ice.getMessage());
        }

        // 4. ALLOWED lets the object through even for classes the default
        //    filters would scrutinize
        check(readWith(Status.ALLOWED, bytes) instanceof Payload, "ALLOWED passes");

        // 5. UNDECIDED leaves the decision to later filters / defaults: a lone
        //    filter returning UNDECIDED still deserializes ordinary classes
        check(readWith(Status.UNDECIDED, bytes) instanceof Payload, "UNDECIDED falls through");

        // 6. toString is the constant name
        check("ALLOWED".equals(Status.ALLOWED.toString()), "toString");

        // 7. serializable round-trip of the enum constant keeps identity
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos)) {
            oos.writeObject(Status.REJECTED);
        }
        try (java.io.ObjectInputStream ois = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()))) {
            check(ois.readObject() == Status.REJECTED, "enum identity across serialization");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
