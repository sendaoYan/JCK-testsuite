/*
 * @test
 * @summary java.io.ByteArrayOutputStream conformance: white-box structure (reflection) + full
 *          black-box contract coverage. Constructors (negative->IAE), write(int) low 8 bits,
 *          write(byte[],off,len) validation (NPE/IOBE), writeBytes (JDK 11+), auto-grow,
 *          toByteArray copy semantics, reset, toString()/toString(Charset)/
 *          UnsupportedEncodingException, writeTo copy + null->NPE, close no-op.
 *          Deepened (contract level): write(int) boundary values, grow across the
 *          initial capacity, state invariant after failed writes, explicit charset
 *          matrix incl. toString(Charset), writeTo delegation shape (single bulk
 *          call), writeTo to self, writeTo to failing sink, writeBytes contract.
 * @run main ByteArrayOutputStreamTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Modifier;
import java.nio.charset.Charset;
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

    static void hasMethod(Class<?> c, String name, Class<?>... params) {
        try { c.getMethod(name, params); }
        catch (NoSuchMethodException e) { throw new AssertionError("missing method " + name); }
        passed++;
    }

    // ---- white-box: structure via reflection ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.io.ByteArrayOutputStream");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "is concrete class");
        check(c.getSuperclass() == java.io.OutputStream.class, "extends OutputStream");
        hasMethod(c, "toByteArray");
        hasMethod(c, "size");
        hasMethod(c, "reset");
        hasMethod(c, "toString", Charset.class);
        hasMethod(c, "writeBytes", byte[].class);
        hasMethod(c, "writeTo", java.io.OutputStream.class);
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
        out.write(0x1FF);
        out.write(256);
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
        out.write(new byte[4], 4, 0);
        check(out.size() == 0, "off==len,len==0 is a legal no-op");
        checkThrows(NullPointerException.class, () -> out.write((byte[]) null),
                    "write((byte[])null) -> NPE");
    }

    // ---- 4. write(byte[],off,len) + writeBytes + auto-grow ----
    static void testWriteArrayAndGrow() {
        ByteArrayOutputStream out = new ByteArrayOutputStream(4);
        byte[] data = {1, 2, 3, 4, 5, 6, 7, 8};
        out.write(data, 2, 4);
        check(out.size() == 4, "write size");
        check(out.toByteArray()[0] == 3 && out.toByteArray()[3] == 6, "write offset content");

        out.writeBytes(new byte[]{9, 10});
        check(out.size() == 6 && out.toByteArray()[4] == 9 && out.toByteArray()[5] == 10,
              "writeBytes (JDK 11+)");

        ByteArrayOutputStream big = new ByteArrayOutputStream(2);
        byte[] many = new byte[10000];
        for (int i = 0; i < many.length; i++) many[i] = (byte) (i * 31);
        big.write(many, 0, many.length);
        check(big.size() == many.length, "auto-grow size");
        byte[] got = big.toByteArray();
        boolean eq = got.length == many.length;
        for (int i = 0; eq && i < many.length; i++) if (got[i] != many[i]) eq = false;
        check(eq, "auto-grow preserves all content");
    }

    // ---- 5. toByteArray copy semantics ----
    static void testToByteArrayCopy() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(new byte[]{10, 20, 30}, 0, 3);
        byte[] first = out.toByteArray();
        first[0] = 99;
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
        check("ABC".equals(ascii.toString()), "toString() ASCII");

        ByteArrayOutputStream utf = new ByteArrayOutputStream();
        utf.write("héllo".getBytes(StandardCharsets.UTF_8));
        check("héllo".equals(utf.toString("UTF-8")), "toString(String) UTF-8 non-ASCII");
        check("héllo".equals(utf.toString(StandardCharsets.UTF_8)), "toString(Charset) UTF-8");

        checkThrows(UnsupportedEncodingException.class,
                    () -> utf.toString("no-such-charset-xyz"),
                    "toString(unknown charset) -> UnsupportedEncodingException");
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
        out.close();
        out.write(66);
        check(out.size() == 2, "close is no-op, writes still work");
        check(out.toByteArray()[1] == 66, "content after close");
    }

    // ---- 10. write(int) boundary values (only low 8 bits kept) ----
    static void testWriteIntBoundaries() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(0);
        out.write(255);
        out.write(256);                 // -> 0x00
        out.write(-1);                  // -> 0xFF
        out.write(-256);                // -> 0x00
        out.write(0x141);               // -> 0x41 ('A')
        out.write(Integer.MAX_VALUE);   // -> 0xFF
        byte[] b = out.toByteArray();
        check(out.size() == 7, "boundary write count");
        check((b[0] & 0xFF) == 0 && (b[1] & 0xFF) == 255 && (b[2] & 0xFF) == 0,
              "write(int) low 8 bits: 0/255/256");
        check((b[3] & 0xFF) == 0xFF && (b[4] & 0xFF) == 0x00,
              "write(int) negatives truncate");
        check((b[5] & 0xFF) == 0x41 && (b[6] & 0xFF) == 0xFF, "write(int) large values");
    }

    // ---- 11. writeBytes(byte[]) (JDK 11+) full contract ----
    static void testWriteBytes() {
        ByteArrayOutputStream out = new ByteArrayOutputStream(2);
        out.writeBytes(new byte[]{1, 2, 3});
        check(out.size() == 3, "writeBytes whole array");
        out.writeBytes(new byte[0]);
        check(out.size() == 3, "writeBytes(empty) is a no-op");
        byte[] many = new byte[2000];
        for (int i = 0; i < many.length; i++) many[i] = (byte) (i * 13);
        ByteArrayOutputStream big = new ByteArrayOutputStream(4);
        big.writeBytes(many);
        check(big.size() == many.length, "writeBytes grows as needed");
        check(java.util.Arrays.equals(big.toByteArray(), many), "writeBytes content after grow");
        checkThrows(NullPointerException.class, () -> big.writeBytes(null),
                    "writeBytes(null) -> NPE");
        // writeBytes(b) is equivalent to write(b, 0, b.length)
        ByteArrayOutputStream a = new ByteArrayOutputStream();
        ByteArrayOutputStream c = new ByteArrayOutputStream();
        a.writeBytes(many);
        c.write(many, 0, many.length);
        check(java.util.Arrays.equals(a.toByteArray(), c.toByteArray()),
              "writeBytes(b) == write(b, 0, b.length)");
    }

    // ---- 12. grow across the initial capacity (impl detail: 32; data integrity observable) ----
    static void testGrowAcrossCapacity() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();   // initial capacity 32
        for (int i = 0; i < 40; i++) out.write(i);
        check(out.size() == 40, "default-capacity grow: size");
        byte[] b = out.toByteArray();
        boolean eq = b.length == 40;
        for (int i = 0; eq && i < 40; i++) if ((b[i] & 0xFF) != i) eq = false;
        check(eq, "default-capacity grow preserves all content");

        ByteArrayOutputStream one = new ByteArrayOutputStream(1);
        byte[] many = new byte[1000];
        for (int i = 0; i < many.length; i++) many[i] = (byte) (i * 7);
        one.write(many, 0, many.length);
        check(one.size() == many.length, "capacity-1 grow via single large write");

        ByteArrayOutputStream zero = new ByteArrayOutputStream(0);
        for (int i = 0; i < 100; i++) zero.write(i);
        check(zero.size() == 100, "capacity-0 grow via single writes");
    }

    // ---- 13. state invariant after failed writes ----
    static void testStateInvariantAfterFailedWrite() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(new byte[]{1, 2, 3}, 0, 3);
        try { out.write(new byte[4], 0, -1); } catch (IndexOutOfBoundsException expected) { }
        try { out.write(new byte[4], 2, 3); } catch (IndexOutOfBoundsException expected) { }
        try { out.write(null, 0, 1); } catch (NullPointerException expected) { }
        check(out.size() == 3, "failed writes leave size unchanged");
        check(out.toByteArray()[0] == 1 && out.toByteArray()[2] == 3,
              "failed writes leave content unchanged");
    }

    // ---- 14. reset after grow ----
    static void testResetAfterGrow() {
        ByteArrayOutputStream out = new ByteArrayOutputStream(4);
        byte[] many = new byte[500];
        for (int i = 0; i < many.length; i++) many[i] = (byte) (i * 3);
        out.write(many, 0, many.length);
        out.reset();
        check(out.size() == 0, "reset after grow: size 0");
        out.write(new byte[]{9, 9}, 0, 2);
        check(out.size() == 2 && out.toByteArray()[0] == 9 && out.toByteArray()[1] == 9,
              "reset after grow: buffer reusable with new content only");
    }

    // ---- 15. toString charset matrix: String-name, Charset overloads, default dynamic ----
    static void testToStringCharsetMatrix() throws Exception {
        // ISO-8859-1: exact 1:1 byte->char round trip through both overloads
        ByteArrayOutputStream iso = new ByteArrayOutputStream();
        iso.write("héllo".getBytes(StandardCharsets.ISO_8859_1), 0, 5);
        check("héllo".equals(iso.toString("ISO-8859-1")), "toString(String ISO-8859-1) round trip");
        check("héllo".equals(iso.toString(StandardCharsets.ISO_8859_1)),
              "toString(Charset ISO-8859-1) round trip");

        // US-ASCII: unmappable bytes decode to U+FFFD (measured on JDK-27)
        ByteArrayOutputStream ascii = new ByteArrayOutputStream();
        ascii.write(new byte[]{0x41, (byte) 0xE9, 0x42}, 0, 3);
        String asciiStr = ascii.toString(StandardCharsets.US_ASCII);
        check(asciiStr.length() == 3 && asciiStr.charAt(0) == 'A'
              && asciiStr.charAt(1) == '�' && asciiStr.charAt(2) == 'B',
              "toString(US-ASCII) replacement char U+FFFD");

        // UTF-16BE via both overloads must agree
        ByteArrayOutputStream u16 = new ByteArrayOutputStream();
        u16.write("héllo".getBytes(StandardCharsets.UTF_16BE), 0, 10);
        check("héllo".equals(u16.toString("UTF-16BE")), "toString(UTF-16BE String) round trip");
        check("héllo".equals(u16.toString(StandardCharsets.UTF_16BE)),
              "toString(UTF-16BE Charset) round trip");

        // toString() decodes with the platform default charset (dynamic expectation)
        ByteArrayOutputStream def = new ByteArrayOutputStream();
        def.write("héllo".getBytes(StandardCharsets.UTF_8), 0, 6);
        check(def.toString().equals(new String(def.toByteArray(), Charset.defaultCharset())),
              "toString() uses the platform default charset");

        // nulls -> NPE (String form measured with message 'charsetName' on JDK-8, null on JDK-27)
        checkThrows(NullPointerException.class, () -> def.toString((String) null),
                    "toString((String) null) -> NPE");
        checkThrows(NullPointerException.class, () -> def.toString((Charset) null),
                    "toString((Charset) null) -> NPE");

        // failed charset does not disturb state
        ByteArrayOutputStream before = new ByteArrayOutputStream();
        before.write(new byte[]{1}, 0, 1);
        try { before.toString("no-such-charset-xyz"); } catch (UnsupportedEncodingException expected) { }
        check(before.size() == 1, "failed toString(charset) leaves state unchanged");
    }

    // ---- 16. writeTo delegation shape: exactly one bulk write(buf, 0, count) ----
    static void testWriteToDelegation() throws Exception {
        final int[] singleCalls = {0};
        final int[] bulkCalls = {0};
        final ByteArrayOutputStream sinkData = new ByteArrayOutputStream();
        class RecSink extends OutputStream {
            public void write(int b) { singleCalls[0]++; sinkData.write(b); }
            public void write(byte[] b, int off, int len) {
                bulkCalls[0]++;
                sinkData.write(b, off, len);
            }
        }
        ByteArrayOutputStream src = new ByteArrayOutputStream();
        src.write(new byte[]{10, 20, 30, 40}, 0, 4);
        src.writeTo(new RecSink());
        check(bulkCalls[0] == 1, "writeTo makes exactly one bulk write to the sink");
        check(singleCalls[0] == 0, "writeTo never writes byte-by-byte");
        check(sinkData.size() == 4 && sinkData.toByteArray()[3] == 40, "writeTo sink content");

        // writeTo to a failing sink propagates the IOException with the original message
        ByteArrayOutputStream failing = new ByteArrayOutputStream();
        failing.write(new byte[]{1}, 0, 1);
        checkThrows(java.io.IOException.class, () -> failing.writeTo(
                new OutputStream() {
                    public void write(int b) throws java.io.IOException {
                        throw new java.io.IOException("sink write failed");
                    }
                }), "writeTo propagates sink IOException");
    }

    // ---- 17. writeTo(this) appends a copy of the current contents (impl-observed,
    //          identical on JDK-8 and JDK-27) ----
    static void testWriteToSelf() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(new byte[]{1, 2, 3}, 0, 3);
        out.writeTo(out);
        check(out.size() == 6, "writeTo(this) doubles the content");
        byte[] b = out.toByteArray();
        check(b[0] == 1 && b[2] == 3 && b[3] == 1 && b[5] == 3, "writeTo(this) content duplicated");
    }

    // ---- 18. toString(int hibyte) masks hibyte to 8 bits ----
    static void testToStringHibyteMask() {
        ByteArrayOutputStream hib = new ByteArrayOutputStream();
        hib.write(new byte[]{0x41}, 0, 1);
        String masked = hib.toString(0x101);            // (hibyte & 0xff) == 1
        String plain = hib.toString(0x01);
        check(masked.charAt(0) == plain.charAt(0), "toString(hibyte) masks hibyte to low 8 bits");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testConstructors();
        testWriteInt();
        testWriteArrayValidation();
        testWriteArrayAndGrow();
        testToByteArrayCopy();
        testReset();
        testToString();
        testWriteTo();
        testCloseNoOp();
        testWriteIntBoundaries();
        testWriteBytes();
        testGrowAcrossCapacity();
        testStateInvariantAfterFailedWrite();
        testResetAfterGrow();
        testToStringCharsetMatrix();
        testWriteToDelegation();
        testWriteToSelf();
        testToStringHibyteMask();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
