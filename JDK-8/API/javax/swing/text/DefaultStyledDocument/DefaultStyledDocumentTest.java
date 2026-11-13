/*
 * @test
 * @summary javax.swing.text.DefaultStyledDocument conformance: section/paragraph/content element tree, character/paragraph/logical style application, change events, undoable style edits, style registry (headless-safe).
 * @run main DefaultStyledDocumentTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.Vector;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.undo.UndoableEdit;

public class DefaultStyledDocumentTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testTypeShape() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.DefaultStyledDocument", false, DefaultStyledDocumentTest.class.getClassLoader());
        check(!c.isInterface(), "is class");
        check(!Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(!Modifier.isFinal(c.getModifiers()), "not final");
        check(c.getSuperclass().getName().equals("javax.swing.text.AbstractDocument"), "superclass AbstractDocument");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("setCharacterAttributes"), "method setCharacterAttributes");
        check(names.contains("setParagraphAttributes"), "method setParagraphAttributes");
        check(names.contains("setLogicalStyle"), "method setLogicalStyle");
        check(names.contains("getLogicalStyle"), "method getLogicalStyle");
        check(names.contains("getCharacterElement"), "method getCharacterElement");
        check(names.contains("getParagraphElement"), "method getParagraphElement");
        check(names.contains("getForeground"), "method getForeground");
        check(names.contains("getBackground"), "method getBackground");
        check(names.contains("getFont"), "method getFont");
        check(names.contains("addStyle"), "method addStyle");
        check(names.contains("removeStyle"), "method removeStyle");
        check(names.contains("getStyle"), "method getStyle");
        check(names.contains("getStyleNames"), "method getStyleNames");
        check(DefaultStyledDocument.BUFFER_SIZE_DEFAULT == 4096, "BUFFER_SIZE_DEFAULT 4096");
    }

    static void testElementTree() throws Exception {
        DefaultStyledDocument d = new DefaultStyledDocument();
        Element sec = d.getDefaultRootElement();
        check(sec.getName().equals("section"), "root named section");
        check(sec.getStartOffset() == 0 && sec.getEndOffset() == 1, "empty section [0,1)");
        check(sec.getElementCount() == 1, "empty section one paragraph");
        check(sec.getParentElement() == null, "section has no parent");
        check(sec.getAttributes().getAttributeCount() == 0, "section attrs empty");
        Element para = sec.getElement(0);
        check(para.getName().equals("paragraph"), "paragraph named");
        check(para.getParentElement() == sec, "paragraph parent is section");
        check(!para.isLeaf(), "paragraph is branch");
        check(para.getElementCount() == 1, "empty paragraph one content element");
        Element content = para.getElement(0);
        check(content.getName().equals("content"), "content named");
        check(content.isLeaf(), "content is leaf");
        check(content.getStartOffset() == 0 && content.getEndOffset() == 1, "empty content [0,1)");
        check(content.getParentElement() == para, "content parent is paragraph");
        check(content.getAttributes().getAttributeCount() == 0, "content attrs empty by default");

        d.insertString(0, "hello world", null);
        check(sec.getEndOffset() == 12, "section end follows content");
        check(para.getEndOffset() == 12, "paragraph spans the line");
        check(para.getElementCount() == 1, "single run for plain insert");
        check(content.getStartOffset() == 0 && content.getEndOffset() == 12, "content run [0,12)");

        // multi-paragraph documents get one paragraph element per line
        DefaultStyledDocument m = new DefaultStyledDocument();
        m.insertString(0, "ab\ncd\nef", null);
        Element sec2 = m.getDefaultRootElement();
        check(sec2.getElementCount() == 3, "three paragraphs");
        int[][] ranges = { {0, 3}, {3, 6}, {6, 9} };
        for (int i = 0; i < 3; i++) {
            Element p = sec2.getElement(i);
            check(p.getStartOffset() == ranges[i][0] && p.getEndOffset() == ranges[i][1],
                    "paragraph " + i + " [" + ranges[i][0] + "," + ranges[i][1] + ")");
            check(m.getParagraphElement(ranges[i][0]) == p, "getParagraphElement(" + ranges[i][0] + ") identity");
        }
        check(m.getParagraphElement(100) == sec2.getElement(2), "getParagraphElement clamps to last");
        check(m.getCharacterElement(4).getParentElement() == sec2.getElement(1), "getCharacterElement parent");
        check(m.getCharacterElement(100).getStartOffset() == 6, "getCharacterElement clamps to last run");
    }

    static void testCharacterAttributes() throws Exception {
        DefaultStyledDocument d = new DefaultStyledDocument();
        d.insertString(0, "hello world", null);
        SimpleAttributeSet bold = new SimpleAttributeSet();
        StyleConstants.setBold(bold, true);
        d.setCharacterAttributes(0, 5, bold, false);
        check(StyleConstants.isBold(d.getCharacterElement(2).getAttributes()), "styled range is bold");
        check(!StyleConstants.isBold(d.getCharacterElement(7).getAttributes()), "outside range unchanged");
        check(d.getCharacterElement(2).getStartOffset() == 0 && d.getCharacterElement(2).getEndOffset() == 5,
                "styled run [0,5)");

        // non-replace merges with existing attributes
        SimpleAttributeSet ital = new SimpleAttributeSet();
        StyleConstants.setItalic(ital, true);
        d.setCharacterAttributes(0, 2, ital, false);
        AttributeSet merged = d.getCharacterElement(1).getAttributes();
        check(StyleConstants.isBold(merged) && StyleConstants.isItalic(merged), "non-replace merge keeps both");
        // replace discards the previous attributes
        d.setCharacterAttributes(0, 2, ital, true);
        AttributeSet replaced = d.getCharacterElement(1).getAttributes();
        check(!StyleConstants.isBold(replaced) && StyleConstants.isItalic(replaced), "replace clears others");

        // newly inserted text does not inherit neighbouring character attributes
        d.insertString(0, "N", null);
        AttributeSet fresh = d.getCharacterElement(0).getAttributes();
        check(!StyleConstants.isBold(fresh) && !StyleConstants.isItalic(fresh), "insert does not inherit run attributes");

        // zero-length apply is a silent no-op
        d.setCharacterAttributes(0, 0, bold, false);
        check(!StyleConstants.isBold(d.getCharacterElement(0).getAttributes()), "zero-length character apply is a no-op");
        // an offset past the end is clamped to the document end: the trailing run picks
        // up the attributes
        d.setCharacterAttributes(100, 1, bold, false);
        Element last = d.getParagraphElement(0);
        Element lastRun = last.getElement(last.getElementCount() - 1);
        check(StyleConstants.isBold(lastRun.getAttributes()), "past-end offset applies at the tail run");
        check(!StyleConstants.isBold(d.getCharacterElement(0).getAttributes()), "head run untouched by tail apply");
        d.setCharacterAttributes(-5, 1, bold, false); // negative offset tolerated
    }

    static void testParagraphAttributesAndLogicalStyle() throws Exception {
        DefaultStyledDocument d = new DefaultStyledDocument();
        d.insertString(0, "hello world", null);
        Element p0 = d.getParagraphElement(0);
        // the paragraph carries exactly one attribute: the resolver pointing at the logical style
        AttributeSet pa = p0.getAttributes();
        check(pa.getAttributeCount() == 1, "paragraph has the resolver entry");
        check(pa.getAttribute(StyleConstants.ResolveAttribute) != null, "resolver present");
        Style def = d.getLogicalStyle(0);
        check(def != null && def.getName().equals("default"), "default logical style");
        check(pa.getResolveParent() == def, "paragraph resolve parent is the logical style");

        // setLogicalStyle swaps the resolver target
        Style my = d.addStyle("mystyle", null);
        StyleConstants.setFontSize(my, 40);
        d.setLogicalStyle(0, my);
        check(d.getLogicalStyle(0) == my, "getLogicalStyle returns what was set");
        check(p0.getAttributes().getResolveParent() == my, "paragraph resolver follows setLogicalStyle");
        check(StyleConstants.getFontSize(p0.getAttributes()) == 40, "attribute resolution reaches the logical style");

        // paragraph attributes
        SimpleAttributeSet center = new SimpleAttributeSet();
        StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
        d.setParagraphAttributes(0, 5, center, false);
        check(StyleConstants.getAlignment(p0.getAttributes()) == StyleConstants.ALIGN_CENTER, "paragraph alignment set");
        check(StyleConstants.ALIGN_CENTER == 1, "ALIGN_CENTER value 1");
        // zero-length and past-end applies still reach the paragraph
        SimpleAttributeSet right = new SimpleAttributeSet();
        StyleConstants.setAlignment(right, StyleConstants.ALIGN_RIGHT);
        d.setParagraphAttributes(0, 0, right, false);
        check(StyleConstants.getAlignment(p0.getAttributes()) == StyleConstants.ALIGN_RIGHT, "zero-length paragraph apply still lands");
        check(StyleConstants.ALIGN_RIGHT == 2, "ALIGN_RIGHT value 2");
        d.setParagraphAttributes(100, 1, center, false);
        check(StyleConstants.getAlignment(p0.getAttributes()) == StyleConstants.ALIGN_CENTER, "past-end paragraph apply clamps to the paragraph");
        // setLogicalStyle past the end also targets the last paragraph
        d.setLogicalStyle(100, def);
        check(d.getLogicalStyle(0) == def, "past-end setLogicalStyle reaches the paragraph");
    }

    static void testStyleRegistry() {
        DefaultStyledDocument d = new DefaultStyledDocument();
        Style s = d.addStyle("mystyle", null);
        check(s != null, "addStyle returns a style");
        check(d.getStyle("mystyle") == s, "getStyle identity");
        check(d.getStyle("missing") == null, "missing style null");
        Style anon = d.addStyle(null, null);
        check(anon != null, "anonymous style allowed");
        Style parent = d.addStyle("parent", null);
        StyleConstants.setFontSize(parent, 20);
        Style child = d.addStyle("child", parent);
        check(child.getResolveParent() == parent, "parent style recorded");
        check(StyleConstants.getFontSize(child) == 20, "resolution through parent style");
        Vector<String> names = new Vector<String>();
        for (Enumeration<?> e = d.getStyleNames(); e.hasMoreElements(); ) names.add(String.valueOf(e.nextElement()));
        check(names.contains("mystyle"), "style names contain mystyle");
        check(names.contains("default"), "style names contain default");
        check(!names.contains("other"), "no phantom names");
        d.removeStyle("mystyle");
        check(d.getStyle("mystyle") == null, "removed style gone");
        d.removeStyle("missing"); // silent
    }

    static void testColorsAndFont() {
        DefaultStyledDocument d = new DefaultStyledDocument();
        SimpleAttributeSet red = new SimpleAttributeSet();
        StyleConstants.setForeground(red, java.awt.Color.RED);
        check(d.getForeground(red).equals(java.awt.Color.RED), "foreground from attributes");
        check(d.getBackground(red).equals(java.awt.Color.BLACK), "default background black");
        SimpleAttributeSet bold = new SimpleAttributeSet();
        StyleConstants.setBold(bold, true);
        java.awt.Font f = d.getFont(bold);
        check(f.isBold(), "font bold from attributes");
        check(f.getSize() == 12, "default font size 12");
        check(f.getFamily().equals("Monospaced"), "default font family Monospaced");
    }

    static void testChangeEventsAndUndo() throws Exception {
        DefaultStyledDocument d = new DefaultStyledDocument();
        d.insertString(0, "hello world", null);
        final Vector<String> events = new Vector<String>();
        d.addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { events.add("I" + e.getOffset() + ":" + e.getLength()); }
            public void removeUpdate(DocumentEvent e) { events.add("R" + e.getOffset() + ":" + e.getLength()); }
            public void changedUpdate(DocumentEvent e) { events.add("C" + e.getOffset() + ":" + e.getLength() + ":" + e.getType()); }
        });
        SimpleAttributeSet bold = new SimpleAttributeSet();
        StyleConstants.setBold(bold, true);
        d.setCharacterAttributes(0, 3, bold, false);
        check(events.size() == 1 && events.get(0).equals("C0:3:CHANGE"), "attribute change fires changedUpdate only");

        // the change is published as a significant undoable edit and can be undone
        final UndoableEdit[] edit = new UndoableEdit[1];
        d.addUndoableEditListener(e -> edit[0] = e.getEdit());
        SimpleAttributeSet ital = new SimpleAttributeSet();
        StyleConstants.setItalic(ital, true);
        d.setCharacterAttributes(0, 3, ital, false);
        check(edit[0] != null, "style change produces undoable edit");
        check(edit[0].isSignificant(), "style change is significant");
        // 展示名来自 UIManager 资源键，随默认 locale 本地化（中文 Windows 上
        // 不是 "style change"）；用实现所用的同一资源键取期望值。
        String styleChangeText = javax.swing.UIManager.getString("AbstractDocument.styleChangeText");
        check(styleChangeText != null && styleChangeText.length() > 0, "AbstractDocument.styleChangeText resource present");
        check(styleChangeText.equals(edit[0].getPresentationName()), "presentation name");
        edit[0].undo();
        check(!StyleConstants.isItalic(d.getCharacterElement(1).getAttributes()), "undo reverts the style change");
        edit[0].redo();
        check(StyleConstants.isItalic(d.getCharacterElement(1).getAttributes()), "redo reapplies the style change");
    }

    public static void main(String[] args) throws Exception {
        Locale.setDefault(Locale.ENGLISH);
        testTypeShape();
        testElementTree();
        testCharacterAttributes();
        testParagraphAttributesAndLogicalStyle();
        testStyleRegistry();
        testColorsAndFont();
        testChangeEventsAndUndo();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
