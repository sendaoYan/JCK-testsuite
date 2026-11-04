/*
 * @test
 * @summary javax.sound.midi.MidiUnavailableException conformance: white-box structure + black-box behavior.
 * @run main MidiUnavailableExceptionTest
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import javax.sound.midi.MidiUnavailableException;

public class MidiUnavailableExceptionTest {
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

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.sound.midi.MidiUnavailableException");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(Modifier.isAbstract(mods) == false, "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Exception"), "superclass Exception");
        check(c.getInterfaces().length == 0, "interface count");
        Constructor<?>[] ctors = c.getDeclaredConstructors();
        check(ctors.length == 2, "constructor count");
        boolean hasString = false;
        for (Constructor<?> k : ctors) {
            if (k.getParameterTypes().length == 1 && k.getParameterTypes()[0] == String.class) hasString = true;
        }
        check(hasString, "String constructor present");
        boolean hasSvid = false;
        for (Field f : c.getDeclaredFields()) {
            if (f.getName().equals("serialVersionUID")) hasSvid = true;
        }
        check(hasSvid, "serialVersionUID present");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        MidiUnavailableException e = new MidiUnavailableException();
        check(e != null, "no-arg constructor");
        check(e instanceof Exception, "is Exception");
        check(e.getMessage() == null, "no-arg message null");

        MidiUnavailableException e2 = new MidiUnavailableException("device unavailable");
        check("device unavailable".equals(e2.getMessage()), "String constructor message");

        expectThrows(MidiUnavailableException.class,
                () -> { throw new MidiUnavailableException("boom"); },
                "throw/catch round-trip");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
