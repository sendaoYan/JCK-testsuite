/*
 * @test
 * @summary javax.sound.sampled.CompoundControl conformance: white-box structure + black-box behavior.
 * @run main CompoundControlTest
 */

import java.lang.reflect.Modifier;
import javax.sound.sampled.CompoundControl;
import javax.sound.sampled.Control;

public class CompoundControlTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.sound.sampled.CompoundControl");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(Modifier.isAbstract(mods), "abstract modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.sound.sampled.Control"), "superclass Control");
        check(c.getDeclaredConstructors().length == 1, "constructor count 1");

        Class<?> t = Class.forName("javax.sound.sampled.CompoundControl$Type");
        check(t != null && t.getEnclosingClass() == c, "Type nested");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Control gain = new Control(new Control.Type("Gain") {}) {};
        Control pan  = new Control(new Control.Type("Pan") {}) {};
        Control[] members = { gain, pan };

        CompoundControl.Type type = new CompoundControl.Type("EQ") {};
        CompoundControl cc = new CompoundControl(type, members) {};

        check(cc.getType() == type, "getType identity");
        check("EQ".equals(cc.getType().toString()), "type name");

        Control[] got = cc.getMemberControls();
        check(got.length == 2, "member count 2");
        check(got[0] == gain && got[1] == pan, "member identity preserved");
        check(got != members, "getMemberControls returns a clone");

        // mutating the returned array must not affect the control
        got[0] = null;
        Control[] again = cc.getMemberControls();
        check(again.length == 2, "member count unchanged after external mutation");
        check(again[0] == gain, "original member still present");

        check(cc.toString().contains("Gain") && cc.toString().contains("Pan"), "toString lists member types");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: recording subclass + member array semantics ----
    static void testContractDeep() throws Exception {
        javax.sound.sampled.Control[] members = { new RecBoolSub() };
        javax.sound.sampled.CompoundControl cc = new RecCompound();
        check(cc.getMemberControls().length == 1, "one member control");
        check(cc.toString().contains("Deep"), "toString contains name");
        check(java.lang.reflect.Modifier.isProtected(javax.sound.sampled.CompoundControl.Type.class.getDeclaredConstructors()[0].getModifiers()), "Type ctor protected");
        javax.sound.sampled.CompoundControl empty = new javax.sound.sampled.CompoundControl(new javax.sound.sampled.CompoundControl.Type("E") {}, new javax.sound.sampled.Control[0]) {};
        check(empty.getMemberControls().length == 0, "empty members");
        javax.sound.sampled.CompoundControl multi = new javax.sound.sampled.CompoundControl(new javax.sound.sampled.CompoundControl.Type("M") {}, members) {};
        check("Deep".equals(cc.getType().toString()), "type name");
    }
    static class RecCompound extends javax.sound.sampled.CompoundControl {
        RecCompound() { super(new Type("Deep") {}, new javax.sound.sampled.Control[] { new RecBoolSub() }); }
    }
    static class RecBoolSub extends javax.sound.sampled.BooleanControl {
        RecBoolSub() { super(new Type("B1") {}, true); }
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
