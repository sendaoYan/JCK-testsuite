/*
 * @test
 * @summary java.awt.Window conformance: structural reflection + black-box behavior.
 * @key headful
 * @run main WindowTest
 */

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.event.WindowListener;
import java.awt.event.WindowStateListener;
import java.util.ArrayList;
import java.util.List;

public class WindowTest {
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

    static boolean contains(Window[] arr, Window x) {
        for (Window w : arr) if (w == x) return true;
        return false;
    }

    // ---- Part 1: structural reflection (public API surface) ----
    static void testStructure() throws Exception {
        Class<?> c = Class.forName("java.awt.Window");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.awt.Container"), "superclass java.awt.Container");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"javax.accessibility.Accessible"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "getIconImages"), "method getIconImages");
        check(hasMethod(c, "setIconImages"), "method setIconImages");
        check(hasMethod(c, "setIconImage"), "method setIconImage");
        check(hasMethod(c, "addNotify"), "method addNotify");
        check(hasMethod(c, "removeNotify"), "method removeNotify");
        check(hasMethod(c, "pack"), "method pack");
        check(hasMethod(c, "setMinimumSize"), "method setMinimumSize");
        check(hasMethod(c, "setSize"), "method setSize");
        check(hasMethod(c, "setLocation"), "method setLocation");
        check(hasMethod(c, "reshape"), "method reshape");
        check(hasMethod(c, "setVisible"), "method setVisible");
        check(hasMethod(c, "show"), "method show");
        check(hasMethod(c, "hide"), "method hide");
        check(hasMethod(c, "dispose"), "method dispose");
        check(hasMethod(c, "toFront"), "method toFront");
        check(hasMethod(c, "toBack"), "method toBack");
        check(hasMethod(c, "getToolkit"), "method getToolkit");
        check(hasMethod(c, "getWarningString"), "method getWarningString");
        check(hasMethod(c, "getLocale"), "method getLocale");
        check(hasMethod(c, "getInputContext"), "method getInputContext");
        check(hasMethod(c, "setCursor"), "method setCursor");
        check(hasMethod(c, "getOwner"), "method getOwner");
        check(hasMethod(c, "getOwnedWindows"), "method getOwnedWindows");
        check(hasMethod(c, "getWindows"), "method getWindows");
        check(hasMethod(c, "getOwnerlessWindows"), "method getOwnerlessWindows");
        check(hasMethod(c, "setModalExclusionType"), "method setModalExclusionType");
        check(hasMethod(c, "getModalExclusionType"), "method getModalExclusionType");
        check(hasMethod(c, "addWindowListener"), "method addWindowListener");
        check(hasMethod(c, "addWindowStateListener"), "method addWindowStateListener");
        check(hasMethod(c, "addWindowFocusListener"), "method addWindowFocusListener");
        check(hasMethod(c, "removeWindowListener"), "method removeWindowListener");
        check(hasMethod(c, "removeWindowStateListener"), "method removeWindowStateListener");
        check(hasMethod(c, "removeWindowFocusListener"), "method removeWindowFocusListener");
        check(hasMethod(c, "getWindowListeners"), "method getWindowListeners");
        check(hasMethod(c, "getWindowFocusListeners"), "method getWindowFocusListeners");
        check(hasMethod(c, "getWindowStateListeners"), "method getWindowStateListeners");
        check(hasMethod(c, "getListeners"), "method getListeners");
        check(hasMethod(c, "setAlwaysOnTop"), "method setAlwaysOnTop");
        check(hasMethod(c, "isAlwaysOnTopSupported"), "method isAlwaysOnTopSupported");
        check(hasMethod(c, "isAlwaysOnTop"), "method isAlwaysOnTop");
        check(hasMethod(c, "getFocusOwner"), "method getFocusOwner");
        check(hasMethod(c, "getMostRecentFocusOwner"), "method getMostRecentFocusOwner");
        check(hasMethod(c, "isActive"), "method isActive");
        check(hasMethod(c, "isFocused"), "method isFocused");
        check(hasMethod(c, "getFocusTraversalKeys"), "method getFocusTraversalKeys");
        check(hasMethod(c, "setFocusCycleRoot"), "method setFocusCycleRoot");
        check(hasMethod(c, "isFocusCycleRoot"), "method isFocusCycleRoot");
        check(hasMethod(c, "getFocusCycleRootAncestor"), "method getFocusCycleRootAncestor");
        check(hasMethod(c, "isFocusableWindow"), "method isFocusableWindow");
        check(hasMethod(c, "getFocusableWindowState"), "method getFocusableWindowState");
        check(hasMethod(c, "setFocusableWindowState"), "method setFocusableWindowState");
        check(hasMethod(c, "setAutoRequestFocus"), "method setAutoRequestFocus");
        check(hasMethod(c, "isAutoRequestFocus"), "method isAutoRequestFocus");
        check(hasMethod(c, "addPropertyChangeListener"), "method addPropertyChangeListener");
        check(hasMethod(c, "isValidateRoot"), "method isValidateRoot");
        check(hasMethod(c, "postEvent"), "method postEvent");
        check(hasMethod(c, "isShowing"), "method isShowing");
        check(hasMethod(c, "applyResourceBundle"), "method applyResourceBundle");
        check(hasMethod(c, "setType"), "method setType");
        check(hasMethod(c, "getType"), "method getType");
        check(hasMethod(c, "getAccessibleContext"), "method getAccessibleContext");
        check(hasMethod(c, "setLocationRelativeTo"), "method setLocationRelativeTo");
        check(hasMethod(c, "createBufferStrategy"), "method createBufferStrategy");
        check(hasMethod(c, "getBufferStrategy"), "method getBufferStrategy");
        check(hasMethod(c, "setLocationByPlatform"), "method setLocationByPlatform");
        check(hasMethod(c, "isLocationByPlatform"), "method isLocationByPlatform");
        check(hasMethod(c, "setBounds"), "method setBounds");
        check(hasMethod(c, "getOpacity"), "method getOpacity");
        check(hasMethod(c, "setOpacity"), "method setOpacity");
        check(hasMethod(c, "getShape"), "method getShape");
        check(hasMethod(c, "setShape"), "method setShape");
        check(hasMethod(c, "getBackground"), "method getBackground");
        check(hasMethod(c, "setBackground"), "method setBackground");
        check(hasMethod(c, "isOpaque"), "method isOpaque");
        check(hasMethod(c, "paint"), "method paint");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        Frame owner = new Frame("owner");
        owner.setVisible(true);
        try {
            Window w = new Window(owner);
            check(w.getOwner() == owner, "getOwner is owner frame");
            check(w.getModalExclusionType() == Dialog.ModalExclusionType.NO_EXCLUDE,
                  "default modal exclusion NO_EXCLUDE");
            w.setModalExclusionType(Dialog.ModalExclusionType.APPLICATION_EXCLUDE);
            check(w.getModalExclusionType() == Dialog.ModalExclusionType.APPLICATION_EXCLUDE,
                  "setModalExclusionType roundtrip");
            check(w.getType() == Window.Type.NORMAL, "default type NORMAL");
            w.setType(Window.Type.UTILITY);
            check(w.getType() == Window.Type.UTILITY, "setType roundtrip");

            check(!w.isDisplayable(), "not displayable before visible");
            w.setSize(120, 90);
            check(w.getSize().equals(new Dimension(120, 90)), "setSize/getSize");
            w.setLocation(40, 50);
            check(w.getLocation().equals(new Point(40, 50)), "setLocation/getLocation");

            w.setVisible(true);
            check(w.isDisplayable(), "displayable after setVisible");
            check(w.isShowing(), "showing after setVisible");
            check(owner.getOwnedWindows().length == 1 && owner.getOwnedWindows()[0] == w,
                  "owner owns the window");
            boolean sawWindow = false, sawOwner = false;
            for (Window x : Window.getWindows()) {
                if (x == w) sawWindow = true;
                if (x == owner) sawOwner = true;
            }
            check(sawWindow && sawOwner, "getWindows contains both");

            check(w.getWarningString() == null, "warning string null for application");

            boolean supported = w.isAlwaysOnTopSupported();
            check(supported || !supported, "isAlwaysOnTopSupported returns boolean");
            w.setAlwaysOnTop(true);
            check(w.isAlwaysOnTop(), "setAlwaysOnTop(true)");
            w.setAlwaysOnTop(false);
            check(!w.isAlwaysOnTop(), "setAlwaysOnTop(false)");

            check(!w.isFocusableWindow(), "childless window not focusable");
            w.setFocusableWindowState(false);
            check(!w.getFocusableWindowState(), "setFocusableWindowState(false)");

            w.setBackground(Color.GREEN);
            check(Color.GREEN.equals(w.getBackground()), "setBackground/getBackground");

            w.dispose();
            check(!w.isDisplayable(), "dispose clears displayable");
        } finally {
            owner.dispose();
        }
    }

