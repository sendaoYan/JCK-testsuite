/*
 * @test
 * @summary javax.swing.plaf.synth.SynthConstants conformance: white-box structure + black-box behavior.
 * @run main SynthConstantsTest
 */

import java.lang.reflect.Modifier;
import javax.swing.plaf.synth.SynthConstants;

public class SynthConstantsTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.plaf.synth.SynthConstants");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == true, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    static void testBlackBox() throws Exception {
        check(SynthConstants.ENABLED == 1, "ENABLED == 1");
        check(SynthConstants.MOUSE_OVER == 2, "MOUSE_OVER == 2");
        check(SynthConstants.PRESSED == 4, "PRESSED == 4");
        check(SynthConstants.DISABLED == 8, "DISABLED == 8");
        check(SynthConstants.FOCUSED == 256, "FOCUSED == 256");
        check(SynthConstants.SELECTED == 512, "SELECTED == 512");
        check(SynthConstants.DEFAULT == 1024, "DEFAULT == 1024");

        check((SynthConstants.ENABLED | SynthConstants.FOCUSED) == 257, "ENABLED|FOCUSED == 257");
        check((SynthConstants.ENABLED | SynthConstants.SELECTED) == 513, "ENABLED|SELECTED == 513");
        check((SynthConstants.DISABLED | SynthConstants.FOCUSED) == 264, "DISABLED|FOCUSED == 264");
        check((SynthConstants.ENABLED & SynthConstants.DISABLED) == 0, "ENABLED/DISABLED disjoint");
        check((SynthConstants.FOCUSED & SynthConstants.SELECTED) == 0, "FOCUSED/SELECTED disjoint");
    }
}
