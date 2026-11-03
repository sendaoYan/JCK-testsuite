/*
 * @test
 * @summary java.util.zip.Adler32 conformance: white-box structure + black-box checksum behavior.
 * @run main Adler32Test
 */

import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.util.zip.Adler32;
import java.util.zip.Checksum;

public class Adler32Test {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static Class<?> clazz(String n) {
        try { return Class.forName(n); } catch (ClassNotFoundException e) { throw new AssertionError(e); }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = clazz("java.util.zip.Adler32");
        check(Modifier.isPublic(c.getModifiers()), "public class");
        check(!c.isInterface() && !c.isEnum() && !Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(!Modifier.isFinal(c.getModifiers()), "not final");
        check(c.getSuperclass() == Object.class, "extends Object");
        check(Checksum.class.isAssignableFrom(c), "implements Checksum");

        c.getMethod("update", int.class);
        c.getMethod("update", byte[].class, int.class, int.class);
        c.getMethod("update", ByteBuffer.class);
        c.getMethod("reset");
        c.getMethod("getValue");
        check(true, "update/reset/getValue methods present");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        byte[] nine = "123456789".getBytes("UTF-8");
        byte[] wiki = "Wikipedia".getBytes("UTF-8");

        Adler32 a = new Adler32();
        check(a.getValue() == 1L, "initial value 1");
        a.update(nine, 0, nine.length);
        check(a.getValue() == 0x091E01DEL, "adler32('123456789') == 0x091E01DE");
        a.reset();
        check(a.getValue() == 1L, "reset -> 1");

        Adler32 b = new Adler32();
        for (byte x : nine) b.update(x);
        check(b.getValue() == 0x091E01DEL, "update(int) byte-wise");

        Adler32 c = new Adler32();
        c.update(ByteBuffer.wrap(nine));
        check(c.getValue() == 0x091E01DEL, "update(ByteBuffer)");

        Adler32 d = new Adler32();
        d.update(wiki, 0, wiki.length);
        check(d.getValue() == 0x11E60398L, "adler32('Wikipedia') == 0x11E60398");

        Adler32 e = new Adler32();
        e.update(new byte[0], 0, 0);
        check(e.getValue() == 1L, "empty input leaves 1");

        Adler32 f = new Adler32();
        f.update(nine, 0, 4);
        f.update(nine, 4, nine.length - 4);
        check(f.getValue() == 0x091E01DEL, "incremental update equals whole");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
