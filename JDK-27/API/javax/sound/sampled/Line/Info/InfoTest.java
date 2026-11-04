/*
 * @test
 * @summary javax.sound.sampled.Line.Info conformance: white-box structure + black-box behavior.
 * @run main InfoTest
 */

import java.lang.reflect.Modifier;
import javax.sound.sampled.Clip;
import javax.sound.sampled.Line;
import javax.sound.sampled.SourceDataLine;

public class InfoTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.sound.sampled.Line$Info");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "extends Object");
        check(c.getConstructor(Class.class) != null, "constructor(Class)");
        check(c.getMethod("getLineClass") != null, "method getLineClass");
        check(c.getMethod("matches", Line.Info.class) != null, "method matches");
        check(c.getMethod("toString") != null, "method toString");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        Line.Info info = new Line.Info(SourceDataLine.class);
        check(info.getLineClass() == SourceDataLine.class, "getLineClass round-trip");
        check(info.matches(info), "matches self");
        check(!info.matches(null), "matches null false");
        check(!info.matches(new Line.Info(Clip.class)), "matches other line class false");
        check(info.matches(new Line.Info(SourceDataLine.class)), "matches same line class");
        check(info.toString() != null && info.toString().length() > 0, "toString non-empty");

        Line.Info clipInfo = new Line.Info(Clip.class);
        check(clipInfo.getLineClass() == Clip.class, "Clip info line class");
        check(clipInfo.matches(clipInfo), "Clip matches self");
        check(clipInfo.toString().contains("Clip"), "toString names line class");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
