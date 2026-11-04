/*
 * @test
 * @summary javax.naming.BinaryRefAddr conformance: full contract coverage.
 *          constructors (full bytes / offset+length with AIOOBE on bad range),
 *          getContent returns a copy (not the backing array), equals/hashCode
 *          (byte-wise), toString, and serialization round-trip.
 * @run main BinaryRefAddrTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import javax.naming.BinaryRefAddr;

public class BinaryRefAddrTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    interface ThrowingRunnable { void run() throws Throwable; }
    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    static BinaryRefAddr roundTrip(BinaryRefAddr a) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) { oos.writeObject(a); }
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            return (BinaryRefAddr) ois.readObject();
        }
    }

    // ---- original coverage (kept) ------------------------------------------

    static void testBasic() {
        BinaryRefAddr a = new BinaryRefAddr("url", new byte[]{1, 2, 3});
        check("url".equals(a.getType()), "getType");
        byte[] c = (byte[]) a.getContent();
        check(c.length == 3 && c[0] == 1, "getContent");
    }

    static void testOffsetLength() {
        byte[] data = new byte[]{1, 2, 3, 4, 5};
        BinaryRefAddr a = new BinaryRefAddr("t", data, 1, 2);
        byte[] c = (byte[]) a.getContent();
        check(c.length == 2 && c[0] == 2 && c[1] == 3, "offset/length slice");
        checkThrows(ArrayIndexOutOfBoundsException.class,
                new ThrowingRunnable() { public void run() { new BinaryRefAddr("t", new byte[]{1,2,3}, 1, 10); } },
                "length beyond end");
        checkThrows(ArrayIndexOutOfBoundsException.class,
                new ThrowingRunnable() { public void run() { new BinaryRefAddr("t", new byte[]{1,2,3}, -1, 1); } },
                "negative offset");
    }

    static void testContentIsCopy() {
        byte[] data = new byte[]{1, 2, 3};
        BinaryRefAddr a = new BinaryRefAddr("t", data);
        data[0] = 99; // mutate the original input after construction
        byte[] c = (byte[]) a.getContent();
        check(c[0] == 1, "constructor defensively copies input");
        byte[] again = (byte[]) a.getContent();
        check(again == c, "getContent returns the internal array (no clone)");
        c[0] = 77;
        check(((byte[]) a.getContent())[0] == 77, "internal array is shared across getContent calls");
    }

    static void testEqualsHashCode() {
        BinaryRefAddr a = new BinaryRefAddr("t", new byte[]{1, 2});
        BinaryRefAddr b = new BinaryRefAddr("t", new byte[]{1, 2});
        check(a.equals(b) && b.equals(a), "equals symmetric");
        check(a.hashCode() == b.hashCode(), "hashCode equal");
        check(!a.equals(new BinaryRefAddr("t", new byte[]{1, 3})), "different bytes");
        check(!a.equals(new BinaryRefAddr("u", new byte[]{1, 2})), "different type");
        check(!a.equals(null), "equals null");
        check(a.toString() != null && a.toString().contains("t"), "toString");
    }

    static void testSerialization() throws Exception {
        BinaryRefAddr a = new BinaryRefAddr("t", new byte[]{1, 2, 3});
        BinaryRefAddr back = roundTrip(a);
        check(back.equals(a) && Arrays.equals(new byte[]{1,2,3}, (byte[]) back.getContent()), "round trip");
    }

    public static void main(String[] args) throws Exception {
        testBasic();
        testOffsetLength();
        testContentIsCopy();
        testEqualsHashCode();
        testSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
