/*
 * @test
 * @summary javax.sound.midi.MidiFileFormat conformance: white-box structure + black-box behavior.
 * @run main MidiFileFormatTest
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.sound.midi.MidiFileFormat;
import javax.sound.midi.Sequence;

public class MidiFileFormatTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    interface ThrowingRunnable { void run() throws Exception; }
    static void expectThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        boolean thrown = false;
        try { r.run(); } catch (Throwable t) {
            thrown = true;
            check(expected.isAssignableFrom(t.getClass()),
                  msg + " expected " + expected.getSimpleName() + " but got " + t.getClass().getSimpleName());
        }
        check(thrown, msg + " should throw " + expected.getSimpleName());
    }
    static Set<String> methodNames(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) s.add(m.getName());
        return s;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.sound.midi.MidiFileFormat");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(Modifier.isAbstract(mods) == false, "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(MidiFileFormat.UNKNOWN_LENGTH == -1, "UNKNOWN_LENGTH constant");
        Set<String> dm = methodNames(c);
        check(dm.contains("getType"), "method getType");
        check(dm.contains("getDivisionType"), "method getDivisionType");
        check(dm.contains("getResolution"), "method getResolution");
        check(dm.contains("getByteLength"), "method getByteLength");
        check(dm.contains("getMicrosecondLength"), "method getMicrosecondLength");
        check(dm.contains("properties"), "method properties");
        check(dm.contains("getProperty"), "method getProperty");
        Constructor<?>[] ctors = c.getDeclaredConstructors();
        check(ctors.length == 2, "constructor count");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        MidiFileFormat ff = new MidiFileFormat(1, Sequence.PPQ, 480, 100, 5000L);
        check(ff.getType() == 1, "getType");
        check(ff.getDivisionType() == Sequence.PPQ, "getDivisionType");
        check(ff.getResolution() == 480, "getResolution");
        check(ff.getByteLength() == 100, "getByteLength");
        check(ff.getMicrosecondLength() == 5000L, "getMicrosecondLength");
        check(ff.properties() != null, "properties non-null");
        check(ff.properties().isEmpty(), "no-property format returns empty map");
        check(ff.getProperty("absent") == null, "getProperty absent returns null");

        // Unknown lengths.
        MidiFileFormat unknown = new MidiFileFormat(0, Sequence.PPQ, 96,
                MidiFileFormat.UNKNOWN_LENGTH, MidiFileFormat.UNKNOWN_LENGTH);
        check(unknown.getByteLength() == -1, "UNKNOWN_LENGTH byteLength");
        check(unknown.getMicrosecondLength() == -1, "UNKNOWN_LENGTH microsecondLength");

        // Properties constructor round-trip + unmodifiable copy.
        Map<String, Object> props = new HashMap<>();
        props.put("author", "Alice");
        props.put("title", "Song");
        MidiFileFormat ff2 = new MidiFileFormat(2, Sequence.PPQ, 192, 2048, 123456L, props);
        check("Alice".equals(ff2.getProperty("author")), "getProperty author");
        check("Song".equals(ff2.getProperty("title")), "getProperty title");
        check(ff2.getProperty("missing") == null, "getProperty missing");
        check(ff2.getType() == 2, "properties ctor type");
        check(ff2.getResolution() == 192, "properties ctor resolution");

        Map<String, Object> returned = ff2.properties();
        check(returned.containsKey("author") && returned.containsKey("title"), "properties map contents");
        expectThrows(UnsupportedOperationException.class, () -> returned.put("x", "y"), "properties unmodifiable");

        // Mutating the source map after construction does not affect the format.
        props.put("author", "Bob");
        check("Alice".equals(ff2.getProperty("author")), "constructor copies source map");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