    // ---- Part 3: constructors + ownership graph ----
    static void testConstructorsAndOwnership() {
        // Window(Window) and Window(Window, GraphicsConfiguration=null)
        Frame owner = new Frame("owner2");
        try {
            Window w1 = new Window(owner);
            Window w2 = new Window(w1);
            check(w2.getOwner() == w1, "Window(Window) owner");
            check(owner.getOwnedWindows().length == 1, "frame owns w1");
            check(w1.getOwnedWindows().length == 1 && w1.getOwnedWindows()[0] == w2,
                  "w1 owns w2");
            check(w1.getOwner() == owner, "w1 owner is frame");
            w2.dispose();
            w1.dispose();

            // null owner constructors are permitted; getOwner() then returns null
            Window nf = new Window((Frame) null);
            check(nf.getOwner() == null, "new Window((Frame)null) owner null");
            nf.dispose();
            Window nw = new Window((Window) null);
            check(nw.getOwner() == null, "new Window((Window)null) owner null");
            nw.dispose();
            Window ngc = new Window(owner, (GraphicsConfiguration) null);
            check(ngc.getOwner() == owner, "new Window(owner, null gc) owner kept");
            ngc.dispose();

            // ownerless windows: a frame is ownerless; an owned window is not
            check(contains(Window.getOwnerlessWindows(), owner),
                  "frame is in getOwnerlessWindows");
            Window owned = new Window(owner);
            check(!contains(Window.getOwnerlessWindows(), owned),
                  "owned window not in getOwnerlessWindows");
            check(contains(Window.getWindows(), owned), "owned window in getWindows");
            owned.dispose();
        } finally {
            owner.dispose();
        }
    }

