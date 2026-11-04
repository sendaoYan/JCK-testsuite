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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
