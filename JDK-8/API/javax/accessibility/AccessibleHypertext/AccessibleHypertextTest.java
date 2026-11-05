/*
 * @test
 * @summary javax.accessibility.AccessibleHypertext conformance: interface reflection.
 * @run main AccessibleHypertextTest
 */

public class AccessibleHypertextTest {
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
        Class<?> c = Class.forName("javax.accessibility.AccessibleHypertext");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"javax.accessibility.AccessibleText"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "getLinkCount"), "method getLinkCount");
        check(hasMethod(c, "getLink"), "method getLink");
        check(hasMethod(c, "getLinkIndex"), "method getLinkIndex");
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: recording subclass + interface surface ----
    static void testContractDeep() throws Exception {
        check(javax.accessibility.AccessibleHypertext.class.isInterface(), "interface");
        check(javax.accessibility.AccessibleText.class.isAssignableFrom(javax.accessibility.AccessibleHypertext.class), "extends AccessibleText");
        javax.accessibility.AccessibleHyperlink link = new RecLink();
        javax.accessibility.AccessibleHypertext ht = new RecHyper(link);
        check(ht.getLinkCount() == 1, "count");
        check(ht.getLink(0) == link, "link by index");
        check(ht.getLinkIndex(5) == 0, "index within 4..10 maps to link 0");
        check(ht.getLinkIndex(2) == -1, "before range -1");
        javax.accessibility.AccessibleHypertext ht2 = new RecHyper(link);
        check(ht2.getLinkCount() == 1, "second instance");
    }
    static class RecLink extends javax.accessibility.AccessibleHyperlink {
        public boolean isValid() { return true; }
        public int getAccessibleActionCount() { return 1; }
        public String getAccessibleActionDescription(int i) { return "d"; }
        public Object getAccessibleActionObject(int i) { return null; }
        public Object getAccessibleActionAnchor(int i) { return null; }
        public int getStartIndex() { return 4; }
        public int getEndIndex() { return 10; }
        public boolean doAccessibleAction(int i) { return true; }
    }
    static class RecHyper implements javax.accessibility.AccessibleHypertext {
        final javax.accessibility.AccessibleHyperlink l;
        RecHyper(javax.accessibility.AccessibleHyperlink l) { this.l = l; }
        public int getLinkCount() { return 1; }
        public javax.accessibility.AccessibleHyperlink getLink(int i) { return i == 0 ? l : null; }
        public int getLinkIndex(int charIndex) { return charIndex >= 4 && charIndex < 10 ? 0 : -1; }
        public int getCaretPosition() { return 0; }
        public String getSelectedText() { return null; }
        public int getSelectionStart() { return 0; }
        public int getSelectionEnd() { return 0; }
        public String getAtIndex(int part, int index) { return null; }
        public String getAfterIndex(int part, int index) { return null; }
        public String getBeforeIndex(int part, int index) { return null; }
        public int getCharCount() { return 10; }
        public javax.swing.text.AttributeSet getCharacterAttribute(int i) { return null; }
        public java.awt.Rectangle getCharacterBounds(int i) { return null; }
        public int getIndexAtPoint(java.awt.Point p) { return -1; }
        public String getTextRange(int s, int e) { return null; }
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
