/*
 * @test
 * @summary javax.accessibility.AccessibleHypertext conformance: white-box structure + black-box behavior.
 * @run main AccessibleHypertextTest
 */

import java.lang.reflect.*;
import java.awt.*;

public class AccessibleHypertextTest {
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

    static class Model implements javax.accessibility.AccessibleHypertext {
        javax.accessibility.AccessibleHyperlink[] links = new javax.accessibility.AccessibleHyperlink[]{
            new javax.accessibility.AccessibleHyperlink() {
                public boolean isValid() { return true; }
                public int getAccessibleActionCount() { return 1; }
                public boolean doAccessibleAction(int i) { return true; }
                public String getAccessibleActionDescription(int i) { return "jump"; }
                public Object getAccessibleActionObject(int i) { return "url"; }
                public Object getAccessibleActionAnchor(int i) { return "here"; }
                public int getStartIndex() { return 0; }
                public int getEndIndex() { return 5; }
            }
        };
        public int getLinkCount() { return links.length; }
        public javax.accessibility.AccessibleHyperlink getLink(int i) { return links[i]; }
        public int getLinkIndex(int charIndex) { return charIndex < 5 ? 0 : -1; }
        // AccessibleText
        public int getIndexAtPoint(Point p) { return 0; }
        public Rectangle getCharacterBounds(int i) { return new Rectangle(i, 0, 1, 1); }
        public int getCharCount() { return 10; }
        public int getCaretPosition() { return 0; }
        public String getAtIndex(int part, int index) { return "x"; }
        public String getAfterIndex(int part, int index) { return "y"; }
        public String getBeforeIndex(int part, int index) { return "z"; }
        public javax.swing.text.AttributeSet getCharacterAttribute(int i) { return new javax.swing.text.SimpleAttributeSet(); }
        public int getSelectionStart() { return 0; }
        public int getSelectionEnd() { return 0; }
        public String getSelectedText() { return null; }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.accessibility.AccessibleHypertext");
        check(c.isInterface(), "isInterface");
        check(c.getInterfaces().length == 1, "1 superinterface(s)");
        check(hasInterface(c, "javax.accessibility.AccessibleText"), "extends AccessibleText");
        check(hasMethod(c, "getLinkCount"), "declares getLinkCount");
        check(hasMethod(c, "getLink"), "declares getLink");
        check(hasMethod(c, "getLinkIndex"), "declares getLinkIndex");
    }

    static void testBlackBox() throws Exception {
        Model m = new Model();
        check(m.getLinkCount() == 1, "getLinkCount");
        check(m.getLink(0) == m.links[0], "getLink identity");
        check(m.getLinkIndex(0) == 0, "getLinkIndex inside link");
        check(m.getLinkIndex(7) == -1, "getLinkIndex outside link");
        check(m.getLink(0).isValid(), "link isValid");
        check(m.getLink(0).getStartIndex() == 0, "link start index");
        check(m.getLink(0).getEndIndex() == 5, "link end index");
        check(m.getCharCount() == 10, "inherited getCharCount");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
