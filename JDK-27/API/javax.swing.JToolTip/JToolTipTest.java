/*
 * @test
 * @summary javax.swing.JToolTip conformance: white-box structure + black-box behavior.
 * @run main JToolTipTest
 */

import java.lang.reflect.Modifier;

import javax.swing.JPanel;
import javax.swing.JToolTip;

public class JToolTipTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.JToolTip");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isProtected(mods), "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.JComponent"), "superclass");
        check(!Modifier.isAbstract(mods), "abstract modifier");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.accessibility.Accessible"), "interface 0");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        JToolTip tip = new JToolTip();
        check(tip.getComponent() == null, "component initially null");
        tip.setTipText("tip text");
        check("tip text".equals(tip.getTipText()), "setTipText/getTipText");
        JPanel c = new JPanel();
        tip.setComponent(c);
        check(tip.getComponent() == c, "setComponent/getComponent");
        check(tip.getUIClassID() != null, "UIClassID");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
