/*
 * @test
 * @summary javax.swing.LayoutStyle conformance: type shape via reflection + shared-instance
 *          lifecycle, ComponentPlacement values, Metal spacing values (headless-safe).
 * @run main LayoutStyleTest
 */

import java.awt.Container;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

public class LayoutStyleTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    static void testStructure() throws Exception {
        Class<?> c = Class.forName("javax.swing.LayoutStyle", false, LayoutStyleTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("setInstance"), "method setInstance");
        check(names.contains("getInstance"), "method getInstance");
        check(names.contains("getPreferredGap"), "method getPreferredGap");
        check(names.contains("getContainerGap"), "method getContainerGap");
    }

    static void testPlacementEnum() {
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
        // a custom instance takes over until setInstance(null) restores the shared one
        final LayoutStyle custom = new LayoutStyle() {
            @Override
            public int getPreferredGap(JComponent a, JComponent b,
                    LayoutStyle.ComponentPlacement placement, int position, Container parent) {
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
        UIManager.setLookAndFeel(new javax.swing.plaf.metal.MetalLookAndFeel());
        testStructure();
        testPlacementEnum();
        testInstanceLifecycle();
        testMetalSpacing();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
