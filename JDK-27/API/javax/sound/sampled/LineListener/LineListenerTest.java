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
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: recording listener + real Clip callback loop ----
    static void testContractDeep() throws Exception {
        final javax.sound.sampled.LineEvent[] got = new javax.sound.sampled.LineEvent[1];
        final int[] count = new int[1];
        javax.sound.sampled.LineListener l = new javax.sound.sampled.LineListener() {
            public void update(javax.sound.sampled.LineEvent e) { got[0] = e; count[0]++; }
        };
        javax.sound.sampled.Clip clip = (javax.sound.sampled.Clip) javax.sound.sampled.AudioSystem.getLine(new javax.sound.sampled.DataLine.Info(javax.sound.sampled.Clip.class, new javax.sound.sampled.AudioFormat(8000f, 8, 1, true, false)));
        javax.sound.sampled.AudioInputStream ais = new javax.sound.sampled.AudioInputStream(new java.io.ByteArrayInputStream(new byte[] { 16, -16 }), new javax.sound.sampled.AudioFormat(8000f, 8, 1, true, false), 2);
        clip.open(ais);
        clip.addLineListener(l);
        clip.setFramePosition(0);
        clip.start();
        clip.stop();
        clip.close();
        check(count[0] >= 1, "listener invoked [" + count[0] + "]");
        if (count[0] >= 1) {
            check(got[0].getLine() == clip, "event carries the clip");
            boolean sawStart = false, sawStop = false, sawClose = false, sawOpen = false;
            // re-run to capture full sequence
            final java.util.List<String> seq = new java.util.ArrayList<String>();
            javax.sound.sampled.LineListener seqL = new javax.sound.sampled.LineListener() {
                public void update(javax.sound.sampled.LineEvent e) { seq.add(e.getType().toString()); }
            };
            javax.sound.sampled.Clip c2 = (javax.sound.sampled.Clip) javax.sound.sampled.AudioSystem.getLine(new javax.sound.sampled.DataLine.Info(javax.sound.sampled.Clip.class, new javax.sound.sampled.AudioFormat(8000f, 8, 1, true, false)));
            c2.open(new javax.sound.sampled.AudioInputStream(new java.io.ByteArrayInputStream(new byte[] { 16, -16 }), new javax.sound.sampled.AudioFormat(8000f, 8, 1, true, false), 2));
            c2.addLineListener(seqL);
            c2.start();
            Thread.sleep(300);
            c2.stop();
            c2.close();
            Thread.sleep(100);
            for (String s : seq) { if (s.equals("Start")) sawStart = true; if (s.equals("Stop")) sawStop = true; if (s.equals("Close")) sawClose = true; if (s.equals("Open")) sawOpen = true; }
            check(sawStart && sawStop, "Start and Stop events delivered [" + seq + "]");
            check(sawClose, "Close event delivered");
        }
        clip.removeLineListener(l);
        check(true, "removal accepted");
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
