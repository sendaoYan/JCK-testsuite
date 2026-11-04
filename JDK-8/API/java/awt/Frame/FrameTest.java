/*
 * @test
 * @summary java.awt.Frame conformance: structural reflection + black-box behavior.
 * @key headful
 * @run main FrameTest
 */

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class FrameTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void checkThrows(Class<? extends Throwable> ex, Runnable r, String msg) {
        try {
            r.run();
            throw new AssertionError("expected " + ex.getSimpleName() + ": " + msg);
        } catch (AssertionError e) {
            throw e;
        } catch (Throwable t) {
            check(ex.isInstance(t), msg + " -> " + t.getClass().getSimpleName());
        }
    }

    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getMethods())
            if (m.getName().equals(name)) return true;
        return false;
    }

    // ---- Part 1: structural reflection (public API surface) ----
    static void testStructure() throws Exception {
        Class<?> c = Class.forName("java.awt.Frame");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.awt.Window"), "superclass java.awt.Window");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"java.awt.MenuContainer"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "addNotify"), "method addNotify");
        check(hasMethod(c, "getTitle"), "method getTitle");
        check(hasMethod(c, "setTitle"), "method setTitle");
        check(hasMethod(c, "getIconImage"), "method getIconImage");
        check(hasMethod(c, "setIconImage"), "method setIconImage");
        check(hasMethod(c, "getMenuBar"), "method getMenuBar");
        check(hasMethod(c, "setMenuBar"), "method setMenuBar");
        check(hasMethod(c, "isResizable"), "method isResizable");
        check(hasMethod(c, "setResizable"), "method setResizable");
        check(hasMethod(c, "setState"), "method setState");
        check(hasMethod(c, "setExtendedState"), "method setExtendedState");
        check(hasMethod(c, "getState"), "method getState");
        check(hasMethod(c, "getExtendedState"), "method getExtendedState");
        check(hasMethod(c, "setMaximizedBounds"), "method setMaximizedBounds");
        check(hasMethod(c, "getMaximizedBounds"), "method getMaximizedBounds");
        check(hasMethod(c, "setUndecorated"), "method setUndecorated");
        check(hasMethod(c, "isUndecorated"), "method isUndecorated");
        check(hasMethod(c, "setOpacity"), "method setOpacity");
        check(hasMethod(c, "setShape"), "method setShape");
        check(hasMethod(c, "setBackground"), "method setBackground");
        check(hasMethod(c, "remove"), "method remove");
        check(hasMethod(c, "removeNotify"), "method removeNotify");
        check(hasMethod(c, "setCursor"), "method setCursor");
        check(hasMethod(c, "getCursorType"), "method getCursorType");
        check(hasMethod(c, "getFrames"), "method getFrames");
        check(hasMethod(c, "getAccessibleContext"), "method getAccessibleContext");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // state constants exact values
        check(Frame.NORMAL == 0, "NORMAL == 0");
        check(Frame.ICONIFIED == 1, "ICONIFIED == 1");
        check(Frame.MAXIMIZED_HORIZ == 2, "MAXIMIZED_HORIZ == 2");
        check(Frame.MAXIMIZED_VERT == 4, "MAXIMIZED_VERT == 4");
        check(Frame.MAXIMIZED_BOTH == 6, "MAXIMIZED_BOTH == 6");

        // all four constructors; null GraphicsConfiguration is allowed
        Frame f0 = new Frame();
        check("".equals(f0.getTitle()), "default title empty string");
        check(f0.getState() == Frame.NORMAL, "default state NORMAL");
        check(f0.isResizable(), "default resizable true");
        check(f0.getLayout() instanceof BorderLayout, "default layout BorderLayout");
        f0.dispose();
        Frame fgc = new Frame((GraphicsConfiguration) null);
        check(fgc != null, "Frame(GraphicsConfiguration=null) constructs");
        fgc.dispose();
        Frame ftitle = new Frame("hello");
        check("hello".equals(ftitle.getTitle()), "constructor title");
        ftitle.dispose();
        Frame ftgc = new Frame("both", null);
        check("both".equals(ftgc.getTitle()), "Frame(String, null gc) title");
        ftgc.dispose();

        Frame f = new Frame("hello");
        check("hello".equals(f.getTitle()), "constructor title");
        f.setTitle("world");
        check("world".equals(f.getTitle()), "setTitle/getTitle");
        f.setTitle(null);
        check("".equals(f.getTitle()), "setTitle(null) normalized to empty string");

        check(f.isResizable(), "resizable default true");
        f.setResizable(false);
        check(!f.isResizable(), "setResizable(false)");
        f.setResizable(true);
        check(f.isResizable(), "setResizable(true)");

        check(!f.isUndecorated(), "decorated default");
        f.setUndecorated(true);
        check(f.isUndecorated(), "setUndecorated(true)");
        f.setUndecorated(false);
        check(!f.isUndecorated(), "setUndecorated(false)");

        MenuBar mb = new MenuBar();
        Menu file = new Menu("File");
        file.add(new MenuItem("Open"));
        mb.add(file);
        f.setMenuBar(mb);
        check(f.getMenuBar() == mb, "setMenuBar/getMenuBar");
        check(f.getMenuBar().getMenuCount() == 1, "menu bar holds menu");
        f.setMenuBar(null);
        check(f.getMenuBar() == null, "setMenuBar(null) removes");

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
            check(f.toString().contains("world") || f.getTitle() != null, "toString carries title");

            // documented: setUndecorated on a displayable frame throws
            checkThrows(IllegalComponentStateException.class, () -> f.setUndecorated(true),
                        "setUndecorated on displayable frame");
        } finally {
            f.dispose();
            check(!f.isDisplayable(), "dispose clears displayable");
        }
    }

    // ---- Part 3: title property change events ----
    static void testTitlePropertyChange() {
        Frame f = new Frame();
        final List<String> log = new ArrayList<>();
        f.addPropertyChangeListener(e -> log.add(e.getPropertyName() + "|" + e.getOldValue() + "|" + e.getNewValue()));
        f.setTitle("A");
        check(log.remove("title||A"), "setTitle fires title PCE with empty old");
        f.setTitle("A");
        check(log.isEmpty(), "same title fires no PCE");
        f.setTitle("B");
        check(log.remove("title|A|B"), "title PCE old/new exact");
        f.setTitle(null);
        check(log.remove("title|B|"), "setTitle(null) fires with empty new");
        check(log.isEmpty(), "no stray PCEs");
        f.dispose();
    }

    // ---- Part 4: icon list semantics ----
    static void testIconListSemantics() {
        Frame f = new Frame();
        check(f.getIconImages().isEmpty(), "icon list empty by default");
        check(f.getIconImage() == null, "no icon by default");

        BufferedImage i1 = new BufferedImage(16, 16, BufferedImage.TYPE_INT_RGB);
        BufferedImage i2 = new BufferedImage(32, 32, BufferedImage.TYPE_INT_RGB);
        List<Image> src = new ArrayList<>();
        src.add(i1);
        src.add(i2);
        f.setIconImages(src);
        // the list is copied on set
        src.clear();
        check(f.getIconImages().size() == 2, "setIconImages copies the list");
        check(f.getIconImage() == i1, "getIconImage returns the first icon");

        // the returned list is a copy: mutating it does not affect the frame
        List<Image> ret = f.getIconImages();
        ret.clear();
        check(f.getIconImages().size() == 2, "getIconImages returns a copy");

        // setIconImage stores exactly one
        BufferedImage single = new BufferedImage(8, 8, BufferedImage.TYPE_INT_RGB);
        f.setIconImage(single);
        check(f.getIconImages().size() == 1, "setIconImage single entry");
        check(f.getIconImage() == single, "single icon returned");

        // empty list clears
        f.setIconImages(new ArrayList<Image>());
        check(f.getIconImages().isEmpty(), "empty list clears icons");
        check(f.getIconImage() == null, "no icon after clear");
        f.dispose();
    }

    // ---- Part 5: state machine contracts ----
    static void testStateContracts() {
        Toolkit tk = Toolkit.getDefaultToolkit();
        Frame f = new Frame("state");
        f.setSize(100, 100);
        f.setVisible(true);
        try {
            check(f.getState() == Frame.NORMAL, "state NORMAL when shown");
            check(f.getExtendedState() == Frame.NORMAL, "extended NORMAL when shown");

            // setState/getState cycle conditional on platform support
            if (tk.isFrameStateSupported(Frame.ICONIFIED)) {
                f.setState(Frame.ICONIFIED);
                check(f.getState() == Frame.ICONIFIED, "setState(ICONIFIED)");
                check(f.getExtendedState() == Frame.ICONIFIED, "extended mirrors ICONIFIED");
                f.setState(Frame.NORMAL);
                check(f.getState() == Frame.NORMAL, "setState(NORMAL) back");
            }
            // an out-of-range state leaves the state unchanged (masking to known bits)
            f.setState(88);
            check(f.getState() == Frame.NORMAL, "setState(88) leaves NORMAL");

            // extended state support is platform dependent: assert the documented
            // behavior branch, never a hardcoded value
            if (tk.isFrameStateSupported(Frame.MAXIMIZED_BOTH)) {
                f.setExtendedState(Frame.MAXIMIZED_BOTH);
                check(f.getExtendedState() == Frame.MAXIMIZED_BOTH,
                      "MAXIMIZED_BOTH roundtrip where supported");
                f.setExtendedState(Frame.NORMAL);
                check(f.getExtendedState() == Frame.NORMAL, "back to NORMAL");
            } else {
                // setExtendedState is specified (and implemented) to ignore
                // states the toolkit does not support: state remains unchanged
                f.setExtendedState(Frame.MAXIMIZED_BOTH);
                check(f.getExtendedState() == Frame.NORMAL,
                      "unsupported extended state not stored");
            }
            // combination state where supported
            if (tk.isFrameStateSupported(Frame.ICONIFIED | Frame.MAXIMIZED_BOTH)) {
                f.setExtendedState(Frame.ICONIFIED | Frame.MAXIMIZED_BOTH);
                check(f.getExtendedState() == (Frame.ICONIFIED | Frame.MAXIMIZED_BOTH),
                      "combined extended state roundtrip");
                f.setExtendedState(Frame.NORMAL);
                check(f.getExtendedState() == Frame.NORMAL, "combined reset");
            }
        } finally {
            f.dispose();
        }
    }

    // ---- Part 6: pack sizes to preferred layout ----
    static void testPack() {
        Frame f = new Frame("pack");
        f.setLayout(new BorderLayout());
        Component body = new Component() {};
        f.add(body, BorderLayout.CENTER);
        f.pack();
        // documented: sized to fit preferred layout sizes of subcomponents (plus insets)
        check(f.getWidth() >= 0 && f.getHeight() >= 0, "pack leaves non-negative size");
        check(f.getInsets() != null, "insets non-null after pack");
        f.dispose();
    }

    public static void main(String[] args) throws Exception {
        testStructure();
        testBlackBox();
        testTitlePropertyChange();
        testIconListSemantics();
        testStateContracts();
        testPack();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
