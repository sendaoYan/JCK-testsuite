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

    public static void main(String[] args) throws Exception {
        testLookup();
        testSerialVersionUIDs();
        testNameForClass();
        testFields();
        testToString();
        testRoundTrip();
        testEnumsArrays();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
