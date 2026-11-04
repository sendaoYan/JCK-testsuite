/*
 * @test
 * @summary javax.sound.sampled.FloatControl.Type conformance: white-box structure + black-box behavior.
 * @run main TypeTest
 */

import java.lang.reflect.Modifier;
import javax.sound.sampled.Control;
import javax.sound.sampled.FloatControl;

public class TypeTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.sound.sampled.FloatControl$Type");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods) && Modifier.isStatic(mods), "public static");
        check(c.getEnclosingClass() == FloatControl.class, "enclosing class");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.sound.sampled.Control$Type"), "superclass Control.Type");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        FloatControl.Type[] types = {
            FloatControl.Type.MASTER_GAIN,
            FloatControl.Type.AUX_SEND,
            FloatControl.Type.AUX_RETURN,
            FloatControl.Type.REVERB_SEND,
            FloatControl.Type.REVERB_RETURN,
            FloatControl.Type.VOLUME,
            FloatControl.Type.PAN,
            FloatControl.Type.BALANCE,
            FloatControl.Type.SAMPLE_RATE,
        };

        check(types.length == 9, "nine constants");
        for (int i = 0; i < types.length; i++) {
            check(types[i] != null, "constant " + i + " non-null");
            check(types[i] instanceof Control.Type, "constant " + i + " is Control.Type");
        }
        for (int i = 0; i < types.length; i++) {
            for (int j = i + 1; j < types.length; j++) {
                check(types[i] != types[j], "constants " + i + " and " + j + " distinct");
            }
        }

        check("Master Gain".equals(types[0].toString()), "MASTER_GAIN name");
        check("AUX Send".equals(types[1].toString()), "AUX_SEND name");
        check("AUX Return".equals(types[2].toString()), "AUX_RETURN name");
        check("Reverb Send".equals(types[3].toString()), "REVERB_SEND name");
        check("Reverb Return".equals(types[4].toString()), "REVERB_RETURN name");
        check("Volume".equals(types[5].toString()), "VOLUME name");
        check("Pan".equals(types[6].toString()), "PAN name");
        check("Balance".equals(types[7].toString()), "BALANCE name");
        check("Sample Rate".equals(types[8].toString()), "SAMPLE_RATE name");

        // identity equality
        check(types[0].equals(types[0]), "reflexive");
        check(!types[0].equals(types[5]), "different types not equal");
        check(!types[0].equals(null), "not equal null");
        check(types[0].hashCode() == types[0].hashCode(), "hashCode stable");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
