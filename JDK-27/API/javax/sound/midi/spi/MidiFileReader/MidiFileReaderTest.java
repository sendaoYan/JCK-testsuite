/*
 * @test
 * @summary javax.sound.midi.spi.MidiFileReader conformance: white-box structure + black-box behavior.
 * @run main MidiFileReaderTest
 */

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.lang.reflect.Modifier;
import javax.sound.midi.MidiFileFormat;
import javax.sound.midi.Sequence;
import javax.sound.midi.spi.MidiFileReader;

public class MidiFileReaderTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.sound.midi.spi.MidiFileReader");
        check(!c.isInterface(), "is not interface");
        check(Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(Modifier.isPublic(c.getModifiers()), "is public");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "extends Object");
        check(Modifier.isProtected(c.getDeclaredConstructors()[0].getModifiers()), "protected constructor");
        java.util.Set<String> methods = new java.util.HashSet<>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) methods.add(m.getName());
        check(methods.contains("getMidiFileFormat"), "method getMidiFileFormat");
        check(methods.contains("getSequence"), "method getSequence");
    }

    static void testBlackBox() throws Exception {
        MidiFileFormat fmt = new MidiFileFormat(0, Sequence.PPQ, 96, 100, 500000);
        Sequence seq = new Sequence(Sequence.PPQ, 96);
        seq.createTrack();
        FakeReader r = new FakeReader(fmt, seq);
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[]{1, 2, 3});
        check(r.getMidiFileFormat(in) == fmt, "getMidiFileFormat(InputStream) round-trip");
        check(r.getSequence(in) == seq, "getSequence(InputStream) round-trip");
        URL url = new File("dummy").toURI().toURL();
        check(r.getMidiFileFormat(url) == fmt, "getMidiFileFormat(URL) round-trip");
        check(r.getSequence(url) == seq, "getSequence(URL) round-trip");
        File f = new File("dummy");
        check(r.getMidiFileFormat(f) == fmt, "getMidiFileFormat(File) round-trip");
        check(r.getSequence(f) == seq, "getSequence(File) round-trip");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

class FakeReader extends MidiFileReader {
    private final MidiFileFormat fmt;
    private final Sequence seq;
    FakeReader(MidiFileFormat f, Sequence s) { fmt = f; seq = s; }
    public MidiFileFormat getMidiFileFormat(InputStream stream) { return fmt; }
    public MidiFileFormat getMidiFileFormat(URL url) { return fmt; }
    public MidiFileFormat getMidiFileFormat(File file) { return fmt; }
    public Sequence getSequence(InputStream stream) { return seq; }
    public Sequence getSequence(URL url) { return seq; }
    public Sequence getSequence(File file) { return seq; }
}
