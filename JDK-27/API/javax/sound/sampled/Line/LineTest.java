/*
 * @test
 * @summary javax.sound.sampled.Line conformance: white-box structure + black-box behavior.
 * @key sound
 * @run main LineTest
 */

import java.lang.reflect.Modifier;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Control;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class LineTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.sound.sampled.Line");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == true, "is interface");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(c.getInterfaces().length == 1, "direct interface count");
        check(c.getInterfaces()[0].getName().equals("java.lang.AutoCloseable"), "extends AutoCloseable");
        for (String m : new String[]{"getLineInfo", "open", "close", "isOpen", "getControls", "isControlSupported", "getControl", "addLineListener", "removeLineListener"}) {
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
            Line line = sdl;
            check(line instanceof AutoCloseable, "Line is AutoCloseable");

            Line.Info info = line.getLineInfo();
            check(info != null, "getLineInfo non-null");
            check(info.getLineClass() == SourceDataLine.class, "getLineInfo line class");

            check(!line.isOpen(), "not open initially");
            sdl.open(fmt, 1024);
            check(line.isOpen(), "isOpen after open");

            Control[] controls = line.getControls();
            check(controls != null, "getControls non-null");
            for (Control ct : controls) {
                check(ct.getType() != null, "control type non-null");
                check(line.isControlSupported(ct.getType()), "isControlSupported true for returned control");
            }

            LineListener listener = new LineListener() {
                public void update(LineEvent e) { }
            };
            line.addLineListener(listener);
            line.removeLineListener(listener);

            line.close();
            check(!line.isOpen(), "isOpen false after close");
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
