/*
 * @test
 * @summary javax.swing.JComponent conformance: white-box structure + black-box behavior.
 * @run main JComponentTest
 */

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.plaf.metal.MetalLookAndFeel;

public class JComponentTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(new MetalLookAndFeel());
        SwingUtilities.invokeAndWait(() -> {
            try {
                testWhiteBox();
                testBlackBox();
                testContract();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.JComponent");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.awt.Container"), "superclass");
        check(Modifier.isAbstract(mods) == true, "abstract modifier");
        check(c.getInterfaces().length == 2, "interface count");
        check(c.getInterfaces()[0].getName().equals("java.io.Serializable"), "interface 0");
        check(c.getInterfaces()[1].getName().equals("javax.swing.TransferHandler$HasGetTransferHandler"), "interface 1");
    }

    static void testBlackBox() throws Exception {
        JComponent c = new JComponent() {};
        check("ToolTipText".equals(JComponent.TOOL_TIP_TEXT_KEY), "tooltip key constant");
        check(c.isEnabled() == true, "default enabled");
        check(c.isOpaque() == false, "default opaque false");
        check(c.getToolTipText() == null, "default tooltip null");
        check(c.getClientProperty("anything") == null, "default client property null");
        check(c.getBorder() == null, "default border null");
        check(c.getFont() == null, "default font null");
        check(c.getForeground() == null, "default foreground null");
        check(c.getBackground() == null, "default background null");

        c.setEnabled(false);
        check(c.isEnabled() == false, "disabled");
        c.setEnabled(true);
        check(c.isEnabled() == true, "enabled");

        c.setToolTipText("tip");
        check("tip".equals(c.getToolTipText()), "tooltip set");
        MouseEvent evt = new MouseEvent(c, MouseEvent.MOUSE_ENTERED, 0L, 0, 0, 0, 0, false);
        check("tip".equals(c.getToolTipText(evt)), "tooltip for event");
        c.setToolTipText(null);
        check(c.getToolTipText() == null, "tooltip cleared");

        c.putClientProperty("k", "v");
        check("v".equals(c.getClientProperty("k")), "client property set");
        c.putClientProperty("k", null);
        check(c.getClientProperty("k") == null, "client property removed");
        Object key = new Object();
        Object val = new Object();
        c.putClientProperty(key, val);
        check(c.getClientProperty(key) == val, "object key client property");

        c.setPreferredSize(new Dimension(10, 20));
        check(new Dimension(10, 20).equals(c.getPreferredSize()), "preferred size");
        c.setMaximumSize(new Dimension(30, 40));
        check(new Dimension(30, 40).equals(c.getMaximumSize()), "maximum size");
        c.setMinimumSize(new Dimension(5, 6));
        check(new Dimension(5, 6).equals(c.getMinimumSize()), "minimum size");

        Border b = new EmptyBorder(1, 2, 3, 4);
        c.setBorder(b);
        check(c.getBorder() == b, "border set");

        c.setVisible(true);
        check(c.isVisible() == true, "visible on");
        c.setVisible(false);
        check(c.isVisible() == false, "visible off");

        Font font = new Font(Font.SERIF, Font.BOLD, 12);
        c.setFont(font);
        check(font.equals(c.getFont()), "font set");
        c.setForeground(Color.RED);
        check(Color.RED.equals(c.getForeground()), "foreground set");
        c.setBackground(Color.BLUE);
        check(Color.BLUE.equals(c.getBackground()), "background set");

        c.setOpaque(true);
        check(c.isOpaque() == true, "opaque on");
        c.setOpaque(false);
        check(c.isOpaque() == false, "opaque off");

        c.setAlignmentX(0.75f);
        check(c.getAlignmentX() == 0.75f, "alignment x");
        c.setAlignmentY(0.25f);
        check(c.getAlignmentY() == 0.25f, "alignment y");

        c.setAutoscrolls(true);
        check(c.getAutoscrolls() == true, "autoscrolls on");
        c.setAutoscrolls(false);
        check(c.getAutoscrolls() == false, "autoscrolls off");

        c.setInheritsPopupMenu(true);
        check(c.getInheritsPopupMenu() == true, "inherits popup on");
        JPopupMenu popup = new JPopupMenu();
        c.setComponentPopupMenu(popup);
        check(c.getComponentPopupMenu() == popup, "component popup menu");
    }

    // JDK-27 only: getUI() is a public covariant override here (absent on JDK-8)
    static void testVersionSpecific() throws Exception {
        Method getUI = JComponent.class.getMethod("getUI");
        check(getUI.getReturnType().getName().equals("javax.swing.plaf.ComponentUI"),
                "JDK-27 JComponent.getUI() returns ComponentUI");
    }

    static void testContract() throws Exception {
        JPanel p = new JPanel();

        // ---- client property semantics: null value removes the key
        p.putClientProperty("k", "v");
        check("v".equals(p.getClientProperty("k")), "putClientProperty/getClientProperty");
        p.putClientProperty("k", null);
        check(p.getClientProperty("k") == null, "putClientProperty(k,null) removes");
        Object objKey = new Object();
        p.putClientProperty(objKey, Integer.valueOf(7));
        check(Integer.valueOf(7).equals(p.getClientProperty(objKey)), "arbitrary object key");

        // ---- tooltip round trip, null clears
        p.setToolTipText("tip");
        check("tip".equals(p.getToolTipText()), "setToolTipText/getToolTipText");
        p.setToolTipText(null);
        check(p.getToolTipText() == null, "setToolTipText(null) clears");

        // ---- property change event names fired by the standard setters
        List<String> names = new ArrayList<>();
        p.addPropertyChangeListener((PropertyChangeListener) e -> names.add(e.getPropertyName()));
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
        Locale custom = Locale.of("xx");
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

        // ---- vetoable-change listener bookkeeping
        int vcBefore = p.getVetoableChangeListeners().length;
        VetoableChangeListener vcl = (PropertyChangeEvent e) -> { };
        p.addVetoableChangeListener(vcl);
        check(p.getVetoableChangeListeners().length == vcBefore + 1, "addVetoableChangeListener counted");
        p.removeVetoableChangeListener(vcl);
        check(p.getVetoableChangeListeners().length == vcBefore, "removeVetoableChangeListener counted");

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

        p.getToolkit(); // no-throw headless

        testVersionSpecific();
    }
}
