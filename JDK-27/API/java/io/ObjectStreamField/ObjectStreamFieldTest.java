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


    // ================= deep additions (contract level) =================

    /** serialPersistentFields with an unshared reference field. */
    static class UnsharedShape implements Serializable {
        private static final long serialVersionUID = 9L;   // fixed for cross-version stability
        int skipMe = 5;
        String keep = "kept";
        private static final ObjectStreamField[] serialPersistentFields = {
            new ObjectStreamField("keep", String.class, true),
            new ObjectStreamField("skipMe", int.class),
        };
    }

    // ---- full type-code matrix, one assertion per primitive plus void ----
    static void testTypeCodeMatrix() {
        check(new ObjectStreamField("f", byte.class).getTypeCode() == 'B', "byte -> B");
        check(new ObjectStreamField("f", boolean.class).getTypeCode() == 'Z', "boolean -> Z");
        check(new ObjectStreamField("f", char.class).getTypeCode() == 'C', "char -> C");
        check(new ObjectStreamField("f", short.class).getTypeCode() == 'S', "short -> S");
        check(new ObjectStreamField("f", int.class).getTypeCode() == 'I', "int -> I");
        check(new ObjectStreamField("f", long.class).getTypeCode() == 'J', "long -> J");
        check(new ObjectStreamField("f", float.class).getTypeCode() == 'F', "float -> F");
        check(new ObjectStreamField("f", double.class).getTypeCode() == 'D', "double -> D");
        ObjectStreamField v = new ObjectStreamField("f", void.class);
        check(v.getTypeCode() == 'V', "void -> V (accepted by the descriptor)");
        check(v.isPrimitive(), "void counts as primitive for the descriptor");
        check(v.getTypeString() == null, "void has no type string");
    }

    // ---- typeString matrix: arrays nest, classes use JVM form ----
    static void testTypeStringMatrix() {
        check(new ObjectStreamField("f", byte.class).getTypeString() == null,
              "primitive typeString is null");
        check(new ObjectStreamField("f", double.class).getTypeString() == null,
              "double typeString is null");
        check("Ljava/lang/Object;".equals(new ObjectStreamField("f", Object.class).getTypeString()),
              "Object -> Ljava/lang/Object;");
        check("[I".equals(new ObjectStreamField("f", int[].class).getTypeString()),
              "int[] -> [I");
        check("[[I".equals(new ObjectStreamField("f", int[][].class).getTypeString()),
              "int[][] -> [[I");
        check("[Ljava/lang/String;".equals(new ObjectStreamField("f", String[].class).getTypeString()),
              "String[] -> [Ljava/lang/String;");
        check("[[Ljava/lang/String;".equals(
                  new ObjectStreamField("f", String[][].class).getTypeString()),
              "String[][] -> [[Ljava/lang/String;");
        check("Z".charAt(0) == 'Z' && new ObjectStreamField("f", boolean[].class).getTypeString()
              .equals("[Z"), "boolean[] -> [Z");
    }

    // ---- compareTo: name is the tiebreaker; the type is NOT compared ----
    static void testCompareToMatrix() {
        check(new ObjectStreamField("f", int.class)
                  .compareTo(new ObjectStreamField("f", int.class)) == 0,
              "same name and type compare 0");
        check(new ObjectStreamField("f", int.class)
                  .compareTo(new ObjectStreamField("f", long.class)) == 0,
              "same name, different primitive type still 0 (name dominates)");
        check(new ObjectStreamField("f", String.class)
                  .compareTo(new ObjectStreamField("f", Integer.class)) == 0,
              "same name, different reference type still 0");
        check(new ObjectStreamField("a", String.class)
                  .compareTo(new ObjectStreamField("b", int.class)) > 0,
              "primitive-vs-reference kind dominates name ordering (reference is greater)");
        check(new ObjectStreamField("b", int.class)
                  .compareTo(new ObjectStreamField("a", String.class)) < 0,
              "primitive is smaller than reference regardless of names");
        try {
            new ObjectStreamField("f", int.class).compareTo("not-a-field");
            check(false, "compareTo with a foreign type must throw");
        } catch (ClassCastException expected) {
            passed++;                                // message differs across JDKs: type only
        }
    }

    // ---- equals is identity semantics (not overridden) ----
    static void testEqualsIdentity() {
        ObjectStreamField a = new ObjectStreamField("x", int.class);
        ObjectStreamField b = new ObjectStreamField("x", int.class);
        check(!a.equals(b), "content-equal descriptors are NOT equals (identity semantics)");
        check(a.equals(a), "self-equality");
        check(!a.equals(null), "not equal to null");
        check(!a.equals("x"), "not equal to a foreign type");
        check(a.hashCode() == a.hashCode(), "hashCode stable within an instance");
    }

    // ---- toString covers every kind ----
    static void testToStringKinds() {
        check("Z b".equals(new ObjectStreamField("b", boolean.class).toString()),
              "boolean toString");
        check("J j".equals(new ObjectStreamField("j", long.class).toString()),
              "long toString");
        check("Ljava/lang/Object; o".equals(
                  new ObjectStreamField("o", Object.class).toString()),
              "Object toString");
        check("[Ljava/lang/String; s".equals(
                  new ObjectStreamField("s", String[].class).toString()),
              "String[] toString");
        check("Ljava/lang/String; u".equals(
                  new ObjectStreamField("u", String.class, true).toString()),
              "unshared toString identical to shared form");
    }

    // ---- unshared field through a real descriptor and stream ----
    static void testUnsharedInDescriptor() throws Exception {
        java.io.ObjectStreamClass osc = java.io.ObjectStreamClass.lookup(UnsharedShape.class);
        java.io.ObjectStreamField[] fs = osc.getFields();
        String[] names = new String[fs.length];
        for (int i = 0; i < fs.length; i++)
            names[i] = fs[i].getName() + ":" + fs[i].isUnshared();
        check(java.util.Arrays.equals(names, new String[]{"skipMe:false", "keep:true"}),
              "descriptor exposes the declared unshared flag: "
              + java.util.Arrays.toString(names));
        check(osc.getField("keep").isUnshared(), "getField sees the unshared flag");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(new UnsharedShape());
        oos.close();
        ObjectInputStream ois = new ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()));
        UnsharedShape back = (UnsharedShape) ois.readObject();
        ois.close();
        check("kept".equals(back.keep) && back.skipMe == 5,
              "unshared field still round-trips its value");
        check(back.keep != null, "value present");
    }

    // ---- offsets: standalone 0, descriptor-assigned non-negative and typed ----
    static void testOffsets() {
        check(new ObjectStreamField("f", int.class).getOffset() == 0,
              "standalone (unbound) field has offset 0");
        check(new ObjectStreamField("f", String.class, true).getOffset() == 0,
              "standalone unshared field has offset 0");
        java.io.ObjectStreamClass osc = java.io.ObjectStreamClass.lookup(UnsharedShape.class);
        for (java.io.ObjectStreamField f : osc.getFields())
            check(f.getOffset() >= 0, "bound field offset assigned: " + f.getName());
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testTypeCodeMatrix();
        testTypeStringMatrix();
        testCompareToMatrix();
        testEqualsIdentity();
        testToStringKinds();
        testUnsharedInDescriptor();
        testOffsets();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
