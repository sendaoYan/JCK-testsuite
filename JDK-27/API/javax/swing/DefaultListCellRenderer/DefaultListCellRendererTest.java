/*
 * @test
 * @summary javax.swing.DefaultListCellRenderer conformance: white-box structure + black-box behavior.
 * @run main DefaultListCellRendererTest
 */

import java.awt.Component;
import java.lang.reflect.Modifier;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;

public class DefaultListCellRendererTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.DefaultListCellRenderer");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isProtected(mods), "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.JLabel"), "superclass");
        check(!Modifier.isAbstract(mods), "abstract modifier");
        check(c.getInterfaces().length == 2, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.swing.ListCellRenderer"), "interface 0");
        check(c.getInterfaces()[1].getName().equals("java.io.Serializable"), "interface 1");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        DefaultListCellRenderer renderer = new DefaultListCellRenderer();
        JList<String> list = new JList<>(new String[]{"a", "b", "c"});

        // the renderer reuses itself and copies the value into its text
        Component c = renderer.getListCellRendererComponent(list, "value", 1, false, false);
        check(c == renderer, "returns self");
        check("value".equals(((JLabel) c).getText()), "text set from value");
        check(c.isEnabled(), "enabled renderer");

        // DefaultListCellRenderer sets itself opaque in its constructor
        check(renderer.isOpaque(), "default opaque");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
