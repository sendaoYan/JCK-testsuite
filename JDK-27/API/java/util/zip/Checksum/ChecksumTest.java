/*
 * @test
 * @summary java.util.zip.Checksum conformance: interface shape + default method behavior.
 * @run main ChecksumTest
 */

import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.util.zip.Checksum;

public class ChecksumTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static Class<?> clazz(String n) {
        try { return Class.forName(n); } catch (ClassNotFoundException e) { throw new AssertionError(e); }
    }

    // Implementation of only the abstract methods; default methods are exercised
    // against this to verify their spec-mandated delegation.
    static class Impl implements Checksum {
        long value;
        int intCalls, byteArrayCalls, resets;

        public void update(int b) { intCalls++; value = value * 31 + (b & 0xff); }
        public void update(byte[] b, int off, int len) { byteArrayCalls++; for (int i = off; i < off + len; i++) update(b[i]); }
        public long getValue() { return value; }
        public void reset() { resets++; value = 0; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = clazz("java.util.zip.Checksum");
        check(c.isInterface(), "is an interface");
        check(Modifier.isPublic(c.getModifiers()), "public interface");
        check(Modifier.isAbstract(c.getMethod("update", int.class).getModifiers()), "update(int) abstract");
        check(Modifier.isAbstract(c.getMethod("update", byte[].class, int.class, int.class).getModifiers()), "update(byte[],int,int) abstract");
        check(Modifier.isAbstract(c.getMethod("getValue").getModifiers()), "getValue abstract");
        check(Modifier.isAbstract(c.getMethod("reset").getModifiers()), "reset abstract");
        check(c.getMethod("update", byte[].class).isDefault(), "update(byte[]) default");
        check(c.getMethod("update", ByteBuffer.class).isDefault(), "update(ByteBuffer) default");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        byte[] data = { 1, 2, 3 };

        Impl c = new Impl();
        check(c.getValue() == 0, "initial value 0");
        c.update(65);
        check(c.intCalls == 1, "update(int) invoked");
        check(c.getValue() == 65, "update(int) value");
        c.update(data, 0, data.length);
        check(c.byteArrayCalls == 1, "update(byte[],off,len) invoked");
        check(c.getValue() != 0, "value accumulated");

        Impl d = new Impl();
        d.update(data); // default method must delegate to update(byte[],0,len)
        check(d.byteArrayCalls == 1, "default update(byte[]) delegates to update(byte[],off,len)");
        check(d.intCalls == 3, "default update(byte[]) updates each byte");

        Impl e = new Impl();
        ByteBuffer buf = ByteBuffer.wrap(data);
        e.update(buf); // default method must consume remaining bytes via update(int)
        check(e.intCalls == 3, "default update(ByteBuffer) uses update(int) per byte");
        check(buf.position() == buf.limit(), "default update(ByteBuffer) advances position to limit");

        Impl f = new Impl();
        f.update(1);
        f.reset();
        check(f.resets == 1 && f.getValue() == 0, "reset zeroes value");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
