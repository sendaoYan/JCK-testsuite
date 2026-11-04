/*
 * @test
 * @summary java.io.ByteArrayOutputStream conformance: white-box structure (reflection) + full
 *          black-box contract coverage. Constructors (negative->IAE), write(int) low 8 bits,
 *          write(byte[],off,len) validation (NPE/IOBE), writeBytes (JDK 11+), auto-grow,
 *          toByteArray copy semantics, reset, toString()/toString(Charset)/
 *          UnsupportedEncodingException, writeTo copy + null->NPE, close no-op.
 * @run main ByteArrayOutputStreamTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
