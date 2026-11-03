/*
 * @test
 * @summary javax.swing.text.DefaultStyledDocument conformance: white-box structure + black-box behavior.
 * @run main DefaultStyledDocumentTest
 */

import java.lang.reflect.Modifier;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import javax.swing.text.AttributeSet;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

public class DefaultStyledDocumentTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.DefaultStyledDocument");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.text.AbstractDocument"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.swing.text.StyledDocument"), "interface 0");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        DefaultStyledDocument doc = new DefaultStyledDocument();
        check(doc.getLength() == 0, "initial length 0");

        // root is a section of paragraphs
        Element root = doc.getDefaultRootElement();
        check(root != null, "root non-null");
        check("section".equals(root.getName()), "root name section");
        check(root.getElementCount() == 1, "initial one paragraph");

        // text round-trip and structure
        doc.insertString(0, "abc\ndef", null);
        check("abc\ndef".equals(doc.getText(0, doc.getLength())), "getText round-trip");
        check(root.getElementCount() == 2, "two paragraphs after newline");

        Element para = doc.getParagraphElement(0);
        check("paragraph".equals(para.getName()), "paragraph name");
        check(para.getStartOffset() == 0 && para.getEndOffset() == 4, "paragraph 0 range");
        check(doc.getParagraphElement(4) != para, "paragraph 1 distinct");
        check(doc.getParagraphElement(4).getStartOffset() == 4, "paragraph 1 start");

        Element run = doc.getCharacterElement(0);
        check(run.isLeaf(), "character element is leaf");
        check("content".equals(run.getName()), "character element name content");
        check(run.getStartOffset() == 0 && run.getEndOffset() == 4, "character run range");

        // styles: add/get/remove + names
        Style s = doc.addStyle("myStyle", null);
        check(s != null, "addStyle returns style");
        check(doc.getStyle("myStyle") == s, "getStyle returns same");
        List<String> names = new ArrayList<String>();
        Enumeration<?> en = doc.getStyleNames();
        while (en.hasMoreElements()) names.add((String) en.nextElement());
        Collections.sort(names);
        check(names.contains("default"), "default style present");
        check(names.contains("myStyle"), "added style present");
        doc.removeStyle("myStyle");
        check(doc.getStyle("myStyle") == null, "removeStyle removes");

        // logical style assignment
        Style logical = doc.addStyle("logical", null);
        doc.setLogicalStyle(0, logical);
        check(doc.getLogicalStyle(0) == logical, "set/get logical style");
        check(doc.getLogicalStyle(4) != logical, "other paragraph keeps default logical style");

        // character attributes
        SimpleAttributeSet charAttr = new SimpleAttributeSet();
        charAttr.addAttribute("mykey", "charval");
        doc.setCharacterAttributes(0, 3, charAttr, false);
        check(doc.getCharacterElement(0).getAttributes().containsAttribute("mykey", "charval"),
              "setCharacterAttributes applies to run");

        // paragraph attributes
        SimpleAttributeSet paraAttr = new SimpleAttributeSet();
        paraAttr.addAttribute("pkey", "pval");
        doc.setParagraphAttributes(0, doc.getLength(), paraAttr, false);
        check(doc.getParagraphElement(0).getAttributes().containsAttribute("pkey", "pval"),
              "setParagraphAttributes applies to paragraph");

        // foreground/background/font resolution through StyleConstants
        Style colored = doc.addStyle("colored", null);
        StyleConstants.setForeground(colored, Color.RED);
        StyleConstants.setBackground(colored, Color.BLUE);
        StyleConstants.setFontFamily(colored, "Serif");
        check(doc.getForeground(colored).equals(Color.RED), "getForeground resolves red");
        check(doc.getBackground(colored).equals(Color.BLUE), "getBackground resolves blue");
        check("Serif".equals(doc.getFont(colored).getFamily()), "getFont resolves family");
        check(doc.getForeground(SimpleAttributeSet.EMPTY) != null, "getForeground empty non-null");
        check(doc.getBackground(SimpleAttributeSet.EMPTY) != null, "getBackground empty non-null");
        check(doc.getFont(SimpleAttributeSet.EMPTY) != null, "getFont empty non-null");

        // element removal collapses the document
        DefaultStyledDocument d2 = new DefaultStyledDocument();
        d2.insertString(0, "abc\ndef", null);
        Element p0 = d2.getParagraphElement(0);
        d2.removeElement(p0);
        check("def".equals(d2.getText(0, d2.getLength())), "removeElement removes paragraph text");
        check(d2.getDefaultRootElement().getElementCount() == 1, "removeElement leaves one paragraph");

        // constructor with a shared StyleContext
        StyleContext sc = new StyleContext();
        DefaultStyledDocument d3 = new DefaultStyledDocument(sc);
        d3.insertString(0, "x", null);
        check("x".equals(d3.getText(0, 1)), "StyleContext constructor usable");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
