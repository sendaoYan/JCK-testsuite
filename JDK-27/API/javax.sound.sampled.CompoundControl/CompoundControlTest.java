/*
 * @test
 * @summary javax.sound.sampled.CompoundControl conformance: white-box structure + black-box behavior.
 * @run main CompoundControlTest
 */

import java.lang.reflect.Modifier;
import javax.sound.sampled.CompoundControl;
import javax.sound.sampled.Control;

public class CompoundControlTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.sound.sampled.CompoundControl");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(Modifier.isAbstract(mods), "abstract modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.sound.sampled.Control"), "superclass Control");
        check(c.getDeclaredConstructors().length == 1, "constructor count 1");

        Class<?> t = Class.forName("javax.sound.sampled.CompoundControl$Type");
        check(t != null && t.getEnclosingClass() == c, "Type nested");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Control gain = new Control(new Control.Type("Gain") {}) {};
        Control pan  = new Control(new Control.Type("Pan") {}) {};
        Control[] members = { gain, pan };

        CompoundControl.Type type = new CompoundControl.Type("EQ") {};
        CompoundControl cc = new CompoundControl(type, members) {};

        check(cc.getType() == type, "getType identity");
        check("EQ".equals(cc.getType().toString()), "type name");

        Control[] got = cc.getMemberControls();
        check(got.length == 2, "member count 2");
        check(got[0] == gain && got[1] == pan, "member identity preserved");
        check(got != members, "getMemberControls returns a clone");

        // mutating the returned array must not affect the control
        got[0] = null;
        Control[] again = cc.getMemberControls();
        check(again.length == 2, "member count unchanged after external mutation");
        check(again[0] == gain, "original member still present");

        check(cc.toString().contains("Gain") && cc.toString().contains("Pan"), "toString lists member types");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
