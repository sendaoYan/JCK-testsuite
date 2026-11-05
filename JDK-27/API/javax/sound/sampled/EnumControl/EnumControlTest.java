/*
 * @test
 * @summary javax.sound.sampled.EnumControl conformance: white-box structure + black-box behavior.
 * @run main EnumControlTest
 */

import java.lang.reflect.Modifier;
import javax.sound.sampled.EnumControl;

public class EnumControlTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    interface ThrowingRunnable { void run() throws Exception; }
    static void expectThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        boolean thrown = false;
        try { r.run(); } catch (Throwable t) {
            thrown = true;
            check(expected.isAssignableFrom(t.getClass()),
                  msg + " expected " + expected.getSimpleName() + " but got " + t.getClass().getSimpleName());
        }
        check(thrown, msg + " should throw " + expected.getSimpleName());
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.sound.sampled.EnumControl");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(Modifier.isAbstract(mods), "abstract modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.sound.sampled.Control"), "superclass Control");
        check(c.getDeclaredConstructors().length == 1, "constructor count 1");

        Class<?> t = Class.forName("javax.sound.sampled.EnumControl$Type");
        check(t != null && t.getEnclosingClass() == c, "Type nested");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Object[] values = { "small hall", "large hall", "cathedral" };
        EnumControl ec = new EnumControl(EnumControl.Type.REVERB, values, "large hall") {};

        check(ec.getType() == EnumControl.Type.REVERB, "type REVERB");
        check("large hall".equals(ec.getValue()), "initial value");

        Object[] got = ec.getValues();
        check(got.length == 3, "values length 3");
        check(got != values, "getValues returns a clone");
        check("small hall".equals(got[0]) && "large hall".equals(got[1]) && "cathedral".equals(got[2]), "values order preserved");

        ec.setValue("cathedral");
        check("cathedral".equals(ec.getValue()), "setValue round-trip");
        ec.setValue("small hall");
        check("small hall".equals(ec.getValue()), "setValue back");

        check(ec.toString().contains("small hall"), "toString has current value");

        expectThrows(IllegalArgumentException.class, () -> ec.setValue("nope"), "setValue unsupported");
        check("small hall".equals(ec.getValue()), "value unchanged after failed set");

        // mutating the returned array must not affect the control
        got = ec.getValues();
        got[0] = "corrupted";
        check("small hall".equals(ec.getValues()[0]), "internal values protected");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: values array / get-set / protected Type ----
    static void testContractDeep() throws Exception {
        Object[] vals = { "A", "B" };
        javax.sound.sampled.EnumControl ec = new RecEnum();
        check(java.util.Arrays.asList(ec.getValues()).toString().equals("[A, B]"), "values list");
        check("A".equals(ec.getValue()), "current value");
        ec.setValue("B");
        check("B".equals(ec.getValue()), "after setValue");
        check(java.lang.reflect.Modifier.isProtected(javax.sound.sampled.EnumControl.Type.class.getDeclaredConstructors()[0].getModifiers()), "Type ctor protected");
        Object[] got = ec.getValues();
        got[0] = "X";
        check("A".equals(ec.getValues()[0]), "getValues defensively copied");
        try { ec.setValue("Z"); check(false, "setValue outside values must fail"); }
        catch (IllegalArgumentException e) { passed++; }
        check(ec.toString().contains("Deep"), "toString contains type");
    }
    static class RecEnum extends javax.sound.sampled.EnumControl {
        RecEnum() { super(new Type("Deep") {}, new Object[] { "A", "B" }, "A"); }
    }

    static Object serRoundTrip(Object o) throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        new java.io.ObjectOutputStream(bos).writeObject(o);
        return new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
    }
    // hand-built WAV: RIFF/WAVE, PCM 8000Hz 8-bit mono, samples {16,-16}, 44-byte header
    static byte[] wav8() throws java.io.IOException {
        java.io.ByteArrayOutputStream w = new java.io.ByteArrayOutputStream();
        byte[] pcm = { 0x10, (byte) 0xF0 };
        w.write('R'); w.write('I'); w.write('F'); w.write('F');
        int riffLen = 36 + pcm.length;
        w.write(riffLen & 0xFF); w.write((riffLen >> 8) & 0xFF); w.write((riffLen >> 16) & 0xFF); w.write((riffLen >> 24) & 0xFF);
        w.write('W'); w.write('A'); w.write('V'); w.write('E');
        w.write('f'); w.write('m'); w.write('t'); w.write(' ');
        w.write(16); w.write(0); w.write(0); w.write(0);
        w.write(1); w.write(0); w.write(1); w.write(0);
        int rate = 8000; w.write(rate & 0xFF); w.write((rate >> 8) & 0xFF); w.write((rate >> 16) & 0xFF); w.write((rate >> 24) & 0xFF);
        w.write(rate & 0xFF); w.write((rate >> 8) & 0xFF); w.write((rate >> 16) & 0xFF); w.write((rate >> 24) & 0xFF);
        w.write(1); w.write(0); w.write(8); w.write(0);
        w.write('d'); w.write('a'); w.write('t'); w.write('a');
        w.write(pcm.length & 0xFF); w.write(0); w.write(0); w.write(0);
        w.write(pcm);
        return w.toByteArray();
    }
    // hand-built AU (.snd, big-endian header): encoding 1 = 8-bit mu-law, 8000Hz mono, 3 data bytes
    static byte[] au(int sizeField, int enc, int rate, int ch, byte[] data) throws java.io.IOException {
        java.io.ByteArrayOutputStream u = new java.io.ByteArrayOutputStream();
        u.write('.'); u.write('s'); u.write('n'); u.write('d');
        u.write((sizeField >> 24) & 0xFF); u.write((sizeField >> 16) & 0xFF); u.write((sizeField >> 8) & 0xFF); u.write(sizeField & 0xFF);
        u.write(0); u.write(0); u.write(0); u.write(24);
        u.write((enc >> 24) & 0xFF); u.write((enc >> 16) & 0xFF); u.write((enc >> 8) & 0xFF); u.write(enc & 0xFF);
        u.write((rate >> 24) & 0xFF); u.write((rate >> 16) & 0xFF); u.write((rate >> 8) & 0xFF); u.write(rate & 0xFF);
        u.write((ch >> 24) & 0xFF); u.write((ch >> 16) & 0xFF); u.write((ch >> 8) & 0xFF); u.write(ch & 0xFF);
        u.write(data);
        return u.toByteArray();
    }
    static byte[] auUlaw() throws java.io.IOException { return au(27, 1, 8000, 1, new byte[] { 0, (byte) 0xFF, 0x7F }); }

}
