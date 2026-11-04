/*
 * @test
 * @summary java.awt.Component conformance: structural reflection + black-box behavior.
 * @key headful
 * @run main ComponentTest
 */

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ComponentTest {
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

    static class C extends Component {}

    // ---- Part 1: structural reflection (public API surface) ----
    static void testStructure() throws Exception {
        Class<?> c = Class.forName("java.awt.Component");
        check(!c.isInterface() && !c.isEnum(), "not interface/enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"java.awt.MenuContainer", "java.awt.image.ImageObserver", "java.io.Serializable"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "getName"), "method getName");
        check(hasMethod(c, "setName"), "method setName");
        check(hasMethod(c, "getParent"), "method getParent");
        check(hasMethod(c, "getPeer"), "method getPeer");
        check(hasMethod(c, "setDropTarget"), "method setDropTarget");
        check(hasMethod(c, "getDropTarget"), "method getDropTarget");
        check(hasMethod(c, "getGraphicsConfiguration"), "method getGraphicsConfiguration");
        check(hasMethod(c, "getTreeLock"), "method getTreeLock");
        check(hasMethod(c, "getToolkit"), "method getToolkit");
        check(hasMethod(c, "isValid"), "method isValid");
        check(hasMethod(c, "isDisplayable"), "method isDisplayable");
        check(hasMethod(c, "isVisible"), "method isVisible");
        check(hasMethod(c, "getMousePosition"), "method getMousePosition");
        check(hasMethod(c, "isShowing"), "method isShowing");
        check(hasMethod(c, "isEnabled"), "method isEnabled");
        check(hasMethod(c, "setEnabled"), "method setEnabled");
        check(hasMethod(c, "enable"), "method enable");
        check(hasMethod(c, "disable"), "method disable");
        check(hasMethod(c, "isDoubleBuffered"), "method isDoubleBuffered");
        check(hasMethod(c, "enableInputMethods"), "method enableInputMethods");
        check(hasMethod(c, "setVisible"), "method setVisible");
        check(hasMethod(c, "show"), "method show");
        check(hasMethod(c, "hide"), "method hide");
        check(hasMethod(c, "getForeground"), "method getForeground");
        check(hasMethod(c, "setForeground"), "method setForeground");
        check(hasMethod(c, "isForegroundSet"), "method isForegroundSet");
        check(hasMethod(c, "getBackground"), "method getBackground");
        check(hasMethod(c, "setBackground"), "method setBackground");
        check(hasMethod(c, "isBackgroundSet"), "method isBackgroundSet");
        check(hasMethod(c, "getFont"), "method getFont");
        check(hasMethod(c, "setFont"), "method setFont");
        check(hasMethod(c, "isFontSet"), "method isFontSet");
        check(hasMethod(c, "getLocale"), "method getLocale");
        check(hasMethod(c, "setLocale"), "method setLocale");
        check(hasMethod(c, "getColorModel"), "method getColorModel");
        check(hasMethod(c, "getLocation"), "method getLocation");
        check(hasMethod(c, "getLocationOnScreen"), "method getLocationOnScreen");
        check(hasMethod(c, "location"), "method location");
        check(hasMethod(c, "setLocation"), "method setLocation");
        check(hasMethod(c, "move"), "method move");
        check(hasMethod(c, "getSize"), "method getSize");
        check(hasMethod(c, "size"), "method size");
        check(hasMethod(c, "setSize"), "method setSize");
        check(hasMethod(c, "resize"), "method resize");
        check(hasMethod(c, "getBounds"), "method getBounds");
        check(hasMethod(c, "bounds"), "method bounds");
        check(hasMethod(c, "setBounds"), "method setBounds");
        check(hasMethod(c, "reshape"), "method reshape");
        check(hasMethod(c, "getX"), "method getX");
        check(hasMethod(c, "getY"), "method getY");
        check(hasMethod(c, "getWidth"), "method getWidth");
        check(hasMethod(c, "getHeight"), "method getHeight");
        check(hasMethod(c, "isOpaque"), "method isOpaque");
        check(hasMethod(c, "isLightweight"), "method isLightweight");
        check(hasMethod(c, "setPreferredSize"), "method setPreferredSize");
        check(hasMethod(c, "isPreferredSizeSet"), "method isPreferredSizeSet");
        check(hasMethod(c, "getPreferredSize"), "method getPreferredSize");
        check(hasMethod(c, "preferredSize"), "method preferredSize");
        check(hasMethod(c, "setMinimumSize"), "method setMinimumSize");
        check(hasMethod(c, "isMinimumSizeSet"), "method isMinimumSizeSet");
        check(hasMethod(c, "getMinimumSize"), "method getMinimumSize");
        check(hasMethod(c, "minimumSize"), "method minimumSize");
        check(hasMethod(c, "setMaximumSize"), "method setMaximumSize");
        check(hasMethod(c, "isMaximumSizeSet"), "method isMaximumSizeSet");
        check(hasMethod(c, "getMaximumSize"), "method getMaximumSize");
        check(hasMethod(c, "getAlignmentX"), "method getAlignmentX");
        check(hasMethod(c, "getAlignmentY"), "method getAlignmentY");
        check(hasMethod(c, "getBaseline"), "method getBaseline");
        check(hasMethod(c, "getBaselineResizeBehavior"), "method getBaselineResizeBehavior");
        check(hasMethod(c, "doLayout"), "method doLayout");
        check(hasMethod(c, "layout"), "method layout");
        check(hasMethod(c, "validate"), "method validate");
        check(hasMethod(c, "invalidate"), "method invalidate");
        check(hasMethod(c, "revalidate"), "method revalidate");
        check(hasMethod(c, "getGraphics"), "method getGraphics");
        check(hasMethod(c, "getFontMetrics"), "method getFontMetrics");
        check(hasMethod(c, "setCursor"), "method setCursor");
        check(hasMethod(c, "getCursor"), "method getCursor");
        check(hasMethod(c, "isCursorSet"), "method isCursorSet");
        check(hasMethod(c, "paint"), "method paint");
        check(hasMethod(c, "update"), "method update");
        check(hasMethod(c, "paintAll"), "method paintAll");
        check(hasMethod(c, "repaint"), "method repaint");
        check(hasMethod(c, "print"), "method print");
        check(hasMethod(c, "printAll"), "method printAll");
        check(hasMethod(c, "imageUpdate"), "method imageUpdate");
        check(hasMethod(c, "createImage"), "method createImage");
        check(hasMethod(c, "createVolatileImage"), "method createVolatileImage");
        check(hasMethod(c, "prepareImage"), "method prepareImage");
        check(hasMethod(c, "checkImage"), "method checkImage");
        check(hasMethod(c, "setIgnoreRepaint"), "method setIgnoreRepaint");
        check(hasMethod(c, "getIgnoreRepaint"), "method getIgnoreRepaint");
        check(hasMethod(c, "contains"), "method contains");
        check(hasMethod(c, "inside"), "method inside");
        check(hasMethod(c, "getComponentAt"), "method getComponentAt");
        check(hasMethod(c, "locate"), "method locate");
        check(hasMethod(c, "deliverEvent"), "method deliverEvent");
        check(hasMethod(c, "dispatchEvent"), "method dispatchEvent");
        check(hasMethod(c, "postEvent"), "method postEvent");
        check(hasMethod(c, "addComponentListener"), "method addComponentListener");
        check(hasMethod(c, "removeComponentListener"), "method removeComponentListener");
        check(hasMethod(c, "getComponentListeners"), "method getComponentListeners");
        check(hasMethod(c, "addFocusListener"), "method addFocusListener");
        check(hasMethod(c, "removeFocusListener"), "method removeFocusListener");
        check(hasMethod(c, "getFocusListeners"), "method getFocusListeners");
        check(hasMethod(c, "addHierarchyListener"), "method addHierarchyListener");
        check(hasMethod(c, "removeHierarchyListener"), "method removeHierarchyListener");
        check(hasMethod(c, "getHierarchyListeners"), "method getHierarchyListeners");
        check(hasMethod(c, "addHierarchyBoundsListener"), "method addHierarchyBoundsListener");
        check(hasMethod(c, "removeHierarchyBoundsListener"), "method removeHierarchyBoundsListener");
        check(hasMethod(c, "getHierarchyBoundsListeners"), "method getHierarchyBoundsListeners");
        check(hasMethod(c, "addKeyListener"), "method addKeyListener");
        check(hasMethod(c, "removeKeyListener"), "method removeKeyListener");
        check(hasMethod(c, "getKeyListeners"), "method getKeyListeners");
        check(hasMethod(c, "addMouseListener"), "method addMouseListener");
        check(hasMethod(c, "removeMouseListener"), "method removeMouseListener");
        check(hasMethod(c, "getMouseListeners"), "method getMouseListeners");
        check(hasMethod(c, "addMouseMotionListener"), "method addMouseMotionListener");
        check(hasMethod(c, "removeMouseMotionListener"), "method removeMouseMotionListener");
        check(hasMethod(c, "getMouseMotionListeners"), "method getMouseMotionListeners");
        check(hasMethod(c, "addMouseWheelListener"), "method addMouseWheelListener");
        check(hasMethod(c, "removeMouseWheelListener"), "method removeMouseWheelListener");
        check(hasMethod(c, "getMouseWheelListeners"), "method getMouseWheelListeners");
        check(hasMethod(c, "addInputMethodListener"), "method addInputMethodListener");
        check(hasMethod(c, "removeInputMethodListener"), "method removeInputMethodListener");
        check(hasMethod(c, "getInputMethodListeners"), "method getInputMethodListeners");
        check(hasMethod(c, "getListeners"), "method getListeners");
        check(hasMethod(c, "getInputMethodRequests"), "method getInputMethodRequests");
        check(hasMethod(c, "getInputContext"), "method getInputContext");
        check(hasMethod(c, "handleEvent"), "method handleEvent");
        check(hasMethod(c, "mouseDown"), "method mouseDown");
        check(hasMethod(c, "mouseDrag"), "method mouseDrag");
        check(hasMethod(c, "mouseUp"), "method mouseUp");
        check(hasMethod(c, "mouseMove"), "method mouseMove");
        check(hasMethod(c, "mouseEnter"), "method mouseEnter");
        check(hasMethod(c, "mouseExit"), "method mouseExit");
        check(hasMethod(c, "keyDown"), "method keyDown");
        check(hasMethod(c, "keyUp"), "method keyUp");
        check(hasMethod(c, "action"), "method action");
        check(hasMethod(c, "addNotify"), "method addNotify");
        check(hasMethod(c, "removeNotify"), "method removeNotify");
        check(hasMethod(c, "gotFocus"), "method gotFocus");
        check(hasMethod(c, "lostFocus"), "method lostFocus");
        check(hasMethod(c, "isFocusTraversable"), "method isFocusTraversable");
        check(hasMethod(c, "isFocusable"), "method isFocusable");
        check(hasMethod(c, "setFocusable"), "method setFocusable");
        check(hasMethod(c, "setFocusTraversalKeys"), "method setFocusTraversalKeys");
        check(hasMethod(c, "getFocusTraversalKeys"), "method getFocusTraversalKeys");
        check(hasMethod(c, "areFocusTraversalKeysSet"), "method areFocusTraversalKeysSet");
        check(hasMethod(c, "setFocusTraversalKeysEnabled"), "method setFocusTraversalKeysEnabled");
        check(hasMethod(c, "getFocusTraversalKeysEnabled"), "method getFocusTraversalKeysEnabled");
        check(hasMethod(c, "requestFocus"), "method requestFocus");
        check(hasMethod(c, "requestFocusInWindow"), "method requestFocusInWindow");
        check(hasMethod(c, "getFocusCycleRootAncestor"), "method getFocusCycleRootAncestor");
        check(hasMethod(c, "isFocusCycleRoot"), "method isFocusCycleRoot");
        check(hasMethod(c, "transferFocus"), "method transferFocus");
        check(hasMethod(c, "nextFocus"), "method nextFocus");
        check(hasMethod(c, "transferFocusBackward"), "method transferFocusBackward");
        check(hasMethod(c, "transferFocusUpCycle"), "method transferFocusUpCycle");
        check(hasMethod(c, "hasFocus"), "method hasFocus");
        check(hasMethod(c, "isFocusOwner"), "method isFocusOwner");
        check(hasMethod(c, "add"), "method add");
        check(hasMethod(c, "remove"), "method remove");
        check(hasMethod(c, "toString"), "method toString");
        check(hasMethod(c, "list"), "method list");
        check(hasMethod(c, "addPropertyChangeListener"), "method addPropertyChangeListener");
        check(hasMethod(c, "removePropertyChangeListener"), "method removePropertyChangeListener");
        check(hasMethod(c, "getPropertyChangeListeners"), "method getPropertyChangeListeners");
        check(hasMethod(c, "firePropertyChange"), "method firePropertyChange");
        check(hasMethod(c, "setComponentOrientation"), "method setComponentOrientation");
        check(hasMethod(c, "getComponentOrientation"), "method getComponentOrientation");
        check(hasMethod(c, "applyComponentOrientation"), "method applyComponentOrientation");
        check(hasMethod(c, "getAccessibleContext"), "method getAccessibleContext");
    }

    // ---- Part 2: black-box behavior in a shown frame ----
    static void testBlackBox() {
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

    // ---- Part 3: standalone defaults + geometry overload family ----
    static void testStandaloneDefaultsAndGeometry() {
        C s = new C();
        check(s.isVisible(), "default visible true");
        check(!s.isShowing(), "not showing while parentless");
        check(!s.isDisplayable(), "not displayable while parentless");
        check(s.isEnabled(), "default enabled");
        check(s.isFocusable(), "default focusable");
        check(s.getName() == null, "default name null for plain subclass");
        check(s.getX() == 0 && s.getY() == 0, "default x/y 0");
        check(s.getWidth() == 0 && s.getHeight() == 0, "default width/height 0");
        check(!s.isPreferredSizeSet() && !s.isMinimumSizeSet() && !s.isMaximumSizeSet(),
              "size hints unset by default");
        check(!s.isBackgroundSet() && !s.isForegroundSet() && !s.isFontSet(),
              "color/font unset by default");
        check(!s.isCursorSet(), "cursor unset by default");
        check(s.getCursor().getType() == Cursor.DEFAULT_CURSOR, "inherited cursor is default");
        check(s.getMousePosition() == null, "getMousePosition null when not showing");
        check(!s.getIgnoreRepaint(), "default ignoreRepaint false");
        check(s.getFocusTraversalKeysEnabled(), "default focus traversal keys enabled");
        check(s.getComponentOrientation() != null, "orientation non-null");

        s.setSize(new Dimension(9, 10));
        check(s.getSize().equals(new Dimension(9, 10)), "setSize(Dimension)");
        s.setLocation(new Point(11, 12));
        check(s.getLocation().equals(new Point(11, 12)), "setLocation(Point)");
        s.setBounds(new Rectangle(21, 22, 23, 24));
        check(s.getBounds().equals(new Rectangle(21, 22, 23, 24)), "setBounds(Rectangle)");
        s.resize(new Dimension(30, 31));
        check(s.getSize().equals(new Dimension(30, 31)), "resize(Dimension)");
        s.reshape(1, 2, 3, 4);
        check(s.getBounds().equals(new Rectangle(1, 2, 3, 4)), "reshape");

        check(s.size().equals(s.getSize()), "size() == getSize()");
        check(s.location().equals(s.getLocation()), "location() == getLocation()");
        check(s.bounds().equals(s.getBounds()), "bounds() == getBounds()");

        s.setLocation(-5, -6);
        check(s.getX() == -5 && s.getY() == -6, "negative coordinates");
        check(s.getLocation().equals(new Point(-5, -6)), "negative location roundtrip");

        check(s.getSize() != s.getSize(), "getSize returns fresh copy");
        check(s.getLocation() != s.getLocation(), "getLocation returns fresh copy");
        check(s.getBounds() != s.getBounds(), "getBounds returns fresh copy");

        Dimension rvD = new Dimension(1, 1);
        check(s.getSize(rvD) == rvD && rvD.width == 3 && rvD.height == 4, "getSize(rv) fills and returns rv");
        Point rvP = new Point(0, 0);
        check(s.getLocation(rvP) == rvP && rvP.x == -5 && rvP.y == -6, "getLocation(rv) fills and returns rv");
        Rectangle rvR = new Rectangle();
        check(s.getBounds(rvR) == rvR && rvR.equals(new Rectangle(-5, -6, 3, 4)), "getBounds(rv) fills and returns rv");

        s.setSize(0, 0);
        check(s.getSize().equals(new Dimension(0, 0)), "setSize(0,0) accepted");
        s.setSize(-5, -7);
        check(s.getSize().equals(new Dimension(-5, -7)), "setSize(negative) stores negative");
        s.setSize(new Dimension(0, -1));
        check(s.getSize().equals(new Dimension(0, -1)), "setSize(Dimension) stores mixed");

        s.setBounds(0, 0, 50, 50);
        check(s.contains(0, 0), "contains(0,0)");
        check(s.contains(49, 49), "contains(w-1,h-1)");
        check(!s.contains(50, 50), "contains(w,h) exclusive");
        check(!s.contains(-1, -1), "contains(-1,-1)");
        check(s.contains(new Point(5, 5)), "contains(Point) hit");
        check(!s.contains(new Point(500, 500)), "contains(Point) miss");
        check(s.inside(1, 1), "inside deprecated hit");

        check(s.getComponentAt(10, 10) == s, "getComponentAt hit returns this");
        check(s.getComponentAt(200, 200) == null, "getComponentAt miss returns null");
        check(s.getComponentAt(new Point(10, 10)) == s, "getComponentAt(Point) hit");
        check(s.getComponentAt(new Point(200, 200)) == null, "getComponentAt(Point) miss null");

        s.setPreferredSize(new Dimension(5, 5));
        check(s.isPreferredSizeSet(), "pref set");
        s.setPreferredSize(null);
        check(!s.isPreferredSizeSet(), "setPreferredSize(null) clears");
        s.setMinimumSize(new Dimension(5, 5));
        check(s.isMinimumSizeSet(), "min set");
        s.setMinimumSize(null);
        check(!s.isMinimumSizeSet(), "setMinimumSize(null) clears");
        s.setMaximumSize(new Dimension(5, 5));
        check(s.isMaximumSizeSet(), "max set");
        s.setMaximumSize(null);
        check(!s.isMaximumSizeSet(), "setMaximumSize(null) clears");

        Cursor hand = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
        s.setCursor(hand);
        check(s.isCursorSet() && s.getCursor().getType() == Cursor.HAND_CURSOR, "setCursor roundtrip");
        s.setCursor(null);
        check(!s.isCursorSet(), "setCursor(null) clears flag");
        check(s.getCursor().getType() == Cursor.DEFAULT_CURSOR, "setCursor(null) inherits default");

        s.setFocusable(false);
        check(!s.isFocusable() && !s.isFocusTraversable(), "setFocusable(false) / isFocusTraversable");
        s.setFocusable(true);
        check(s.isFocusable() && s.isFocusTraversable(), "setFocusable(true) / isFocusTraversable");

        s.enable(false);
        check(!s.isEnabled(), "enable(false) disables");
        s.enable(true);
        check(s.isEnabled(), "enable(true) enables");

        s.setFocusTraversalKeysEnabled(false);
        check(!s.getFocusTraversalKeysEnabled(), "setFocusTraversalKeysEnabled(false)");
        s.setFocusTraversalKeysEnabled(true);
        check(s.getFocusTraversalKeysEnabled(), "setFocusTraversalKeysEnabled(true)");

        checkThrows(NullPointerException.class, () -> s.applyComponentOrientation(null),
                    "applyComponentOrientation(null)");
        // setLocale(null) itself is accepted even parentless; getLocale() then throws
        // (documented: no own locale and not in a containment hierarchy)
        s.setLocale(Locale.US);
        s.setLocale(null);
        checkThrows(IllegalComponentStateException.class, () -> s.getLocale(),
                    "getLocale() parentless without locale");
    }

    // ---- Part 4: locale inheritance ----
    static void testLocaleInheritance() {
        Container parent = new Container();
        C child = new C();
        parent.add(child);
        parent.setLocale(Locale.CANADA);
        check(Locale.CANADA.equals(child.getLocale()), "child inherits parent locale");
        child.setLocale(Locale.JAPAN);
        check(Locale.JAPAN.equals(child.getLocale()), "own locale overrides parent");
        child.setLocale(null);
        check(Locale.CANADA.equals(child.getLocale()), "setLocale(null) falls back to parent");
    }

    // ---- Part 5: property change contracts ----
    static void testPropertyChanges() {
        C c = new C();
        final List<String> log = new ArrayList<>();
        PropertyChangeListener pcl = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) {
                log.add(e.getPropertyName() + "|" + e.getOldValue() + "|" + e.getNewValue());
            }
        };
        c.addPropertyChangeListener(pcl);
        check(c.getPropertyChangeListeners().length == 1, "one pcl registered");

        c.setName("first");
        check(log.remove("name|null|first"), "setName fires name PCE null->first");
        c.setName("first");
        check(log.isEmpty(), "same name fires no PCE");
        c.setName("second");
        check(log.remove("name|first|second"), "name PCE old/new exact");

        c.setBackground(Color.RED);
        check(log.remove("background|null|java.awt.Color[r=255,g=0,b=0]"),
              "background PCE null->RED");
        c.setBackground(Color.BLUE);
        check(log.remove("background|java.awt.Color[r=255,g=0,b=0]|java.awt.Color[r=0,g=0,b=255]"),
              "background PCE RED->BLUE exact");

        c.setForeground(Color.WHITE);
        check(log.remove("foreground|null|java.awt.Color[r=255,g=255,b=255]"), "foreground PCE");

        Font f1 = new Font("Dialog", Font.PLAIN, 12);
        Font f2 = new Font("Dialog", Font.BOLD, 14);
        c.setFont(f1);
        check(log.remove("font|null|" + f1), "font PCE null->f1");
        c.setFont(f2);
        check(log.remove("font|" + f1 + "|" + f2), "font PCE f1->f2 exact");

        c.setFocusable(false);
        check(log.remove("focusable|true|false"), "focusable PCE true->false");
        c.setFocusable(false);
        check(log.isEmpty(), "same focusable fires no PCE");
        c.setFocusable(true);
        check(log.remove("focusable|false|true"), "focusable PCE false->true");

        c.setLocale(Locale.US);
        check(log.remove("locale|null|en_US"), "locale PCE null->US");
        c.setLocale(Locale.FRANCE);
        check(log.remove("locale|en_US|fr_FR"), "locale PCE US->FRANCE exact");

        check(log.isEmpty(), "no stray PCEs: " + log);

        c.removePropertyChangeListener(pcl);
        check(c.getPropertyChangeListeners().length == 0, "pcl removed");
        c.setName("after-remove");
        check("after-remove".equals(c.getName()), "setter still works after listener removal");
        check(log.isEmpty(), "no events after listener removal");

        c.addPropertyChangeListener(null);
        check(c.getPropertyChangeListeners().length == 0, "addPropertyChangeListener(null) no-op");
        c.removePropertyChangeListener(null);

        final List<String> fired = new ArrayList<>();
        c.addPropertyChangeListener(e -> fired.add(e.getPropertyName() + "|" + e.getOldValue() + "|" + e.getNewValue()));
        c.firePropertyChange("b", (byte) 1, (byte) 2);
        check(fired.remove("b|1|2"), "firePropertyChange(byte)");
        c.firePropertyChange("b", (byte) 1, (byte) 1);
        check(fired.isEmpty(), "equal byte values fire nothing");
        c.firePropertyChange("c", 'a', 'b');
        check(fired.remove("c|a|b"), "firePropertyChange(char)");
        c.firePropertyChange("s", (short) 1, (short) 2);
        check(fired.remove("s|1|2"), "firePropertyChange(short)");
        c.firePropertyChange("l", 1L, 2L);
        check(fired.remove("l|1|2"), "firePropertyChange(long)");
        c.firePropertyChange("f", 1f, 2f);
        check(fired.remove("f|1.0|2.0"), "firePropertyChange(float)");
        c.firePropertyChange("d", 1d, 2d);
        check(fired.remove("d|1.0|2.0"), "firePropertyChange(double)");
        c.firePropertyChange("l", 5L, 5L);
        check(fired.isEmpty(), "equal long values fire nothing");
    }

    // ---- Part 6: accessible-state property change on enable/disable ----
    static void testAccessibleStateEvents() {
        Canvas cv = new Canvas();
        check(cv.getAccessibleContext() != null, "canvas accessible context non-null");
        final List<String> log = new ArrayList<>();
        cv.getAccessibleContext().addPropertyChangeListener(e ->
            log.add(e.getPropertyName()));
        cv.setEnabled(false);
        check(log.size() == 1 && javax.accessibility.AccessibleContext.ACCESSIBLE_STATE_PROPERTY.equals(log.get(0)),
              "disable fires ACCESSIBLE_STATE_PROPERTY once");
        cv.setEnabled(false);
        check(log.size() == 1, "no event when state unchanged");
        cv.setEnabled(true);
        check(log.size() == 2, "enable fires once more");
        cv.setEnabled(true);
        check(log.size() == 2, "no event when state unchanged (enabled)");
    }

    // ---- Part 7: listener family registration contracts ----
    static void testListenerFamilies() {
        C c = new C();
        check(c.getComponentListeners().length == 0
           && c.getMouseListeners().length == 0
           && c.getMouseMotionListeners().length == 0
           && c.getMouseWheelListeners().length == 0
           && c.getKeyListeners().length == 0
           && c.getFocusListeners().length == 0
           && c.getHierarchyListeners().length == 0
           && c.getHierarchyBoundsListeners().length == 0
           && c.getInputMethodListeners().length == 0, "all listener lists empty initially");

        c.addComponentListener(null);
        c.removeComponentListener(null);
        c.addMouseListener(null);
        c.removeMouseListener(null);
        c.addKeyListener(null);
        c.removeKeyListener(null);
        c.addFocusListener(null);
        c.removeFocusListener(null);
        c.addMouseMotionListener(null);
        c.addMouseWheelListener(null);
        c.addHierarchyListener(null);
        c.addHierarchyBoundsListener(null);
        c.addInputMethodListener(null);
        check(c.getComponentListeners().length == 0 && c.getMouseListeners().length == 0
           && c.getKeyListeners().length == 0 && c.getFocusListeners().length == 0,
              "null listener registration is a no-op");

        ComponentListener cl1 = new ComponentAdapter() {};
        ComponentListener cl2 = new ComponentAdapter() {};
        c.addComponentListener(cl1);
        c.addComponentListener(cl1);
        c.addComponentListener(cl2);
        check(c.getComponentListeners().length == 3, "component listener count 3 with duplicate");
        c.removeComponentListener(cl1);
        check(c.getComponentListeners().length == 2, "one remove drops one occurrence");
        c.removeComponentListener(cl1);
        check(c.getComponentListeners().length == 1, "second remove drops the duplicate");
        c.removeComponentListener(cl1);
        check(c.getComponentListeners().length == 1, "removing unregistered no-op");
        c.removeComponentListener(cl2);
        check(c.getComponentListeners().length == 0, "component listener list empty again");

        MouseListener ml = new MouseAdapter() {};
        c.addMouseListener(ml);
        check(c.getMouseListeners().length == 1, "mouse listener added");
        c.removeMouseListener(ml);
        check(c.getMouseListeners().length == 0, "mouse listener removed");
        MouseMotionListener mml = new MouseMotionAdapter() {};
        c.addMouseMotionListener(mml);
        check(c.getMouseMotionListeners().length == 1, "motion listener added");
        c.removeMouseMotionListener(mml);
        check(c.getMouseMotionListeners().length == 0, "motion listener removed");
        KeyListener kl = new KeyAdapter() {};
        c.addKeyListener(kl);
        check(c.getKeyListeners().length == 1, "key listener added");
        c.removeKeyListener(kl);
        check(c.getKeyListeners().length == 0, "key listener removed");
        FocusListener fl = new FocusAdapter() {};
        c.addFocusListener(fl);
        check(c.getFocusListeners().length == 1, "focus listener added");
        c.removeFocusListener(fl);
        check(c.getFocusListeners().length == 0, "focus listener removed");
        MouseWheelListener mwl = new MouseWheelListener() {
            public void mouseWheelMoved(MouseWheelEvent e) {}
        };
        c.addMouseWheelListener(mwl);
        check(c.getMouseWheelListeners().length == 1, "wheel listener added");
        c.removeMouseWheelListener(mwl);
        check(c.getMouseWheelListeners().length == 0, "wheel listener removed");

        HierarchyListener hl = new HierarchyListener() {
            public void hierarchyChanged(HierarchyEvent e) {}
        };
        c.addHierarchyListener(hl);
        HierarchyBoundsListener hbl = new HierarchyBoundsAdapter() {};
        c.addHierarchyBoundsListener(hbl);
        InputMethodListener iml = new InputMethodListener() {
            public void inputMethodTextChanged(java.awt.event.InputMethodEvent e) {}
            public void caretPositionChanged(java.awt.event.InputMethodEvent e) {}
        };
        c.addInputMethodListener(iml);
        check(c.getListeners(HierarchyListener.class).length == c.getHierarchyListeners().length,
              "getListeners(HierarchyListener) parity");
        check(c.getListeners(HierarchyBoundsListener.class).length == c.getHierarchyBoundsListeners().length,
              "getListeners(HierarchyBoundsListener) parity");
        check(c.getListeners(InputMethodListener.class).length == c.getInputMethodListeners().length,
              "getListeners(InputMethodListener) parity");
        check(c.getListeners(MouseListener.class).length == 0, "getListeners(MouseListener) empty");
        check(c.getListeners(java.awt.event.ActionListener.class).length == 0,
              "getListeners of never-registered type is empty");
        c.removeHierarchyListener(hl);
        c.removeHierarchyBoundsListener(hbl);
        c.removeInputMethodListener(iml);
        check(c.getHierarchyListeners().length == 0 && c.getHierarchyBoundsListeners().length == 0
           && c.getInputMethodListeners().length == 0, "hierarchy/input listeners removed");
    }

    // ---- Part 8: synchronized dispatch of synthesized events ----
    static void testSynthesizedDispatch() {
        final C c = new C();
        c.setBounds(0, 0, 100, 100);
        final List<String> log = new ArrayList<>();
        c.addComponentListener(new ComponentAdapter() {
            public void componentMoved(ComponentEvent e) {
                log.add("moved:" + e.getID() + ":" + (e.getSource() == c) + ":"
                        + (e.getComponent() == c));
            }
            public void componentResized(ComponentEvent e) {
                log.add("resized:" + e.getID());
            }
        });
        c.dispatchEvent(new ComponentEvent(c, ComponentEvent.COMPONENT_MOVED));
        check(log.size() == 1, "componentMoved delivered synchronously");
        check(log.get(0).equals("moved:" + ComponentEvent.COMPONENT_MOVED + ":true:true"),
              "moved event id/source/component exact");

        c.dispatchEvent(new ComponentEvent(c, ComponentEvent.COMPONENT_RESIZED));
        check(log.size() == 2, "componentResized delivered");
        check(log.get(1).equals("resized:" + ComponentEvent.COMPONENT_RESIZED),
              "resized event id exact");

        final List<String> mlog = new ArrayList<>();
        c.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) { mlog.add("press:" + e.getX() + "," + e.getY() + ":" + e.getClickCount()); }
            public void mouseReleased(MouseEvent e) { mlog.add("release:" + e.getX() + "," + e.getY()); }
        });
        long when = EventQueue.getMostRecentEventTime();
        c.dispatchEvent(new MouseEvent(c, MouseEvent.MOUSE_PRESSED, when, 0, 5, 6, 1, false));
        c.dispatchEvent(new MouseEvent(c, MouseEvent.MOUSE_RELEASED, when, 0, 5, 6, 1, false));
        check(mlog.remove("press:5,6:1"), "mousePressed x/y/clickCount exact");
        check(mlog.remove("release:5,6"), "mouseReleased x/y exact");
        check(mlog.isEmpty(), "no stray mouse callbacks");
    }

    public static void main(String[] args) throws Exception {
        testStructure();
        testBlackBox();
        testStandaloneDefaultsAndGeometry();
        testLocaleInheritance();
        testPropertyChanges();
        testAccessibleStateEvents();
        testListenerFamilies();
        testSynthesizedDispatch();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
