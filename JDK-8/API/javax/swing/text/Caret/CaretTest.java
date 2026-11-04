/*
 * @test
 * @summary javax.swing.text.Caret conformance: type shape via reflection (headless-safe).
 * @run main CaretTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class CaretTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.text.Caret", false, CaretTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface(), "is interface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "interface has no superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("install"), "method install");
        check(names.contains("deinstall"), "method deinstall");
        check(names.contains("paint"), "method paint");
        check(names.contains("addChangeListener"), "method addChangeListener");
        check(names.contains("removeChangeListener"), "method removeChangeListener");
        check(names.contains("isVisible"), "method isVisible");
        check(names.contains("setVisible"), "method setVisible");
        check(names.contains("isSelectionVisible"), "method isSelectionVisible");
        check(names.contains("setDot"), "method setDot");
        check(names.contains("moveDot"), "method moveDot");
        check(names.contains("getDot"), "method getDot");
        check(names.contains("getMark"), "method getMark");
        check(names.contains("setBlinkRate"), "method setBlinkRate");
        check(names.contains("getBlinkRate"), "method getBlinkRate");
        check(names.contains("setMagicCaretPosition"), "method setMagicCaretPosition");
        check(names.contains("getMagicCaretPosition"), "method getMagicCaretPosition");

        testDefaultCaretModelState();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testDefaultCaretModelState() {
        // DefaultCaret's pure model state is observable before installation
        javax.swing.text.DefaultCaret c = new javax.swing.text.DefaultCaret();
        check(c instanceof javax.swing.text.Caret, "DefaultCaret implements Caret");
        check(c.getDot() == 0 && c.getMark() == 0, "dot and mark start at 0");
        check(c.getBlinkRate() == 0, "blink rate starts at 0");
        c.setBlinkRate(300);
        check(c.getBlinkRate() == 300, "blink rate settable");
        check(!c.isVisible(), "not visible before install");
        check(!c.isSelectionVisible(), "selection not visible before install");
        java.awt.Point p = new java.awt.Point(1, 2);
        c.setMagicCaretPosition(p);
        check(c.getMagicCaretPosition() != null && c.getMagicCaretPosition().x == 1, "magic position stored");
        check(c.getChangeListeners().length == 0, "no listeners initially");
        // moving the caret requires an installed text component
        try {
            c.setDot(3);
            check(false, "setDot before install should NPE");
        } catch (NullPointerException e) {
            check(true, "setDot before install NPE");
        }
    }
}
