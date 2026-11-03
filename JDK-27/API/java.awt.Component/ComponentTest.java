/*
 * @test
 * @summary java.awt.Component conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main ComponentTest
 */

import java.awt.*;
import java.util.Locale;
import java.lang.reflect.*;

public class ComponentTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }

    static class C extends Component {}

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.Component");
        check(c != null, "type loads: java.awt.Component");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public");
        check(Modifier.isAbstract(m), "abstract");
        check(c.getSuperclass() == Object.class, "extends Object");
        boolean imageObserver = false, menuContainer = false, serializable = false;
        for (Class<?> i : c.getInterfaces()) {
            if (i == java.awt.image.ImageObserver.class) imageObserver = true;
            if (i == MenuContainer.class) menuContainer = true;
            if (i == java.io.Serializable.class) serializable = true;
        }
        check(imageObserver, "implements ImageObserver");
        check(menuContainer, "implements MenuContainer");
        check(serializable, "implements Serializable");
        for (String name : new String[]{"getName", "setName", "getParent", "setEnabled",
                "isEnabled", "setVisible", "isShowing", "setBackground", "getBackground",
                "setForeground", "getForeground", "setFont", "getFont", "setSize", "getSize",
                "setBounds", "getBounds", "setLocation", "getLocation", "getPreferredSize",
                "getMinimumSize", "getMaximumSize", "setPreferredSize", "setMinimumSize",
                "setMaximumSize", "setCursor", "getCursor", "setIgnoreRepaint", "getIgnoreRepaint",
                "isLightweight", "isOpaque", "setComponentOrientation", "getComponentOrientation",
                "setFocusable", "isFocusable", "getTreeLock", "getToolkit"}) {
            check(hasMethod(c, name), "has " + name);
        }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // alignment constants
        check(Component.TOP_ALIGNMENT == 0.0f, "TOP_ALIGNMENT 0");
        check(Component.CENTER_ALIGNMENT == 0.5f, "CENTER_ALIGNMENT 0.5");
        check(Component.BOTTOM_ALIGNMENT == 1.0f, "BOTTOM_ALIGNMENT 1");
        check(Component.LEFT_ALIGNMENT == 0.0f, "LEFT_ALIGNMENT 0");
        check(Component.RIGHT_ALIGNMENT == 1.0f, "RIGHT_ALIGNMENT 1");

        Frame frame = new Frame("ComponentTest");
        frame.setLayout(null);
        frame.setSize(300, 300);
        frame.setVisible(true);
        try {
            C c = new C();
            frame.add(c);
            check(c.isDisplayable(), "displayable after add to showing frame");
            check(c.isShowing(), "showing after add");
            check(c.isLightweight(), "custom component is lightweight");
            check(!c.isOpaque(), "lightweight component not opaque");
            check(c.getParent() == frame, "parent is frame");
            check(c.getGraphicsConfiguration() != null, "graphics configuration non-null");
            check(c.getToolkit() != null, "toolkit non-null");
            check(c.getTreeLock() != null, "tree lock non-null");
            check(c.getName() == null, "default name null");

            c.setName("widget");
            check("widget".equals(c.getName()), "setName/getName");

            c.setEnabled(false);
            check(!c.isEnabled(), "setEnabled(false)");
            c.setEnabled(true);
            check(c.isEnabled(), "setEnabled(true)");
            c.disable();
            check(!c.isEnabled(), "disable()");
            c.enable();
            check(c.isEnabled(), "enable()");

            c.setBackground(Color.RED);
            check(Color.RED.equals(c.getBackground()), "setBackground/getBackground");
            check(c.isBackgroundSet(), "isBackgroundSet");
            c.setForeground(Color.BLUE);
            check(Color.BLUE.equals(c.getForeground()), "setForeground/getForeground");
            check(c.isForegroundSet(), "isForegroundSet");
            Font f = new Font("Dialog", Font.PLAIN, 12);
            c.setFont(f);
            check(f.equals(c.getFont()), "setFont/getFont");
            check(c.isFontSet(), "isFontSet");
            c.setLocale(Locale.US);
            check(Locale.US.equals(c.getLocale()), "setLocale/getLocale");

            c.setSize(50, 60);
            check(c.getSize().equals(new Dimension(50, 60)), "setSize/getSize");
            check(c.getWidth() == 50 && c.getHeight() == 60, "getWidth/getHeight");
            c.resize(70, 80);
            check(c.getSize().equals(new Dimension(70, 80)), "resize");

            c.setBounds(10, 20, 30, 40);
            check(c.getBounds().equals(new Rectangle(10, 20, 30, 40)), "setBounds/getBounds");
            check(c.getX() == 10 && c.getY() == 20, "getX/getY");
            c.setLocation(5, 6);
            check(c.getLocation().equals(new Point(5, 6)), "setLocation/getLocation");
            c.move(7, 8);
            check(c.getLocation().equals(new Point(7, 8)), "move");

            c.setSize(100, 100);
            check(c.contains(50, 50), "contains inside");
            check(!c.contains(150, 150), "contains outside");
            check(c.inside(0, 0), "inside origin");

            c.setPreferredSize(new Dimension(77, 88));
            check(c.getPreferredSize().equals(new Dimension(77, 88)), "setPreferredSize/getPreferredSize");
            check(c.isPreferredSizeSet(), "isPreferredSizeSet");
            c.setMinimumSize(new Dimension(9, 8));
            check(c.getMinimumSize().equals(new Dimension(9, 8)), "setMinimumSize/getMinimumSize");
            check(c.isMinimumSizeSet(), "isMinimumSizeSet");
            c.setMaximumSize(new Dimension(100, 200));
            check(c.getMaximumSize().equals(new Dimension(100, 200)), "setMaximumSize/getMaximumSize");
            check(c.isMaximumSizeSet(), "isMaximumSizeSet");

            Cursor cur = Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
            c.setCursor(cur);
            check(c.getCursor() == cur, "setCursor/getCursor");
            check(c.isCursorSet(), "isCursorSet");

            c.setIgnoreRepaint(true);
            check(c.getIgnoreRepaint(), "setIgnoreRepaint/getIgnoreRepaint");

            c.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            check(c.getComponentOrientation() == ComponentOrientation.RIGHT_TO_LEFT,
                  "setComponentOrientation/getComponentOrientation");

            c.setFocusable(true);
            check(c.isFocusable(), "setFocusable/isFocusable");

            check(c.getAlignmentX() == 0.5f && c.getAlignmentY() == 0.5f, "default alignment center");

            c.setVisible(false);
            check(!c.isVisible(), "setVisible(false)");
            c.setVisible(true);
            check(c.isVisible(), "setVisible(true)");
            c.hide();
            check(!c.isVisible(), "hide()");
            c.show();
            check(c.isVisible(), "show()");

            check(c.toString().contains("widget"), "toString contains name");
        } finally {
            frame.dispose();
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
