/*
 * @test
 * @summary com.sun.java.accessibility.util.AWTEventMonitor conformance: white-box structure + black-box behavior.
 * @run main AWTEventMonitorTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.awt.event.*;

public class AWTEventMonitorTest {
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
        Class<?> c = load("com.sun.java.accessibility.util.AWTEventMonitor");
        check(!c.isInterface(), "not an interface");
        check(!Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(c.getDeclaredConstructor() != null, "public constructor");
        Set<String> m = methodNames(c);
        check(m.contains("getComponentWithFocus"), "getComponentWithFocus");
        check(m.contains("addComponentListener"), "addComponentListener");
        check(m.contains("removeComponentListener"), "removeComponentListener");
        check(m.contains("addContainerListener"), "addContainerListener");
        check(m.contains("addFocusListener"), "addFocusListener");
        check(m.contains("addKeyListener"), "addKeyListener");
        check(m.contains("addMouseListener"), "addMouseListener");
        check(m.contains("addMouseMotionListener"), "addMouseMotionListener");
        check(m.contains("addWindowListener"), "addWindowListener");
        check(m.contains("addActionListener"), "addActionListener");
        check(m.contains("addAdjustmentListener"), "addAdjustmentListener");
        check(m.contains("addItemListener"), "addItemListener");
        check(m.contains("addTextListener"), "addTextListener");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        com.sun.java.accessibility.util.AWTEventMonitor mon =
            new com.sun.java.accessibility.util.AWTEventMonitor();
        check(mon != null, "new AWTEventMonitor() non-null");
        check(com.sun.java.accessibility.util.AWTEventMonitor.getComponentWithFocus() == null,
              "no component with focus in headless");

        // Each listener category can be added and removed without touching a display.
        ComponentListener cl = new ComponentListener() {
            public void componentResized(ComponentEvent e) {}
            public void componentMoved(ComponentEvent e) {}
            public void componentShown(ComponentEvent e) {}
            public void componentHidden(ComponentEvent e) {}
        };
        com.sun.java.accessibility.util.AWTEventMonitor.addComponentListener(cl);
        check(com.sun.java.accessibility.util.AWTEventMonitor.getComponentWithFocus() == null, "focus still null after component add");
        com.sun.java.accessibility.util.AWTEventMonitor.removeComponentListener(cl);

        ContainerListener cnl = new ContainerListener() {
            public void componentAdded(ContainerEvent e) {}
            public void componentRemoved(ContainerEvent e) {}
        };
        com.sun.java.accessibility.util.AWTEventMonitor.addContainerListener(cnl);
        com.sun.java.accessibility.util.AWTEventMonitor.removeContainerListener(cnl);

        FocusListener fl = new FocusListener() {
            public void focusGained(FocusEvent e) {}
            public void focusLost(FocusEvent e) {}
        };
        com.sun.java.accessibility.util.AWTEventMonitor.addFocusListener(fl);
        com.sun.java.accessibility.util.AWTEventMonitor.removeFocusListener(fl);

        KeyListener kl = new KeyListener() {
            public void keyTyped(KeyEvent e) {}
            public void keyPressed(KeyEvent e) {}
            public void keyReleased(KeyEvent e) {}
        };
        com.sun.java.accessibility.util.AWTEventMonitor.addKeyListener(kl);
        com.sun.java.accessibility.util.AWTEventMonitor.removeKeyListener(kl);

        MouseListener ml = new MouseListener() {
            public void mouseClicked(MouseEvent e) {}
            public void mousePressed(MouseEvent e) {}
            public void mouseReleased(MouseEvent e) {}
            public void mouseEntered(MouseEvent e) {}
            public void mouseExited(MouseEvent e) {}
        };
        com.sun.java.accessibility.util.AWTEventMonitor.addMouseListener(ml);
        com.sun.java.accessibility.util.AWTEventMonitor.removeMouseListener(ml);

        MouseMotionListener mml = new MouseMotionListener() {
            public void mouseDragged(MouseEvent e) {}
            public void mouseMoved(MouseEvent e) {}
        };
        com.sun.java.accessibility.util.AWTEventMonitor.addMouseMotionListener(mml);
        com.sun.java.accessibility.util.AWTEventMonitor.removeMouseMotionListener(mml);

        WindowListener wl = new WindowListener() {
            public void windowOpened(WindowEvent e) {}
            public void windowClosing(WindowEvent e) {}
            public void windowClosed(WindowEvent e) {}
            public void windowIconified(WindowEvent e) {}
            public void windowDeiconified(WindowEvent e) {}
            public void windowActivated(WindowEvent e) {}
            public void windowDeactivated(WindowEvent e) {}
        };
        com.sun.java.accessibility.util.AWTEventMonitor.addWindowListener(wl);
        com.sun.java.accessibility.util.AWTEventMonitor.removeWindowListener(wl);

        ActionListener al = new ActionListener() {
            public void actionPerformed(ActionEvent e) {}
        };
        com.sun.java.accessibility.util.AWTEventMonitor.addActionListener(al);
        com.sun.java.accessibility.util.AWTEventMonitor.removeActionListener(al);

        AdjustmentListener adj = new AdjustmentListener() {
            public void adjustmentValueChanged(AdjustmentEvent e) {}
        };
        com.sun.java.accessibility.util.AWTEventMonitor.addAdjustmentListener(adj);
        com.sun.java.accessibility.util.AWTEventMonitor.removeAdjustmentListener(adj);

        ItemListener il = new ItemListener() {
            public void itemStateChanged(ItemEvent e) {}
        };
        com.sun.java.accessibility.util.AWTEventMonitor.addItemListener(il);
        com.sun.java.accessibility.util.AWTEventMonitor.removeItemListener(il);

        TextListener tl = new TextListener() {
            public void textValueChanged(TextEvent e) {}
        };
        com.sun.java.accessibility.util.AWTEventMonitor.addTextListener(tl);
        com.sun.java.accessibility.util.AWTEventMonitor.removeTextListener(tl);

        // Multi-listener combination: chain two component listeners then unwind.
        ComponentListener cl2 = new ComponentListener() {
            public void componentResized(ComponentEvent e) {}
            public void componentMoved(ComponentEvent e) {}
            public void componentShown(ComponentEvent e) {}
            public void componentHidden(ComponentEvent e) {}
        };
        com.sun.java.accessibility.util.AWTEventMonitor.addComponentListener(cl);
        com.sun.java.accessibility.util.AWTEventMonitor.addComponentListener(cl2);
        com.sun.java.accessibility.util.AWTEventMonitor.removeComponentListener(cl);
        com.sun.java.accessibility.util.AWTEventMonitor.removeComponentListener(cl2);
        check(com.sun.java.accessibility.util.AWTEventMonitor.getComponentWithFocus() == null,
              "focus null after combined add/remove");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
