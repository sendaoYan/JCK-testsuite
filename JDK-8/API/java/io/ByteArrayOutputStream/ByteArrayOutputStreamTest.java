/*
 * @test
 * @summary java.io.ByteArrayOutputStream API conformance: full contract coverage.
 *          Constructors (default/negative size->IAE/zero), write(int) low-8-bits,
 *          write(byte[],off,len) parameter validation (NPE/IndexOutOfBoundsException),
 *          auto-grow preserving content, toByteArray copy semantics, size/reset,
 *          toString()/toString(String) incl. UnsupportedEncodingException,
 *          toString(int hibyte), writeTo copy + null->NPE, close() no-op.
 * @run main ByteArrayOutputStreamTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

public class ByteArrayOutputStreamTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

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

    // ---- 1. constructors ----
    static void testConstructors() {
        ByteArrayOutputStream def = new ByteArrayOutputStream();
        check(def.size() == 0, "default size 0");

        ByteArrayOutputStream zero = new ByteArrayOutputStream(0);
        check(zero.size() == 0, "size 0 constructor valid");

        checkThrows(IllegalArgumentException.class, () -> new ByteArrayOutputStream(-1),
                    "negative size -> IllegalArgumentException");
    }

    // ---- 2. write(int) ----
    static void testWriteInt() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(65);
        out.write(0x1FF);               // only low 8 bits are kept
        out.write(256);                 // -> 0
        check(out.size() == 3, "write(int) size");
        byte[] b = out.toByteArray();
        check(b[0] == 65 && (b[1] & 0xFF) == 0xFF && b[2] == 0, "write(int) low 8 bits");
    }

    // ---- 3. write(byte[],off,len) validation ----
    static void testWriteArrayValidation() {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        checkThrows(NullPointerException.class, () -> out.write(null, 0, 1),
                    "write(null,0,1) -> NPE");
        checkThrows(IndexOutOfBoundsException.class, () -> out.write(new byte[4], -1, 1),
                    "write(b,-1,1) -> IndexOutOfBoundsException");
        checkThrows(IndexOutOfBoundsException.class, () -> out.write(new byte[4], 5, 0),
                    "write(b,5,0) -> IndexOutOfBoundsException (off > len)");
        checkThrows(IndexOutOfBoundsException.class, () -> out.write(new byte[4], 0, -1),
                    "write(b,0,-1) -> IndexOutOfBoundsException");
        checkThrows(IndexOutOfBoundsException.class, () -> out.write(new byte[4], 2, 3),
                    "write(b,2,3) -> IndexOutOfBoundsException (off+len > len)");
        // off == b.length with len == 0 is legal
        out.write(new byte[4], 4, 0);
        check(out.size() == 0, "off==len,len==0 is a legal no-op");
        // inherited write(byte[]) with null -> NPE
        checkThrows(NullPointerException.class, () -> out.write((byte[]) null),
                    "write((byte[])null) -> NPE");
    }

    // ---- 4. write(byte[],off,len) + auto-grow ----
    static void testWriteArrayAndGrow() {
        ByteArrayOutputStream out = new ByteArrayOutputStream(4);
        byte[] data = {1, 2, 3, 4, 5, 6, 7, 8};
        out.write(data, 2, 4);          // 3,4,5,6
        check(out.size() == 4, "write size");
        check(out.toByteArray()[0] == 3 && out.toByteArray()[3] == 6, "write offset content");

        // auto-grow beyond initial capacity preserves all content
        ByteArrayOutputStream big = new ByteArrayOutputStream(2);
        byte[] many = new byte[10000];
        for (int i = 0; i < many.length; i++) many[i] = (byte) (i * 31);
        big.write(many, 0, many.length);
        check(big.size() == many.length, "auto-grow size");
        byte[] got = big.toByteArray();
        check(got.length == many.length, "auto-grow toByteArray length");
        boolean eq = true;
        for (int i = 0; i < many.length; i++) if (got[i] != many[i]) { eq = false; break; }
        check(eq, "auto-grow preserves all content");
    }

    // ---- 5. toByteArray copy semantics ----
    static void testToByteArrayCopy() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(new byte[]{10, 20, 30}, 0, 3);
        byte[] first = out.toByteArray();
        first[0] = 99;                  // mutate returned copy
        byte[] second = out.toByteArray();
        check(second[0] == 10, "toByteArray returns a fresh copy each call");
        check(out.size() == 3, "size unaffected by copy mutation");
    }

    // ---- 6. reset ----
    static void testReset() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(new byte[]{1, 2, 3}, 0, 3);
        out.reset();
        check(out.size() == 0, "reset size -> 0");
        check(out.toByteArray().length == 0, "reset toByteArray empty");
        out.write(9);
        check(out.size() == 1 && out.toByteArray()[0] == 9, "reuse after reset");
    }

    // ---- 7. toString variants ----
    static void testToString() throws Exception {
        ByteArrayOutputStream ascii = new ByteArrayOutputStream();
        ascii.write(new byte[]{65, 66, 67}, 0, 3);
        check("ABC".equals(ascii.toString()), "toString() ASCII (charset-independent)");

        ByteArrayOutputStream utf = new ByteArrayOutputStream();
        utf.write("héllo".getBytes(StandardCharsets.UTF_8));
        check("héllo".equals(utf.toString("UTF-8")), "toString(String) UTF-8 non-ASCII");

        // toString(String) with unknown charset -> UnsupportedEncodingException
        checkThrows(UnsupportedEncodingException.class,
                    () -> utf.toString("no-such-charset-xyz"),
                    "toString(unknown charset) -> UnsupportedEncodingException");

        // toString(int hibyte): c = ((hibyte & 0xff) << 8) | (b & 0xff)
        ByteArrayOutputStream hib = new ByteArrayOutputStream();
        hib.write(new byte[]{0x41, 0x42}, 0, 2);   // 'A', 'B'
        String s = hib.toString(0);
        check(s.length() == 2 && s.charAt(0) == 'A' && s.charAt(1) == 'B',
              "toString(0) maps bytes to chars 0..255");
        String hi = hib.toString(0x01);
        check(hi.charAt(0) == (char) ((0x01 << 8) | 0x41), "toString(hibyte) high byte mapping");
    }

    // ---- 8. writeTo ----
    static void testWriteTo() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(new byte[]{1, 2, 3}, 0, 3);
        ByteArrayOutputStream dest = new ByteArrayOutputStream();
        out.writeTo(dest);
        check(dest.size() == 3 && dest.toByteArray()[0] == 1 && dest.toByteArray()[2] == 3,
              "writeTo copies contents");
        check(out.size() == 3, "writeTo does not consume source");

        checkThrows(NullPointerException.class, () -> out.writeTo(null),
                    "writeTo(null) -> NPE");
    }

    // ---- 9. close is a no-op ----
    static void testCloseNoOp() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(65);
        out.close();
        out.close();                    // idempotent
        out.write(66);
        check(out.size() == 2, "close is no-op, writes still work");
        check(out.toByteArray()[1] == 66, "content after close");
    }

    public static void main(String[] args) throws Exception {
        testConstructors();
        testWriteInt();
        testWriteArrayValidation();
        testWriteArrayAndGrow();
        testToByteArrayCopy();
        testReset();
        testToString();
        testWriteTo();
        testCloseNoOp();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
