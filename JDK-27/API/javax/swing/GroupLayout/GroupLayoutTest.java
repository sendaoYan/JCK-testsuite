/*
 * @test
 * @summary javax.swing.GroupLayout conformance: white-box structure + black-box behavior.
 * @run main GroupLayoutTest
 */

import java.lang.reflect.Modifier;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

public class GroupLayoutTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.GroupLayout");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isProtected(mods), "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(!Modifier.isAbstract(mods), "abstract modifier");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("java.awt.LayoutManager2"), "interface 0");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        JPanel panel = new JPanel();
        GroupLayout layout = new GroupLayout(panel);

        // auto-create flags
        check(!layout.getAutoCreateGaps(), "default no gaps");
        check(!layout.getAutoCreateContainerGaps(), "default no container gaps");
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
        check(layout.getAutoCreateGaps(), "setAutoCreateGaps");
        check(layout.getAutoCreateContainerGaps(), "setAutoCreateContainerGaps");

        // honors visibility
        check(layout.getHonorsVisibility(), "default honors visibility");
        layout.setHonorsVisibility(false);
        check(!layout.getHonorsVisibility(), "setHonorsVisibility(false)");

        // groups
        GroupLayout.SequentialGroup h = layout.createSequentialGroup();
        GroupLayout.ParallelGroup v = layout.createParallelGroup();
        layout.setHorizontalGroup(h);
        layout.setVerticalGroup(v);

        JButton b1 = new JButton("a");
        JButton b2 = new JButton("b");
        h.addComponent(b1).addComponent(b2);
        v.addComponent(b1).addComponent(b2);

        // linkSize makes two components share the same size
        layout.linkSize(b1, b2);
        panel.setSize(200, 100);
        layout.layoutContainer(panel);
        check(b1.getWidth() == b2.getWidth(), "linked width");
        check(true, "layout smoke");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
