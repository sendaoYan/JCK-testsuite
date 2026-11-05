/*
 * @test
 * @summary javax.sound.sampled.FloatControl conformance: abstract hierarchy and methods.
 * @run main FloatControlTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class FloatControlTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("javax.sound.sampled.FloatControl");
        check(!c.isInterface(), "not interface");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("javax.sound.sampled.Control"), "extends javax.sound.sampled.Control");
        Set<String> dm = new HashSet<>(); for (Method m : c.getDeclaredMethods()) dm.add(m.getName());
        check(dm.contains("setValue"), "method setValue");
        check(dm.contains("getValue"), "method getValue");
        check(dm.contains("getMaximum"), "method getMaximum");
        check(dm.contains("getMinimum"), "method getMinimum");
        check(dm.contains("getUnits"), "method getUnits");
        check(dm.contains("getMinLabel"), "method getMinLabel");
        check(dm.contains("getMidLabel"), "method getMidLabel");
        check(dm.contains("getMaxLabel"), "method getMaxLabel");
        check(dm.contains("getPrecision"), "method getPrecision");
        check(dm.contains("getUpdatePeriod"), "method getUpdatePeriod");
        check(dm.contains("shift"), "method shift");
        check(dm.contains("toString"), "method toString");
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
