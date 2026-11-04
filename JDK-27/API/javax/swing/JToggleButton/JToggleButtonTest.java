/*
 * @test
 * @summary javax.swing.JToggleButton conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main JToggleButtonTest
 */

import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ButtonModel;
import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JToggleButton;

public class JToggleButtonTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static Icon fixedIcon() {
        return new Icon() {
            public int getIconWidth() { return 8; }
            public int getIconHeight() { return 8; }
            public void paintIcon(java.awt.Component c, Graphics g, int x, int y) { }
        };
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.JToggleButton");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.AbstractButton"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.accessibility.Accessible"), "interface 0");
        // supplemented structural checks
        Class<?> model = Class.forName("javax.swing.JToggleButton$ToggleButtonModel");
        check(model.getSuperclass().getName().equals("javax.swing.DefaultButtonModel"), "ToggleButtonModel superclass");
        check(Class.forName("javax.swing.JToggleButton$AccessibleJToggleButton").getSuperclass().getName()
                .equals("javax.swing.AbstractButton$AccessibleAbstractButton"), "AccessibleJToggleButton nesting");
        check(c.getConstructor(String.class) != null, "ctor String");
        check(c.getConstructor(String.class, boolean.class) != null, "ctor String,boolean");
        check(c.getConstructor(Icon.class) != null, "ctor Icon");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // constructors: text / icon / preselected
        JToggleButton toggle = new JToggleButton("Toggle");
        check(toggle.getText().equals("Toggle"), "ctor text");
        check(!toggle.isSelected(), "default not selected");
        Icon icon = fixedIcon();
        JToggleButton iconToggle = new JToggleButton(icon);
        check(iconToggle.getIcon() == icon, "ctor Icon identity");
        JToggleButton preselected = new JToggleButton("Pre", true);
        check(preselected.isSelected(), "ctor(text, true) selected");
        check(toggle.getModel() instanceof JToggleButton.ToggleButtonModel, "toggle uses ToggleButtonModel");

        // selection chain driven through the shared model
        toggle.setSelected(true);
        check(toggle.isSelected(), "setSelected(true) round trip");
        ButtonModel model = toggle.getModel();
        check(model.isSelected(), "ButtonModel.isSelected mirrors button");
        toggle.setSelected(false);
        check(!model.isSelected(), "ButtonModel cleared with button");
        model.setSelected(true);
        check(toggle.isSelected(), "button mirrors model.setSelected");
        toggle.setSelected(false);
        check(!toggle.isSelected(), "toggle reset to unselected before event recording");

        // ItemEvent recording: state changes fire exactly once per transition
        final List<Integer> states = new ArrayList<>();
        final List<Object> sources = new ArrayList<>();
        toggle.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                states.add(e.getStateChange());
                sources.add(e.getItem());
            }
        });
        toggle.setSelected(true);
        check(states.size() == 1, "one ItemEvent for select");
        check(states.get(0) == ItemEvent.SELECTED, "ItemEvent SELECTED");
        check(sources.get(0) == toggle, "ItemEvent item is the button");
        toggle.setSelected(true);
        check(states.size() == 1, "redundant setSelected fires nothing");
        toggle.setSelected(false);
        check(states.size() == 2, "one ItemEvent for deselect");
        check(states.get(1) == ItemEvent.DESELECTED, "ItemEvent DESELECTED");

        // doClick flips a toggle's selection through the model
        int before = states.size();
        toggle.doClick();
        check(toggle.isSelected(), "doClick toggles selection on");
        check(states.size() == before + 1, "doClick produced one ItemEvent");

        // setText fires a PropertyChangeEvent carrying property name and values
        final List<PropertyChangeEvent> events = new ArrayList<>();
        toggle.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) { events.add(e); }
        });
        toggle.setText("Renamed");
        check(toggle.getText().equals("Renamed"), "setText round trip");
        boolean found = false;
        for (PropertyChangeEvent e : events) {
            if ("text".equals(e.getPropertyName())) {
                found = true;
                check("Toggle".equals(e.getOldValue()) && "Renamed".equals(e.getNewValue()),
                        "text property old/new values");
            }
        }
        check(found, "setText fires property change with name 'text'");
        events.clear();
        toggle.setMnemonic('R');
        found = false;
        for (PropertyChangeEvent e : events) {
            if ("mnemonic".equals(e.getPropertyName())) found = true;
        }
        check(found, "setMnemonic fires property change with name 'mnemonic'");
        check(toggle.getMnemonic() == (int) 'R', "mnemonic round trip");

        // enabled chain: disabled toggle keeps state but ignores clicks
        toggle.setEnabled(false);
        check(!toggle.isEnabled(), "setEnabled(false) round trip");
        int beforeDisabled = states.size();
        toggle.doClick();
        check(states.size() == beforeDisabled, "disabled doClick does not toggle");

        // realized-state chain
        JFrame frame = new JFrame("JToggleButtonTest");
        try {
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.getContentPane().add(toggle);
            frame.pack();
            frame.setVisible(true);
            check(frame.isVisible(), "frame visible");
            check(toggle.isDisplayable(), "toggle displayable in realized frame");
        } finally {
            frame.dispose();
        }
        check(!frame.isVisible(), "frame not visible after dispose");
    }

    public static void main(String[] args) throws Exception {
        runOnEDT(JToggleButtonTest::testWhiteBox);
        runOnEDT(JToggleButtonTest::testBlackBox);
        System.out.println("PASS (" + passed + " assertions)");
    }
    interface ThrowingRunnable { void run() throws Exception; }
    static void runOnEDT(ThrowingRunnable r) throws Exception {
        try {
            EventQueue.invokeAndWait(() -> {
                try { r.run(); }
                catch (Exception e) { throw new RuntimeException(e); }
            });
        } catch (java.lang.reflect.InvocationTargetException e) {
            Throwable t = e.getCause();
            if (t instanceof RuntimeException) throw (RuntimeException) t;
            if (t instanceof Error) throw (Error) t;
            throw new RuntimeException(t);
        }
    }
}
