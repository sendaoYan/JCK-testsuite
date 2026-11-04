/*
 * @test
 * @summary javax.swing.JLabel conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main JLabelTest
 */

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class JLabelTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static Icon fixedIcon(final int w, final int h) {
        return new Icon() {
            public int getIconWidth() { return w; }
            public int getIconHeight() { return h; }
            public void paintIcon(Component c, Graphics g, int x, int y) { }
        };
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.JLabel");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.JComponent"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 2, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.swing.SwingConstants"), "interface 0");
        check(c.getInterfaces()[1].getName().equals("javax.accessibility.Accessible"), "interface 1");
        // supplemented structural checks
        check(c.getConstructor(String.class) != null, "ctor String");
        check(c.getConstructor(String.class, Icon.class, int.class) != null, "ctor String,Icon,int");
        boolean hasLabeledBy = false;
        for (java.lang.reflect.Field f : c.getDeclaredFields())
            if (f.getName().equals("LABELED_BY_PROPERTY")) hasLabeledBy = true;
        check(hasLabeledBy, "LABELED_BY_PROPERTY field declared");
        check(Class.forName("javax.swing.JLabel$AccessibleJLabel").getSuperclass().getName()
                .equals("javax.swing.JComponent$AccessibleJComponent"), "AccessibleJLabel nesting");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // text/icon/disabledIcon round trips on a fresh label
        JLabel label = new JLabel("Hello");
        check(label.getText().equals("Hello"), "ctor text round trip");
        check(label.getIcon() == null, "icon null before set");
        check(label.getDisabledIcon() == null, "disabledIcon null before set");
        check(label.getDisplayedMnemonicIndex() == -1, "mnemonicIndex default -1");
        check(label.getDisplayedMnemonic() == 0, "displayedMnemonic default VK_UNDEFINED");
        label.setText("World");
        check(label.getText().equals("World"), "setText round trip");
        label.setText("Hello");
        check(label.getText().equals("Hello"), "setText back to Hello");

        Icon i1 = fixedIcon(8, 8);
        Icon i2 = fixedIcon(6, 10);
        label.setIcon(i1);
        check(label.getIcon() == i1, "setIcon/getIcon same instance");
        label.setDisabledIcon(i2);
        check(label.getDisabledIcon() == i2, "setDisabledIcon/getDisabledIcon same instance");

        // iconTextGap round trip + code-level default (field initializer, not L&F)
        check(label.getIconTextGap() == 4, "default iconTextGap is 4");
        label.setIconTextGap(12);
        check(label.getIconTextGap() == 12, "iconTextGap round trip");
        label.setIconTextGap(0);
        check(label.getIconTextGap() == 0, "iconTextGap zero round trip");

        // setLabelFor round trip + property change notification
        JTextField tf = new JTextField();
        final List<String> pnames = new ArrayList<>();
        label.addPropertyChangeListener(e -> pnames.add(e.getPropertyName()));
        check(label.getLabelFor() == null, "labelFor null before set");
        label.setLabelFor(tf);
        check(label.getLabelFor() == tf, "setLabelFor/getLabelFor same instance");
        check(pnames.contains("labelFor"), "labelFor property change fired");
        pnames.clear();
        label.setText("Changed");
        check(pnames.contains("text"), "text property change fired");

        // alignment constants and getters
        check(label.getHorizontalAlignment() == SwingConstants.LEADING, "default horizontalAlignment LEADING");
        label.setHorizontalAlignment(SwingConstants.CENTER);
        check(label.getHorizontalAlignment() == SwingConstants.CENTER, "horizontalAlignment CENTER round trip");
        label.setHorizontalAlignment(SwingConstants.RIGHT);
        check(label.getHorizontalAlignment() == SwingConstants.RIGHT, "horizontalAlignment RIGHT round trip");
        label.setHorizontalAlignment(SwingConstants.LEFT);
        check(label.getHorizontalAlignment() == SwingConstants.LEFT, "horizontalAlignment LEFT round trip");
        try {
            label.setHorizontalAlignment(33);
            check(false, "setHorizontalAlignment(33) must throw IAE");
        } catch (IllegalArgumentException expected) {
            check(true, "setHorizontalAlignment(33) throws IllegalArgumentException");
        }
        check(label.getVerticalAlignment() == SwingConstants.CENTER, "default verticalAlignment CENTER");
        label.setVerticalAlignment(SwingConstants.TOP);
        check(label.getVerticalAlignment() == SwingConstants.TOP, "verticalAlignment TOP round trip");
        label.setVerticalAlignment(SwingConstants.BOTTOM);
        check(label.getVerticalAlignment() == SwingConstants.BOTTOM, "verticalAlignment BOTTOM round trip");
        try {
            label.setVerticalAlignment(9);
            check(false, "setVerticalAlignment(9) must throw IAE");
        } catch (IllegalArgumentException expected) {
            check(true, "setVerticalAlignment(9) throws IllegalArgumentException");
        }

        // text position round trips
        label.setVerticalTextPosition(SwingConstants.BOTTOM);
        check(label.getVerticalTextPosition() == SwingConstants.BOTTOM, "verticalTextPosition BOTTOM round trip");
        label.setVerticalTextPosition(SwingConstants.TOP);
        check(label.getVerticalTextPosition() == SwingConstants.TOP, "verticalTextPosition TOP round trip");
        label.setHorizontalTextPosition(SwingConstants.TRAILING);
        check(label.getHorizontalTextPosition() == SwingConstants.TRAILING, "horizontalTextPosition TRAILING round trip");
        try {
            label.setVerticalTextPosition(7);
            check(false, "setVerticalTextPosition(7) must throw IAE");
        } catch (IllegalArgumentException expected) {
            check(true, "setVerticalTextPosition(7) throws IllegalArgumentException");
        }

        // displayed mnemonic chain: char sets key code and auto-locates the index
        label.setDisplayedMnemonic('l');            // "Changed" has no 'l' -> index -1
        check(label.getDisplayedMnemonic() == (int) 'L', "setDisplayedMnemonic(char) stores upper-case keycode");
        label.setText("Hello");
        label.setDisplayedMnemonic('l');
        check(label.getDisplayedMnemonicIndex() == 2, "mnemonic auto index for 'l' in Hello");
        label.setDisplayedMnemonic(java.awt.event.KeyEvent.VK_X);
        check(label.getDisplayedMnemonic() == java.awt.event.KeyEvent.VK_X, "setDisplayedMnemonic(int) round trip");
        label.setDisplayedMnemonicIndex(0);
        check(label.getDisplayedMnemonicIndex() == 0, "manual mnemonicIndex round trip");
        label.setDisplayedMnemonicIndex(-1);
        check(label.getDisplayedMnemonicIndex() == -1, "mnemonicIndex -1 clears");
        try {
            label.setDisplayedMnemonicIndex(5);     // "Hello".length() == 5
            check(false, "setDisplayedMnemonicIndex(len) must throw IAE");
        } catch (IllegalArgumentException expected) {
            check(true, "setDisplayedMnemonicIndex(len) throws IllegalArgumentException");
        }
        try {
            label.setDisplayedMnemonicIndex(-2);
            check(false, "setDisplayedMnemonicIndex(-2) must throw IAE");
        } catch (IllegalArgumentException expected) {
            check(true, "setDisplayedMnemonicIndex(-2) throws IllegalArgumentException");
        }
        try {
            new JLabel().setDisplayedMnemonicIndex(0);
            check(false, "index 0 on empty text must throw IAE");
        } catch (IllegalArgumentException expected) {
            check(true, "index 0 on empty text throws IllegalArgumentException");
        }

        // realized-state chain: pack a frame with the label, verify visibility, then dispose
        JFrame frame = new JFrame("JLabelTest");
        try {
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.getContentPane().add(label);
            frame.pack();
            frame.setVisible(true);
            check(frame.isVisible(), "frame visible after setVisible(true)");
            check(label.isVisible(), "label visible inside realized frame");
            check(label.isDisplayable(), "label displayable inside realized frame");
            check(label.isShowing() || !frame.isVisible(), "label showing consistency");
        } finally {
            frame.dispose();
        }
        check(!frame.isVisible(), "frame not visible after dispose");
    }

    // ---- Part 3: deep contract additions under Metal ----
    static void testContract() throws Exception {
        // constructor matrix and LaF defaults
        JLabel l0 = new JLabel();
        check("".equals(l0.getText()), "no-arg text is the LaF default (empty string)");
        check(l0.getIcon() == null, "no-arg icon null");
        check(l0.getHorizontalTextPosition() == SwingConstants.TRAILING, "horizontalTextPosition default TRAILING");
        check(l0.getVerticalTextPosition() == SwingConstants.CENTER, "verticalTextPosition default CENTER");
        check(l0.getAutoscrolls() == false, "labels do not autoscroll by default");
        check("LabelUI".equals(l0.getUIClassID()), "getUIClassID LabelUI");
        check("javax.swing.plaf.metal.MetalLabelUI".equals(l0.getUI().getClass().getName()),
                "Metal installs MetalLabelUI");
        javax.swing.ImageIcon ic = new javax.swing.ImageIcon(
                new java.awt.image.BufferedImage(3, 2, java.awt.image.BufferedImage.TYPE_INT_RGB));
        check(new JLabel(ic).getIcon() == ic, "icon ctor");
        check("ti".equals(new JLabel("ti", ic, SwingConstants.RIGHT).getText())
                && new JLabel("ti", ic, SwingConstants.RIGHT).getHorizontalAlignment() == SwingConstants.RIGHT,
                "text+icon+alignment ctor");
        check(new JLabel(null, null, SwingConstants.CENTER).getHorizontalAlignment() == SwingConstants.CENTER,
                "null text/icon ctor tolerated");

        // ---- text semantics
        l0.setText("hello");
        check("hello".equals(l0.getText()), "setText/getText");
        l0.setText(null);
        check(l0.getText() == null, "setText(null) -> null (not empty)");

        // ---- iconTextGap accepts negative values (no validation)
        l0.setIconTextGap(-1);
        check(l0.getIconTextGap() == -1, "setIconTextGap(-1) accepted");

        // ---- displayed mnemonic auto-index + range validation
        JLabel lm = new JLabel("abc");
        lm.setDisplayedMnemonic(java.awt.event.KeyEvent.VK_B);
        check(lm.getDisplayedMnemonic() == java.awt.event.KeyEvent.VK_B, "setDisplayedMnemonic(int)");
        check(lm.getDisplayedMnemonicIndex() == 1, "UI auto-derives index 1 for 'B' in 'abc'");
        lm.setDisplayedMnemonicIndex(0);
        check(lm.getDisplayedMnemonicIndex() == 0, "index may point at any in-range char (no mnemonic-match rule)");
        try {
            lm.setDisplayedMnemonicIndex(99);
            check(false, "IAE mnemonicIndex 99");
        } catch (IllegalArgumentException expected) { passed++; }
        try {
            new JLabel((String) null).setDisplayedMnemonicIndex(0);
            check(false, "IAE mnemonicIndex on null text");
        } catch (IllegalArgumentException expected) { passed++; }
        check(lm.getDisplayedMnemonicIndex() == 0, "failed setters leave index untouched");

        // ---- derived disabled icon: ImageIcon only, derived on query, explicit wins
        l0.setIcon(ic);
        check(l0.getIcon() == ic, "setIcon identity");
        javax.swing.Icon derived = l0.getDisabledIcon();
        check(derived != null && derived != ic, "ImageIcon gets a LaF-derived disabledIcon");
        l0.setDisabledIcon(ic);
        check(l0.getDisabledIcon() == ic, "explicit disabledIcon wins");
        check(new JLabel("d", fixedIcon(2, 2), SwingConstants.LEFT).getDisabledIcon() == null,
                "plain Icon gets no derived disabledIcon");
    }

    public static void main(String[] args) throws Exception {
        javax.swing.UIManager.setLookAndFeel(new javax.swing.plaf.metal.MetalLookAndFeel());
        runOnEDT(JLabelTest::testWhiteBox);
        runOnEDT(JLabelTest::testBlackBox);
        runOnEDT(JLabelTest::testContract);
        System.out.println("PASS (" + passed + " assertions)");
    }
    interface ThrowingRunnable { void run() throws Exception; }
    static void runOnEDT(ThrowingRunnable r) throws Exception {
        try {
            EventQueue.invokeAndWait(() -> {
                try { r.run(); }
                catch (Exception e) { throw new RuntimeException(e); }
            });
        } catch (java.lang.reflect.InvocationTargetException e) {
            Throwable t = e.getCause();
            if (t instanceof RuntimeException) throw (RuntimeException) t;
            if (t instanceof Error) throw (Error) t;
            throw new RuntimeException(t);
        }
    }
}
