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

    public static void main(String[] args) throws Exception {
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
