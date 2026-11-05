/*
 * @test
 * @summary javax.sound.sampled.spi.AudioFileReader conformance: abstract hierarchy and methods.
 * @run main AudioFileReaderTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class AudioFileReaderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("javax.sound.sampled.spi.AudioFileReader");
        check(!c.isInterface(), "not interface");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "extends Object");
        Set<String> dm = new HashSet<>(); for (Method m : c.getDeclaredMethods()) dm.add(m.getName());
        check(dm.contains("getAudioFileFormat"), "method getAudioFileFormat");
        check(dm.contains("getAudioFileFormat"), "method getAudioFileFormat");
        check(dm.contains("getAudioFileFormat"), "method getAudioFileFormat");
        check(dm.contains("getAudioInputStream"), "method getAudioInputStream");
        check(dm.contains("getAudioInputStream"), "method getAudioInputStream");
        check(dm.contains("getAudioInputStream"), "method getAudioInputStream");
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: abstract SPI surface + recording subclass closed loop ----
    static void testContractDeep() throws Exception {
        check(java.lang.reflect.Modifier.isAbstract(javax.sound.sampled.spi.AudioFileReader.class.getModifiers()), "abstract");
        check(java.lang.reflect.Modifier.isPublic(javax.sound.sampled.spi.AudioFileReader.class.getModifiers()), "public");
        
        RecReader r = new RecReader();
        javax.sound.sampled.AudioFileFormat aff = r.getAudioFileFormat(new java.io.ByteArrayInputStream(wav8()));
        check(aff.getType() == javax.sound.sampled.AudioFileFormat.Type.WAVE && aff.getFrameLength() == 2, "recording reader parses hand-built WAV");
        javax.sound.sampled.AudioInputStream ais = r.getAudioInputStream(new java.io.ByteArrayInputStream(auUlaw()));
        check(ais.getFormat().getEncoding() == javax.sound.sampled.AudioFormat.Encoding.ULAW, "recording reader streams AU");
        java.io.File tmp = java.io.File.createTempFile("deep", ".wav");
        tmp.deleteOnExit();
        java.io.FileOutputStream fo = new java.io.FileOutputStream(tmp); fo.write(wav8()); fo.close();
        javax.sound.sampled.AudioFileFormat fromFile = r.getAudioFileFormat(tmp);
        check(fromFile.getType() == javax.sound.sampled.AudioFileFormat.Type.WAVE, "File overload");
        try { r.getAudioFileFormat(new java.io.ByteArrayInputStream(new byte[] { 1 })); check(false, "junk must fail"); }
        catch (Exception e) { check(e instanceof javax.sound.sampled.UnsupportedAudioFileException || e instanceof java.io.IOException, "junk rejected: " + e); }
    }
    static class RecReader extends javax.sound.sampled.spi.AudioFileReader {
        public javax.sound.sampled.AudioFileFormat getAudioFileFormat(java.io.InputStream in) throws javax.sound.sampled.UnsupportedAudioFileException, java.io.IOException { return javax.sound.sampled.AudioSystem.getAudioFileFormat(in); }
        public javax.sound.sampled.AudioFileFormat getAudioFileFormat(java.net.URL url) throws javax.sound.sampled.UnsupportedAudioFileException, java.io.IOException { return null; }
        public javax.sound.sampled.AudioFileFormat getAudioFileFormat(java.io.File f) throws javax.sound.sampled.UnsupportedAudioFileException, java.io.IOException { return javax.sound.sampled.AudioSystem.getAudioFileFormat(f); }
        public javax.sound.sampled.AudioInputStream getAudioInputStream(java.io.InputStream in) throws javax.sound.sampled.UnsupportedAudioFileException, java.io.IOException { return javax.sound.sampled.AudioSystem.getAudioInputStream(in); }
        public javax.sound.sampled.AudioInputStream getAudioInputStream(java.net.URL url) throws javax.sound.sampled.UnsupportedAudioFileException, java.io.IOException { return null; }
        public javax.sound.sampled.AudioInputStream getAudioInputStream(java.io.File f) throws javax.sound.sampled.UnsupportedAudioFileException, java.io.IOException { return null; }
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
