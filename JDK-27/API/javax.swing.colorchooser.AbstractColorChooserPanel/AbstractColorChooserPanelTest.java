/*
 * @test
 * @summary javax.swing.colorchooser.AbstractColorChooserPanel conformance: white-box structure + black-box behavior.
 * @run main AbstractColorChooserPanelTest
 */

import java.awt.Color;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import javax.swing.Icon;
import javax.swing.JColorChooser;
import javax.swing.colorchooser.AbstractColorChooserPanel;

public class AbstractColorChooserPanelTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // Minimal concrete panel, tracking the two lifecycle callbacks.
    static class TestPanel extends AbstractColorChooserPanel {
        boolean built;
        boolean updated;
        public void updateChooser() { updated = true; }
        protected void buildChooser() { built = true; }
        public String getDisplayName() { return "Test Panel"; }
        public Icon getSmallDisplayIcon() { return null; }
        public Icon getLargeDisplayIcon() { return null; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.colorchooser.AbstractColorChooserPanel");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.JPanel"), "superclass");
        check(Modifier.isAbstract(mods) == true, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");

        String prop = (String) c.getField("TRANSPARENCY_ENABLED_PROPERTY").get(null);
        check("TransparencyEnabled".equals(prop), "TRANSPARENCY_ENABLED_PROPERTY value");

        for (String name : new String[] { "updateChooser", "buildChooser", "getDisplayName",
                "getSmallDisplayIcon", "getLargeDisplayIcon" }) {
            Method m = findMethod(c, name);
            check(m != null && Modifier.isAbstract(m.getModifiers()), "abstract method " + name);
        }
    }

    static Method findMethod(Class<?> c, String name) {
        for (Method m : c.getDeclaredMethods()) {
            if (m.getName().equals(name)) return m;
        }
        return null;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        TestPanel panel = new TestPanel();
        check("Test Panel".equals(panel.getDisplayName()), "getDisplayName returns subclass name");

        // Mnemonic hints default to "none".
        check(panel.getMnemonic() == 0, "default mnemonic 0");
        check(panel.getDisplayedMnemonicIndex() == -1, "default displayed mnemonic index -1");

        // Transparency selection flag defaults to true (base impl is a no-op hint).
        check(panel.isColorTransparencySelectionEnabled(), "transparency enabled by default");
        panel.setColorTransparencySelectionEnabled(false);
        check(panel.isColorTransparencySelectionEnabled(), "base transparency flag stays enabled (no-op)");

        // No chooser installed yet: no model, and lifecycle callbacks not invoked.
        check(panel.getColorSelectionModel() == null, "no model before install");
        check(!panel.built && !panel.updated, "lifecycle callbacks not invoked before install");

        JColorChooser chooser = new JColorChooser();
        panel.installChooserPanel(chooser);
        check(panel.built, "install invokes buildChooser");
        check(panel.updated, "install invokes updateChooser");
        check(panel.getColorSelectionModel() == chooser.getSelectionModel(),
                "model wired to enclosing chooser after install");

        // Re-installing the already-installed panel is an error.
        try {
            panel.installChooserPanel(chooser);
            check(false, "double install should throw");
        } catch (RuntimeException expected) {
            check(true, "double install throws RuntimeException");
        }

        // Uninstall detaches the model.
        panel.uninstallChooserPanel(chooser);
        check(panel.getColorSelectionModel() == null, "model detached after uninstall");

        // A fresh chooser with a chosen color round-trips through the panel's model.
        JColorChooser chooser2 = new JColorChooser(Color.red);
        panel.installChooserPanel(chooser2);
        check(Color.red.equals(panel.getColorSelectionModel().getSelectedColor()),
                "selected color visible through panel model");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
