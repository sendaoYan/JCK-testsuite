/*
 * @test
 * @summary javax.swing.text.AttributeSet conformance: interface shape + cross-implementation contract (SimpleAttributeSet / StyleContext sets / EMPTY) (headless-safe).
 * @run main AttributeSetTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

public class AttributeSetTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    static void testTypeShape() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.AttributeSet", false, AttributeSetTest.class.getClassLoader());
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(!c.isEnum() && !c.isAnnotation(), "not enum/annotation");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getAttributeCount"), "method getAttributeCount");
        check(names.contains("isDefined"), "method isDefined");
        check(names.contains("isEqual"), "method isEqual");
        check(names.contains("copyAttributes"), "method copyAttributes");
        check(names.contains("getAttribute"), "method getAttribute");
        check(names.contains("getAttributeNames"), "method getAttributeNames");
        check(names.contains("containsAttribute"), "method containsAttribute");
        check(names.contains("containsAttributes"), "method containsAttributes");
        check(names.contains("getResolveParent"), "method getResolveParent");
        // well-known keys defined by the interface
        Set<java.lang.reflect.Field> fields = new HashSet<java.lang.reflect.Field>();
        for (java.lang.reflect.Field f : c.getFields()) fields.add(f);
        check(fields.size() >= 2, "interface declares key constants");
    }

    static void testWellKnownKeys() {
        // The interface keys are the very same objects aliased by StyleConstants.
        check(AttributeSet.NameAttribute == StyleConstants.NameAttribute, "NameAttribute identity with StyleConstants");
        check(AttributeSet.ResolveAttribute == StyleConstants.ResolveAttribute, "ResolveAttribute identity with StyleConstants");
        check("name".equals(AttributeSet.NameAttribute.toString()), "NameAttribute toString");
        check("resolver".equals(AttributeSet.ResolveAttribute.toString()), "ResolveAttribute toString");
    }

    static void testCrossImplementationEquals() {
        StyleContext sc = new StyleContext();
        AttributeSet empty = sc.getEmptySet();
        SimpleAttributeSet simpleEmpty = new SimpleAttributeSet();
        check(empty.getAttributeCount() == 0, "context empty count 0");
        check(empty.equals(simpleEmpty), "context empty equals simple empty");
        check(simpleEmpty.equals(empty), "simple empty equals context empty");
        check(empty.getResolveParent() == null, "context empty no parent");

        AttributeSet small = sc.addAttribute(empty, "k", "v");
        SimpleAttributeSet simple = new SimpleAttributeSet();
        simple.addAttribute("k", "v");
        check(small.getAttributeCount() == 1, "small count 1");
        check(simple.getAttributeCount() == 1, "simple count 1");
        check(small.equals(simple), "small equals simple");
        check(simple.equals(small), "simple equals small (symmetric)");
        check(small.isEqual(simple) && simple.isEqual(small), "isEqual symmetric");
        check(small.containsAttribute("k", "v"), "small containsAttribute");
        check(small.isDefined("k"), "small isDefined");
        check(!small.isDefined("z"), "small not defined z");
        check(small.getAttribute("z") == null, "small missing null");

        // Mutable view through the context: adding a second attribute keeps equality
        AttributeSet small2 = sc.addAttribute(small, "k2", "v2");
        check(small2.getAttributeCount() == 2, "small2 count 2");
        check(!small2.equals(simple), "different content not equal");
        check(small.getAttributeCount() == 1, "original small unchanged");

        // removal drops back to empty-equivalent content
        AttributeSet back = sc.removeAttribute(small, "k");
        check(back.getAttributeCount() == 0, "removeAttribute back to empty count");
        check(back.equals(simpleEmpty), "removed set equals empty");
    }

    static void testContractThroughImplementations() {
        AttributeSet[] impls = {
            new SimpleAttributeSet(),
            SimpleAttributeSet.EMPTY,
            new StyleContext().getEmptySet(),
        };
        for (AttributeSet a : impls) {
            check(a.getAttributeCount() == 0, implName(a) + " count 0");
            check(!a.isDefined("x"), implName(a) + " not defined");
            check(a.getAttribute("x") == null, implName(a) + " missing null");
            check(!a.getAttributeNames().hasMoreElements(), implName(a) + " names empty");
            check(!a.containsAttribute("x", "y"), implName(a) + " containsAttribute false");
            check(a.getResolveParent() == null, implName(a) + " no resolve parent");
            AttributeSet copy = a.copyAttributes();
            check(copy.getAttributeCount() == 0, implName(a) + " copy empty");
            check(copy != null, implName(a) + " copy non-null");
        }
    }

    static String implName(AttributeSet a) {
        return a.getClass().getSimpleName();
    }

    public static void main(String[] args) throws Exception {
        testTypeShape();
        testWellKnownKeys();
        testCrossImplementationEquals();
        testContractThroughImplementations();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
