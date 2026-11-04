/*
 * @test
 * @summary java.io.ObjectStreamField conformance: white-box structure + black-box behavior
 *          (field descriptor construction for primitives/references/arrays, unshared flag,
 *          offset management, Comparable ordering, toString, serialPersistentFields use).
 * @run main ObjectStreamFieldTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.io.Serializable;
import java.io.Serial;
import java.lang.reflect.Modifier;

public class ObjectStreamFieldTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // a class steering serialization with explicit serialPersistentFields
    static class Shaped implements Serializable {
        @Serial
        private static final long serialVersionUID = 8L;
        int keep = 42;
        String keepRef = "kept";
        transient int drop = 7;

        @Serial
        private static final ObjectStreamField[] serialPersistentFields = {
            new ObjectStreamField("keep", int.class),
            new ObjectStreamField("keepRef", String.class),
        };
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.io.ObjectStreamField");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(c.getSuperclass() == Object.class, "extends Object");
        check(java.lang.Comparable.class.isAssignableFrom(c), "implements Comparable");
        check(c.getConstructor(String.class, Class.class) != null, "ctor (String, Class)");
        check(c.getConstructor(String.class, Class.class, boolean.class) != null,
              "ctor (String, Class, boolean)");
        for (String m : new String[]{"getName", "getType", "getTypeCode", "getTypeString",
                                     "getOffset", "setOffset", "isPrimitive", "isUnshared",
                                     "compareTo", "toString"})
            check(hasName(c, m), "has method " + m);
    }

    static boolean hasName(Class<?> c, String m) {
        for (java.lang.reflect.Method mm : c.getMethods()) if (mm.getName().equals(m)) return true;
        for (java.lang.reflect.Method mm : c.getDeclaredMethods()) if (mm.getName().equals(m)) return true;
        return false;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        ObjectStreamField i = new ObjectStreamField("x", int.class);
        ObjectStreamField s = new ObjectStreamField("y", String.class);
        ObjectStreamField u = new ObjectStreamField("z", String.class, true);
        ObjectStreamField arr = new ObjectStreamField("w", int[].class);
        ObjectStreamField dbl = new ObjectStreamField("d", double.class);

        // 1. name/type accessors
        check("x".equals(i.getName()) && "y".equals(s.getName()), "getName");
        check(i.getType() == int.class && s.getType() == String.class
              && arr.getType() == int[].class, "getType");
        check(u.getName().equals("z"), "unshared field name");

        // 2. type codes follow the JVM signature letters
        check(i.getTypeCode() == 'I', "int typecode I");
        check(dbl.getTypeCode() == 'D', "double typecode D");
        check(s.getTypeCode() == 'L', "String typecode L");
        check(arr.getTypeCode() == '[', "array typecode [");
        check(u.getTypeCode() == 'L', "unshared keeps component typecode");

        // 3. typeString: null for primitives, JVM signature for references
        check(i.getTypeString() == null, "primitive typeString null");
        check("Ljava/lang/String;".equals(s.getTypeString()), "String typeString");
        check("[I".equals(arr.getTypeString()), "int[] typeString");

        // 4. primitive / unshared predicates
        check(i.isPrimitive() && dbl.isPrimitive(), "primitives recognized");
        check(!s.isPrimitive() && !arr.isPrimitive(), "references not primitive");
        check(u.isUnshared(), "unshared flag true");
        check(!s.isUnshared() && !i.isUnshared(), "default is shared");

        // 5. offset starts at 0; setOffset is protected but a subclass (or this package
        //    convention) can set it -- use an anonymous subclass to exercise the contract
        check(i.getOffset() == 0, "initial offset 0");
        ObjectStreamField shifted = new ObjectStreamField("x", int.class) {
            { setOffset(5); }
        };
        check(shifted.getOffset() == 5, "setOffset updates getOffset");

        // 6. Comparable: same name+kind equal; primitives order before references;
        //    otherwise by field name
        check(i.compareTo(new ObjectStreamField("x", int.class)) == 0,
              "equal descriptors compare 0");
        check(i.compareTo(s) < 0 && s.compareTo(i) > 0, "primitive < reference");
        check(new ObjectStreamField("a", int.class)
                  .compareTo(new ObjectStreamField("b", int.class)) < 0,
              "name ordering");
        check(new ObjectStreamField("b", String.class)
                  .compareTo(new ObjectStreamField("a", int.class)) > 0,
              "kind dominates name");

        // 7. toString is "<signature> <name>"
        check("I x".equals(i.toString()), "int toString");
        check("Ljava/lang/String; y".equals(s.toString()), "String toString");
        check("[I w".equals(arr.toString()), "array toString");

        // 8. real serialization driven by serialPersistentFields built from this type:
        //    only the declared fields land in the stream
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(new Shaped());
        }
        try (ObjectInputStream ois =
                 new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            Shaped back = (Shaped) ois.readObject();
            check(back.keep == 42 && "kept".equals(back.keepRef),
                  "fields named in serialPersistentFields round-trip");
            check(back.drop == 0, "field outside serialPersistentFields not written");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
