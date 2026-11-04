/*
 * @test
 * @summary javax.swing.JLabel conformance: type shape + label property contract (headless-safe, Metal LaF).
 * @run main JLabelTest
 */

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLookAndFeel;

public class JLabelTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static Throwable checkThrows(Class<? extends Throwable> ex, Runnable r) {
        try { r.run(); } catch (Throwable t) {
            if (!ex.isInstance(t)) throw new AssertionError("expected " + ex.getSimpleName() + " got " + t);
            passed++; return t; }
        throw new AssertionError("expected " + ex.getSimpleName() + " not thrown");
    }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    static void testShape() throws Exception {
        Class<?> c = Class.forName("javax.swing.JLabel", false, JLabelTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.JComponent"), "superclass");
        check(hasInterface(c, "javax.swing.SwingConstants"), "implements javax.swing.SwingConstants");
        check(hasInterface(c, "javax.accessibility.Accessible"), "implements javax.accessibility.Accessible");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getUI"), "method getUI");
        check(names.contains("setUI"), "method setUI");
        check(names.contains("getLabelFor"), "method getLabelFor");
        check(names.contains("setLabelFor"), "method setLabelFor");
    }

    static void testContract() throws Exception {
        // ---- constructor matrix
        JLabel l0 = new JLabel();
        check("".equals(l0.getText()), "no-arg text is the LaF default (empty string)");
        check(l0.getIcon() == null, "no-arg icon null");
        check("t".equals(new JLabel("t").getText()), "text ctor");
        ImageIcon ic = new ImageIcon(new BufferedImage(3, 2, BufferedImage.TYPE_INT_RGB));
        check(new JLabel(ic).getIcon() == ic, "icon ctor");
        check("ti".equals(new JLabel("ti", ic, SwingConstants.RIGHT).getText())
                && new JLabel("ti", ic, SwingConstants.RIGHT).getHorizontalAlignment() == SwingConstants.RIGHT,
                "text+icon+alignment ctor");
        check(new JLabel(null, null, SwingConstants.CENTER).getHorizontalAlignment() == SwingConstants.CENTER,
                "null text/icon ctor tolerated");

        // ---- Metal defaults
        check(l0.getHorizontalAlignment() == SwingConstants.LEADING, "horizontalAlignment LEADING");
        check(l0.getVerticalAlignment() == SwingConstants.CENTER, "verticalAlignment CENTER");
        check(l0.getHorizontalTextPosition() == SwingConstants.TRAILING, "horizontalTextPosition TRAILING");
        check(l0.getVerticalTextPosition() == SwingConstants.CENTER, "verticalTextPosition CENTER");
        check(l0.getIconTextGap() == 4, "iconTextGap 4");
        check(l0.getDisplayedMnemonic() == KeyEvent.VK_UNDEFINED, "displayedMnemonic 0");
        check(l0.getDisplayedMnemonicIndex() == -1, "displayedMnemonicIndex -1");
        check("LabelUI".equals(l0.getUIClassID()), "getUIClassID LabelUI");
        check("javax.swing.plaf.metal.MetalLabelUI".equals(l0.getUI().getClass().getName()),
                "Metal installs MetalLabelUI");
        check(l0.getAutoscrolls() == false, "labels do not autoscroll by default");

        // ---- text semantics
        l0.setText("hello");
        check("hello".equals(l0.getText()), "setText/getText");
        l0.setText(null);
        check(l0.getText() == null, "setText(null) -> null (not empty)");

        // ---- alignment setters reject illegal values
        final JLabel lf = new JLabel("x");
        checkThrows(IllegalArgumentException.class, new Runnable() {
            public void run() { lf.setHorizontalAlignment(99); } });
        checkThrows(IllegalArgumentException.class, new Runnable() {
            public void run() { lf.setVerticalAlignment(99); } });
        checkThrows(IllegalArgumentException.class, new Runnable() {
            public void run() { lf.setHorizontalTextPosition(99); } });
        checkThrows(IllegalArgumentException.class, new Runnable() {
            public void run() { lf.setVerticalTextPosition(99); } });
        lf.setHorizontalAlignment(SwingConstants.CENTER);
        check(lf.getHorizontalAlignment() == SwingConstants.CENTER, "horizontalAlignment setter");
        lf.setVerticalAlignment(SwingConstants.TOP);
        check(lf.getVerticalAlignment() == SwingConstants.TOP, "verticalAlignment setter");

        // ---- iconTextGap accepts negative values on labels too (no validation)
        lf.setIconTextGap(-1);
        check(lf.getIconTextGap() == -1, "setIconTextGap(-1) accepted");
        lf.setIconTextGap(9);
        check(lf.getIconTextGap() == 9, "iconTextGap setter");

        // ---- labelFor association
        check(l0.getLabelFor() == null, "labelFor default null");
        JButton target = new JButton("target");
        l0.setLabelFor(target);
        check(l0.getLabelFor() == target, "setLabelFor identity");
        l0.setLabelFor(null);
        check(l0.getLabelFor() == null, "setLabelFor(null) clears");

        // ---- displayed mnemonic family: index only needs to be in range
        JLabel lm = new JLabel("abc");
        lm.setDisplayedMnemonic(KeyEvent.VK_B);
        check(lm.getDisplayedMnemonic() == KeyEvent.VK_B, "setDisplayedMnemonic(int)");
        check(lm.getDisplayedMnemonicIndex() == 1, "UI auto-derives index 1 for 'B' in 'abc'");
        lm.setDisplayedMnemonicIndex(0);
        check(lm.getDisplayedMnemonicIndex() == 0, "index may point at any in-range char (no mnemonic-match rule)");
        checkThrows(IllegalArgumentException.class, new Runnable() {
            public void run() { lm.setDisplayedMnemonicIndex(99); } });
        checkThrows(IllegalArgumentException.class, new Runnable() {
            public void run() { lm.setDisplayedMnemonicIndex(-2); } });
        checkThrows(IllegalArgumentException.class, new Runnable() {
            public void run() { new JLabel((String) null).setDisplayedMnemonicIndex(0); } });
        check(lm.getDisplayedMnemonicIndex() == 0, "failed setters leave index untouched");

        // ---- icon plumbing and derived disabled icon (ImageIcon only)
        l0.setIcon(ic);
        check(l0.getIcon() == ic, "setIcon identity");
        check(l0.getDisabledIcon() != null && l0.getDisabledIcon() != ic,
                "ImageIcon gets a LaF-derived disabledIcon");
        l0.setDisabledIcon(ic);
        check(l0.getDisabledIcon() == ic, "explicit disabledIcon wins");
        check(new JLabel("d", new javax.swing.Icon() {
            public void paintIcon(java.awt.Component c, java.awt.Graphics g, int x, int y) { }
            public int getIconWidth() { return 2; }
            public int getIconHeight() { return 2; }
        }, SwingConstants.LEFT).getDisabledIcon() == null,
                "plain Icon gets no derived disabledIcon");

        // ---- foreground color plumbing
        l0.setForeground(Color.MAGENTA);
        check(l0.getForeground() == Color.MAGENTA, "setForeground identity");
    }

    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(new MetalLookAndFeel());
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                try {
                    testShape();
                    testContract();
                } catch (Exception e) { throw new RuntimeException(e); }
            }
        });
        System.out.println("PASS (" + passed + " assertions)");
    }
}
