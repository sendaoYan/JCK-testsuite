/*
 * @test
 * @summary java.awt.Frame conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main FrameTest
 */

import java.awt.*;
import java.awt.image.BufferedImage;
import java.lang.reflect.*;

public class FrameTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.Frame");
        check(c != null, "type loads: java.awt.Frame");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public");
        check(!Modifier.isAbstract(m), "not abstract");
        check(c.getSuperclass() == Window.class, "extends Window");
        boolean menuContainer = false;
        for (Class<?> i : c.getInterfaces()) if (i == MenuContainer.class) menuContainer = true;
        check(menuContainer, "implements MenuContainer");
        for (String name : new String[]{"getTitle", "setTitle", "setUndecorated", "isUndecorated",
                "setResizable", "isResizable", "setMenuBar", "getMenuBar", "setIconImage",
                "getIconImage", "setCursor", "getCursorType", "setMaximizedBounds",
                "getMaximizedBounds", "getState", "setState", "getFrames"}) {
            check(hasMethod(c, name), "has " + name);
        }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        Frame f = new Frame("hello");
        check("hello".equals(f.getTitle()), "constructor title");
        f.setTitle("world");
        check("world".equals(f.getTitle()), "setTitle/getTitle");

        check(f.isResizable(), "resizable default true");
        f.setResizable(false);
        check(!f.isResizable(), "setResizable(false)");

        check(!f.isUndecorated(), "decorated default");
        f.setUndecorated(true);
        check(f.isUndecorated(), "setUndecorated(true)");
        f.setUndecorated(false);
        check(!f.isUndecorated(), "setUndecorated(false)");

        MenuBar mb = new MenuBar();
        f.setMenuBar(mb);
        check(f.getMenuBar() == mb, "setMenuBar/getMenuBar");

        BufferedImage icon = new BufferedImage(16, 16, BufferedImage.TYPE_INT_RGB);
        f.setIconImage(icon);
        check(f.getIconImage() == icon, "setIconImage/getIconImage");

        Rectangle r = new Rectangle(10, 20, 100, 200);
        f.setMaximizedBounds(r);
        check(r.equals(f.getMaximizedBounds()), "setMaximizedBounds/getMaximizedBounds");

        check(f.getState() == Frame.NORMAL, "default state NORMAL");
        check(f.getCursorType() == Frame.DEFAULT_CURSOR, "default cursor type DEFAULT_CURSOR");
        f.setCursor(Frame.CROSSHAIR_CURSOR);
        check(f.getCursorType() == Frame.CROSSHAIR_CURSOR, "setCursor/getCursorType");

        check(!f.isDisplayable(), "not displayable before setVisible");
        f.setSize(200, 150);
        f.setVisible(true);
        try {
            check(f.isDisplayable(), "displayable after setVisible");
            check(f.isShowing(), "showing after setVisible");
            check(f.getSize().equals(new Dimension(200, 150)), "setSize/getSize");
            boolean saw = false;
            for (Frame x : Frame.getFrames()) if (x == f) saw = true;
            check(saw, "getFrames contains this frame");
        } finally {
            f.dispose();
            check(!f.isDisplayable(), "dispose clears displayable");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
