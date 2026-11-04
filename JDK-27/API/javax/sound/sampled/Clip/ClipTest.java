/*
 * @test
 * @summary javax.sound.sampled.Clip conformance: white-box structure + black-box behavior.
 * @key sound
 * @run main ClipTest
 */

import java.lang.reflect.Modifier;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;

public class ClipTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.sound.sampled.Clip");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == true, "is interface");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(c.getInterfaces().length == 1, "direct interface count");
        check(c.getInterfaces()[0].getName().equals("javax.sound.sampled.DataLine"), "extends DataLine");
        check(c.getField("LOOP_CONTINUOUSLY") != null, "field LOOP_CONTINUOUSLY");
        for (String m : new String[]{"open", "getFrameLength", "getMicrosecondLength", "setFramePosition", "setMicrosecondPosition", "setLoopPoints", "loop"}) {
            check(hasMethod(c, m), "method " + m);
        }
    }

    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        check(Clip.LOOP_CONTINUOUSLY == -1, "LOOP_CONTINUOUSLY == -1");

        AudioFormat fmt = new AudioFormat(8000f, 8, 1, true, false);
        byte[] data = new byte[800];
        try {
            Clip c = AudioSystem.getClip();
            check(c instanceof DataLine, "Clip is a DataLine");
            check(c instanceof Line, "Clip is a Line");
            check(!c.isOpen(), "not open initially");
            c.open(fmt, data, 0, 800);
            check(c.isOpen(), "open makes isOpen true");
            check(c.getFrameLength() == 800, "getFrameLength 800");
            check(c.getMicrosecondLength() == 100000L, "getMicrosecondLength 100000");
            c.setFramePosition(100);
            check(((DataLine) c).getFramePosition() == 100, "setFramePosition round-trip");
            c.setMicrosecondPosition(0);
            check(((DataLine) c).getFramePosition() == 0, "setMicrosecondPosition 0");
            c.setLoopPoints(0, -1);
            c.start();
            check(c.isOpen(), "still open after start");
            c.stop();
            c.close();
            check(!c.isOpen(), "close makes isOpen false");
        } catch (LineUnavailableException lue) {
            // No clip line available: degraded to enumeration/exception paths above.
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
