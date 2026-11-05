/*
 * @test
 * @summary java.lang.Record conformance: white-box structure + black-box behavior.
 * @run main RecordTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class RecordTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    record Point(int x, int y) {}

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.Record");
        check(Modifier.isAbstract(c.getModifiers()), "Record is abstract");
        check(!c.isInterface(), "Record is not an interface");
        check(c.getSuperclass() == Object.class, "Record extends Object");
        check(Modifier.isProtected(c.getDeclaredConstructor().getModifiers()), "protected ctor");
        boolean hasEq = false, hasHash = false, hasStr = false;
        for (Method m : c.getDeclaredMethods()) {
            if (Modifier.isAbstract(m.getModifiers())) {
                if (m.getName().equals("equals")) hasEq = true;
                if (m.getName().equals("hashCode")) hasHash = true;
                if (m.getName().equals("toString")) hasStr = true;
            }
        }
        check(hasEq && hasHash && hasStr, "abstract equals/hashCode/toString");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testRecordBehavior() {
        Point p = new Point(1, 2);
        check(p instanceof Record, "record instance instanceof Record");
        check(p.x() == 1 && p.y() == 2, "record accessors");
        check(p.equals(new Point(1, 2)), "record value equality");
        check(!p.equals(new Point(1, 3)), "record inequality");
        check(p.hashCode() == new Point(1, 2).hashCode(), "record hashCode");
        check("Point[x=1, y=2]".equals(p.toString()), "record toString");
        check(p.getClass().isRecord(), "isRecord true");
        check(Point.class.getRecordComponents().length == 2, "record components");
    }


    static void testBlackBox() throws Exception {
        testRecordBehavior();
    }

    // ==== Contract deepening (lcN1, 2026-11) ====
    record Point2(int x, String y) {}
    record Box(String s) {}
    record Clamped(int v) { public Clamped { v = Math.max(v, 0); } }
    record Pair<A, B>(A a, B b) {}
    record Empty() {}
    record ArrayRec(int[] a) {}
    record SerPoint(int x, String y) implements java.io.Serializable {}
    record SerBox(String s) implements java.io.Serializable {}
    record Outer2(int x) { record Inner(String z) {} }
    record WithStatic(int i) { static final int CONST = 42; static int f() { return CONST * 2; } }

    static void testPointFullContract() {
        Point2 p = new Point2(1, "a");
        check(p.x() == 1, "int accessor exact");
        check("a".equals(p.y()), "String accessor exact");
        check("Point2[x=1, y=a]".equals(p.toString()), "toString exact form");
        check(p.equals(new Point2(1, "a")), "value equality across instances");
        check(!p.equals(new Point2(2, "a")), "inequality on first component");
        check(!p.equals(new Point2(1, "b")), "inequality on second component");
        check(p.hashCode() == new Point2(1, "a").hashCode(), "hashCode consistent with equals");
        check(p.getClass().isRecord(), "isRecord");
        check(java.lang.reflect.Modifier.isFinal(p.getClass().getModifiers()),
              "record class is final");
        check(p instanceof Record, "instanceof Record");
        check(p instanceof Object, "instanceof Object");
        check(p.getClass().getSuperclass() == Record.class, "superclass is java.lang.Record");
        check(Record.class.getSuperclass() == Object.class, "Record itself extends Object");
        check(!p.getClass().isInterface(), "record class is not an interface");
    }

    static void testNullComponent() {
        Box b = new Box(null);
        check(b.s() == null, "null component permitted, accessor returns null");
        check("Box[s=null]".equals(b.toString()), "null renders as literal null in toString");
        check(b.equals(new Box(null)), "null equals null component-wise");
        check(!b.equals(new Box("x")), "null != \"x\" in equals");
        check(b.hashCode() == new Box(null).hashCode(), "hashCode handles null component");
        check(b.equals(b), "equals self with null component");
    }

    static void testCompactConstructor() {
        Clamped c1 = new Clamped(-5);
        check(c1.v() == 0, "compact constructor clamps negative to 0");
        Clamped c2 = new Clamped(5);
        check(c2.v() == 5, "compact constructor leaves valid value untouched");
        Clamped c3 = new Clamped(0);
        check(c3.v() == 0, "boundary zero stays zero");
        check("Clamped[v=0]".equals(c1.toString()), "toString reflects compacted value");
        check(c1.equals(new Clamped(-100)), "two different negative inputs collapse to equal records");
        check(!c1.equals(new Clamped(7)), "positive input distinct");
        try {
            new Clamped(-1);
        } catch (Throwable t) {
            throw new AssertionError("compact constructor must not throw", t);
        }
    }

    static void testStaticAndNestedMembers() {
        check(WithStatic.CONST == 42, "static final field on record");
        check(WithStatic.f() == 84, "static method on record");
        WithStatic w = new WithStatic(3);
        check(w.i() == 3 && WithStatic.f() == 84, "instance and static sides coexist");
        check(WithStatic.class.isRecord(), "record with static members still a record");
        Outer2.Inner in = new Outer2.Inner("z");
        check(Outer2.Inner.class.isRecord(), "nested record isRecord");
        check("z".equals(in.z()), "nested record accessor");
        check("Inner[z=z]".equals(in.toString()), "nested record toString uses simple name");
        check(Outer2.Inner.class.getSuperclass() == Record.class, "nested record superclass");
        check(Outer2.Inner.class.getEnclosingClass() == Outer2.class, "enclosing class link");
        check(new Outer2(1).x() == 1, "outer record still works");
    }

    static void testEmptyAndMultiComponent() {
        Empty e = new Empty();
        check("Empty[]".equals(e.toString()), "empty record toString");
        check(e.equals(new Empty()), "empty records all equal");
        check(e.hashCode() == new Empty().hashCode(), "empty record hashCode constant");
        check(Empty.class.getRecordComponents().length == 0, "zero record components");
        Pair<Integer, String> p = new Pair<>(1, "x");
        check("Pair[a=1, b=x]".equals(p.toString()), "generic multi-component toString");
        check(p.a() == 1 && "x".equals(p.b()), "generic accessors");
        check(p.equals(new Pair<Integer, String>(1, "x")), "generic equality");
        check(!p.equals(new Pair<String, Integer>("x", 1)), "component order matters");
        Pair<String, String> q = new Pair<>("a", "b");
        check(!p.equals(q), "cross-instantiation inequality (different types, same shapes)");
    }

    static void testArrayComponentReferenceSemantics() {
        // GOTCHA (measured): record array components use Object.equals reference
        // semantics - int[] does not override equals, so equal content in two
        // distinct arrays does NOT make the records equal.
        ArrayRec r1 = new ArrayRec(new int[]{1, 2});
        ArrayRec r2 = new ArrayRec(new int[]{1, 2});
        check(!r1.equals(r2), "equal contents in distinct arrays are NOT equal (reference semantics)");
        int[] shared = {1, 2};
        ArrayRec r3 = new ArrayRec(shared);
        check(r3.equals(new ArrayRec(shared)), "same array reference makes records equal");
        check(r3.hashCode() == new ArrayRec(shared).hashCode(), "same reference same hashCode");
        check(r3.hashCode() != r1.hashCode(), "distinct arrays yield distinct hashCodes");
        check(java.util.Arrays.equals(r3.a(), new int[]{1, 2}), "array accessor exact");
        check(r3.a() == shared, "accessor returns the very array instance (no copy)");
        check(r3.toString().startsWith("ArrayRec[a=[I@"),
              "array component prints as [I@... in toString: " + r3);
    }

    static void testSerializationRoundTrip() throws Exception {
        // Records serialize to their component values; round-trip yields a NEW
        // instance that is equals() (in contrast to enum's == singleton).
        SerPoint p = new SerPoint(7, "seven");
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(p);
        oos.close();
        java.io.ObjectInputStream oin =
            new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()));
        SerPoint r = (SerPoint) oin.readObject();
        oin.close();
        check(r instanceof SerPoint, "round-trip class");
        check(r != p, "record round-trip creates a new instance (not a singleton)");
        check(r.equals(p), "round-trip equals original");
        check(r.hashCode() == p.hashCode(), "round-trip hashCode equal");
        check(r.toString().equals(p.toString()), "round-trip toString equal");
        check(r.x() == 7 && "seven".equals(r.y()), "round-trip components");
        SerBox nb = new SerBox(null);
        java.io.ByteArrayOutputStream bos2 = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos2 = new java.io.ObjectOutputStream(bos2);
        oos2.writeObject(nb);
        oos2.close();
        SerBox rb = (SerBox) new java.io.ObjectInputStream(
            new java.io.ByteArrayInputStream(bos2.toByteArray())).readObject();
        check(rb.equals(nb) && rb.s() == null, "null component survives serialization");
        try {
            new java.io.ObjectOutputStream(new java.io.ByteArrayOutputStream())
                .writeObject(new Point2(1, "a"));
            throw new AssertionError("non-Serializable record should be rejected");
        } catch (java.io.NotSerializableException expected) {
            passed++;
        }
    }

    static int destructure(Object o) {
        if (o instanceof Point2(int x, String y)) return x + y.length();
        return -1;
    }

    static void testRecordPatternMatching() {
        check(destructure(new Point2(3, "ab")) == 5, "record pattern deconstruction");
        check(destructure(new Point2(0, "")) == 0, "pattern with empty string component");
        check(destructure("not a record") == -1, "non-matching object falls through");
        Object o = new Empty();
        if (o instanceof Empty e) {
            check(e.equals(new Empty()), "type pattern for empty record");
        } else {
            throw new AssertionError("empty record pattern should match");
        }
        // nested record pattern
        Object outer = new Outer2(9);
        if (outer instanceof Outer2(int x)) {
            check(x == 9, "primitive pattern binding");
        } else {
            throw new AssertionError("outer record pattern should match");
        }
    }

    static void testLocalRecord() {
        record LocalRec(int n) {}
        LocalRec l = new LocalRec(4);
        check(l.n() == 4, "locally declared record accessor");
        check("LocalRec[n=4]".equals(l.toString()), "local record toString");
        check(LocalRec.class.isRecord(), "local record isRecord");
        check(l.equals(new LocalRec(4)), "local record equality");
    }

    static void testReflectionLight() throws Exception {
        // light-touch: the Class-batch test suite covers RecordComponent deeply
        java.lang.reflect.RecordComponent[] rcs = Point2.class.getRecordComponents();
        check(rcs.length == 2, "component count");
        check("x".equals(rcs[0].getName()) && "y".equals(rcs[1].getName()),
              "component names in declaration order");
        check(rcs[0].getType() == int.class, "component type int");
        check(rcs[1].getType() == String.class, "component type String");
        check(java.lang.reflect.Modifier.isFinal(
                  Point2.class.getDeclaredField("x").getModifiers()),
              "record field is final");
        check(Point2.class.getDeclaredMethod("x").getReturnType() == int.class,
              "accessor method shape");
        check(!Point2.class.isEnum(), "record is not an enum");
        check(String.class.isRecord() == false, "String is not a record");
        check(java.lang.Enum.class.isRecord() == false, "Enum is not a record");
    }

    static void testRecordClassLevel() {
        check(java.lang.reflect.Modifier.isAbstract(Record.class.getModifiers()),
              "Record is abstract");
        check(java.lang.reflect.Modifier.isPublic(Record.class.getModifiers()),
              "Record is public");
        check(!java.lang.reflect.Modifier.isFinal(Record.class.getModifiers()),
              "Record itself is not final");
        check(!Record.class.isSealed(), "Record is not sealed");
        check(Record.class.getPermittedSubclasses() == null, "no permitted subclasses list");
        check(Record.class.isRecord() == false, "Record itself is not a record");
        check(Record.class.getDeclaredMethods().length >= 3,
              "declares equals/hashCode/toString (and more)");
        check(Record.class.getName().equals("java.lang.Record"), "binary name");
    }

public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testPointFullContract();
        testNullComponent();
        testCompactConstructor();
        testStaticAndNestedMembers();
        testEmptyAndMultiComponent();
        testArrayComponentReferenceSemantics();
        testSerializationRoundTrip();
        testRecordPatternMatching();
        testLocalRecord();
        testReflectionLight();
        testRecordClassLevel();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
