/*
 * @test
 * @summary javax.swing.LookAndFeel conformance: type shape + Metal identity + install helpers (headless-safe).
 * @run main LookAndFeelTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JLabel;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.text.JTextComponent;

public class LookAndFeelTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    static void testShape() throws Exception {
        Class<?> c = Class.forName("javax.swing.LookAndFeel", false, LookAndFeelTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("installColors"), "method installColors");
        check(names.contains("installColorsAndFont"), "method installColorsAndFont");
        check(names.contains("installBorder"), "method installBorder");
        check(names.contains("uninstallBorder"), "method uninstallBorder");
        check(names.contains("installProperty"), "method installProperty");
        check(names.contains("makeKeyBindings"), "method makeKeyBindings");
        check(names.contains("makeInputMap"), "method makeInputMap");
    }

    static void testMetalIdentity() {
        LookAndFeel metal = new MetalLookAndFeel();
        check("Metal".equals(metal.getName()), "Metal name");
        check("Metal".equals(metal.getID()), "Metal id");
        check(metal.getDescription() != null && !metal.getDescription().isEmpty(),
                "Metal description nonempty");
        check(!metal.isNativeLookAndFeel(), "Metal is not native");
        check(metal.isSupportedLookAndFeel(), "Metal is supported");
        check(metal.getSupportsWindowDecorations(), "Metal supports window decorations");
        check(metal.toString() != null && !metal.toString().isEmpty(), "toString nonempty");
    }

    static void testMakeHelpers() {
        JTextComponent.KeyBinding[] kb = LookAndFeel.makeKeyBindings(
                new Object[]{"A", "action-a", "ctrl B", "action-b"});
        check(kb.length == 2, "makeKeyBindings count");
        check(kb[0].key == javax.swing.KeyStroke.getKeyStroke("A"), "binding 0 keystroke");
        check("action-a".equals(kb[0].actionName), "binding 0 action name");
        check(kb[1].key == javax.swing.KeyStroke.getKeyStroke("ctrl B"), "binding 1 keystroke");
        check("action-b".equals(kb[1].actionName), "binding 1 action name");
        javax.swing.InputMap im = LookAndFeel.makeInputMap(new Object[]{
                "A", "action-a", "ctrl B", "action-b"});
        check("action-a".equals(im.get(javax.swing.KeyStroke.getKeyStroke("A"))),
                "makeInputMap binding");
        check("action-b".equals(im.get(javax.swing.KeyStroke.getKeyStroke("ctrl B"))),
                "makeInputMap modifier binding");
        check(im.size() == 2, "makeInputMap size");
    }

    static class UIResourceBorder extends EmptyBorder implements javax.swing.plaf.UIResource {
        UIResourceBorder(int t, int l, int b, int r) { super(t, l, b, r); }
    }

    static void testInstallBorder() {
        JLabel c = new JLabel("x");
        Border uiBorder = new UIResourceBorder(1, 2, 3, 4);
        UIManager.put("test.border.key", uiBorder);
        LookAndFeel.installBorder(c, "test.border.key");
        check(c.getBorder() == uiBorder, "installBorder sets border from defaults");
        LookAndFeel.uninstallBorder(c);
        check(c.getBorder() == null, "uninstallBorder clears UIResource border");
        Border keep = new EmptyBorder(9, 9, 9, 9);
        c.setBorder(keep);
        LookAndFeel.uninstallBorder(c);
        check(c.getBorder() == keep, "uninstallBorder keeps non-UIResource border");
        UIManager.put("test.border.key", null);
        LookAndFeel.installBorder(c, "test.border.key"); // missing default: no change
        check(c.getBorder() == keep, "installBorder with missing default is a no-op");
    }

    static void testInstallColorsAndFont() {
        JLabel c = new JLabel("x");
        ColorUIResource bg = new ColorUIResource(11, 22, 33);
        ColorUIResource fg = new ColorUIResource(44, 55, 66);
        FontUIResource font = new FontUIResource("Dialog", java.awt.Font.BOLD, 15);
        UIManager.put("test.bg.key", bg);
        UIManager.put("test.fg.key", fg);
        UIManager.put("test.font.key", font);
        LookAndFeel.installColors(c, "test.bg.key", "test.fg.key");
        check(c.getBackground() == bg, "installColors background");
        check(c.getForeground() == fg, "installColors foreground");
        LookAndFeel.installColorsAndFont(new JLabel("y"), "test.bg.key", "test.fg.key", "test.font.key");
        JLabel z = new JLabel("z");
        LookAndFeel.installColorsAndFont(z, "test.bg.key", "test.fg.key", "test.font.key");
        check(z.getBackground() == bg, "installColorsAndFont background");
        check(z.getForeground() == fg, "installColorsAndFont foreground");
        check(z.getFont() == font, "installColorsAndFont font");
    }

    static void testInstallProperty() {
        JLabel c = new JLabel("x");
        LookAndFeel.installProperty(c, "opaque", Boolean.TRUE);
        check(c.isOpaque(), "installProperty opaque true");
    }

    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(new MetalLookAndFeel());
        testShape();
        testMetalIdentity();
        testMakeHelpers();
        testInstallBorder();
        testInstallColorsAndFont();
        testInstallProperty();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
