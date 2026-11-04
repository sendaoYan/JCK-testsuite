/*
 * @test
 * @summary javax.swing.plaf.multi.MultiLookAndFeel conformance: white-box structure + black-box behavior.
 * @run main MultiLookAndFeelTest
 */

import java.lang.reflect.Modifier;
import java.util.Vector;
import javax.swing.JComponent;
import javax.swing.UIDefaults;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.multi.MultiLookAndFeel;

public class MultiLookAndFeelTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.plaf.multi.MultiLookAndFeel");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.LookAndFeel"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
        check(c.getConstructor() != null, "public no-arg ctor");
        check(c.getMethod("getName") != null, "getName");
        check(c.getMethod("getID") != null, "getID");
        check(c.getMethod("getDescription") != null, "getDescription");
        check(c.getMethod("isNativeLookAndFeel") != null, "isNativeLookAndFeel");
        check(c.getMethod("isSupportedLookAndFeel") != null, "isSupportedLookAndFeel");
        check(c.getMethod("getDefaults") != null, "getDefaults");
        check(c.getMethod("createUIs", ComponentUI.class, Vector.class, JComponent.class) != null, "createUIs");
        check(c.getDeclaredMethod("uisToArray", Vector.class) != null, "uisToArray");
    }

    static void testBlackBox() throws Exception {
        MultiLookAndFeel laf = new MultiLookAndFeel();
        check("Multiplexing Look and Feel".equals(laf.getName()), "getName");
        check("Multiplex".equals(laf.getID()), "getID");
        check("Allows multiple UI instances per component instance".equals(laf.getDescription()), "getDescription");
        check(laf.isNativeLookAndFeel() == false, "isNativeLookAndFeel false");
        check(laf.isSupportedLookAndFeel() == true, "isSupportedLookAndFeel true");

        UIDefaults defaults = laf.getDefaults();
        check(defaults != null, "getDefaults non-null");
        check("javax.swing.plaf.multi.MultiButtonUI".equals(defaults.get("ButtonUI")), "ButtonUI mapping");
        check("javax.swing.plaf.multi.MultiLabelUI".equals(defaults.get("LabelUI")), "LabelUI mapping");
        check("javax.swing.plaf.multi.MultiTextUI".equals(defaults.get("TextFieldUI")), "TextFieldUI mapping");
    }
}
