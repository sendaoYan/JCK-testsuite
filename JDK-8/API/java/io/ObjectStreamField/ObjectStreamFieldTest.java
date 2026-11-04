/*
 * @test
 * @summary java.io.ObjectStreamField API conformance: full contract coverage.
 *          Field descriptor for primitives/references/arrays, name/type/typeCode/
 *          typeString accessors, unshared flag, offset/setOffset, isPrimitive,
 *          Comparable ordering, toString, serialPersistentFields round-trip.
 * @run main ObjectStreamFieldTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.io.Serializable;

public class ObjectStreamFieldTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // a class steering serialization with explicit serialPersistentFields
    static class Shaped implements Serializable {
        private static final long serialVersionUID = 8L;
        int keep = 42;
        String keepRef = "kept";
        transient int drop = 7;

        private static final ObjectStreamField[] serialPersistentFields = {
            new ObjectStreamField("keep", int.class),
            new ObjectStreamField("keepRef", String.class),
        };
    }

    // ---- 1. name/type accessors ----
    static void testAccessors() {
        ObjectStreamField i = new ObjectStreamField("x", int.class);
        ObjectStreamField s = new ObjectStreamField("y", String.class);
        ObjectStreamField u = new ObjectStreamField("z", String.class, true);
        ObjectStreamField arr = new ObjectStreamField("w", int[].class);

        check("x".equals(i.getName()) && "y".equals(s.getName()), "getName");
        check(i.getType() == int.class && s.getType() == String.class
              && arr.getType() == int[].class, "getType");
        check("z".equals(u.getName()), "unshared field name");
    }

    // ---- 2. type codes follow JVM signature letters ----
    static void testTypeCodes() {
        ObjectStreamField i = new ObjectStreamField("x", int.class);
        ObjectStreamField dbl = new ObjectStreamField("d", double.class);
        ObjectStreamField s = new ObjectStreamField("y", String.class);
        ObjectStreamField arr = new ObjectStreamField("w", int[].class);
        ObjectStreamField u = new ObjectStreamField("z", String.class, true);

        check(i.getTypeCode() == 'I', "int typecode I");
        check(dbl.getTypeCode() == 'D', "double typecode D");
        check(s.getTypeCode() == 'L', "String typecode L");
        check(arr.getTypeCode() == '[', "array typecode [");
        check(u.getTypeCode() == 'L', "unshared keeps component typecode");
    }

    // ---- 3. typeString ----
    static void testTypeString() {
        ObjectStreamField i = new ObjectStreamField("x", int.class);
        ObjectStreamField s = new ObjectStreamField("y", String.class);
        ObjectStreamField arr = new ObjectStreamField("w", int[].class);

        check(i.getTypeString() == null, "primitive typeString null");
        check("Ljava/lang/String;".equals(s.getTypeString()), "String typeString");
        check("[I".equals(arr.getTypeString()), "int[] typeString");
    }

    // ---- 4. primitive / unshared predicates ----
    static void testPredicates() {
        ObjectStreamField i = new ObjectStreamField("x", int.class);
        ObjectStreamField dbl = new ObjectStreamField("d", double.class);
        ObjectStreamField s = new ObjectStreamField("y", String.class);
        ObjectStreamField arr = new ObjectStreamField("w", int[].class);
        ObjectStreamField u = new ObjectStreamField("z", String.class, true);

        check(i.isPrimitive() && dbl.isPrimitive(), "primitives recognized");
        check(!s.isPrimitive() && !arr.isPrimitive(), "references not primitive");
        check(u.isUnshared(), "unshared flag true");
        check(!s.isUnshared() && !i.isUnshared(), "default is shared");
    }

    // ---- 5. offset management ----
    static void testOffset() {
        ObjectStreamField i = new ObjectStreamField("x", int.class);
        check(i.getOffset() == 0, "initial offset 0");
        ObjectStreamField shifted = new ObjectStreamField("x", int.class) {
            { setOffset(5); }
        };
        check(shifted.getOffset() == 5, "setOffset updates getOffset");
    }

    // ---- 6. Comparable ordering ----
    static void testComparable() {
        ObjectStreamField i = new ObjectStreamField("x", int.class);
        ObjectStreamField s = new ObjectStreamField("y", String.class);
        check(i.compareTo(new ObjectStreamField("x", int.class)) == 0,
              "equal descriptors compare 0");
        check(i.compareTo(s) < 0 && s.compareTo(i) > 0, "primitive < reference");
        check(new ObjectStreamField("a", int.class)
                  .compareTo(new ObjectStreamField("b", int.class)) < 0,
              "name ordering");
        check(new ObjectStreamField("b", String.class)
                  .compareTo(new ObjectStreamField("a", int.class)) > 0,
              "kind dominates name");
    }

    // ---- 7. toString ----
    static void testToString() {
        check("I x".equals(new ObjectStreamField("x", int.class).toString()), "int toString");
        check("Ljava/lang/String; y".equals(new ObjectStreamField("y", String.class).toString()),
              "String toString");
        check("[I w".equals(new ObjectStreamField("w", int[].class).toString()), "array toString");
    }

    // ---- 8. serialPersistentFields round-trip ----
    static void testSerialPersistentFields() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(new Shaped());
        oos.close();
        ObjectInputStream ois =
                new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        Shaped back = (Shaped) ois.readObject();
        ois.close();
        check(back.keep == 42 && "kept".equals(back.keepRef),
              "fields named in serialPersistentFields round-trip");
        check(back.drop == 0, "field outside serialPersistentFields not written");
    }

    public static void main(String[] args) throws Exception {
        testAccessors();
        testTypeCodes();
        testTypeString();
        testPredicates();
        testOffset();
        testComparable();
        testToString();
        testSerialPersistentFields();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
