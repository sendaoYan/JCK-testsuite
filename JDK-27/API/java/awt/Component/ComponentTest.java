/*
 * @test
 * @summary java.awt.Component conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main ComponentTest
 */

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.*;
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
                "setFocusable", "isFocusable", "getTreeLock", "getToolkit",
                "addPropertyChangeListener", "removePropertyChangeListener",
                "getPropertyChangeListeners", "firePropertyChange",
                "setFocusTraversalKeysEnabled", "getFocusTraversalKeysEnabled"}) {
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

    // ---- Part 3: standalone defaults + geometry overload family ----
    static void testStandaloneDefaultsAndGeometry() {
        C s = new C();
        // defaults on a peerless component (pure in-memory contract)
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
        check(s.getIgnoreRepaint() == false, "default ignoreRepaint false");
        check(s.getFocusTraversalKeysEnabled(), "default focus traversal keys enabled");
        check(s.getComponentOrientation() != null, "orientation non-null");

        // setter overload family: Dimension/Point/Rectangle variants
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

        // deprecated read aliases return equal values
        check(s.size().equals(s.getSize()), "size() == getSize()");
        check(s.location().equals(s.getLocation()), "location() == getLocation()");
        check(s.bounds().equals(s.getBounds()), "bounds() == getBounds()");

        // getX/getY track negative locations
        s.setLocation(-5, -6);
        check(s.getX() == -5 && s.getY() == -6, "negative coordinates");
        check(s.getLocation().equals(new Point(-5, -6)), "negative location roundtrip");

        // returned objects are fresh copies (two calls not identical)
        check(s.getSize() != s.getSize(), "getSize returns fresh copy");
        check(s.getLocation() != s.getLocation(), "getLocation returns fresh copy");
        check(s.getBounds() != s.getBounds(), "getBounds returns fresh copy");

        // rv overloads fill the supplied object and return the same instance
        Dimension rvD = new Dimension(1, 1);
        check(s.getSize(rvD) == rvD && rvD.width == 3 && rvD.height == 4, "getSize(rv) fills and returns rv");
        Point rvP = new Point(0, 0);
        check(s.getLocation(rvP) == rvP && rvP.x == -5 && rvP.y == -6, "getLocation(rv) fills and returns rv");
        Rectangle rvR = new Rectangle();
        check(s.getBounds(rvR) == rvR && rvR.equals(new Rectangle(-5, -6, 3, 4)), "getBounds(rv) fills and returns rv");

        // boundary sizes: zero and negative accepted, stored verbatim (no exception)
        s.setSize(0, 0);
        check(s.getSize().equals(new Dimension(0, 0)), "setSize(0,0) accepted");
        s.setSize(-5, -7);
        check(s.getSize().equals(new Dimension(-5, -7)), "setSize(negative) stores negative");
        s.setSize(new Dimension(0, -1));
        check(s.getSize().equals(new Dimension(0, -1)), "setSize(Dimension) stores mixed");

        // contains boundary matrix on a 50x50 component
        s.setBounds(0, 0, 50, 50);
        check(s.contains(0, 0), "contains(0,0)");
        check(s.contains(49, 49), "contains(w-1,h-1)");
        check(!s.contains(50, 50), "contains(w,h) exclusive");
        check(!s.contains(-1, -1), "contains(-1,-1)");
        check(s.contains(new Point(5, 5)), "contains(Point) hit");
        check(!s.contains(new Point(500, 500)), "contains(Point) miss");
        check(s.inside(1, 1), "inside deprecated hit");

        // getComponentAt on a plain (non-container) component
        check(s.getComponentAt(10, 10) == s, "getComponentAt hit returns this");
        check(s.getComponentAt(200, 200) == null, "getComponentAt miss returns null");
        check(s.getComponentAt(new Point(10, 10)) == s, "getComponentAt(Point) hit");
        check(s.getComponentAt(new Point(200, 200)) == null, "getComponentAt(Point) miss null");

        // size-hint setters clear on null
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

        // cursor: null reverts to inherited (default), clears the set flag
        Cursor hand = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
        s.setCursor(hand);
        check(s.isCursorSet() && s.getCursor().getType() == Cursor.HAND_CURSOR, "setCursor roundtrip");
        s.setCursor(null);
        check(!s.isCursorSet(), "setCursor(null) clears flag");
        check(s.getCursor().getType() == Cursor.DEFAULT_CURSOR, "setCursor(null) inherits default");

        // focusable + deprecated isFocusTraversable pair
        s.setFocusable(false);
        check(!s.isFocusable() && !s.isFocusTraversable(), "setFocusable(false) / isFocusTraversable");
        s.setFocusable(true);
        check(s.isFocusable() && s.isFocusTraversable(), "setFocusable(true) / isFocusTraversable");

        // deprecated enable(boolean)/disable
        s.enable(false);
        check(!s.isEnabled(), "enable(false) disables");
        s.enable(true);
        check(s.isEnabled(), "enable(true) enables");

        // focus traversal keys enabled flag
        s.setFocusTraversalKeysEnabled(false);
        check(!s.getFocusTraversalKeysEnabled(), "setFocusTraversalKeysEnabled(false)");
        s.setFocusTraversalKeysEnabled(true);
        check(s.getFocusTraversalKeysEnabled(), "setFocusTraversalKeysEnabled(true)");

        // negative contracts
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

        // remove stops delivery
        c.removePropertyChangeListener(pcl);
        check(c.getPropertyChangeListeners().length == 0, "pcl removed");
        c.setName("after-remove");
        check("after-remove".equals(c.getName()), "setter still works after listener removal");
        check(log.isEmpty(), "no events after listener removal");

        // null listener is a documented no-op
        c.addPropertyChangeListener(null);
        check(c.getPropertyChangeListeners().length == 0, "addPropertyChangeListener(null) no-op");
        c.removePropertyChangeListener(null);

        // public firePropertyChange numeric overloads
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

        // null add/remove are documented no-ops
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

        // component family
        ComponentListener cl1 = new ComponentAdapter() {};
        ComponentListener cl2 = new ComponentAdapter() {};
        c.addComponentListener(cl1);
        c.addComponentListener(cl1);  // duplicate allowed
        c.addComponentListener(cl2);
        check(c.getComponentListeners().length == 3, "component listener count 3 with duplicate");
        c.removeComponentListener(cl1);
        check(c.getComponentListeners().length == 2, "one remove drops one occurrence");
        c.removeComponentListener(cl1);
        check(c.getComponentListeners().length == 1, "second remove drops the duplicate");
        c.removeComponentListener(cl1); // removing unregistered is a no-op
        check(c.getComponentListeners().length == 1, "removing unregistered no-op");
        c.removeComponentListener(cl2);
        check(c.getComponentListeners().length == 0, "component listener list empty again");

        // mouse / key / focus / motion / wheel families
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
        MouseWheelListener mwl = e -> {};
        c.addMouseWheelListener(mwl);
        check(c.getMouseWheelListeners().length == 1, "wheel listener added");
        c.removeMouseWheelListener(mwl);
        check(c.getMouseWheelListeners().length == 0, "wheel listener removed");

        // getListeners(Class) mirrors the typed getters
        HierarchyListener hl = e -> {};
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
        C c = new C();
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
        testWhiteBox();
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
