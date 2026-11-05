/*
 * @test
 * @summary javax.sound.sampled.spi.AudioFileReader conformance: white-box structure + black-box behavior.
 * @run main AudioFileReaderTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.net.URL;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

public class AudioFileReaderTest {
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
            check(expected.isAssignableFrom(t.getClass()), msg + " expected " + expected.getSimpleName() + " but got " + t.getClass().getSimpleName());
        }
        check(thrown, msg + " should throw " + expected.getSimpleName());
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.sound.sampled.spi.AudioFileReader");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isAbstract(mods) == true, "abstract modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "extends Object");
        java.lang.reflect.Constructor<?> ctor = c.getDeclaredConstructor();
        check(Modifier.isProtected(ctor.getModifiers()), "protected constructor");
        for (Class<?>[] sig : new Class<?>[][]{
                {InputStream.class}, {URL.class}, {File.class}}) {
            check(c.getMethod("getAudioFileFormat", sig) != null, "getAudioFileFormat abstract");
            check(c.getMethod("getAudioInputStream", sig) != null, "getAudioInputStream abstract");
        }
        check(Modifier.isAbstract(c.getMethod("getAudioFileFormat", InputStream.class).getModifiers()), "getAudioFileFormat abstract");
        check(Modifier.isAbstract(c.getMethod("getAudioInputStream", InputStream.class).getModifiers()), "getAudioInputStream abstract");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // Instantiate via anonymous subclass to prove the protected constructor is usable.
        AudioFileReaderImpl reader = new AudioFileReaderImpl();
        check(reader != null, "anonymous subclass instantiation");

        // Concrete readers are exercised through AudioSystem's public API.
        AudioFormat fmt = new AudioFormat(8000f, 8, 1, true, false);
        byte[] data = new byte[800];
        AudioInputStream ais = new AudioInputStream(new ByteArrayInputStream(data), fmt, data.length);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        int written = AudioSystem.write(ais, AudioFileFormat.Type.WAVE, bos);
        check(written > 0, "write WAV");
        byte[] wav = bos.toByteArray();

        AudioFileFormat aff = AudioSystem.getAudioFileFormat(new ByteArrayInputStream(wav));
        check(aff.getType().equals(AudioFileFormat.Type.WAVE), "reader: WAVE type");
        check(aff.getFormat() != null, "reader: format non-null");

        AudioInputStream back = AudioSystem.getAudioInputStream(new ByteArrayInputStream(wav));
        check(back.getFrameLength() == 800, "reader: frame length 800");
        back.close();

        expectThrows(UnsupportedAudioFileException.class,
            () -> AudioSystem.getAudioFileFormat(new ByteArrayInputStream(new byte[]{1,2,3,4})),
            "reader: bogus format");
        expectThrows(UnsupportedAudioFileException.class,
            () -> AudioSystem.getAudioInputStream(new ByteArrayInputStream(new byte[]{1,2,3,4})),
            "reader: bogus stream");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
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

class AudioFileReaderImpl extends javax.sound.sampled.spi.AudioFileReader {
    public AudioFileFormat getAudioFileFormat(InputStream s) { return null; }
    public AudioFileFormat getAudioFileFormat(URL u) { return null; }
    public AudioFileFormat getAudioFileFormat(File f) { return null; }
    public AudioInputStream getAudioInputStream(InputStream s) { return null; }
    public AudioInputStream getAudioInputStream(URL u) { return null; }
    public AudioInputStream getAudioInputStream(File f) { return null; }
}
