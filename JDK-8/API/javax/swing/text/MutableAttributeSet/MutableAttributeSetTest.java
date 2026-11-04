/*
 * @test
 * @summary javax.swing.text.MutableAttributeSet conformance: interface shape + mutation contract through SimpleAttributeSet and NamedStyle (headless-safe).
 * @run main MutableAttributeSetTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

public class MutableAttributeSetTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    static void testTypeShape() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.MutableAttributeSet", false, MutableAttributeSetTest.class.getClassLoader());
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(hasInterface(c, "javax.swing.text.AttributeSet"), "extends javax.swing.text.AttributeSet");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("addAttribute"), "method addAttribute");
        check(names.contains("addAttributes"), "method addAttributes");
        check(names.contains("removeAttribute"), "method removeAttribute");
        check(names.contains("removeAttributes"), "method removeAttributes");
        check(names.contains("setResolveParent"), "method setResolveParent");
        // also inherits the full AttributeSet query surface
        check(names.contains("getAttributeCount"), "inherited getAttributeCount");
        check(names.contains("getAttribute"), "inherited getAttribute");
        check(names.contains("isEqual"), "inherited isEqual");
        check(names.contains("getResolveParent"), "inherited getResolveParent");
    }

    static void exercise(MutableAttributeSet a, String tag) {
        a.addAttribute("k1", "v1");
        check("v1".equals(a.getAttribute("k1")), tag + " addAttribute/getAttribute");
        check(a.getAttributeCount() == 1, tag + " count after add");

        SimpleAttributeSet more = new SimpleAttributeSet();
        more.addAttribute("k2", "v2");
        more.addAttribute("k3", "v3");
        a.addAttributes(more);
        check(a.getAttributeCount() == 3, tag + " addAttributes merges");
        check("v2".equals(a.getAttribute("k2")) && "v3".equals(a.getAttribute("k3")), tag + " merged values");

        Vector<Object> names = new Vector<Object>();
        names.add("k2");
        a.removeAttributes(names.elements());
        check(a.getAttribute("k2") == null, tag + " removeAttributes(Enumeration)");
        check(a.isDefined("k1") && a.isDefined("k3"), tag + " others intact");

        a.removeAttribute("k1");
        check(!a.isDefined("k1"), tag + " removeAttribute");

        SimpleAttributeSet parent = new SimpleAttributeSet();
        parent.addAttribute("pk", "pv");
        a.setResolveParent(parent);
        check(a.getResolveParent() == parent, tag + " setResolveParent/getResolveParent");
        check("pv".equals(a.getAttribute("pk")), tag + " parent fallthrough");

        // removeAttributes(AttributeSet) with matching values removes
        SimpleAttributeSet toRemove = new SimpleAttributeSet();
        toRemove.addAttribute("k3", "v3");
        a.removeAttributes(toRemove);
        check(a.getAttribute("k3") == null, tag + " removeAttributes(AttributeSet)");
    }

    static void testThroughSimpleAttributeSet() {
        exercise(new SimpleAttributeSet(), "SAS");
    }

    static void testThroughNamedStyle() {
        StyleContext sc = new StyleContext();
        // anonymous style carries no NameAttribute so counts stay comparable
        Style s = sc.addStyle(null, null);
        check(s.getName() == null, "anonymous style has no name");
        exercise(s, "NamedStyle");
        // NamedStyle.setResolveParent(null) removes the link (unlike raw add/remove).
        s.setResolveParent(null);
        check(s.getResolveParent() == null, "NamedStyle null parent clears");
    }

    public static void main(String[] args) throws Exception {
        testTypeShape();
        testThroughSimpleAttributeSet();
        testThroughNamedStyle();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
