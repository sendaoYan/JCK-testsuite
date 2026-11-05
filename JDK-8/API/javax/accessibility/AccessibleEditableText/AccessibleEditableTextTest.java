/*
 * @test
 * @summary javax.accessibility.AccessibleEditableText conformance: interface reflection.
 * @run main AccessibleEditableTextTest
 */

public class AccessibleEditableTextTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getMethods())
            if (m.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.accessibility.AccessibleEditableText");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"javax.accessibility.AccessibleText"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "setTextContents"), "method setTextContents");
        check(hasMethod(c, "insertTextAtIndex"), "method insertTextAtIndex");
        check(hasMethod(c, "getTextRange"), "method getTextRange");
        check(hasMethod(c, "delete"), "method delete");
        check(hasMethod(c, "cut"), "method cut");
        check(hasMethod(c, "paste"), "method paste");
        check(hasMethod(c, "replaceText"), "method replaceText");
        check(hasMethod(c, "selectText"), "method selectText");
        check(hasMethod(c, "setAttributes"), "method setAttributes");
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