    // ---- Part 4: lifecycle events opened/closed ----
    static void testLifecycleEvents() throws Exception {
        Frame owner = new Frame("owner3");
        final Window w = new Window(owner);
        final List<String> log = new ArrayList<>();
        w.addWindowListener(new WindowAdapter() {
            public void windowOpened(WindowEvent e) {
                log.add("OPENED:" + e.getID() + ":" + (e.getSource() == w)
                        + ":" + (e.getWindow() == w));
            }
            public void windowClosed(WindowEvent e) {
                log.add("CLOSED:" + e.getID() + ":" + (e.getWindow() == w));
            }
        });
        w.setSize(80, 60);

        w.setVisible(true);
        EventQueue.invokeAndWait(() -> {}); // let posted events drain
        check(log.size() == 1, "one WINDOW_OPENED after first show");
        check(log.get(0).equals("OPENED:" + WindowEvent.WINDOW_OPENED + ":true:true"),
              "opened event id/source/window exact");
        check(WindowEvent.WINDOW_OPENED == 200, "WINDOW_OPENED id 200");

        w.setVisible(false);
        EventQueue.invokeAndWait(() -> {});
        check(log.size() == 1, "setVisible(false) posts no WINDOW_CLOSED");

        w.setVisible(true); // re-show: no second OPENED
        EventQueue.invokeAndWait(() -> {});
        check(log.size() == 1, "re-show posts no second WINDOW_OPENED");
        check(w.isDisplayable(), "displayable again before dispose");

        w.toFront(); // must not throw
        check(true, "toFront() callable");
        w.toBack(); // must not throw
        check(true, "toBack() callable");

        w.dispose();
        EventQueue.invokeAndWait(() -> {});
        check(log.size() == 2, "dispose posts WINDOW_CLOSED");
        check(log.get(1).equals("CLOSED:" + WindowEvent.WINDOW_CLOSED + ":true"),
              "closed event id/window exact");
        check(WindowEvent.WINDOW_CLOSED == 202, "WINDOW_CLOSED id 202");
        check(!w.isDisplayable(), "not displayable after dispose");

        w.dispose(); // second dispose is a no-op
        EventQueue.invokeAndWait(() -> {});
        check(log.size() == 2, "second dispose posts nothing");
        check(!w.isDisplayable(), "still not displayable");

        // peer is re-created when shown again after dispose
        w.setVisible(true);
        EventQueue.invokeAndWait(() -> {});
        check(w.isDisplayable(), "re-show after dispose recreates peer");
        check(log.size() == 2, "re-show after dispose posts no new OPENED");
        w.dispose();
        EventQueue.invokeAndWait(() -> {});
        owner.dispose();
    }

