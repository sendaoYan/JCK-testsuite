/*
 * @test
 * @summary javax.sound.sampled.TargetDataLine conformance: white-box structure + black-box behavior.
 * @key sound
 * @run main TargetDataLineTest
 */

import java.lang.reflect.Modifier;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

public class TargetDataLineTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.sound.sampled.TargetDataLine");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == true, "is interface");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(c.getInterfaces().length == 1, "direct interface count");
        check(c.getInterfaces()[0].getName().equals("javax.sound.sampled.DataLine"), "extends DataLine");
        check(hasMethod(c, "open"), "method open");
        check(hasMethod(c, "read"), "method read");
    }

    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        AudioFormat fmt = new AudioFormat(8000f, 8, 1, true, false);
        try {
            TargetDataLine tdl = AudioSystem.getTargetDataLine(fmt);
            check(tdl instanceof DataLine, "TargetDataLine is a DataLine");
            check(!tdl.isOpen(), "not open initially");
            tdl.open(fmt, 1024);
            check(tdl.isOpen(), "open(format,buffer) makes isOpen true");
            check(tdl.getBufferSize() == 1024, "getBufferSize 1024");
            check(tdl.getFormat().matches(fmt), "getFormat matches requested format");
            tdl.start();
            byte[] buf = new byte[16];
            int read = tdl.read(buf, 0, buf.length);
            check(read >= 0, "read returns non-negative count");
            tdl.stop();
            tdl.close();
            check(!tdl.isOpen(), "close makes isOpen false");
        } catch (LineUnavailableException lue) {
            // No input line available: degraded to enumeration paths.
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
