/*
 * @test
 * @summary javax.swing.LayoutFocusTraversalPolicy conformance: white-box structure + black-box behavior.
 * @run main LayoutFocusTraversalPolicyTest
 */

import java.lang.reflect.Modifier;

import javax.swing.JPanel;
import javax.swing.LayoutFocusTraversalPolicy;

public class LayoutFocusTraversalPolicyTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.LayoutFocusTraversalPolicy");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isProtected(mods), "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.SortingFocusTraversalPolicy"), "superclass");
        check(!Modifier.isAbstract(mods), "abstract modifier");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("java.io.Serializable"), "interface 0");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        LayoutFocusTraversalPolicy p = new LayoutFocusTraversalPolicy();
        check(p.getImplicitDownCycleTraversal(), "implicit down cycle default");
        p.setImplicitDownCycleTraversal(false);
        check(!p.getImplicitDownCycleTraversal(), "implicit down cycle toggled");
        p.setImplicitDownCycleTraversal(true);
        check(p.getImplicitDownCycleTraversal(), "implicit down cycle restored");

        JPanel panel = new JPanel();
        panel.setFocusCycleRoot(true);
        JPanel child = new JPanel();
        panel.add(child);
        // non-displayable container yields no traversable component
        check(p.getFirstComponent(panel) == null, "first null for empty panel");
        check(p.getLastComponent(panel) == null, "last null for empty panel");
        check(p.getDefaultComponent(panel) == null, "default null for empty panel");
        check(p.getComponentAfter(panel, child) == null, "componentAfter null");
        check(p.getComponentBefore(panel, child) == null, "componentBefore null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
