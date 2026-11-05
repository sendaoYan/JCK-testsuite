/*
 * @test
 * @summary javax.sound.sampled.FloatControl conformance: white-box structure + black-box behavior.
 * @run main FloatControlTest
 */

import java.lang.reflect.Modifier;
import javax.sound.sampled.FloatControl;

public class FloatControlTest {
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
        Class<?> c = Class.forName("javax.sound.sampled.FloatControl");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(Modifier.isAbstract(mods), "abstract modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.sound.sampled.Control"), "superclass Control");
        check(c.getDeclaredConstructors().length == 2, "constructor count 2");

        Class<?> t = Class.forName("javax.sound.sampled.FloatControl$Type");
        check(t != null && t.getEnclosingClass() == c, "Type nested");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        FloatControl fc = new FloatControl(FloatControl.Type.MASTER_GAIN,
                -80f, 6f, 0.1f, 1000, 0f, "dB", "Min", "Mid", "Max") {};

        check(fc.getType() == FloatControl.Type.MASTER_GAIN, "type MASTER_GAIN");
        check(fc.getMinimum() == -80f, "minimum");
        check(fc.getMaximum() == 6f, "maximum");
        check(fc.getPrecision() == 0.1f, "precision");
        check(fc.getUpdatePeriod() == 1000, "updatePeriod");
        check("dB".equals(fc.getUnits()), "units");
        check("Min".equals(fc.getMinLabel()), "minLabel");
        check("Mid".equals(fc.getMidLabel()), "midLabel");
        check("Max".equals(fc.getMaxLabel()), "maxLabel");
        check(fc.getValue() == 0f, "initial value");

        fc.setValue(3.5f);
        check(fc.getValue() == 3.5f, "setValue round-trip");
        fc.setValue(-80f);
        check(fc.getValue() == -80f, "setValue at minimum");
        fc.setValue(6f);
        check(fc.getValue() == 6f, "setValue at maximum");

        expectThrows(IllegalArgumentException.class, () -> fc.setValue(6.1f), "setValue above maximum");
        expectThrows(IllegalArgumentException.class, () -> fc.setValue(-80.1f), "setValue below minimum");

        // shift validates 'from' and applies 'to' via setValue
        fc.shift(0f, -20f, 5000);
        check(fc.getValue() == -20f, "shift sets value to 'to'");
        expectThrows(IllegalArgumentException.class, () -> fc.shift(-200f, 0f, 1000), "shift 'from' below minimum");
        expectThrows(IllegalArgumentException.class, () -> fc.shift(0f, 200f, 1000), "shift 'to' above maximum");

        // 6-arg constructor gives empty min/mid/max labels
        FloatControl fc2 = new FloatControl(FloatControl.Type.VOLUME, 0f, 1f, 0.01f, -1, 0.5f, "") {};
        check("".equals(fc2.getMinLabel()), "empty minLabel");
        check("".equals(fc2.getMidLabel()), "empty midLabel");
        check("".equals(fc2.getMaxLabel()), "empty maxLabel");
        check(fc2.getUpdatePeriod() == -1, "updatePeriod -1 (no shifting)");
        check(fc2.getValue() == 0.5f, "initial value 0.5");

        check(fc.toString().contains("dB"), "toString has units");

        // constructor validation: minimum > maximum
        expectThrows(IllegalArgumentException.class,
                () -> new FloatControl(FloatControl.Type.PAN, 10f, -10f, 1f, 100, 0f, "u") {},
                "min > max rejected");
        // constructor validation: initial value out of range
        expectThrows(IllegalArgumentException.class,
                () -> new FloatControl(FloatControl.Type.PAN, -1f, 1f, 0.1f, 100, 2f, "u") {},
                "initial value above range rejected");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: real MASTER_GAIN clamping matrix + exact IAE messages ----
    static void testContractDeep() throws Exception {
        javax.sound.sampled.Clip clip = (javax.sound.sampled.Clip) javax.sound.sampled.AudioSystem.getLine(new javax.sound.sampled.DataLine.Info(javax.sound.sampled.Clip.class, new javax.sound.sampled.AudioFormat(8000f, 8, 1, true, false)));
        clip.open(new javax.sound.sampled.AudioInputStream(new java.io.ByteArrayInputStream(new byte[] { 16, -16 }), new javax.sound.sampled.AudioFormat(8000f, 8, 1, true, false), 2));
        javax.sound.sampled.FloatControl gain = (javax.sound.sampled.FloatControl) clip.getControl(javax.sound.sampled.FloatControl.Type.MASTER_GAIN);
        check(gain.getMinimum() == -80.0f, "min -80.0 dB");
        check(gain.getMaximum() == 6.0206f, "max 6.0206 dB");
        check(gain.getPrecision() == 0.625f, "precision 0.625");
        check("dB".equals(gain.getUnits()), "unit dB");
        check(gain.getValue() == 0.0f, "default 0.0");
        try { gain.setValue(gain.getMaximum() + 100f); check(false, "over max must fail"); }
        catch (IllegalArgumentException e) { check("Requested value 106.0206 exceeds allowable maximum value 6.0206.".equals(e.getMessage()), "over-max message [" + e.getMessage() + "]"); }
        try { gain.setValue(gain.getMinimum() - 100f); check(false, "under min must fail"); }
        catch (IllegalArgumentException e) { check("Requested value -180.0 smaller than allowable minimum value -80.0.".equals(e.getMessage()), "under-min message [" + e.getMessage() + "]"); }
        gain.setValue(-6.0f);
        check(gain.getValue() == -6.0000005f, "quantized to precision [-6.0000005]");
        check(gain.toString().startsWith("Master Gain"), "toString label");
        check(javax.sound.sampled.FloatControl.Type.MASTER_GAIN.toString().equals("Master Gain"), "type name");
        // Type constants table
        check("Master Gain".equals(javax.sound.sampled.FloatControl.Type.MASTER_GAIN.toString()), "MASTER_GAIN");
        check("AUX Send".equals(javax.sound.sampled.FloatControl.Type.AUX_SEND.toString()), "AUX Send");
        check("AUX Return".equals(javax.sound.sampled.FloatControl.Type.AUX_RETURN.toString()), "AUX Return");
        check("Reverb Send".equals(javax.sound.sampled.FloatControl.Type.REVERB_SEND.toString()), "Reverb Send");
        check("Reverb Return".equals(javax.sound.sampled.FloatControl.Type.REVERB_RETURN.toString()), "Reverb Return");
        check("Volume".equals(javax.sound.sampled.FloatControl.Type.VOLUME.toString()), "VOLUME");
        check("Pan".equals(javax.sound.sampled.FloatControl.Type.PAN.toString()), "PAN");
        check("Balance".equals(javax.sound.sampled.FloatControl.Type.BALANCE.toString()), "BALANCE");
        check("Sample Rate".equals(javax.sound.sampled.FloatControl.Type.SAMPLE_RATE.toString()), "SAMPLE_RATE");
        clip.close();
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
