/*
 * @test
 * @summary javax.sound.midi.spi.SoundbankReader conformance: white-box structure + black-box behavior.
 * @run main SoundbankReaderTest
 */

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.lang.reflect.Modifier;
import javax.sound.midi.Instrument;
import javax.sound.midi.Patch;
import javax.sound.midi.Soundbank;
import javax.sound.midi.SoundbankResource;
import javax.sound.midi.spi.SoundbankReader;

public class SoundbankReaderTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.sound.midi.spi.SoundbankReader");
        check(!c.isInterface(), "is not interface");
        check(Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(Modifier.isPublic(c.getModifiers()), "is public");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "extends Object");
        check(Modifier.isProtected(c.getDeclaredConstructors()[0].getModifiers()), "protected constructor");
        java.util.Set<String> methods = new java.util.HashSet<>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) methods.add(m.getName());
        check(methods.contains("getSoundbank"), "method getSoundbank");
    }

    static void testBlackBox() throws Exception {
        FakeSB sb = new FakeSB();
        FakeSoundbankReader r = new FakeSoundbankReader(sb);
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[]{1, 2, 3});
        check(r.getSoundbank(in) == sb, "getSoundbank(InputStream) round-trip");
        URL url = new File("dummy").toURI().toURL();
        check(r.getSoundbank(url) == sb, "getSoundbank(URL) round-trip");
        File f = new File("dummy");
        check(r.getSoundbank(f) == sb, "getSoundbank(File) round-trip");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

class FakeSoundbankReader extends SoundbankReader {
    private final Soundbank sb;
    FakeSoundbankReader(Soundbank s) { sb = s; }
    public Soundbank getSoundbank(URL url) { return sb; }
    public Soundbank getSoundbank(InputStream stream) { return sb; }
    public Soundbank getSoundbank(File file) { return sb; }
}

class FakeSB implements Soundbank {
    public String getName() { return "SB"; }
    public String getVersion() { return "1"; }
    public String getVendor() { return "V"; }
    public String getDescription() { return "D"; }
    public SoundbankResource[] getResources() { return new SoundbankResource[0]; }
    public Instrument[] getInstruments() { return new Instrument[0]; }
    public Instrument getInstrument(Patch p) { return null; }
}
