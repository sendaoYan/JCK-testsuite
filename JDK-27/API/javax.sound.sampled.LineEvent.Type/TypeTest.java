/*
 * @test
 * @summary javax.sound.sampled.LineEvent.Type conformance: white-box structure + black-box behavior.
 * @run main TypeTest
 */

import java.lang.reflect.Modifier;
import javax.sound.sampled.LineEvent;

public class TypeTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.sound.sampled.LineEvent$Type");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods) && Modifier.isStatic(mods), "public static");
        check(c.getEnclosingClass() == LineEvent.class, "enclosing class");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        LineEvent.Type open  = LineEvent.Type.OPEN;
        LineEvent.Type close = LineEvent.Type.CLOSE;
        LineEvent.Type start = LineEvent.Type.START;
        LineEvent.Type stop  = LineEvent.Type.STOP;

        check(open != null && close != null && start != null && stop != null, "constants non-null");
        check(open != close && close != start && start != stop && stop != open, "constants distinct");

        check("Open".equals(open.toString()), "Open name");
        check("Close".equals(close.toString()), "Close name");
        check("Start".equals(start.toString()), "Start name");
        check("Stop".equals(stop.toString()), "Stop name");

        // identity equality
        check(open.equals(open), "reflexive");
        check(!open.equals(close), "different types not equal");
        check(!open.equals(null), "not equal null");
        check(open.hashCode() == open.hashCode(), "hashCode stable");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
