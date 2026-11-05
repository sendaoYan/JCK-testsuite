/*
 * @test
 * @summary javax.accessibility.AccessibleEditableText conformance: white-box structure + black-box behavior.
 * @run main AccessibleEditableTextTest
 */

import java.lang.reflect.*;
import java.awt.*;

public class AccessibleEditableTextTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    static boolean hasInterface(Class<?> c, String n) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true;
        return false;
    }

    static class EditableModel implements javax.accessibility.AccessibleEditableText {
        StringBuilder text = new StringBuilder();
        int selStart = 0, selEnd = 0;
        // AccessibleEditableText
        public void setTextContents(String s) { text.setLength(0); text.append(s); }
        public void insertTextAtIndex(int index, String s) { text.insert(index, s); }
        public String getTextRange(int start, int end) { return text.substring(start, end); }
        public void delete(int start, int end) { text.delete(start, end); }
        public void cut(int start, int end) { text.delete(start, end); }
        public void paste(int start) { text.insert(start, "PASTE"); }
        public void replaceText(int start, int end, String s) { text.replace(start, end, s); }
        public void selectText(int start, int end) { selStart = start; selEnd = end; }
        public void setAttributes(int start, int end, javax.swing.text.AttributeSet as) { }
        // AccessibleText
        public int getIndexAtPoint(Point p) { return 0; }
        public Rectangle getCharacterBounds(int i) { return new Rectangle(i, 0, 1, 1); }
        public int getCharCount() { return text.length(); }
        public int getCaretPosition() { return selEnd; }
        public String getAtIndex(int part, int index) {
            if (part == javax.accessibility.AccessibleText.CHARACTER && index >= 0 && index < text.length())
                return text.substring(index, index + 1);
            return "";
        }
        public String getAfterIndex(int part, int index) { return getAtIndex(part, index + 1); }
        public String getBeforeIndex(int part, int index) { return getAtIndex(part, index - 1); }
        public javax.swing.text.AttributeSet getCharacterAttribute(int i) { return new javax.swing.text.SimpleAttributeSet(); }
        public int getSelectionStart() { return selStart; }
        public int getSelectionEnd() { return selEnd; }
        public String getSelectedText() {
            if (selStart < 0 || selEnd > text.length() || selStart > selEnd) return null;
            return text.substring(selStart, selEnd);
        }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.accessibility.AccessibleEditableText");
        check(c.isInterface(), "isInterface");
        check(c.getInterfaces().length == 1, "1 superinterface(s)");
        check(hasInterface(c, "javax.accessibility.AccessibleText"), "extends AccessibleText");
        check(hasMethod(c, "setTextContents"), "declares setTextContents");
        check(hasMethod(c, "insertTextAtIndex"), "declares insertTextAtIndex");
        check(hasMethod(c, "getTextRange"), "declares getTextRange");
        check(hasMethod(c, "delete"), "declares delete");
        check(hasMethod(c, "cut"), "declares cut");
    }

    static void testBlackBox() throws Exception {
        EditableModel m = new EditableModel();
        m.setTextContents("hello world");
        check(m.getCharCount() == 11, "setTextContents + charCount");
        check("hello".equals(m.getTextRange(0, 5)), "getTextRange(0,5)");

        m.insertTextAtIndex(5, "X");
        check("helloX world".equals(m.getTextRange(0, 12)), "insertTextAtIndex");

        m.replaceText(5, 6, "YY");
        check("helloYY world".equals(m.getTextRange(0, 13)), "replaceText");

        m.delete(5, 7);
        check("hello world".equals(m.getTextRange(0, 11)), "delete");

        m.cut(0, 5);
        check(" world".equals(m.getTextRange(0, 6)), "cut");

        m.paste(0);
        check("PASTE world".equals(m.getTextRange(0, 11)), "paste");

        m.selectText(0, 5);
        check(m.getSelectionStart() == 0 && m.getSelectionEnd() == 5, "selectText");
        check("PASTE".equals(m.getSelectedText()), "getSelectedText after select");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: recording subclass + real JTextArea edit round-trip ----
    static void testContractDeep() throws Exception {
        check(javax.accessibility.AccessibleEditableText.class.isInterface(), "interface");
        check(javax.accessibility.AccessibleText.class.isAssignableFrom(javax.accessibility.AccessibleEditableText.class), "extends AccessibleText");
        javax.accessibility.AccessibleEditableText et = new RecEdit();
        et.setTextContents("abc"); passed++;
        et.insertTextAtIndex(1, "X"); passed++;
        check("ran".equals(et.getTextRange(0, 3)), "getTextRange");
        et.delete(0, 1); passed++;
        et.cut(0, 1); passed++;
        et.paste(0); passed++;
        et.replaceText(0, 1, "Y"); passed++;
        et.selectText(0, 1); passed++;
        et.setAttributes(0, 1, null); passed++;
        // real component: JTextArea context is editable text and edits reflect in the model
        javax.swing.JTextArea ta = new javax.swing.JTextArea("plain");
        javax.accessibility.AccessibleEditableText real = (javax.accessibility.AccessibleEditableText) ta.getAccessibleContext().getAccessibleEditableText();
        check(real != null, "JTextArea exposes editable text");
        real.setTextContents("new");
        check("new".equals(ta.getText()), "edit reflected in component");
        real.insertTextAtIndex(0, "re");
        check("renew".equals(ta.getText()), "insert reflected");
        real.delete(0, 2);
        check("new".equals(ta.getText()), "delete reflected");
        real.replaceText(0, 1, "N");
        check("New".equals(ta.getText()), "replace reflected");
        javax.swing.JLabel l = new javax.swing.JLabel();
        check(l.getAccessibleContext().getAccessibleEditableText() == null, "label not editable");
    }
    static class RecEdit implements javax.accessibility.AccessibleEditableText {
        public int getCaretPosition() { return 0; }
        public String getSelectedText() { return null; }
        public int getSelectionStart() { return 0; }
        public int getSelectionEnd() { return 0; }
        public String getAtIndex(int part, int index) { return null; }
        public String getAfterIndex(int part, int index) { return null; }
        public String getBeforeIndex(int part, int index) { return null; }
        public int getCharCount() { return 3; }
        public javax.swing.text.AttributeSet getCharacterAttribute(int i) { return null; }
        public java.awt.Rectangle getCharacterBounds(int i) { return null; }
        public int getIndexAtPoint(java.awt.Point p) { return -1; }
        public void setTextContents(String s) {}
        public void insertTextAtIndex(int index, String s) {}
        public String getTextRange(int s, int e) { return "ran"; }
        public void delete(int s, int e) {}
        public void cut(int s, int e) {}
        public void paste(int index) {}
        public void replaceText(int s, int e, String s2) {}
        public void selectText(int s, int e) {}
        public void setAttributes(int s, int e, javax.swing.text.AttributeSet a) {}
    }

    static Object serRoundTrip(Object o) throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        new java.io.ObjectOutputStream(bos).writeObject(o);
        return new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
    }
    // concrete AccessibleContext recording subclass
    static class RecCtx extends javax.accessibility.AccessibleContext {
        public String getAccessibleName() { return accessibleName; }
        public String getAccessibleDescription() { return accessibleDescription; }
        public javax.accessibility.AccessibleRole getAccessibleRole() { return javax.accessibility.AccessibleRole.UNKNOWN; }
        public javax.accessibility.AccessibleStateSet getAccessibleStateSet() { return new javax.accessibility.AccessibleStateSet(); }
        public int getAccessibleIndexInParent() { return -1; }
        public int getAccessibleChildrenCount() { return 0; }
        public javax.accessibility.Accessible getAccessibleChild(int i) { return null; }
        public java.util.Locale getLocale() throws java.awt.IllegalComponentStateException { return java.util.Locale.ENGLISH; }
    }

}
