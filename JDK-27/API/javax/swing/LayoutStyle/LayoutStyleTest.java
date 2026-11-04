/*
 * @test
 * @summary javax.swing.LayoutStyle conformance: white-box structure + black-box behavior.
 * @run main LayoutStyleTest
 */

import java.awt.Container;
import javax.swing.JLabel;
import java.lang.reflect.Modifier;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;

public class LayoutStyleTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.LayoutStyle");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isProtected(mods), "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(Modifier.isAbstract(mods), "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        LayoutStyle ls = LayoutStyle.getInstance();
        check(ls != null, "default instance");
        check(ls == LayoutStyle.getInstance(), "singleton instance");

        LayoutStyle custom = new LayoutStyle() {
            public int getPreferredGap(JComponent a, JComponent b,
                    LayoutStyle.ComponentPlacement placement, int position, Container parent) {
                return 7;
            }
            public int getContainerGap(JComponent component, int position, Container parent) {
                return 3;
            }
        };
        JPanel p = new JPanel();
        check(custom.getPreferredGap(p, p, LayoutStyle.ComponentPlacement.RELATED,
                SwingConstants.WEST, p) == 7, "getPreferredGap");
        check(custom.getContainerGap(p, SwingConstants.WEST, p) == 3, "getContainerGap");
    }


    // ---- Part 3: deep contract coverage ----
    static void testPlacementEnum() throws Exception {
        javax.swing.UIManager.setLookAndFeel(new javax.swing.plaf.metal.MetalLookAndFeel());
        LayoutStyle.ComponentPlacement[] vals = LayoutStyle.ComponentPlacement.values();
        check(vals.length == 3, "ComponentPlacement has 3 constants");
        check(LayoutStyle.ComponentPlacement.RELATED.ordinal() == 0, "RELATED ordinal 0");
        check(LayoutStyle.ComponentPlacement.UNRELATED.ordinal() == 1, "UNRELATED ordinal 1");
        check(LayoutStyle.ComponentPlacement.INDENT.ordinal() == 2, "INDENT ordinal 2");
        check(LayoutStyle.ComponentPlacement.valueOf("RELATED") == LayoutStyle.ComponentPlacement.RELATED,
                "valueOf RELATED");
    }

    static void testInstanceLifecycle() {
        LayoutStyle shared = LayoutStyle.getInstance();
        check(shared != null, "getInstance non-null");
        check(LayoutStyle.getInstance() == shared, "getInstance returns the same shared instance");
        final LayoutStyle custom = new LayoutStyle() {
            @Override
            public int getPreferredGap(JComponent a, JComponent b,
                    ComponentPlacement placement, int position, Container parent) {
                return 7;
            }
            @Override
            public int getContainerGap(JComponent component, int position, Container parent) {
                return 3;
            }
        };
        LayoutStyle.setInstance(custom);
        check(LayoutStyle.getInstance() == custom, "setInstance(custom) visible");
        JLabel l1 = new JLabel();
        JLabel l2 = new JLabel();
        JPanel any = new JPanel();
        check(custom.getPreferredGap(l1, l2, LayoutStyle.ComponentPlacement.RELATED, SwingConstants.EAST, any) == 7,
                "custom getPreferredGap override returns 7");
        check(custom.getContainerGap(l1, SwingConstants.EAST, any) == 3,
                "custom getContainerGap override returns 3");
        LayoutStyle.setInstance(null);
        check(LayoutStyle.getInstance() == shared, "setInstance(null) restores the shared instance");
        check(LayoutStyle.getInstance() != custom, "custom no longer installed");
    }

    static void testMetalSpacing() {
        // under Metal: RELATED components are 6px apart in every direction,
        // UNRELATED/INDENT 12px, container padding 12px
        JLabel l1 = new JLabel();
        JLabel l2 = new JLabel();
        JPanel any = new JPanel();
        LayoutStyle ls = LayoutStyle.getInstance();
        check(ls.getPreferredGap(l1, l2, LayoutStyle.ComponentPlacement.RELATED, SwingConstants.EAST, any) == 6,
                "Metal RELATED EAST == 6");
        check(ls.getPreferredGap(l1, l2, LayoutStyle.ComponentPlacement.RELATED, SwingConstants.WEST, any) == 6,
                "Metal RELATED WEST == 6");
        check(ls.getPreferredGap(l1, l2, LayoutStyle.ComponentPlacement.RELATED, SwingConstants.NORTH, any) == 6,
                "Metal RELATED NORTH == 6");
        check(ls.getPreferredGap(l1, l2, LayoutStyle.ComponentPlacement.RELATED, SwingConstants.SOUTH, any) == 6,
                "Metal RELATED SOUTH == 6");
        check(ls.getPreferredGap(l1, l2, LayoutStyle.ComponentPlacement.UNRELATED, SwingConstants.EAST, any) == 12,
                "Metal UNRELATED EAST == 12");
        check(ls.getPreferredGap(l1, l2, LayoutStyle.ComponentPlacement.INDENT, SwingConstants.EAST, any) == 12,
                "Metal INDENT EAST == 12");
        check(ls.getContainerGap(l1, SwingConstants.NORTH, any) == 12, "Metal container gap NORTH == 12");
        check(ls.getContainerGap(l1, SwingConstants.EAST, any) == 12, "Metal container gap EAST == 12");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        javax.swing.UIManager.setLookAndFeel(new javax.swing.plaf.metal.MetalLookAndFeel());
        testBlackBox();
        testPlacementEnum();
        testInstanceLifecycle();
        testMetalSpacing();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
