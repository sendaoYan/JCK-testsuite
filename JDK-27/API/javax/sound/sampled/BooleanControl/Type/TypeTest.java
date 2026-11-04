/*
 * @test
 * @summary javax.sound.sampled.BooleanControl.Type conformance: white-box structure + black-box behavior.
 * @run main TypeTest
 */

import java.lang.reflect.Modifier;
import javax.sound.sampled.BooleanControl;
import javax.sound.sampled.Control;

public class TypeTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.sound.sampled.BooleanControl$Type");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods) && Modifier.isStatic(mods), "public static");
        check(c.getEnclosingClass() == BooleanControl.class, "enclosing class");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.sound.sampled.Control$Type"), "superclass Control.Type");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        BooleanControl.Type mute = BooleanControl.Type.MUTE;
        BooleanControl.Type apply = BooleanControl.Type.APPLY_REVERB;

        check(mute != null && apply != null, "constants non-null");
        check(mute != apply, "constants distinct");

        check("Mute".equals(mute.toString()), "Mute name");
        check("Apply Reverb".equals(apply.toString()), "Apply Reverb name");

        check(mute instanceof Control.Type, "MUTE is a Control.Type");
        check(apply instanceof Control.Type, "APPLY_REVERB is a Control.Type");

        // identity equality
        check(mute.equals(mute), "MUTE equals itself");
        check(!mute.equals(apply), "MUTE != APPLY_REVERB");
        check(!mute.equals(null), "not equal null");
        check(mute.hashCode() == mute.hashCode(), "hashCode stable");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
