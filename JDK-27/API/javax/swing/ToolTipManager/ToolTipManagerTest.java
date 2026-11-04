/*
 * @test
 * @summary javax.swing.ToolTipManager conformance: white-box structure + black-box behavior.
 * @run main ToolTipManagerTest
 */

import java.lang.reflect.Modifier;

import javax.swing.JPanel;
import javax.swing.ToolTipManager;

public class ToolTipManagerTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.ToolTipManager");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isProtected(mods), "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.awt.event.MouseAdapter"), "superclass");
        check(Modifier.isFinal(mods), "final modifier");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("java.awt.event.MouseMotionListener"), "interface 0");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        ToolTipManager m = ToolTipManager.sharedInstance();
        check(m != null, "sharedInstance");
        check(ToolTipManager.sharedInstance() == m, "shared singleton");
        check(m.isEnabled(), "enabled default");
        m.setEnabled(false);
        check(!m.isEnabled(), "disabled");
        m.setEnabled(true);

        m.setLightWeightPopupEnabled(false);
        check(!m.isLightWeightPopupEnabled(), "lightweight popup");
        m.setInitialDelay(500);
        check(m.getInitialDelay() == 500, "initial delay");
        m.setDismissDelay(6000);
        check(m.getDismissDelay() == 6000, "dismiss delay");
        m.setReshowDelay(300);
        check(m.getReshowDelay() == 300, "reshow delay");

        JPanel p = new JPanel();
        m.registerComponent(p);
        m.unregisterComponent(p);
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
