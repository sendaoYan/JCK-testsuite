/*
 * @test
 * @summary java.io.Serializable conformance (pure black-box): marker semantics,
 *          serialization eligibility of JDK classes and arrays, serialVersionUID
 *          round-trip, interface inheritance to Externalizable.
 * @run main SerializableTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class SerializableTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static byte[] ser(Object o) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(o);
        }
        return bos.toByteArray();
    }

    static Object de(byte[] b) throws Exception {
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(b))) {
            return ois.readObject();
        }
    }

    static void testBlackBox() throws Exception {
        // eligibility: which JDK reference types are serializable
        check(Serializable.class.isAssignableFrom(String.class), "String is Serializable");
        check(Serializable.class.isAssignableFrom(Integer.class), "Integer is Serializable");
        check(Serializable.class.isAssignableFrom(java.util.ArrayList.class), "ArrayList is Serializable");
        check(Serializable.class.isAssignableFrom(java.time.LocalDate.class), "LocalDate is Serializable");
        check(!Serializable.class.isAssignableFrom(java.util.Scanner.class), "Scanner is NOT Serializable");
        check(!Serializable.class.isAssignableFrom(Thread.class), "Thread is NOT Serializable");
        check(!Serializable.class.isAssignableFrom(Object.class), "Object is NOT Serializable");

        // arrays: all array types implement Serializable, even of non-serializable components
        check(Serializable.class.isAssignableFrom(int[].class), "int[] is Serializable");
        check(Serializable.class.isAssignableFrom(new Object[0].getClass()), "Object[] is Serializable");
        check(Serializable.class.isAssignableFrom(new int[0][0].getClass()), "int[][] is Serializable");

        // behavior: serializing a String (Serializable) works; non-Serializable rejects
        check("s".equals(de(ser("s"))), "String round-trips through the marker contract");
        int[] arr = (int[]) de(ser(new int[]{5, 6, 7}));
        check(arr.length == 3 && arr[0] == 5 && arr[2] == 7, "int[] round-trips with contents");

        try {
            ser(new Object());
            check(false, "writing a plain Object should throw NotSerializableException");
        } catch (NotSerializableException expected) {
            check("java.lang.Object".equals(expected.getMessage()),
                  "NotSerializableException message is the class name");
        }

        // enum types are serializable through their base
        check(Serializable.class.isAssignableFrom(Enum.class), "java.lang.Enum is Serializable");
        Object dw = de(ser(java.time.DayOfWeek.FRIDAY));
        check(java.time.DayOfWeek.FRIDAY.equals(dw), "enum constant round-trips");
        check(dw == java.time.DayOfWeek.FRIDAY, "enum constant identity preserved");

        // a user class gains serialization behavior purely by implementing the marker
        Point p = (Point) de(ser(new Point(3, 4)));
        check(p.x == 3 && p.y == 4, "user marker class round-trips");

        // implementing only Serializable with no fields still round-trips
        Empty e = (Empty) de(ser(new Empty()));
        check(e != null, "no-field Serializable class round-trips");

        // marker is heritable through class hierarchy
        check(Serializable.class.isAssignableFrom(Child.class),
              "subclass of a Serializable class is Serializable");
        Child ch = (Child) de(ser(new Child(9)));
        check(ch.v == 9 && ch.w == 1, "inherited Serializable fields round-trip");
    }

    static class Point implements Serializable {
        private static final long serialVersionUID = 1L;
        final int x, y;
        Point(int x, int y) { this.x = x; this.y = y; }
    }

    static class Empty implements Serializable {
        private static final long serialVersionUID = 2L;
    }

    static class Base implements Serializable {
        private static final long serialVersionUID = 3L;
        int w = 1;
    }

    static class Child extends Base {
        private static final long serialVersionUID = 4L;
        int v;
        Child(int v) { this.v = v; }
    }

    // ================= deep additions (contract level) =================

    /** NOT Serializable: used to pin the NotSerializableException message. */
    static class Nope { int x; }

    /** Correct hook signatures: private void (ObjectOutputStream)/(ObjectInputStream). */
    static class HookOk implements Serializable {
        private static final long serialVersionUID = 10L;   // fixed for cross-version stability
        static int wcount, rcount;
        int a = 1;
        private void writeObject(ObjectOutputStream o) throws java.io.IOException {
            wcount++; o.defaultWriteObject();
        }
        private void readObject(ObjectInputStream i)
                throws java.io.IOException, ClassNotFoundException {
            rcount++; i.defaultReadObject();
        }
    }

    /** Wrong parameter type: hook must NOT be invoked; default form used silently. */
    static class HookWrongParam implements Serializable {
        private static final long serialVersionUID = 11L;
        static int wcount;
        int a = 2;
        private void writeObject(String s) { wcount++; }
    }

    /** Wrong visibility (public): hook must NOT be invoked. */
    static class HookWrongAccess implements Serializable {
        private static final long serialVersionUID = 12L;
        static int rcount;
        int a = 3;
        public void readObject(ObjectInputStream i) { rcount++; }
    }

    /** Wrong return type: hook must NOT be invoked. */
    static class HookWrongReturn implements Serializable {
        private static final long serialVersionUID = 13L;
        static int wcount;
        int a = 4;
        private int writeObject(ObjectOutputStream o) { wcount++; return 1; }
    }

    /** transient/static skip proof: static is changed between write and read. */
    static class TransStatic implements Serializable {
        private static final long serialVersionUID = 14L;
        static int s;
        transient int t;
        int k;
        TransStatic(int t, int k) { this.t = t; this.k = k; }
    }

    /** Fixed UID + plain fields: byte-for-byte determinism probe. */
    static class FixedA implements Serializable {
        private static final long serialVersionUID = 15L;
        int i; String s; long l;
        FixedA(int i, String s, long l) { this.i = i; this.s = s; this.l = l; }
    }

    /** Sub-interface of the marker is inherited by implementors. */
    interface MarkerIface extends Serializable { }

    static class MarkedImpl implements MarkerIface {
        private static final long serialVersionUID = 16L;
        int v = 9;
    }

    /** writeReplace: the replacement (a String) is what lands in the stream. */
    static class Writer implements Serializable {
        private static final long serialVersionUID = 17L;
        static int wr;
        int marker = 1;
        private Object writeReplace() { wr++; return "replaced-" + marker; }
    }

    /** readResolve: readObject returns the sentinel, not a fresh instance. */
    static class Resolver implements Serializable {
        private static final long serialVersionUID = 18L;
        static int rr;
        static final Object SENTINEL = new Object();
        int v;
        Resolver(int v) { this.v = v; }
        private Object readResolve() { rr++; return SENTINEL; }
    }

    /** writeReplace returning a non-Serializable replacement is rejected at write time. */
    static class BadReplace implements Serializable {
        private static final long serialVersionUID = 19L;
        private Object writeReplace() { return new Object(); }
    }

    /** Shared reference and circular graph probes. */
    static class Node implements Serializable {
        private static final long serialVersionUID = 20L;
        Node next;
    }

    static class Ring implements Serializable {
        private static final long serialVersionUID = 21L;
        Ring peer;
    }

    // ---- marker interface structure: no members at all ----
    static void testMarkerStructure() throws Exception {
        Class<?> c = Serializable.class;
        check(c.getDeclaredMethods().length == 0, "marker interface declares no methods");
        check(c.getDeclaredFields().length == 0, "marker interface declares no fields");
        check(c.getInterfaces().length == 0, "marker interface extends nothing");
        check("java.io".equals(c.getPackage().getName()), "package is java.io");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public interface");
    }

    // ---- rejection message carries the binary class name ----
    static void testNotSerializableMessage() throws Exception {
        try {
            ser(new Nope());
            check(false, "plain object must be rejected");
        } catch (NotSerializableException nse) {
            check(nse.getMessage() != null
                  && nse.getMessage().equals(Nope.class.getName()),
                  "message is the FQCN binary name: " + nse.getMessage());
        }
        check(!Serializable.class.isAssignableFrom(Nope.class), "Nope is not Serializable");
    }

    // ---- only the exact hook signatures are invoked ----
    static void testHookSignatureMatching() throws Exception {
        HookOk.wcount = 0; HookOk.rcount = 0;
        HookOk back = (HookOk) de(ser(new HookOk()));
        check(HookOk.wcount == 1 && HookOk.rcount == 1,
              "exact-signature hooks invoked exactly once (w=" + HookOk.wcount
              + ", r=" + HookOk.rcount + ")");
        check(back.a == 1, "hooked class field restored");

        HookWrongParam.wcount = 0;
        HookWrongParam p = (HookWrongParam) de(ser(new HookWrongParam()));
        check(HookWrongParam.wcount == 0, "writeObject(String) is NOT a hook");
        check(p.a == 2, "default serialization still wrote the field");

        HookWrongAccess.rcount = 0;
        HookWrongAccess a = (HookWrongAccess) de(ser(new HookWrongAccess()));
        check(HookWrongAccess.rcount == 0, "public readObject is NOT a hook");
        check(a.a == 3, "default serialization used for wrong-visibility hook");

        HookWrongReturn.wcount = 0;
        HookWrongReturn r = (HookWrongReturn) de(ser(new HookWrongReturn()));
        check(HookWrongReturn.wcount == 0, "non-void writeObject is NOT a hook");
        check(r.a == 4, "default serialization used for wrong-return hook");
    }

    // ---- transient skipped, static not serialized ----
    static void testTransientStaticSkip() throws Exception {
        TransStatic.s = 100;
        byte[] bytes = ser(new TransStatic(42, 7));
        TransStatic.s = 200;                    // change static between write and read
        TransStatic back = (TransStatic) de(bytes);
        check(back.t == 0, "transient field reset to 0 (skipped by default form)");
        check(back.k == 7, "non-transient field restored");
        check(TransStatic.s == 200, "static field not serialized (still the post-write value)");
    }

    // ---- fixed UID gives byte-for-byte deterministic streams ----
    static void testByteDeterminism() throws Exception {
        byte[] b1 = ser(new FixedA(1, "x", 2L));
        byte[] b2 = ser(new FixedA(1, "x", 2L));
        check(java.util.Arrays.equals(b1, b2),
              "same content -> identical bytes under fixed serialVersionUID");
        byte[] b3 = ser(new FixedA(2, "x", 2L));
        check(!java.util.Arrays.equals(b1, b3), "different content -> different bytes");
        check(b1.length > 4, "stream contains more than the header");
    }

    // ---- marker semantics flow through sub-interfaces ----
    static void testSubInterfaceInheritance() throws Exception {
        check(Serializable.class.isAssignableFrom(MarkerIface.class),
              "sub-interface extends Serializable");
        check(Serializable.class.isAssignableFrom(MarkedImpl.class),
              "implementor of sub-interface is Serializable");
        MarkedImpl m = (MarkedImpl) de(ser(new MarkedImpl()));
        check(m.v == 9, "sub-interface implementor round-trips");
    }

    // ---- writeReplace / readResolve substitution contract ----
    static void testReplaceResolve() throws Exception {
        Writer.wr = 0;
        Object w = de(ser(new Writer()));
        check("replaced-1".equals(w), "writeReplace result is what the stream holds");
        check(Writer.wr == 1, "writeReplace invoked once per write");

        Resolver.rr = 0;
        Object r = de(ser(new Resolver(9)));
        check(r == Resolver.SENTINEL, "readResolve substitutes the returned object (identity)");
        check(Resolver.rr == 1, "readResolve invoked once per read");

        try {
            ser(new BadReplace());
            check(false, "non-Serializable replacement must be rejected");
        } catch (NotSerializableException nse) {
            check("java.lang.Object".equals(nse.getMessage()),
                  "rejection names the replacement class: " + nse.getMessage());
        }
    }

    // ---- graph shape: shared references and cycles survive ----
    static void testGraphShape() throws Exception {
        Node shared = new Node();
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(shared);
        oos.writeObject(shared);                // same object again: back-reference
        oos.close();
        ObjectInputStream ois = new ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()));
        Node first = (Node) ois.readObject();
        Node second = (Node) ois.readObject();
        ois.close();
        check(first == second, "shared reference read back identical (back-reference)");

        Ring a = new Ring();
        Ring b = new Ring();
        a.peer = b; b.peer = a;
        Ring back = (Ring) de(ser(a));
        check(back.peer != null && back.peer.peer == back,
              "circular reference graph restored");
        check(back != back.peer, "cycle has two distinct instances");
    }

    // ---- array matrix: components, nesting, nulls ----
    static void testArrayMatrix() throws Exception {
        String[] sa = (String[]) de(ser(new String[]{"a", null, "c"}));
        check(sa.length == 3 && "a".equals(sa[0]) && sa[1] == null && "c".equals(sa[2]),
              "String[] with null element round-trips");
        Object[] mixed = (Object[]) de(ser(new Object[]{Integer.valueOf(1), "two", null}));
        check(mixed.length == 3 && Integer.valueOf(1).equals(mixed[0])
              && "two".equals(mixed[1]) && mixed[2] == null, "heterogeneous Object[] round-trips");
        long[][] md = (long[][]) de(ser(new long[][]{{1L, 2L}, {3L}}));
        check(md.length == 2 && md[0][1] == 2L && md[1][0] == 3L, "multi-dimensional array round-trips");
        boolean[] ba = (boolean[]) de(ser(new boolean[]{true, false}));
        check(ba[0] && !ba[1], "boolean[] round-trips");
        byte[] empty = (byte[]) de(ser(new byte[0]));
        check(empty.length == 0, "empty array round-trips");
    }

    public static void main(String[] args) throws Exception {
        testBlackBox();
        testMarkerStructure();
        testNotSerializableMessage();
        testHookSignatureMatching();
        testTransientStaticSkip();
        testByteDeterminism();
        testSubInterfaceInheritance();
        testReplaceResolve();
        testGraphShape();
        testArrayMatrix();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
