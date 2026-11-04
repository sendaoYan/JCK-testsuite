/*
 * @test
 * @summary java.io.ObjectStreamClass API conformance: full contract coverage.
 *          lookup (null for non-Serializable), stable serialVersionUIDs, field
 *          descriptors (transient/static excluded, primitives-before-objects),
 *          forClass/name, getField, toString, NO_FIELDS, descriptor round-trip.
 * @run main ObjectStreamClassTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.ObjectStreamField;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

public class ObjectStreamClassTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class Rec implements Serializable {
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

    enum Day { MON, TUE }

    // ---- 1. lookup ----
    static void testLookup() {
        check(ObjectStreamClass.lookup(Rec.class) != null, "lookup(Serializable) non-null");
        check(ObjectStreamClass.lookup(Plain.class) == null,
              "lookup(non-Serializable) returns null");
        check(ObjectStreamClass.lookup(String.class) != null, "lookup(String) non-null");
        check(ObjectStreamClass.lookup(Rec.class) == ObjectStreamClass.lookup(Rec.class),
              "lookup is cached (same instance)");
    }

    // ---- 2. stable serialVersionUIDs ----
    static void testSerialVersionUIDs() {
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
    }

    // ---- 3. name / forClass ----
    static void testNameForClass() {
        ObjectStreamClass osc = ObjectStreamClass.lookup(Rec.class);
        check(osc.getName().endsWith("Rec"), "getName");
        check(osc.forClass() == Rec.class, "forClass");
    }

    // ---- 4. field descriptors ----
    static void testFields() {
        ObjectStreamClass osc = ObjectStreamClass.lookup(Rec.class);
        ObjectStreamField[] fs = osc.getFields();
        String[] names = new String[fs.length];
        for (int i = 0; i < fs.length; i++) names[i] = fs[i].getName() + ":" + fs[i].getTypeCode();
        check(Arrays.equals(names, new String[]{"a:I", "c:J", "d:D", "e:Z", "arr:[", "b:L"}),
              "fields sorted primitives-then-objects, transient/static excluded: "
              + Arrays.toString(names));
        check("Ljava/lang/String;".equals(osc.getField("b").getTypeString()),
              "getField(String).typeString");
        check(osc.getField("t") == null, "transient field not in descriptor");
        check(osc.getField("s") == null, "static field not in descriptor");
        check(osc.getField("nope") == null, "unknown field name yields null");
        check(ObjectStreamClass.lookup(String.class).getFields().length == 0
              && ObjectStreamClass.NO_FIELDS.length == 0,
              "String has no serializable fields (empty like NO_FIELDS)");
    }

    // ---- 5. toString ----
    static void testToString() {
        String ts = ObjectStreamClass.lookup(Rec.class).toString();
        check(ts.contains("Rec") && ts.contains("4242"), "toString: " + ts);
    }

    // ---- 6. descriptor round-trip through a real stream ----
    static void testRoundTrip() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(new Rec());
        oos.close();
        ObjectInputStream ois =
                new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        Rec back = (Rec) ois.readObject();
        ois.close();
        check(back.a == 42 && back.b.equals("hi") && back.arr[1] == 43,
              "values round-trip under the declared descriptor");
    }

    // ---- 7. enums and arrays ----
    static void testEnumsArrays() {
        check(ObjectStreamClass.lookup(Day.class).getFields().length == 0,
              "enum descriptor has no fields");
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
        check(ObjectStreamClass.lookup(Day.class).getFields().length == 0,
              "enum descriptor has no fields");
        check(ObjectStreamClass.lookup(Day.class).getSerialVersionUID() == 0L,
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
        check(!java.lang.reflect.Modifier.isFinal(ObjectStreamClass.class.getModifiers()),
              "ObjectStreamClass is NOT final (JDK-8): subclassable override point");
    }

    public static void main(String[] args) throws Exception {
        testLookup();
        testSerialVersionUIDs();
        testNameForClass();
        testFields();
        testToString();
        testRoundTrip();
        testEnumsArrays();
        testLookupAnyNonSerializable();
        testAutoComputedUid();
        testCanonicalFieldOrder();
        testKindsOfDescriptors();
        testToStringExact();
        testModifiers();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
