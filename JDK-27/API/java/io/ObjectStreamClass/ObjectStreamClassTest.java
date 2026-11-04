/*
 * @test
 * @summary java.io.ObjectStreamClass conformance: white-box structure + black-box behavior
 *          (lookup vs lookupAny, stable serialVersionUIDs, field descriptors of a
 *          serializable class, toString, NO_FIELDS).
 * @run main ObjectStreamClassTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.ObjectStreamField;
import java.io.Serializable;
import java.io.Serial;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;

public class ObjectStreamClassTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class Rec implements Serializable {
        @Serial
        private static final long serialVersionUID = 4242L;
        int a;
        long c;
        double d;
        boolean e;
        transient int t;
        static int s;
        int[] arr;
        String b;

        Rec() { a = 42; c = 1L; d = 2.5; e = true; arr = new int[]{42, 43}; b = "hi"; }
    }

    static class Plain { int x; }   // NOT Serializable

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.io.ObjectStreamClass");
        check(Modifier.isFinal(c.getModifiers()), "class is final");
        check(c.getSuperclass() == Object.class, "extends Object");
        check(Serializable.class.isAssignableFrom(c), "implements Serializable");
        check(c.getConstructors().length == 0, "no public constructors (descriptor objects)");
        check(c.getMethod("lookup", Class.class).getReturnType() == ObjectStreamClass.class,
              "static lookup(Class)");
        check(c.getMethod("lookupAny", Class.class).getReturnType() == ObjectStreamClass.class,
              "static lookupAny(Class)");
        check(c.getField("NO_FIELDS").getType() == ObjectStreamField[].class, "NO_FIELDS constant");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // 1. lookup: null for non-Serializable, descriptor for Serializable
        check(ObjectStreamClass.lookup(Rec.class) != null, "lookup(Serializable) non-null");
        check(ObjectStreamClass.lookup(Plain.class) == null,
              "lookup(non-Serializable) returns null");
        check(ObjectStreamClass.lookup(String.class) != null, "lookup(String) non-null");

        // 2. lookupAny: never null, also describes non-Serializable types
        ObjectStreamClass anyPlain = ObjectStreamClass.lookupAny(Plain.class);
        check(anyPlain != null, "lookupAny(non-Serializable) non-null");
        check("ObjectStreamClassTest$Plain".equals(anyPlain.getName()),
              "lookupAny descriptor name");
        check(anyPlain.getFields().length == 0, "lookupAny on non-serializable has no fields");
        check(ObjectStreamClass.lookupAny(Thread.class).getSerialVersionUID() == 0L,
              "lookupAny non-serializable SUID is 0");
        check(ObjectStreamClass.lookupAny(ObjectStreamClass.lookup(Rec.class).forClass())
              .getName().endsWith("Rec"), "lookupAny of forClass() round name");

        // 3. identity: lookup caches and returns the same descriptor per class
        check(ObjectStreamClass.lookup(Rec.class) == ObjectStreamClass.lookup(Rec.class),
              "lookup is cached (same instance)");

        // 4. stable serialVersionUIDs of JDK classes (fixed by the serialization spec)
        check(ObjectStreamClass.lookup(String.class).getSerialVersionUID()
              == -6849794470754667710L, "String serialVersionUID");
        check(ObjectStreamClass.lookup(Long.class).getSerialVersionUID()
              == 4290774380558885855L, "Long serialVersionUID");
        check(ObjectStreamClass.lookup(Integer.class).getSerialVersionUID()
              == 1360826667806852920L, "Integer serialVersionUID");
        check(ObjectStreamClass.lookup(ArrayList.class).getSerialVersionUID()
              == 8683452581122892189L, "ArrayList serialVersionUID");
        check(ObjectStreamClass.lookup(Rec.class).getSerialVersionUID() == 4242L,
              "declared serialVersionUID of user class");

        // 5. names and forClass()
        ObjectStreamClass osc = ObjectStreamClass.lookup(Rec.class);
        check(osc.getName().endsWith("Rec"), "getName");
        check(osc.forClass() == Rec.class, "forClass");

        // 6. field descriptors: transient/static excluded, primitives before objects,
        //    each in declaration order; toString form "<signature> <name>"
        ObjectStreamField[] fs = osc.getFields();
        String[] names = new String[fs.length];
        for (int i = 0; i < fs.length; i++) names[i] = fs[i].getName() + ":" + fs[i].getTypeCode();
        check(Arrays.equals(names, new String[]{"a:I", "c:J", "d:D", "e:Z", "arr:[", "b:L"}),
              "fields sorted primitives-then-objects, transient/static excluded: "
              + Arrays.toString(names));
        check(osc.getField("b").getTypeString().equals("Ljava/lang/String;"),
              "getField(String).typeString");
        check(osc.getField("t") == null, "transient field not in descriptor");
        check(osc.getField("s") == null, "static field not in descriptor");
        check(osc.getField("nope") == null, "unknown field name yields null");
        check(ObjectStreamClass.lookup(String.class).getFields().length == 0
              && ObjectStreamClass.NO_FIELDS.length == 0,
              "String has no serializable fields (empty like NO_FIELDS)");

        // 7. toString of the descriptor mentions class name and SUID
        String ts = osc.toString();
        check(ts.contains("Rec") && ts.contains("4242"), "toString: " + ts);

        // 8. the descriptor matches what a real stream carries: serialize Rec, then
        //    look the descriptor back up through the stream (class descriptor round-trip)
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(new Rec());
        }
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            Rec back = (Rec) ois.readObject();
            check(back.a == 42 && back.b.equals("hi") && back.arr[1] == 43,
                  "values round-trip under the declared descriptor");
        }

        // 9. enums and arrays get descriptors too
        check(ObjectStreamClass.lookup(java.time.DayOfWeek.class).getFields().length == 0,
              "enum descriptor has no fields");
        check(ObjectStreamClass.lookupAny(int[].class).getName().equals("[I"),
              "array descriptor name is the JVM type name");
    }


    // ================= deep additions (contract level) =================

    /** Deliberately cross-ordered fields to pin the canonical descriptor order. */
    static class Orderly implements Serializable {
        private static final long serialVersionUID = 4243L;   // fixed for cross-version stability
        int zz; byte aa; String mm; Object bb; double cc; long dd;
        transient int tt; static int ss;
    }

    /** Two structurally identical classes and one structurally different. */
    static class Auto1 implements Serializable { int a; String b; }
    static class Auto2 implements Serializable { int a; String b; }
    static class Auto3 implements Serializable { int a; long q; String b; }

    static class Plain2 { int x; }

    /** Only transient/static members: descriptor must have no fields. */
    static class OnlyHidden implements Serializable {
        private static final long serialVersionUID = 4244L;
        transient int t = 1;
        static int s = 2;
    }

    // ---- lookupAny on a non-Serializable class ----
    static void testLookupAnyNonSerializable() {
        ObjectStreamClass any = ObjectStreamClass.lookupAny(Plain2.class);
        check(any != null, "lookupAny(non-Serializable) is non-null");
        check(any.getSerialVersionUID() == 0L, "no serialVersionUID: suid is 0");
        check(any.getFields().length == 0, "no serializable fields reported");
        check(any.forClass() == Plain2.class, "forClass round-trips the input class");
        check(any.getName().equals(Plain2.class.getName()), "getName is the class name");
        check(ObjectStreamClass.lookup(Plain2.class) == null,
              "lookup(non-Serializable) stays null");
    }

    // ---- computed serialVersionUID: stable per class, distinct across classes ----
    static void testAutoComputedUid() {
        long a1 = ObjectStreamClass.lookup(Auto1.class).getSerialVersionUID();
        long a1b = ObjectStreamClass.lookup(Auto1.class).getSerialVersionUID();
        long a2 = ObjectStreamClass.lookup(Auto2.class).getSerialVersionUID();
        long a3 = ObjectStreamClass.lookup(Auto3.class).getSerialVersionUID();
        check(a1 == a1b, "repeated lookup yields the identical computed UID");
        check(a1 != a2, "identical structure in a different class computes a different UID");
        check(a1 != a3, "changed structure computes a different UID");
        check(a3 != a2, "all three classes are pairwise distinct");
    }

    // ---- canonical field order: primitives by name first, then references by name ----
    static void testCanonicalFieldOrder() {
        ObjectStreamField[] fs = ObjectStreamClass.lookup(Orderly.class).getFields();
        String[] names = new String[fs.length];
        for (int i = 0; i < fs.length; i++)
            names[i] = fs[i].getName() + ":" + fs[i].getTypeCode();
        check(java.util.Arrays.equals(names,
              new String[]{"aa:B", "cc:D", "dd:J", "zz:I", "bb:L", "mm:L"}),
              "primitives sorted by name precede references sorted by name: "
              + java.util.Arrays.toString(names));
        boolean transientExcluded = true, staticExcluded = true;
        for (ObjectStreamField f : fs) {
            if (f.getName().equals("tt")) transientExcluded = false;
            if (f.getName().equals("ss")) staticExcluded = false;
        }
        check(transientExcluded, "transient field excluded from the descriptor");
        check(staticExcluded, "static field excluded from the descriptor");
        int lastPrimOffset = -1;
        for (ObjectStreamField f : fs) {
            check(f.getOffset() >= 0, "offset assigned: " + f.getName() + "=" + f.getOffset());
            if (f.isPrimitive()) {
                check(f.getOffset() > lastPrimOffset,
                      "primitive offsets ascend in declaration order: " + f.getName());
                lastPrimOffset = f.getOffset();
            }
        }
    }

    // ---- interface, enum, array and Externalizable descriptors ----
    static void testKindsOfDescriptors() {
        check(ObjectStreamClass.lookup(Serializable.class) != null,
              "the Serializable interface itself has a descriptor");
        check(ObjectStreamClass.lookup(Serializable.class).getSerialVersionUID()
              == 1196656838076753133L, "Serializable interface UID (stable value)");
        check(ObjectStreamClass.lookup(java.io.Externalizable.class).getSerialVersionUID()
              == -282491828744381764L, "Externalizable interface UID (stable value)");
        check(ObjectStreamClass.lookup(int[].class) != null,
              "array classes are serializable: lookup(int[]) non-null");
        check(ObjectStreamClass.lookupAny(int[].class).getName().equals("[I"),
              "array descriptor name is the JVM type name");
        check(ObjectStreamClass.lookup(java.time.DayOfWeek.class).getFields().length == 0,
              "enum descriptor has no fields");
        check(ObjectStreamClass.lookup(java.time.DayOfWeek.class).getSerialVersionUID() == 0L,
              "enum descriptor reports UID 0");
        check(ObjectStreamClass.lookup(OnlyHidden.class).getFields().length == 0,
              "transient/static-only class has an empty descriptor");
    }

    // ---- toString format ----
    static void testToStringExact() {
        String ts = ObjectStreamClass.lookup(Orderly.class).toString();
        check(ts.contains(Orderly.class.getName()), "toString carries the class name");
        check(ts.contains(": static final long serialVersionUID = 4243L;"),
              "toString carries the exact UID phrase: " + ts);
        check(ts.indexOf("4243L") > 0, "UID present after the name");
    }

    // ---- class modifiers (version-differentiated) ----
    static void testModifiers() {
        check(java.lang.reflect.Modifier.isFinal(ObjectStreamClass.class.getModifiers()),
              "ObjectStreamClass IS final (JDK-9+, module sealing): not subclassable");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testLookupAnyNonSerializable();
        testAutoComputedUid();
        testCanonicalFieldOrder();
        testKindsOfDescriptors();
        testToStringExact();
        testModifiers();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
