/*
 * @test
 * @summary java.io.ByteArrayInputStream conformance: white-box structure (reflection) + full
 *          black-box contract coverage. Constructors (buffer-not-copied, offset/length
 *          clamping, null->NPE), read() 0..255 + EOF -1, read(byte[],off,len) validation
 *          (NPE/IndexOutOfBoundsException) + exact-offset/partial/zero-length semantics,
 *          skip (negative->0, past-end), available, mark/reset, close no-op, plus JDK 9+
 *          readAllBytes/readNBytes/transferTo. Deepened (contract level): constructor
 *          corner cases, read boundary placement, skip x mark, mark/reset after close,
 *          large-buffer reads, and full readAllBytes/readNBytes(byte[],int,int)/
 *          readNBytes(int)/transferTo contracts incl. null/throwing sink.
 * @run main ByteArrayInputStreamTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Modifier;
import java.util.Arrays;

public class ByteArrayInputStreamTest {
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
        Class<?> c = Class.forName("java.io.ByteArrayInputStream");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "is concrete class");
        check(c.getSuperclass() == java.io.InputStream.class, "extends InputStream");
        hasMethod(c, "read");
        hasMethod(c, "readAllBytes");
        hasMethod(c, "readNBytes", byte[].class, int.class, int.class);
        hasMethod(c, "transferTo", java.io.OutputStream.class);
        hasMethod(c, "markSupported");
    }

    // ---- 1. constructors ----
    static void testConstructors() {
        byte[] data = {1, 2, 3};
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        check(in.available() == 3, "default available");
        data[1] = 99;
        check(in.read() == 1, "default first byte");
        check(in.read() == 99, "buffer not copied (source mutation visible)");

        ByteArrayInputStream sub = new ByteArrayInputStream(new byte[]{9, 8, 7, 6, 5}, 1, 3);
        check(sub.available() == 3, "offset/length available");
        check(sub.read() == 8 && sub.read() == 7 && sub.read() == 6 && sub.read() == -1,
              "offset/length reads");

        ByteArrayInputStream clamped = new ByteArrayInputStream(new byte[]{1, 2, 3}, 1, 100);
        check(clamped.available() == 2, "off+len clamped to buf.length");
        check(clamped.read() == 2 && clamped.read() == 3 && clamped.read() == -1, "clamped reads");

        ByteArrayInputStream empty = new ByteArrayInputStream(new byte[]{1, 2, 3}, 1, 0);
        check(empty.available() == 0 && empty.read() == -1, "zero length -> empty stream");

        ByteArrayInputStream atEnd = new ByteArrayInputStream(new byte[]{1, 2, 3}, 3, 5);
        check(atEnd.available() == 0 && atEnd.read() == -1, "offset == buf.length -> empty");

        checkThrows(NullPointerException.class, () -> new ByteArrayInputStream(null),
                    "ByteArrayInputStream(null)");
    }

    // ---- 2. read() single byte ----
    static void testReadSingleByte() {
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[]{(byte) 0xFF, (byte) 0x80, 0x7F});
        check(in.read() == 255, "0xFF -> 255");
        check(in.read() == 128, "0x80 -> 128");
        check(in.read() == 127, "0x7F -> 127");
        check(in.read() == -1, "read() at EOF returns -1");
        check(in.read() == -1, "read() repeatedly returns -1 at EOF");

        ByteArrayInputStream empty = new ByteArrayInputStream(new byte[0]);
        check(empty.read() == -1, "empty stream read() == -1");
    }

    // ---- 3. read(byte[],off,len) parameter validation ----
    static void testReadArrayValidation() {
        final ByteArrayInputStream in = new ByteArrayInputStream(new byte[]{1, 2, 3, 4});
        checkThrows(NullPointerException.class, () -> in.read(null, 0, 1),
                    "read(null,0,1) -> NPE");
        checkThrows(IndexOutOfBoundsException.class, () -> in.read(new byte[4], -1, 1),
                    "read(b,-1,1) -> IndexOutOfBoundsException");
        checkThrows(IndexOutOfBoundsException.class, () -> in.read(new byte[4], 0, -1),
                    "read(b,0,-1) -> IndexOutOfBoundsException");
        checkThrows(IndexOutOfBoundsException.class, () -> in.read(new byte[4], 0, 5),
                    "read(b,0,5) -> IndexOutOfBoundsException");
        checkThrows(IndexOutOfBoundsException.class, () -> in.read(new byte[4], 3, 2),
                    "read(b,3,2) -> IndexOutOfBoundsException");
    }

    // ---- 4. read(byte[],off,len) exact semantics ----
    static void testReadArrayContract() throws Exception {
        byte[] buf = new byte[6];
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[]{10, 20, 30, 40, 50});

        int n = in.read(buf, 1, 2);
        check(n == 2, "read count");
        check(buf[0] == 0 && buf[1] == 10 && buf[2] == 20 && buf[3] == 0, "exact offset placement");

        check(in.read(buf, 0, 0) == 0, "zero-length read (not at EOF) returns 0");

        byte[] tail = new byte[10];
        check(in.read(tail) == 3, "partial read at end returns remaining");
        check(tail[0] == 30 && tail[1] == 40 && tail[2] == 50, "tail contents");

        check(in.read(new byte[1], 0, 0) == -1, "zero-length read at EOF returns -1");
        check(in.read(new byte[1]) == -1, "read(byte[]) at EOF returns -1");
    }

    // ---- 5. skip ----
    static void testSkip() {
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[]{1, 2, 3, 4, 5});
        check(in.skip(2) == 2, "skip(2) returns 2");
        check(in.read() == 3, "read after skip");
        check(in.skip(100) == 2, "skip past end returns remaining");
        check(in.read() == -1, "EOF after skip past end");

        ByteArrayInputStream neg = new ByteArrayInputStream(new byte[]{1, 2, 3});
        check(neg.skip(-5) == 0, "negative skip returns 0");
        check(neg.read() == 1, "negative skip does not move position");
        check(neg.skip(0) == 0, "skip(0) returns 0");
        check(neg.read() == 2, "skip(0) does not move position");
    }

    // ---- 6. available ----
    static void testAvailable() {
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[]{1, 2, 3, 4, 5});
        check(in.available() == 5, "initial available");
        in.read();
        in.read();
        check(in.available() == 3, "available after two reads");
        in.skip(10);
        check(in.available() == 0, "available at EOF");
    }

    // ---- 7. mark/reset ----
    static void testMarkReset() {
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[]{10, 20, 30, 40});
        check(in.markSupported(), "markSupported");

        in.read();
        in.read();
        in.reset();
        check(in.read() == 10, "unmarked reset returns to position 0");

        ByteArrayInputStream m = new ByteArrayInputStream(new byte[]{1, 2, 3, 4});
        m.read();
        m.mark(100);
        m.read();
        m.read();
        m.reset();
        check(m.read() == 2 && m.read() == 3, "mark/reset replay");

        ByteArrayInputStream ml = new ByteArrayInputStream(new byte[]{1, 2, 3, 4});
        ml.read();
        ml.mark(0);
        ml.read();
        ml.reset();
        check(ml.read() == 2, "mark(0) at position 1 -> reset returns to 1");

        ByteArrayInputStream off = new ByteArrayInputStream(new byte[]{0, 9, 8, 7}, 1, 3);
        off.read();
        off.reset();
        check(off.read() == 9, "offset constructor initial mark == offset");
    }

    // ---- 8. close is a no-op ----
    static void testCloseNoOp() throws Exception {
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[]{1, 2, 3});
        in.close();
        in.close();
        check(in.available() == 3, "available after close");
        check(in.read() == 1, "read after close still works");
        check(in.skip(1) == 1, "skip after close still works");
        in.reset();
        check(in.read() == 1, "reset after close still works");
    }

    // ---- 9. JDK 9+ readAllBytes / readNBytes / transferTo ----
    static void testJdk9Plus() throws Exception {
        byte[] big = new byte[1000];
        for (int i = 0; i < big.length; i++) big[i] = (byte) (i * 7);
        ByteArrayOutputStream src = new ByteArrayOutputStream();
        src.write(big, 0, big.length);
        ByteArrayInputStream full = new ByteArrayInputStream(src.toByteArray());
        check(Arrays.equals(full.readAllBytes(), big), "readAllBytes");

        ByteArrayInputStream copyIn = new ByteArrayInputStream(big);
        ByteArrayOutputStream dst = new ByteArrayOutputStream();
        check(copyIn.transferTo(dst) == big.length, "transferTo count");
        check(Arrays.equals(dst.toByteArray(), big), "transferTo content");

        ByteArrayInputStream nb = new ByteArrayInputStream(new byte[]{1, 2, 3, 4});
        byte[] nbuf = new byte[3];
        check(nb.readNBytes(nbuf, 0, 3) == 3 && Arrays.equals(nbuf, new byte[]{1, 2, 3}), "readNBytes");
        check(nb.readNBytes(nbuf, 0, 3) == 1 && nbuf[0] == 4, "readNBytes partial at end");
    }

    // ---- 9b. constructor corner cases (offset not validated; implementation-observed,
    //          identical on JDK-8 and JDK-27: count = min(off+len, buf.length), pos = off) ----
    static void testConstructorCorners() {
        ByteArrayInputStream past = new ByteArrayInputStream(new byte[]{1, 2, 3}, 4, 1);
        check(past.available() == -1, "off beyond buf.length: available() = count - pos = -1");
        check(past.read() == -1, "off beyond buf.length: pos >= count -> immediate EOF");

        ByteArrayInputStream neg = new ByteArrayInputStream(new byte[]{1, 2, 3}, -1, 2);
        check(neg.available() == 2, "negative offset: available() = count - pos");
        checkThrows(IndexOutOfBoundsException.class, () -> neg.read(),
                    "negative offset read -> ArrayIndexOutOfBoundsException");
    }

    // ---- 9c. read(byte[],off,len) boundary placement ----
    static void testReadBoundary() {
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[]{1, 2, 3, 4});
        check(in.read(new byte[4], 4, 0) == 0, "off == b.length with len == 0 is legal");
        check(in.read(new byte[4], 0, 4) == 4, "full drain in a single call");
        check(in.available() == 0, "drained after full read");

        ByteArrayInputStream p = new ByteArrayInputStream(new byte[]{1, 2, 3});
        byte[] b = new byte[10];
        check(p.read(b, 2, 8) == 3, "len clamped to remaining");
        check(b[2] == 1 && b[3] == 2 && b[4] == 3 && b[5] == 0, "clamped placement, beyond untouched");

        // JDK-27 read validates before the EOF check: out-of-range args still throw at EOF
        final ByteArrayInputStream eof = new ByteArrayInputStream(new byte[]{1});
        check(eof.read() == 1, "drain");
        checkThrows(IndexOutOfBoundsException.class, () -> eof.read(new byte[2], 0, 3),
                    "read(b,0,3) at EOF -> IndexOutOfBoundsException");
    }

    // ---- 9d. skip x mark interaction ----
    static void testSkipMarkInteraction() {
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[]{1, 2, 3, 4, 5});
        in.read();
        in.mark(0);
        in.skip(2);
        in.reset();
        check(in.read() == 2 && in.read() == 3, "reset after skip returns to marked position");

        ByteArrayInputStream z = new ByteArrayInputStream(new byte[]{9, 8, 7});
        z.read();
        z.mark(5);
        z.skip(0);
        z.reset();
        check(z.read() == 8, "skip(0) keeps mark");

        ByteArrayInputStream e = new ByteArrayInputStream(new byte[]{1, 2});
        e.mark(0);
        e.skip(100);
        e.reset();
        check(e.read() == 1, "reset after skip past end returns to mark");
    }

    // ---- 9e. close() does not affect mark/reset ----
    static void testCloseMarkReset() throws Exception {
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[]{1, 2, 3});
        in.read();
        in.close();
        in.mark(0);
        in.read();
        in.reset();
        check(in.read() == 2, "mark after close + reset replays");
        in.close();
        in.reset();
        check(in.read() == 2, "reset after double close returns to mark");
    }

    // ---- 9f. large buffer ----
    static void testLargeBuffer() {
        byte[] data = new byte[100000];
        for (int i = 0; i < data.length; i++) data[i] = (byte) (i * 31);
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        check(in.available() == data.length, "large buffer available");
        byte[] part = new byte[70000];
        check(in.read(part, 0, part.length) == part.length, "large single read");
        check(Arrays.equals(part, 0, part.length, data, 0, part.length), "large single read content");
        check(in.skip(1) == 1, "skip within large buffer");
        check(in.read() == (data[70001] & 0xFF), "read after skip at exact index");
        check(in.available() == data.length - 70002, "remaining after partial consumption");
    }

    // ---- 10. readAllBytes full contract ----
    static void testReadAllBytesContract() {
        check(new ByteArrayInputStream(new byte[0]).readAllBytes().length == 0,
              "readAllBytes on empty stream -> empty array");

        byte[] data = new byte[5000];
        for (int i = 0; i < data.length; i++) data[i] = (byte) (i * 3);
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        check(Arrays.equals(in.readAllBytes(), data), "readAllBytes content");
        check(in.read() == -1, "readAllBytes advances position to EOF");
        check(in.readAllBytes().length == 0, "second readAllBytes -> empty");
        check(in.available() == 0, "available 0 after readAllBytes");

        // mark survives readAllBytes (mark is untouched): reset replays
        ByteArrayInputStream m = new ByteArrayInputStream(new byte[]{1, 2, 3});
        m.mark(10);
        check(m.readAllBytes().length == 3, "readAllBytes with mark set");
        m.reset();
        check(m.read() == 1, "reset after readAllBytes replays (mark preserved)");

        // on a partially consumed stream only the remainder is returned
        ByteArrayInputStream p = new ByteArrayInputStream(new byte[]{1, 2, 3, 4});
        p.read();
        check(Arrays.equals(p.readAllBytes(), new byte[]{2, 3, 4}),
              "readAllBytes returns only the remainder");
    }

    // ---- 11. readNBytes(byte[],int,int) full contract (BAIS override: single read step) ----
    static void testReadNBytesArray() {
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[]{1, 2, 3, 4, 5});
        byte[] b = new byte[6];
        check(in.readNBytes(b, 1, 3) == 3, "readNBytes count");
        check(b[0] == 0 && b[1] == 1 && b[2] == 2 && b[3] == 3 && b[4] == 0,
              "readNBytes placement at off, beyond untouched");
        byte[] room = new byte[10];
        check(in.readNBytes(room, 0, 10) == 2 && room[0] == 4 && room[1] == 5,
              "readNBytes returns fewer bytes at end of stream");

        // at EOF the BAIS override maps the -1 of its single read to 0
        ByteArrayInputStream eof = new ByteArrayInputStream(new byte[0]);
        check(eof.readNBytes(new byte[1], 0, 0) == 0, "readNBytes len==0 at EOF -> 0");
        check(eof.readNBytes(new byte[1], 0, 1) == 0, "readNBytes len>0 at EOF -> 0");

        checkThrows(NullPointerException.class, () -> new ByteArrayInputStream(new byte[1]).readNBytes(null, 0, 1),
                    "readNBytes(null,0,1) -> NPE");
        checkThrows(IndexOutOfBoundsException.class,
                    () -> new ByteArrayInputStream(new byte[1]).readNBytes(new byte[1], 0, 2),
                    "readNBytes(b,0,2) too long -> IndexOutOfBoundsException");
    }

    // ---- 12. readNBytes(int) (inherited from InputStream, exact count semantics) ----
    static void testReadNBytesCount() throws Exception {
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[]{1, 2, 3, 4});
        byte[] three = in.readNBytes(3);
        check(three.length == 3 && three[0] == 1 && three[2] == 3, "readNBytes(3) exact");
        check(in.read() == 4, "stream continues after readNBytes(3)");
        check(in.readNBytes(0).length == 0, "readNBytes(0) -> empty array");

        ByteArrayInputStream part = new ByteArrayInputStream(new byte[]{7, 8});
        byte[] rest = part.readNBytes(10);
        check(rest.length == 2 && rest[0] == 7 && rest[1] == 8, "readNBytes(n) over end -> remaining");

        ByteArrayInputStream drained = new ByteArrayInputStream(new byte[]{1});
        drained.read();
        check(drained.readNBytes(5).length == 0, "readNBytes(5) on drained stream -> empty");
    }

    // ---- 13. transferTo full contract ----
    static void testTransferToContract() throws Exception {
        byte[] data = new byte[3000];
        for (int i = 0; i < data.length; i++) data[i] = (byte) (i * 11);
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        check(in.transferTo(out) == data.length, "transferTo returns byte count");
        check(Arrays.equals(out.toByteArray(), data), "transferTo content");
        check(in.read() == -1, "transferTo leaves stream at EOF");

        // only the remainder is transferred from a partially consumed stream
        ByteArrayInputStream p = new ByteArrayInputStream(new byte[]{1, 2, 3, 4});
        p.read(); p.read();
        ByteArrayOutputStream po = new ByteArrayOutputStream();
        check(p.transferTo(po) == 2 && Arrays.equals(po.toByteArray(), new byte[]{3, 4}),
              "transferTo remainder");

        check(new ByteArrayInputStream(new byte[0]).transferTo(new ByteArrayOutputStream()) == 0,
              "transferTo on empty stream -> 0");

        checkThrows(NullPointerException.class,
                    () -> new ByteArrayInputStream(new byte[]{1}).transferTo(null),
                    "transferTo(null) -> NPE");
        checkThrows(IOException.class,
                    () -> new ByteArrayInputStream(new byte[]{1, 2}).transferTo(
                        new OutputStream() {
                            public void write(int b) throws IOException { throw new IOException("sink boom"); }
                        }),
                    "transferTo propagates sink IOException");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testConstructors();
        testReadSingleByte();
        testReadArrayValidation();
        testReadArrayContract();
        testSkip();
        testAvailable();
        testMarkReset();
        testCloseNoOp();
        testJdk9Plus();
        testConstructorCorners();
        testReadBoundary();
        testSkipMarkInteraction();
        testCloseMarkReset();
        testLargeBuffer();
        testReadAllBytesContract();
        testReadNBytesArray();
        testReadNBytesCount();
        testTransferToContract();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
