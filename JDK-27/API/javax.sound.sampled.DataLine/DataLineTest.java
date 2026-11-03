/*
 * @test
 * @summary javax.sound.sampled.DataLine conformance: white-box structure + black-box behavior.
 * @key sound
 * @run main DataLineTest
 */

import java.lang.reflect.Modifier;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class DataLineTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.sound.sampled.DataLine");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == true, "is interface");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(c.getInterfaces().length == 1, "direct interface count");
        check(c.getInterfaces()[0].getName().equals("javax.sound.sampled.Line"), "extends Line");
        for (String m : new String[]{"drain", "flush", "start", "stop", "isRunning", "isActive", "getFormat", "getBufferSize", "available", "getFramePosition", "getLongFramePosition", "getMicrosecondPosition", "getLevel"}) {
            check(hasMethod(c, m), "method " + m);
        }
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
            DataLine dl = sdl;
            check(dl instanceof Line, "DataLine is a Line");
            check(dl.getFormat().matches(fmt), "getFormat matches");
            sdl.open(fmt, 1024);
            check(dl.getBufferSize() == 1024, "getBufferSize 1024");
            check(dl.available() >= 0, "available non-negative");
            check(dl.getFramePosition() >= 0, "getFramePosition non-negative");
            check(dl.getLongFramePosition() >= 0L, "getLongFramePosition non-negative");
            check(dl.getMicrosecondPosition() >= 0L, "getMicrosecondPosition non-negative");
            float level = dl.getLevel();
            check(level == -1.0f || (level >= 0.0f && level <= 1.0f), "getLevel in [-1,1]");
            dl.start();
            dl.isRunning();
            dl.isActive();
            dl.flush();
            dl.stop();
            dl.drain();
            dl.close();
            check(!dl.isOpen(), "close makes isOpen false");
        } catch (LineUnavailableException lue) {
            // No line available: degraded to enumeration paths.
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
