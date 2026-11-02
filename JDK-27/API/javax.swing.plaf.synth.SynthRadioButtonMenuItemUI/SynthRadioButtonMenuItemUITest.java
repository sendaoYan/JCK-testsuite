/*
 * @test
 * @summary javax.swing.plaf.synth.SynthRadioButtonMenuItemUI conformance: type shape and supertypes.
 * @run main SynthRadioButtonMenuItemUITest
 */

import java.lang.reflect.Modifier;

public class SynthRadioButtonMenuItemUITest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.plaf.synth.SynthRadioButtonMenuItemUI");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.plaf.synth.SynthMenuItemUI"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
