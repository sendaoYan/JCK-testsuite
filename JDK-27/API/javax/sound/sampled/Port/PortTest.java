/*
 * @test
 * @key sound
 * @summary javax.sound.sampled.Port conformance: white-box structure + black-box behavior.
 * @run main PortTest
 */

import java.lang.reflect.Modifier;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Port;

public class PortTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.sound.sampled.Port");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == true, "is interface");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(c.getInterfaces().length == 1, "direct interface count");
        check(c.getInterfaces()[0].getName().equals("javax.sound.sampled.Line"), "extends Line");
        check(c.getDeclaredMethods().length == 0, "Port declares no methods of its own");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // Port adds no methods; exercise through Port.Info and line queries.
        Line.Info portInfo = new Line.Info(Port.class);
        Line.Info[] srcPorts = AudioSystem.getSourceLineInfo(portInfo);
        Line.Info[] tgtPorts = AudioSystem.getTargetLineInfo(portInfo);
        check(srcPorts != null, "getSourceLineInfo(Port) non-null");
        check(tgtPorts != null, "getTargetLineInfo(Port) non-null");
        check(AudioSystem.isLineSupported(portInfo) == (srcPorts.length > 0 || tgtPorts.length > 0), "isLineSupported(Port) consistent");

        if (srcPorts.length > 0) {
            try {
                Line l = AudioSystem.getLine(srcPorts[0]);
                l.open();
                check(l.isOpen(), "port line open");
                l.close();
                check(!l.isOpen(), "port line close");
            } catch (LineUnavailableException lue) {
                // Port line not available: acceptable.
            }
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: Port.Info table via real mixer ----
    static void testContractDeep() throws Exception {
        check(!javax.sound.sampled.Port.Info.SPEAKER.isSource(), "SPEAKER is a sink");
        check(javax.sound.sampled.Port.Info.MICROPHONE.isSource(), "MICROPHONE is a source");
        check(javax.sound.sampled.Port.Info.LINE_IN.isSource(), "LINE_IN is a source");
        check(!javax.sound.sampled.Port.Info.HEADPHONE.isSource(), "HEADPHONE is a sink");
        check(javax.sound.sampled.Port.Info.COMPACT_DISC.isSource(), "COMPACT_DISC is a source");
        check("SPEAKER".equals(javax.sound.sampled.Port.Info.SPEAKER.getName()), "name SPEAKER");
        check(javax.sound.sampled.Port.Info.SPEAKER.getLineClass() == javax.sound.sampled.Port.class, "line class Port");
        check(javax.sound.sampled.Port.Info.SPEAKER.equals(javax.sound.sampled.Port.Info.SPEAKER), "self equals");
        check(!javax.sound.sampled.Port.Info.SPEAKER.equals(javax.sound.sampled.Port.Info.HEADPHONE), "distinct");
        // hardware ports: the headless default mixer exposes no Port.Info lines (skip reason: no sound card);
        // the constant table itself is the stable contract
        javax.sound.sampled.Mixer.Info[] mi = javax.sound.sampled.AudioSystem.getMixerInfo();
        int portLines = 0;
        for (javax.sound.sampled.Mixer.Info i : mi) {
            javax.sound.sampled.Mixer m = javax.sound.sampled.AudioSystem.getMixer(i);
            for (javax.sound.sampled.Line.Info l : m.getSourceLineInfo()) if (l instanceof javax.sound.sampled.Port.Info) portLines++;
            for (javax.sound.sampled.Line.Info l : m.getTargetLineInfo()) if (l instanceof javax.sound.sampled.Port.Info) portLines++;
        }
        check(portLines == 0, "no hardware port lines on headless default mixer (no-service contract)");
        check("MICROPHONE".equals(javax.sound.sampled.Port.Info.MICROPHONE.getName()), "MIC name");
        check("LINE_IN".equals(javax.sound.sampled.Port.Info.LINE_IN.getName()), "LINE_IN name");
        check("COMPACT_DISC".equals(javax.sound.sampled.Port.Info.COMPACT_DISC.getName()), "CD name");
        check("HEADPHONE".equals(javax.sound.sampled.Port.Info.HEADPHONE.getName()), "HP name");
        // Port is an interface
        check(javax.sound.sampled.Port.class.isInterface(), "Port is an interface");
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
