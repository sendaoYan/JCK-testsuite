/*
 * @test
 * @key sound
 * @summary javax.sound.sampled.BooleanControl conformance: white-box structure + black-box behavior.
 * @run main BooleanControlTest
 */

import java.lang.reflect.Modifier;
import javax.sound.sampled.BooleanControl;

public class BooleanControlTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.sound.sampled.BooleanControl");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(Modifier.isAbstract(mods), "abstract modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.sound.sampled.Control"), "superclass Control");
        check(c.getDeclaredConstructors().length == 2, "constructor count 2");

        Class<?> t = Class.forName("javax.sound.sampled.BooleanControl$Type");
        check(t != null && t.getEnclosingClass() == c, "Type nested");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // labeled constructor
        BooleanControl bc = new BooleanControl(BooleanControl.Type.MUTE, true, "on", "off") {};
        check(bc.getType() == BooleanControl.Type.MUTE, "type MUTE");
        check(bc.getValue(), "initial value true");
        check("on".equals(bc.getStateLabel(true)), "true label");
        check("off".equals(bc.getStateLabel(false)), "false label");
        bc.setValue(false);
        check(!bc.getValue(), "setValue false");
        check(bc.toString().contains("off"), "toString reflects false label");
        bc.setValue(true);
        check(bc.getValue(), "setValue true");

        // default labels constructor
        BooleanControl bc2 = new BooleanControl(BooleanControl.Type.APPLY_REVERB, false) {};
        check(bc2.getType() == BooleanControl.Type.APPLY_REVERB, "type APPLY_REVERB");
        check(!bc2.getValue(), "initial value false");
        check("true".equals(bc2.getStateLabel(true)), "default true label");
        check("false".equals(bc2.getStateLabel(false)), "default false label");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: recording subclass + real MUTE control ----
    static void testContractDeep() throws Exception {
        javax.sound.sampled.BooleanControl bc = new RecBool();
        check(bc.getValue(), "ctor value true");
        bc.setValue(false);
        check(!bc.getValue(), "setValue false");
        check("true".equals(bc.getStateLabel(true)) && "false".equals(bc.getStateLabel(false)), "default state labels true/false");
        javax.sound.sampled.BooleanControl custom = new RecBoolCustom();
        check("YES".equals(custom.getStateLabel(true)) && "NO".equals(custom.getStateLabel(false)), "custom state labels");
        check(java.lang.reflect.Modifier.isProtected(javax.sound.sampled.BooleanControl.Type.class.getDeclaredConstructors()[0].getModifiers()), "Type ctor protected");
        // real MUTE on software clip
        javax.sound.sampled.Clip clip = (javax.sound.sampled.Clip) javax.sound.sampled.AudioSystem.getLine(new javax.sound.sampled.DataLine.Info(javax.sound.sampled.Clip.class, new javax.sound.sampled.AudioFormat(8000f, 8, 1, true, false)));
        clip.open(new javax.sound.sampled.AudioInputStream(new java.io.ByteArrayInputStream(new byte[] { 16, -16 }), new javax.sound.sampled.AudioFormat(8000f, 8, 1, true, false), 2));
        javax.sound.sampled.BooleanControl mute = (javax.sound.sampled.BooleanControl) clip.getControl(javax.sound.sampled.BooleanControl.Type.MUTE);
        check(!mute.getValue(), "MUTE default false");
        mute.setValue(true);
        check(mute.getValue(), "MUTE round-trip");
        check("Mute".equals(mute.getType().toString()), "type name Mute");
        clip.close();
    }
    static class RecBool extends javax.sound.sampled.BooleanControl {
        RecBool() { super(new Type("Deep") {}, true); }
    }
    static class RecBoolCustom extends javax.sound.sampled.BooleanControl {
        RecBoolCustom() { super(new Type("DeepC") {}, false, "YES", "NO"); }
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
