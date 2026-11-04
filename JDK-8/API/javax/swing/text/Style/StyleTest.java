/*
 * @test
 * @summary javax.swing.text.Style conformance: interface shape + Style contract via NamedStyle (name/change listeners/mutable attributes) (headless-safe).
 * @run main StyleTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

public class StyleTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    static void testTypeShape() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.Style", false, StyleTest.class.getClassLoader());
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(hasInterface(c, "javax.swing.text.MutableAttributeSet"), "extends MutableAttributeSet");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getName"), "method getName");
        check(names.contains("addChangeListener"), "method addChangeListener");
        check(names.contains("removeChangeListener"), "method removeChangeListener");
        // inherited mutation surface
        check(names.contains("addAttribute"), "inherited addAttribute");
        check(names.contains("setResolveParent"), "inherited setResolveParent");
        check(names.contains("getAttribute"), "inherited getAttribute");
    }

    static void testNameContract() {
        StyleContext sc = new StyleContext();
        Style named = sc.addStyle("n0", null);
        check("n0".equals(named.getName()), "named style getName");
        // The name is carried as the NameAttribute entry.
        check(named.isDefined(StyleConstants.NameAttribute), "name stored as NameAttribute");
        named.addAttribute(StyleConstants.NameAttribute, "n1");
        check("n1".equals(named.getName()), "name follows NameAttribute");
        Style anon = sc.addStyle(null, null);
        check(anon.getName() == null, "anonymous getName null");
        check(!anon.isDefined(StyleConstants.NameAttribute), "anonymous has no NameAttribute");
    }

    static void testChangeListenerContract() {
        StyleContext sc = new StyleContext();
        Style s = sc.addStyle("cl", null);
        final int[] count = { 0 };
        final Object[] source = { null };
        ChangeListener l = new ChangeListener() {
            public void stateChanged(ChangeEvent e) { count[0]++; source[0] = e.getSource(); }
        };
        s.addChangeListener(l);
        s.addAttribute("k", "v");
        check(count[0] == 1, "attribute change fires listener");
        check(source[0] == s, "event source is the style");
        s.addAttributes(new SimpleAttributeSet());
        check(count[0] == 2, "addAttributes fires");
        s.removeAttribute("k");
        check(count[0] == 3, "removeAttribute fires");

        s.removeChangeListener(l);
        s.addAttribute("k2", "v2");
        check(count[0] == 3, "no events after removeChangeListener");
    }

    static void testStyleHierarchy() {
        StyleContext sc = new StyleContext();
        Style parent = sc.addStyle("parent", null);
        StyleConstants.setFontSize(parent, 40);
        Style child = sc.addStyle("child", parent);
        check(child.getResolveParent() == parent, "parent link");
        check(StyleConstants.getFontSize(child) == 40, "attribute resolved through parent");
        StyleConstants.setFontSize(child, 8);
        check(StyleConstants.getFontSize(child) == 8, "local value shadows parent");
        check(StyleConstants.getFontSize(parent) == 40, "parent unaffected");
        child.setResolveParent(null);
        check(child.getResolveParent() == null, "parent cleared");
        check(StyleConstants.getFontSize(child) == 8, "local value kept after unlink");
        check(child.getAttribute(StyleConstants.FontSize) != null, "still has local size");
    }

    public static void main(String[] args) throws Exception {
        testTypeShape();
        testNameContract();
        testChangeListenerContract();
        testStyleHierarchy();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
