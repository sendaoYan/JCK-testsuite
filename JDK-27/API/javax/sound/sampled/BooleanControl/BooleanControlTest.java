/*
 * @test
 * @summary javax.sound.sampled.BooleanControl conformance: white-box structure + black-box behavior.
 * @run main BooleanControlTest
 */

import java.lang.reflect.Modifier;
import javax.sound.sampled.BooleanControl;

public class BooleanControlTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.sound.sampled.BooleanControl");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(Modifier.isAbstract(mods), "abstract modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.sound.sampled.Control"), "superclass Control");
        check(c.getDeclaredConstructors().length == 2, "constructor count 2");

        Class<?> t = Class.forName("javax.sound.sampled.BooleanControl$Type");
        check(t != null && t.getEnclosingClass() == c, "Type nested");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // labeled constructor
        BooleanControl bc = new BooleanControl(BooleanControl.Type.MUTE, true, "on", "off") {};
        check(bc.getType() == BooleanControl.Type.MUTE, "type MUTE");
        check(bc.getValue(), "initial value true");
        check("on".equals(bc.getStateLabel(true)), "true label");
        check("off".equals(bc.getStateLabel(false)), "false label");
        bc.setValue(false);
        check(!bc.getValue(), "setValue false");
        check(bc.toString().contains("off"), "toString reflects false label");
        bc.setValue(true);
        check(bc.getValue(), "setValue true");

        // default labels constructor
        BooleanControl bc2 = new BooleanControl(BooleanControl.Type.APPLY_REVERB, false) {};
        check(bc2.getType() == BooleanControl.Type.APPLY_REVERB, "type APPLY_REVERB");
        check(!bc2.getValue(), "initial value false");
        check("true".equals(bc2.getStateLabel(true)), "default true label");
        check("false".equals(bc2.getStateLabel(false)), "default false label");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
