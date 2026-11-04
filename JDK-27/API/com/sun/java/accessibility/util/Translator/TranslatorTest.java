/*
 * @test
 * @summary com.sun.java.accessibility.util.Translator conformance: white-box structure + black-box behavior.
 * @run main TranslatorTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.awt.*;
import javax.accessibility.*;

public class TranslatorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Set<String> methodNames(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) s.add(m.getName());
        return s;
    }
    static Set<String> fieldNames(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Field f : c.getDeclaredFields()) s.add(f.getName());
        return s;
    }
    static Set<String> interfaces(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Class<?> i : c.getInterfaces()) s.add(i.getName());
        return s;
    }
    static Class<?> load(String fqcn) throws Exception { return Class.forName(fqcn); }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("com.sun.java.accessibility.util.Translator");
        check(!c.isInterface(), "not an interface");
        check(!Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(c.getSuperclass().getName().equals("javax.accessibility.AccessibleContext"), "extends AccessibleContext");
        Set<String> ifs = interfaces(c);
        check(ifs.contains("javax.accessibility.Accessible"), "implements Accessible");
        check(ifs.contains("javax.accessibility.AccessibleComponent"), "implements AccessibleComponent");
        Set<String> m = methodNames(c);
        check(m.contains("getAccessible"), "static getAccessible");
        check(m.contains("getSource"), "getSource");
        check(m.contains("setSource"), "setSource");
        check(m.contains("getAccessibleName"), "getAccessibleName");
        check(m.contains("getAccessibleRole"), "getAccessibleRole");
        check(m.contains("getAccessibleStateSet"), "getAccessibleStateSet");
        check(m.contains("getAccessibleContext"), "getAccessibleContext");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        com.sun.java.accessibility.util.Translator t;

        // getAccessible(null) -> null.
        check(com.sun.java.accessibility.util.Translator.getAccessible(null) == null,
              "getAccessible(null) == null");

        // getAccessible(plain Object) returns a Translator wrapping the object.
        Object plain = new Object();
        Accessible aPlain = com.sun.java.accessibility.util.Translator.getAccessible(plain);
        check(aPlain != null, "getAccessible(Object) non-null");
        check(aPlain != plain, "getAccessible(Object) not identity of plain object");
        check(aPlain instanceof com.sun.java.accessibility.util.Translator, "getAccessible(Object) is Translator");
        check(((com.sun.java.accessibility.util.Translator) aPlain).getSource() == plain, "wrapped source is original");

        // getAccessible(already Accessible) returns the same instance.
        com.sun.java.accessibility.util.Translator already = new com.sun.java.accessibility.util.Translator();
        Accessible aAlready = com.sun.java.accessibility.util.Translator.getAccessible(already);
        check(aAlready == already, "getAccessible(already Accessible) returns same instance");

        // String source is wrapped and yields default accessible metadata.
        String src = "hello";
        Accessible aStr = com.sun.java.accessibility.util.Translator.getAccessible(src);
        check(aStr != null && aStr instanceof com.sun.java.accessibility.util.Translator, "getAccessible(String) is Translator");
        com.sun.java.accessibility.util.Translator tStr = (com.sun.java.accessibility.util.Translator) aStr;
        check(tStr.getSource() == src, "String source preserved");
        check(tStr.getAccessibleName() == null, "String source accessible name null");

        // Default constructor: source null, then setSource.
        t = new com.sun.java.accessibility.util.Translator();
        check(t.getSource() == null, "default source null");
        Object o2 = new Object();
        t.setSource(o2);
        check(t.getSource() == o2, "setSource then getSource");

        // Constructor with source.
        Object o3 = new Object();
        com.sun.java.accessibility.util.Translator t3 = new com.sun.java.accessibility.util.Translator(o3);
        check(t3.getSource() == o3, "constructor source preserved");

        // Accessible / AccessibleContext identity.
        check(t.getAccessibleContext() == t, "getAccessibleContext() == this");

        // Default AccessibleContext metadata for a non-Component source.
        check(t.getAccessibleName() == null, "getAccessibleName null");
        check(t.getAccessibleDescription() == null, "getAccessibleDescription null");
        check(t.getAccessibleRole() == AccessibleRole.UNKNOWN, "getAccessibleRole UNKNOWN");
        AccessibleStateSet ss = t.getAccessibleStateSet();
        check(ss != null, "getAccessibleStateSet non-null");
        check(ss.contains(AccessibleState.ENABLED), "state contains ENABLED");
        check(!ss.contains(AccessibleState.FOCUSABLE), "state does not contain FOCUSABLE");
        check(t.getLocale() == null, "getLocale null");

        // AccessibleContext tree accessors for a non-Container source.
        check(t.getAccessibleParent() == null, "getAccessibleParent null");
        check(t.getAccessibleIndexInParent() == -1, "getAccessibleIndexInParent -1");
        check(t.getAccessibleChildrenCount() == 0, "getAccessibleChildrenCount 0");
        check(t.getAccessibleChild(0) == null, "getAccessibleChild(0) null");

        // AccessibleComponent accessors for a non-Component source.
        check(t.isEnabled() == true, "isEnabled true");
        check(t.isVisible() == false, "isVisible false");
        check(t.isShowing() == false, "isShowing false");
        check(t.contains(new Point(0, 0)) == false, "contains false");
        check(t.isFocusTraversable() == false, "isFocusTraversable false");
        check(t.getLocationOnScreen() == null, "getLocationOnScreen null");
        check(t.getLocation() == null, "getLocation null");
        check(t.getBounds() == null, "getBounds null");
        check(t.getSize() == null, "getSize null");
        check(t.getBackground() == null, "getBackground null");
        check(t.getForeground() == null, "getForeground null");
        check(t.getCursor() == null, "getCursor null");
        check(t.getFont() == null, "getFont null");
        check(t.getFontMetrics(new Font("SansSerif", Font.PLAIN, 12)) == null, "getFontMetrics null");
        check(t.getAccessibleAt(new Point(0, 0)) == null, "getAccessibleAt null");

        // Setters for a non-Component source are no-ops and never throw.
        t.setAccessibleName("x");
        check(t.getAccessibleName() == null, "setAccessibleName no-op for plain object");
        t.setAccessibleDescription("d");
        t.setBackground(Color.RED);
        t.setForeground(Color.BLUE);
        t.setCursor(null);
        t.setFont(new Font("SansSerif", Font.PLAIN, 12));
        t.setEnabled(false);
        t.setVisible(true);
        t.setLocation(new Point(1, 2));
        t.setBounds(new Rectangle(0, 0, 10, 10));
        t.setSize(new Dimension(5, 5));
        t.requestFocus();
        java.awt.event.FocusListener fl = new java.awt.event.FocusListener() {
            public void focusGained(java.awt.event.FocusEvent e) {}
            public void focusLost(java.awt.event.FocusEvent e) {}
        };
        t.addFocusListener(fl);
        t.removeFocusListener(fl);
        check(t.isEnabled() == true, "setEnabled(false) no-op for plain object");
        check(t.isVisible() == false, "setVisible(true) no-op for plain object");

        // Object identity contract.
        check(new com.sun.java.accessibility.util.Translator().hashCode() == 0, "hashCode 0 for null source");
        check(new com.sun.java.accessibility.util.Translator().equals("x") == false, "equals non-Translator false");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
