/*
 * @test
 * @summary javax.sound.sampled.LineEvent conformance: white-box structure + black-box behavior.
 * @run main LineEventTest
 */

import java.lang.reflect.Modifier;
import javax.sound.sampled.Control;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;

public class LineEventTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class MockLine implements Line {
        public Line.Info getLineInfo() { return new Line.Info(Line.class); }
        public void open() throws LineUnavailableException {}
        public void close() {}
        public boolean isOpen() { return false; }
        public Control[] getControls() { return new Control[0]; }
        public boolean isControlSupported(Control.Type control) { return false; }
        public Control getControl(Control.Type control) { throw new IllegalArgumentException(); }
        public void addLineListener(LineListener listener) {}
        public void removeLineListener(LineListener listener) {}
        public String toString() { return "MockLine"; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.sound.sampled.LineEvent");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isAbstract(mods), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.util.EventObject"), "superclass EventObject");

        Class<?> t = Class.forName("javax.sound.sampled.LineEvent$Type");
        check(t != null && t.getEnclosingClass() == c, "Type nested");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Line line = new MockLine();
        LineEvent ev = new LineEvent(line, LineEvent.Type.START, 42L);

        check(ev.getLine() == line, "getLine identity");
        check(ev.getSource() == line, "getSource identity (EventObject)");
        check(ev.getType() == LineEvent.Type.START, "getType START");
        check(ev.getFramePosition() == 42L, "getFramePosition 42");

        LineEvent open = new LineEvent(line, LineEvent.Type.OPEN, 0L);
        check(open.getType() == LineEvent.Type.OPEN, "OPEN type");
        check(open.getFramePosition() == 0L, "OPEN position 0");

        check(ev.toString().contains("Start"), "toString has type name");
        check(ev.toString().contains("MockLine"), "toString has line");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
