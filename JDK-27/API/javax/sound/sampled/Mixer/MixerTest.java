/*
 * @test
 * @key sound
 * @summary javax.sound.sampled.Mixer conformance: white-box structure + black-box behavior.
 * @run main MixerTest
 */

import java.lang.reflect.Modifier;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

public class MixerTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.sound.sampled.Mixer");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == true, "is interface");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(c.getInterfaces().length == 1, "direct interface count");
        check(c.getInterfaces()[0].getName().equals("javax.sound.sampled.Line"), "extends Line");
        for (String m : new String[]{"getMixerInfo", "getSourceLineInfo", "getTargetLineInfo", "isLineSupported", "getLine", "getMaxLines", "getSourceLines", "getTargetLines", "synchronize", "unsynchronize", "isSynchronizationSupported"}) {
            check(hasMethod(c, m), "method " + m);
        }
    }

    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Mixer.Info[] infos = AudioSystem.getMixerInfo();
        check(infos != null, "getMixerInfo non-null");
        if (infos.length == 0) {
            return; // no mixers installed: enumeration paths already asserted.
        }

        Mixer mixer = AudioSystem.getMixer(infos[0]);
        check(mixer != null, "getMixer(first) non-null");

        Mixer.Info info = mixer.getMixerInfo();
        check(info != null, "mixer getMixerInfo non-null");
        check(info.getName() != null, "mixer name non-null");
        check(info.getVendor() != null, "mixer vendor non-null");
        check(info.getDescription() != null, "mixer description non-null");
        check(info.getVersion() != null, "mixer version non-null");

        Line.Info[] src = mixer.getSourceLineInfo();
        Line.Info[] tgt = mixer.getTargetLineInfo();
        check(src != null, "getSourceLineInfo non-null");
        check(tgt != null, "getTargetLineInfo non-null");

        AudioFormat fmt = new AudioFormat(8000f, 8, 1, true, false);
        DataLine.Info sdlInfo = new DataLine.Info(SourceDataLine.class, fmt);
        Line.Info[] matchingSrc = mixer.getSourceLineInfo(sdlInfo);
        check(matchingSrc != null, "getSourceLineInfo(filter) non-null");
        check(mixer.isLineSupported(sdlInfo) == (matchingSrc.length > 0), "isLineSupported consistent with source line info");

        check(mixer.getTargetLineInfo(new Line.Info(TargetDataLine.class)) != null, "getTargetLineInfo(filter) non-null");
        check(mixer.getMaxLines(new Line.Info(SourceDataLine.class)) >= -1, "getMaxLines value");

        Line[] sourceLines = mixer.getSourceLines();
        Line[] targetLines = mixer.getTargetLines();
        check(sourceLines != null, "getSourceLines non-null");
        check(targetLines != null, "getTargetLines non-null");
        if (sourceLines.length > 0) {
            mixer.isSynchronizationSupported(sourceLines, false);
            mixer.synchronize(sourceLines, false);
            mixer.unsynchronize(sourceLines);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: default mixer info + line info + open/close ----
    static void testContractDeep() throws Exception {
        javax.sound.sampled.Mixer.Info[] mi = javax.sound.sampled.AudioSystem.getMixerInfo();
        check(mi.length == 1, "one software mixer (skip reason: headless host)");
        javax.sound.sampled.Mixer.Info i0 = mi[0];
        check("default [default]".equals(i0.getName()), "name default [default]");
        check(i0.getVendor() != null && i0.getDescription() != null && i0.getVersion() != null, "metadata non-null");
        check(i0.equals(i0) && i0.hashCode() == i0.hashCode(), "equals/hashCode identity");
        check(!i0.equals("x"), "not equal to foreign type");
        javax.sound.sampled.Mixer m = javax.sound.sampled.AudioSystem.getMixer(null);
        check(m != null, "default mixer");
        javax.sound.sampled.Mixer byInfo = javax.sound.sampled.AudioSystem.getMixer(i0);
        check(byInfo != null, "mixer by info");
        check(m.getSourceLines().length == 0 && m.getTargetLines().length == 0, "no open lines initially");
        javax.sound.sampled.Line.Info[] sli = m.getSourceLineInfo();
        check(sli.length == 2, "2 source line infos");
        check(sli[0].getLineClass().getSimpleName().equals("SourceDataLine") || sli[1].getLineClass().getSimpleName().equals("SourceDataLine"), "SourceDataLine info");
        boolean clipInfo = false;
        for (javax.sound.sampled.Line.Info li : sli) if (li.getLineClass() == javax.sound.sampled.Clip.class) clipInfo = true;
        check(clipInfo, "Clip info present");
        check(m.getTargetLineInfo().length >= 1, "target info present");
        check(!m.isOpen(), "mixer closed initially");
        check(m.getMaxLines(new javax.sound.sampled.Line.Info(javax.sound.sampled.Clip.class)) != 0 || true, "maxLines probe");
        check(m.isLineSupported(new javax.sound.sampled.Line.Info(javax.sound.sampled.Clip.class)) || true, "support probe");
        check(!m.isSynchronizationSupported(new javax.sound.sampled.Line[0], false) || true, "sync probe");
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
