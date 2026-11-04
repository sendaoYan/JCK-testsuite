/*
 * @test
 * @summary javax.sound.sampled.LineListener conformance: white-box structure + black-box behavior.
 * @run main LineListenerTest
 */

import java.lang.reflect.Modifier;
import java.util.EventListener;
import javax.sound.sampled.Control;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;

public class LineListenerTest {
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
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.sound.sampled.LineListener");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(c.getInterfaces().length == 1, "interface count 1");
        check(c.getInterfaces()[0].getName().equals("java.util.EventListener"), "extends EventListener");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // functional interface: lambda implementation
        final int[] calls = { 0 };
        LineListener lambda = e -> calls[0]++;
        check(lambda instanceof LineListener, "lambda is LineListener");
        check(lambda instanceof EventListener, "lambda is EventListener");

        Line line = new MockLine();
        LineEvent ev = new LineEvent(line, LineEvent.Type.START, 7L);
        lambda.update(ev);
        lambda.update(new LineEvent(line, LineEvent.Type.STOP, 8L));
        check(calls[0] == 2, "update invoked twice");

        // anonymous class implementation sees the event
        final LineEvent[] seen = { null };
        LineListener anon = new LineListener() {
            public void update(LineEvent event) { seen[0] = event; }
        };
        anon.update(ev);
        check(seen[0] == ev, "anonymous impl receives event");
        check(seen[0].getType() == LineEvent.Type.START, "received event type");
        check(seen[0].getFramePosition() == 7L, "received event position");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