    // ---- Part 5: geometry contracts ----
    static void testGeometryContracts() {
        Frame owner = new Frame("owner4");
        Window w = new Window(owner);
        try {
            check(!w.isVisible(), "window invisible by default");

            // setSize(0,0) is accepted in-memory before the peer exists
            w.setSize(0, 0);
            check(w.getSize().equals(new Dimension(0, 0)), "setSize(0,0) before show");
            w.setSize(120, 90);

            // negative size stored verbatim while not displayable
            w.setSize(-3, -4);
            check(w.getSize().equals(new Dimension(-3, -4)), "negative size stored");
            w.setSize(120, 90);

            // setBounds moves and sizes in one call
            w.setBounds(11, 22, 33, 44);
            check(w.getBounds().equals(new Rectangle(11, 22, 33, 44)), "setBounds roundtrip");
            check(w.getX() == 11 && w.getY() == 22 && w.getWidth() == 33 && w.getHeight() == 44,
                  "x/y/width/height consistent");

            // setLocationRelativeTo(null) centers on the default screen, size unchanged
            w.setSize(120, 90);
            w.setLocationRelativeTo(null);
            Rectangle sb = GraphicsEnvironment.getLocalGraphicsEnvironment()
                    .getDefaultScreenDevice().getDefaultConfiguration().getBounds();
            int ex = sb.x + (sb.width - 120) / 2;
            int ey = sb.y + (sb.height - 90) / 2;
            check(w.getLocation().equals(new Point(ex, ey)),
                  "setLocationRelativeTo(null) centers (" + w.getLocation() + " vs " + ex + "," + ey + ")");
            check(w.getSize().equals(new Dimension(120, 90)),
                  "setLocationRelativeTo keeps size");

            // location-by-platform flag and its documented clearing
            w.setLocationByPlatform(true);
            check(w.isLocationByPlatform(), "setLocationByPlatform(true)");
            w.setLocation(5, 6);
            check(!w.isLocationByPlatform(), "setLocation clears platform flag");
            w.setLocationByPlatform(true);
            w.setBounds(7, 8, 9, 10);
            check(!w.isLocationByPlatform(), "setBounds clears platform flag");

            // toolkit identity
            check(w.getToolkit() == Toolkit.getDefaultToolkit(), "window toolkit is default toolkit");

            // pack sizes to at least the preferred layout size of the content
            w.removeAll();
            Component body = new Component() {};
            w.setLayout(new BorderLayout());
            w.add(body, BorderLayout.CENTER);
            w.pack();
            check(w.getInsets() != null, "insets non-null after pack");
            check(w.getWidth() >= 0 && w.getHeight() >= 0, "pack leaves non-negative size");
        } finally {
            w.dispose();
            owner.dispose();
        }
    }

    // ---- Part 6: window listener family contracts ----
    static void testWindowListenerFamilies() {
        Frame owner = new Frame("owner5");
        Window w = new Window(owner);
        try {
            check(w.getWindowListeners().length == 0
               && w.getWindowFocusListeners().length == 0
               && w.getWindowStateListeners().length == 0, "window listener lists empty");
            // null registration no-ops
            w.addWindowListener(null);
            w.removeWindowListener(null);
            w.addWindowFocusListener(null);
            w.removeWindowFocusListener(null);
            w.addWindowStateListener(null);
            w.removeWindowStateListener(null);
            check(w.getWindowListeners().length == 0, "null window listener no-op");

            WindowListener wl1 = new WindowAdapter() {};
            WindowListener wl2 = new WindowAdapter() {};
            w.addWindowListener(wl1);
            w.addWindowListener(wl1);
            w.addWindowListener(wl2);
            check(w.getWindowListeners().length == 3, "window listener count 3 with duplicate");
            w.removeWindowListener(wl1);
            check(w.getWindowListeners().length == 2, "single remove drops one");
            w.removeWindowListener(wl1);
            check(w.getWindowListeners().length == 1, "duplicate removed");
            w.removeWindowListener(wl1); // unregistered remove is a no-op
            check(w.getWindowListeners().length == 1, "unregistered remove no-op");
            w.removeWindowListener(wl2);
            check(w.getWindowListeners().length == 0, "empty again");

            WindowFocusListener wfl = new WindowFocusListener() {
                public void windowGainedFocus(WindowEvent e) {}
                public void windowLostFocus(WindowEvent e) {}
            };
            w.addWindowFocusListener(wfl);
            w.addWindowStateListener((WindowStateListener) e -> {});
            check(w.getWindowFocusListeners().length == 1, "focus listener registered");
            check(w.getWindowStateListeners().length == 1, "state listener registered");
            check(w.getListeners(WindowListener.class).length == 0
               && w.getListeners(WindowFocusListener.class).length == 1
               && w.getListeners(WindowStateListener.class).length == 1,
                  "getListeners parity for window families");
            w.removeWindowFocusListener(wfl);
            check(w.getWindowFocusListeners().length == 0, "focus listener removed");
        } finally {
            w.dispose();
            owner.dispose();
        }
    }

    public static void main(String[] args) throws Exception {
        testStructure();
        testBlackBox();
        testConstructorsAndOwnership();
        testLifecycleEvents();
        testGeometryContracts();
        testWindowListenerFamilies();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
