/*
 * @test
 * @summary javax.swing.text.StyledDocument conformance: type shape via reflection (headless-safe).
 * @run main StyledDocumentTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class StyledDocumentTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.text.StyledDocument", false, StyledDocumentTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface(), "is interface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(hasInterface(c, "javax.swing.text.Document"), "implements javax.swing.text.Document");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("addStyle"), "method addStyle");
        check(names.contains("removeStyle"), "method removeStyle");
        check(names.contains("getStyle"), "method getStyle");
        check(names.contains("setCharacterAttributes"), "method setCharacterAttributes");
        check(names.contains("setParagraphAttributes"), "method setParagraphAttributes");
        check(names.contains("setLogicalStyle"), "method setLogicalStyle");
        check(names.contains("getLogicalStyle"), "method getLogicalStyle");
        check(names.contains("getParagraphElement"), "method getParagraphElement");
        check(names.contains("getCharacterElement"), "method getCharacterElement");
        check(names.contains("getForeground"), "method getForeground");
        check(names.contains("getBackground"), "method getBackground");
        check(names.contains("getFont"), "method getFont");

        testUmbrella();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testUmbrella() throws Exception {
        // DefaultStyledDocument is the reference StyledDocument implementation
        javax.swing.text.DefaultStyledDocument d = new javax.swing.text.DefaultStyledDocument();
        check(d instanceof javax.swing.text.StyledDocument, "DefaultStyledDocument is a StyledDocument");
        d.insertString(0, "hello world", null);
        check(d.getParagraphElement(0).getName().equals("paragraph"), "styled paragraphs");
        check(d.getCharacterElement(2).getName().equals("content"), "styled runs");
        check(d.getLogicalStyle(0) != null, "logical style present");
        javax.swing.text.Style s = d.addStyle("umb", null);
        check(d.getStyle("umb") == s, "style registry shared");
        d.setLogicalStyle(0, s);
        check(d.getLogicalStyle(0) == s, "logical style settable");
        d.setParagraphAttributes(0, 1, new javax.swing.text.SimpleAttributeSet(), false);
        check(true, "paragraph attributes accepted");
        java.util.Vector<String> names = new java.util.Vector<String>();
        for (java.util.Enumeration<?> e = d.getStyleNames(); e.hasMoreElements(); )
            names.add(String.valueOf(e.nextElement()));
        check(names.contains("umb") && names.contains("default"), "style names enumerable");
        javax.swing.text.SimpleAttributeSet red = new javax.swing.text.SimpleAttributeSet();
        javax.swing.text.StyleConstants.setForeground(red, java.awt.Color.RED);
        check(d.getForeground(red).equals(java.awt.Color.RED), "foreground delegation");
        check(d.getBackground(red) != null, "background delegation");
        check(d.getFont(red) != null, "font delegation");
        d.removeStyle("umb");
        check(d.getStyle("umb") == null, "style removal");
    }
}
