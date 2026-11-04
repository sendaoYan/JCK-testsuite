/*
 * @test
 * @summary javax.swing.JComponent conformance: type shape + base component contract (headless-safe, Metal LaF).
 * @run main JComponentTest
 */

import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.border.EmptyBorder;

public class JComponentTest {
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
        Class<?> c = Class.forName("javax.swing.JComponent", false, JComponentTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.awt.Container"), "superclass");
        check(hasInterface(c, "java.io.Serializable"), "implements java.io.Serializable");
        check(hasInterface(c, "javax.swing.TransferHandler$HasGetTransferHandler"), "implements javax.swing.TransferHandler$HasGetTransferHandler");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("setInheritsPopupMenu"), "method setInheritsPopupMenu");
        check(names.contains("getInheritsPopupMenu"), "method getInheritsPopupMenu");
        check(names.contains("setComponentPopupMenu"), "method setComponentPopupMenu");
        check(names.contains("getComponentPopupMenu"), "method getComponentPopupMenu");
        check(names.contains("updateUI"), "method updateUI");
        check(names.contains("getUIClassID"), "method getUIClassID");
        check(names.contains("update"), "method update");
    }

    static void testContract() throws Exception {
        final JPanel p = new JPanel();

        // ---- client property semantics: null value removes the key
        p.putClientProperty("k", "v");
        check("v".equals(p.getClientProperty("k")), "putClientProperty/getClientProperty");
        p.putClientProperty("k", null);
        check(p.getClientProperty("k") == null, "putClientProperty(k,null) removes");
        check(p.getClientProperty("absent") == null, "absent client property null");
        Object objKey = new Object();
        p.putClientProperty(objKey, Integer.valueOf(7));
        check(Integer.valueOf(7).equals(p.getClientProperty(objKey)), "arbitrary object key");

        // ---- tooltip round trip, null clears
        p.setToolTipText("tip");
        check("tip".equals(p.getToolTipText()), "setToolTipText/getToolTipText");
        p.setToolTipText(null);
        check(p.getToolTipText() == null, "setToolTipText(null) clears");

        // ---- property change event names fired by the standard setters
        final List<String> names = new ArrayList<String>();
        p.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) { names.add(e.getPropertyName()); }
        });
        p.setOpaque(false); // JPanel default opaque=true, so this is a change
        check(names.contains("opaque"), "setOpaque fires 'opaque'");
        p.setToolTipText("tt");
        check(names.contains("ToolTipText"), "setToolTipText fires 'ToolTipText'");
        p.setBorder(new EmptyBorder(1, 2, 3, 4));
        check(names.contains("border"), "setBorder fires 'border'");
        p.setEnabled(false);
        check(names.contains("enabled"), "setEnabled fires 'enabled'");
        p.setFont(new Font("Dialog", Font.PLAIN, 9));
        check(names.contains("font"), "setFont fires 'font'");
        p.setBackground(Color.RED);
        check(names.contains("background"), "setBackground fires 'background'");
        p.setForeground(Color.BLUE);
        check(names.contains("foreground"), "setForeground fires 'foreground'");
        p.setEnabled(true);
        check(p.isEnabled(), "re-enabled");
        p.setOpaque(true);

        // ---- registerKeyboardAction / unregisterKeyboardAction
        KeyStroke ks = KeyStroke.getKeyStroke('a');
        check(p.getInputMap(JComponent.WHEN_FOCUSED).size() == 0, "input map empty initially");
        check(p.getRegisteredKeyStrokes().length == 0, "no registered key strokes initially");
        p.registerKeyboardAction(null, "cmdA", ks, JComponent.WHEN_FOCUSED);
        InputMap im = p.getInputMap(JComponent.WHEN_FOCUSED);
        check(im.size() == 1, "registered binding lands in WHEN_FOCUSED input map");
        check(im.get(ks) != null, "binding value non-null (ActionStandin)");
        check(!"cmdA".equals(im.get(ks)), "binding value is not the raw command string");
        check(p.getRegisteredKeyStrokes().length == 1, "one registered key stroke");
        check(p.getActionMap().get("cmdA") == null, "null action does not create ActionMap entry");
        p.unregisterKeyboardAction(ks);
        check(p.getRegisteredKeyStrokes().length == 0, "unregister removes key stroke");
        check(p.getInputMap(JComponent.WHEN_FOCUSED).size() == 0, "input map empty after unregister");

        // ---- request-focus-enabled flag
        p.setRequestFocusEnabled(false);
        check(!p.isRequestFocusEnabled(), "setRequestFocusEnabled(false)");
        p.setRequestFocusEnabled(true);
        check(p.isRequestFocusEnabled(), "setRequestFocusEnabled(true)");

        // ---- default locale is a static property
        Locale def = JComponent.getDefaultLocale();
        check(def != null, "getDefaultLocale non-null");
        Locale custom = new Locale("xx");
        JComponent.setDefaultLocale(custom);
        check(JComponent.getDefaultLocale() == custom, "setDefaultLocale round trip");
        JComponent.setDefaultLocale(def);
        check(JComponent.getDefaultLocale() == def, "default locale restored");

        // ---- popup menu wiring
        p.setInheritsPopupMenu(true);
        check(p.getInheritsPopupMenu(), "setInheritsPopupMenu(true)");
        p.setInheritsPopupMenu(false);
        check(!p.getInheritsPopupMenu(), "setInheritsPopupMenu(false)");
        JPopupMenu pm = new JPopupMenu();
        p.setComponentPopupMenu(pm);
        check(p.getComponentPopupMenu() == pm, "setComponentPopupMenu identity");
        p.setComponentPopupMenu(null);
        check(p.getComponentPopupMenu() == null, "setComponentPopupMenu(null) clears");

        // ---- setEnabled does not cascade to children, and explicit child state is sticky
        JPanel parent = new JPanel();
        JButton kid = new JButton("kid");
        parent.add(kid);
        parent.setEnabled(false);
        check(!parent.isEnabled(), "parent disabled");
        check(kid.isEnabled(), "child stays enabled when parent disabled");
        kid.setEnabled(false);
        parent.setEnabled(false);
        parent.setEnabled(true);
        check(parent.isEnabled(), "parent re-enabled");
        check(!kid.isEnabled(), "explicitly disabled child stays disabled across parent cycle");
        check(kid.getParent() == parent, "getParent immediate container");

        // ---- no-throw repaint/revalidate hooks (no peer available)
        p.repaint();
        p.revalidate();
        p.repaint(0, 0, 5, 5);
        p.scrollRectToVisible(new Rectangle(0, 0, 1, 1));
        passed++; // reaching here means none of the above threw

        // ---- geometry before realization
        check(p.getVisibleRect().equals(new Rectangle(0, 0, 0, 0)), "visible rect empty before display");
        p.setSize(11, 12);
        check(p.getWidth() == 11 && p.getHeight() == 12, "setSize/getWidth/getHeight");
        check(p.getTopLevelAncestor() == null, "top level ancestor null without window");

        // ---- ancestor listener bookkeeping
        AncestorListener al = new AncestorListener() {
            public void ancestorAdded(AncestorEvent e) { }
            public void ancestorRemoved(AncestorEvent e) { }
            public void ancestorMoved(AncestorEvent e) { }
        };
        int before = p.getAncestorListeners().length;
        p.addAncestorListener(al);
        check(p.getAncestorListeners().length == before + 1, "addAncestorListener counted");
        p.removeAncestorListener(al);
        check(p.getAncestorListeners().length == before, "removeAncestorListener counted");

        // ---- painting hints and UI identity
        check(p.isOptimizedDrawingEnabled(), "JPanel optimized drawing");
        p.setDoubleBuffered(false);
        check(!p.isDoubleBuffered(), "setDoubleBuffered(false)");
        p.setDoubleBuffered(true);
        check(p.isDoubleBuffered(), "setDoubleBuffered(true)");
        check("PanelUI".equals(p.getUIClassID()), "getUIClassID PanelUI");
        check("javax.swing.plaf.basic.BasicPanelUI".equals(p.getUI().getClass().getName()),
                "Metal installs BasicPanelUI");

        // ---- a label keeps its LaF-installed font when the parent font changes later
        JPanel holder = new JPanel();
        JLabel lab = new JLabel("x");
        holder.add(lab);
        Font parentFont = new Font("Dialog", Font.BOLD, 22);
        holder.setFont(parentFont);
        check(lab.getFont() != null && !parentFont.equals(lab.getFont()),
                "child label font not replaced by later parent setFont");

        // ---- accessibility bridge present without a peer
        check(p.getAccessibleContext() != null, "accessible context non-null");

        // ---- vetoable-change listener bookkeeping (JComponent-level API)
        int vcBefore = p.getVetoableChangeListeners().length;
        java.beans.VetoableChangeListener vcl = new java.beans.VetoableChangeListener() {
            public void vetoableChange(PropertyChangeEvent e) { }
        };
        p.addVetoableChangeListener(vcl);
        check(p.getVetoableChangeListeners().length == vcBefore + 1, "addVetoableChangeListener counted");
        p.removeVetoableChangeListener(vcl);
        check(p.getVetoableChangeListeners().length == vcBefore, "removeVetoableChangeListener counted");

        p.getToolkit(); // no-throw headless
    }

    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(new MetalLookAndFeel());
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                try {
                    testShape();
                    testContract();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
        System.out.println("PASS (" + passed + " assertions)");
    }
}
