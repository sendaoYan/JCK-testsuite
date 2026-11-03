/*
 * @test
 * @summary javax.sound.sampled.EnumControl.Type conformance: white-box structure + black-box behavior.
 * @run main TypeTest
 */

import java.lang.reflect.Modifier;
import javax.sound.sampled.Control;
import javax.sound.sampled.EnumControl;
import javax.sound.sampled.FloatControl;

public class TypeTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.sound.sampled.EnumControl$Type");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods) && Modifier.isStatic(mods), "public static");
        check(c.getEnclosingClass() == EnumControl.class, "enclosing class");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.sound.sampled.Control$Type"), "superclass Control.Type");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        EnumControl.Type reverb = EnumControl.Type.REVERB;
        check(reverb != null, "REVERB non-null");
        check("Reverb".equals(reverb.toString()), "Reverb name");
        check(reverb instanceof Control.Type, "REVERB is a Control.Type");

        // identity equality
        check(reverb.equals(reverb), "reflexive");
        check(!reverb.equals(FloatControl.Type.VOLUME), "different type not equal");
        check(!reverb.equals(null), "not equal null");
        check(reverb.hashCode() == reverb.hashCode(), "hashCode stable");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
