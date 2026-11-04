/*
 * @test
 * @summary javax.swing.SortingFocusTraversalPolicy conformance: white-box structure + black-box behavior.
 * @run main SortingFocusTraversalPolicyTest
 */

import java.awt.Component;
import java.lang.reflect.Modifier;
import java.util.Comparator;

import javax.swing.JPanel;
import javax.swing.SortingFocusTraversalPolicy;

public class SortingFocusTraversalPolicyTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.SortingFocusTraversalPolicy");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isProtected(mods), "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.InternalFrameFocusTraversalPolicy"), "superclass");
        check(!Modifier.isAbstract(mods), "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        Comparator<Component> cmp = new Comparator<Component>() {
            public int compare(Component a, Component b) { return 0; }
        };
        SortingFocusTraversalPolicy p = new SortingFocusTraversalPolicy(cmp);
        check(p.getImplicitDownCycleTraversal(), "implicit down cycle default");
        p.setImplicitDownCycleTraversal(false);
        check(!p.getImplicitDownCycleTraversal(), "implicit down cycle toggled");

        JPanel panel = new JPanel();
        panel.setFocusCycleRoot(true);
        check(p.getFirstComponent(panel) == null, "first null for empty panel");
        check(p.getLastComponent(panel) == null, "last null for empty panel");
        check(p.getDefaultComponent(panel) == null, "default null for empty panel");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
