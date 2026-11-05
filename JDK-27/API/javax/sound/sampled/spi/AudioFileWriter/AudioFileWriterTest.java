/*
 * @test
 * @summary javax.sound.sampled.spi.AudioFileWriter conformance: white-box structure + black-box behavior.
 * @run main AudioFileWriterTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.lang.reflect.Modifier;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

public class AudioFileWriterTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.sound.sampled.spi.AudioFileWriter");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isAbstract(mods) == true, "abstract modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "extends Object");
        java.lang.reflect.Constructor<?> ctor = c.getDeclaredConstructor();
        check(Modifier.isProtected(ctor.getModifiers()), "protected constructor");
        check(Modifier.isAbstract(c.getMethod("getAudioFileTypes").getModifiers()), "getAudioFileTypes() abstract");
        check(Modifier.isAbstract(c.getMethod("getAudioFileTypes", AudioInputStream.class).getModifiers()), "getAudioFileTypes(stream) abstract");
        check(Modifier.isAbstract(c.getMethod("write", AudioInputStream.class, AudioFileFormat.Type.class, OutputStream.class).getModifiers()), "write(OutputStream) abstract");
        check(Modifier.isAbstract(c.getMethod("write", AudioInputStream.class, AudioFileFormat.Type.class, File.class).getModifiers()), "write(File) abstract");
        check(!Modifier.isAbstract(c.getMethod("isFileTypeSupported", AudioFileFormat.Type.class).getModifiers()), "isFileTypeSupported(Type) concrete");
        check(!Modifier.isAbstract(c.getMethod("isFileTypeSupported", AudioFileFormat.Type.class, AudioInputStream.class).getModifiers()), "isFileTypeSupported(Type,stream) concrete");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        AudioFormat fmt = new AudioFormat(8000f, 8, 1, true, false);
        AudioInputStream ais = new AudioInputStream(new ByteArrayInputStream(new byte[800]), fmt, 800);

        // Concrete convenience methods on a trivial anonymous subclass.
        AudioFileWriterImpl w = new AudioFileWriterImpl();
        check(!w.isFileTypeSupported(AudioFileFormat.Type.WAVE), "empty writer: WAVE not supported");
        check(!w.isFileTypeSupported(AudioFileFormat.Type.WAVE, ais), "empty writer: WAVE not supported (stream)");

        // Real writers via AudioSystem.
        AudioFileFormat.Type[] types = AudioSystem.getAudioFileTypes();
        check(types != null && types.length > 0, "getAudioFileTypes non-empty");
        check(AudioSystem.isFileTypeSupported(AudioFileFormat.Type.WAVE), "WAVE supported");
        check(AudioSystem.isFileTypeSupported(AudioFileFormat.Type.WAVE, ais), "WAVE supported (stream)");

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        int written = AudioSystem.write(ais, AudioFileFormat.Type.WAVE, bos);
        check(written > 0, "write WAV bytes");
        check(bos.size() == written, "byte count matches written");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: abstract SPI surface + recording subclass closed loop ----
    static void testContractDeep() throws Exception {
        check(java.lang.reflect.Modifier.isAbstract(javax.sound.sampled.spi.AudioFileWriter.class.getModifiers()), "abstract");
        check(java.lang.reflect.Modifier.isPublic(javax.sound.sampled.spi.AudioFileWriter.class.getModifiers()), "public");
        
        javax.sound.sampled.AudioFormat fmt = new javax.sound.sampled.AudioFormat(8000f, 8, 1, true, false);
        javax.sound.sampled.AudioInputStream ais = new javax.sound.sampled.AudioInputStream(new java.io.ByteArrayInputStream(new byte[] { 16, -16 }), fmt, 2);
        RecWriter w = new RecWriter();
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        int n = w.write(ais, javax.sound.sampled.AudioFileFormat.Type.WAVE, bos);
        check(n == 46 && bos.size() == 46, "recording writer emits 46-byte WAV");
        javax.sound.sampled.AudioFileFormat.Type[] ts = w.getAudioFileTypes();
        check(ts.length == 3, "recording writer types (WAVE/AU/AIFF)");
        check(w.isFileTypeSupported(javax.sound.sampled.AudioFileFormat.Type.WAVE), "supports WAVE");
        check(!w.isFileTypeSupported(new javax.sound.sampled.AudioFileFormat.Type("Nope", "nope") {}), "bogus type false");
        javax.sound.sampled.AudioFileFormat.Type[] forStream = w.getAudioFileTypes(ais);
        check(forStream.length == 3, "types for concrete stream");
    }
    static class RecWriter extends javax.sound.sampled.spi.AudioFileWriter {
        public javax.sound.sampled.AudioFileFormat.Type[] getAudioFileTypes() { return javax.sound.sampled.AudioSystem.getAudioFileTypes(); }
        public javax.sound.sampled.AudioFileFormat.Type[] getAudioFileTypes(javax.sound.sampled.AudioInputStream ais) { return javax.sound.sampled.AudioSystem.getAudioFileTypes(ais); }
        public boolean isFileTypeSupported(javax.sound.sampled.AudioFileFormat.Type t) { for (javax.sound.sampled.AudioFileFormat.Type x : getAudioFileTypes()) if (x.equals(t)) return true; return false; }
        public boolean isFileTypeSupported(javax.sound.sampled.AudioFileFormat.Type t, javax.sound.sampled.AudioInputStream ais) { for (javax.sound.sampled.AudioFileFormat.Type x : getAudioFileTypes(ais)) if (x.equals(t)) return true; return false; }
        public int write(javax.sound.sampled.AudioInputStream in, javax.sound.sampled.AudioFileFormat.Type t, java.io.OutputStream out) throws java.io.IOException { return javax.sound.sampled.AudioSystem.write(in, t, out); }
        public int write(javax.sound.sampled.AudioInputStream in, javax.sound.sampled.AudioFileFormat.Type t, java.io.File out) throws java.io.IOException { return 0; }
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

class AudioFileWriterImpl extends javax.sound.sampled.spi.AudioFileWriter {
    public AudioFileFormat.Type[] getAudioFileTypes() { return new AudioFileFormat.Type[0]; }
    public AudioFileFormat.Type[] getAudioFileTypes(AudioInputStream s) { return new AudioFileFormat.Type[0]; }
    public int write(AudioInputStream s, AudioFileFormat.Type t, OutputStream out) { return 0; }
    public int write(AudioInputStream s, AudioFileFormat.Type t, File f) { return 0; }
}
