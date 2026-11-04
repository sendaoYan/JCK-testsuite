/*
 * @test
 * @summary javax.swing.Spring conformance: type shape via reflection + spring algebra
 *          (constant/sum/minus/max/scale/width/height) exact values (headless-safe).
 * @run main SpringTest
 */

import java.awt.Dimension;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Spring;
import javax.swing.UIManager;

public class SpringTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    static void testStructure() throws Exception {
        Class<?> c = Class.forName("javax.swing.Spring", false, SpringTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getMinimumValue"), "method getMinimumValue");
        check(names.contains("getPreferredValue"), "method getPreferredValue");
        check(names.contains("getMaximumValue"), "method getMaximumValue");
        check(names.contains("getValue"), "method getValue");
        check(names.contains("setValue"), "method setValue");
        check(names.contains("constant"), "method constant");
        check(names.contains("minus"), "method minus");
    }

    static void testConstants() {
        check(Spring.UNSET == Integer.MIN_VALUE, "UNSET == Integer.MIN_VALUE");
        Spring five = Spring.constant(5);
        check(five.getValue() == 5, "constant(5) value");
        check(five.getMinimumValue() == 5 && five.getPreferredValue() == 5 && five.getMaximumValue() == 5,
                "constant(5) range collapses to 5");
        Spring range = Spring.constant(2, 6, 10);
        check(range.getMinimumValue() == 2, "constant(2,6,10) minimum");
        check(range.getPreferredValue() == 6, "constant(2,6,10) preferred");
        check(range.getMaximumValue() == 10, "constant(2,6,10) maximum");
        // setValue stores whatever it is given; range springs never clamp
        range.setValue(99);
        check(range.getValue() == 99, "range setValue(99) not clamped to max");
        Spring neg = Spring.constant(-3);
        check(neg.getValue() == -3, "negative constant");
    }

    static void testAlgebra() {
        Spring a = Spring.constant(10);
        Spring b = Spring.constant(5);
        check(Spring.sum(a, b).getValue() == 15, "sum(10,5) == 15");
        check(Spring.minus(a).getValue() == -10, "minus(10) == -10");
        check(Spring.max(a, b).getValue() == 10, "max(10,5) == 10");
        check(Spring.scale(a, 2.0f).getValue() == 20, "scale(10,2f) == 20");

        // composite springs compute their range from the operands' ranges
        Spring r1 = Spring.constant(2, 6, 10);
        Spring r2 = Spring.constant(1, 3, 5);
        Spring sum = Spring.sum(r1, r2);
        check(sum.getMinimumValue() == 3, "sum minimum 2+1");
        check(sum.getPreferredValue() == 9, "sum preferred 6+3");
        check(sum.getMaximumValue() == 15, "sum maximum 10+5");
        Spring neg = Spring.minus(r1);
        check(neg.getMinimumValue() == -10, "minus minimum -10");
        check(neg.getPreferredValue() == -6, "minus preferred -6");
        check(neg.getMaximumValue() == -2, "minus maximum -2");
        Spring big = Spring.max(r1, Spring.constant(1, 3, 20));
        check(big.getMinimumValue() == 2, "max minimum 2");
        check(big.getPreferredValue() == 6, "max preferred 6");
        check(big.getMaximumValue() == 20, "max maximum 20");
        // scale rounds to nearest int (half rounds up) and supports negative factors
        Spring sc = Spring.scale(Spring.constant(2, 4, 6), 0.5f);
        check(sc.getMinimumValue() == 1, "scale 0.5 minimum 1");
        check(sc.getPreferredValue() == 2, "scale 0.5 preferred 2");
        check(sc.getMaximumValue() == 3, "scale 0.5 maximum 3");
        check(Spring.scale(Spring.constant(7), 0.5f).getValue() == 4, "scale(7,0.5f) rounds half up to 4");
        check(Spring.scale(Spring.constant(7), -1.0f).getValue() == -7, "scale(7,-1f) == -7");
    }

    static void testComponentSprings() {
        // width/height track the component's min/pref/max exactly
        JPanel fixed = new JPanel();
        fixed.setMinimumSize(new Dimension(3, 5));
        fixed.setPreferredSize(new Dimension(25, 15));
        fixed.setMaximumSize(new Dimension(40, 50));
        Spring w = Spring.width(fixed);
        check(w.getMinimumValue() == 3, "width minimum from component");
        check(w.getPreferredValue() == 25, "width preferred from component");
        check(w.getMaximumValue() == 40, "width maximum from component");
        Spring h = Spring.height(fixed);
        check(h.getMinimumValue() == 5, "height minimum from component");
        check(h.getPreferredValue() == 15, "height preferred from component");
        check(h.getMaximumValue() == 50, "height maximum from component");
        // unshown component with only a preferred size: min/max report 0
        JLabel label = new JLabel();
        label.setPreferredSize(new Dimension(25, 15));
        Spring lw = Spring.width(label);
        check(lw.getMinimumValue() == 0, "preferred-only width minimum 0");
        check(lw.getPreferredValue() == 25, "preferred-only width preferred 25");
        check(lw.getMaximumValue() == 0, "preferred-only width maximum 0");
    }

    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(new javax.swing.plaf.metal.MetalLookAndFeel());
        testStructure();
        testConstants();
        testAlgebra();
        testComponentSprings();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
