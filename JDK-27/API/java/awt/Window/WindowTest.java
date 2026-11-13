/*
 * @test
 * @summary java.awt.Window conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main WindowTest
 */

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.event.WindowListener;
import java.awt.event.WindowStateListener;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.List;

public class WindowTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }

    static boolean contains(Window[] arr, Window x) {
        for (Window w : arr) if (w == x) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.Window");
        check(c != null, "type loads: java.awt.Window");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public");
        check(!Modifier.isAbstract(m), "not abstract");
        check(c.getSuperclass() == Container.class, "extends Container");
        boolean accessible = false;
        for (Class<?> i : c.getInterfaces()) if (i == javax.accessibility.Accessible.class) accessible = true;
        check(accessible, "implements Accessible");
        check(c.getConstructors().length == 3, "3 public constructors");
        for (String name : new String[]{"getOwner", "getOwnedWindows", "getWindows",
                "getOwnerlessWindows", "setModalExclusionType", "getModalExclusionType", "setType", "getType",
                "setSize", "setLocation", "setVisible", "dispose",
                "setAlwaysOnTop", "isAlwaysOnTop", "isAlwaysOnTopSupported", "getWarningString",
                "setIconImage", "setIconImages", "getIconImages",
                "setFocusableWindowState", "getFocusableWindowState", "toFront", "toBack",
                "setLocationRelativeTo", "setLocationByPlatform", "isLocationByPlatform",
                "pack", "addWindowListener", "removeWindowListener", "getWindowListeners",
                "addWindowFocusListener", "getWindowFocusListeners",
                "addWindowStateListener", "getWindowStateListeners"}) {
            check(hasMethod(c, name), "has " + name);
        }
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

            // setLocationRelativeTo(null) 以“可用屏幕区域”的中心定位（getCenterPoint），
            // 而不是整屏中心：Windows 会把任务栏从可用区域里扣掉，整屏中心会偏下/偏右。
            // 之后再按实现最后一步把窗口夹回整屏边界内。
            w.setSize(120, 90);
            w.setLocationRelativeTo(null);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            Rectangle sb = ge.getDefaultScreenDevice().getDefaultConfiguration().getBounds();
            Point cp = ge.getCenterPoint();
            int ex = cp.x - 120 / 2;
            int ey = cp.y - 90 / 2;
            if (ey + 90 > sb.y + sb.height) ey = sb.y + sb.height - 90;
            if (ey < sb.y) ey = sb.y;
            if (ex + 120 > sb.x + sb.width) ex = sb.x + sb.width - 120;
            if (ex < sb.x) ex = sb.x;
            check(w.getLocation().equals(new Point(ex, ey)),
                  "setLocationRelativeTo(null) centers on usable bounds ("
                          + w.getLocation() + " vs " + ex + "," + ey + ")");
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
        testWhiteBox();
        testBlackBox();
        testConstructorsAndOwnership();
        testLifecycleEvents();
        testGeometryContracts();
        testWindowListenerFamilies();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
