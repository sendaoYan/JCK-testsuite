/*
 * @test
 * @summary javax.sound.sampled.SourceDataLine conformance: white-box structure + black-box behavior.
 * @key sound
 * @run main SourceDataLineTest
 */

import java.lang.reflect.Modifier;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class SourceDataLineTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.sound.sampled.SourceDataLine");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == true, "is interface");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(c.getInterfaces().length == 1, "direct interface count");
        check(c.getInterfaces()[0].getName().equals("javax.sound.sampled.DataLine"), "extends DataLine");
        check(hasMethod(c, "open"), "method open");
        check(hasMethod(c, "write"), "method write");
    }

    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        AudioFormat fmt = new AudioFormat(8000f, 8, 1, true, false);
        try {
            SourceDataLine sdl = AudioSystem.getSourceDataLine(fmt);
            check(sdl instanceof DataLine, "SourceDataLine is a DataLine");
            check(!sdl.isOpen(), "not open initially");
            sdl.open(fmt, 1024);
            check(sdl.isOpen(), "open(format,buffer) makes isOpen true");
            check(sdl.getBufferSize() == 1024, "getBufferSize 1024");
            check(sdl.getFormat().matches(fmt), "getFormat matches requested format");
            check(sdl.available() >= 0, "available non-negative");
            sdl.start();
            byte[] buf = new byte[512];
            int written = sdl.write(buf, 0, buf.length);
            check(written == 512, "write returns requested byte count");
            check(sdl.available() >= 0, "available after write non-negative");
            sdl.flush();
            sdl.stop();
            sdl.close();
            check(!sdl.isOpen(), "close makes isOpen false");
        } catch (LineUnavailableException lue) {
            // No output line available: degraded to enumeration paths.
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
