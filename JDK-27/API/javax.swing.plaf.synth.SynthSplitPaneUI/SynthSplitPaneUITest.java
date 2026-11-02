/*
 * @test
 * @summary javax.swing.plaf.synth.SynthSplitPaneUI conformance: type shape and supertypes.
 * @run main SynthSplitPaneUITest
 */

import java.lang.reflect.Modifier;

public class SynthSplitPaneUITest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.plaf.synth.SynthSplitPaneUI");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.plaf.basic.BasicSplitPaneUI"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 2, "interface count");
        check(c.getInterfaces()[0].getName().equals("java.beans.PropertyChangeListener"), "interface 0");
        check(c.getInterfaces()[1].getName().equals("javax.swing.plaf.synth.SynthUI"), "interface 1");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
