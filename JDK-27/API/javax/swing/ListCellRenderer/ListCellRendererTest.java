/*
 * @test
 * @summary javax.swing.ListCellRenderer conformance: white-box structure + black-box behavior.
 * @run main ListCellRendererTest
 */

import java.awt.Component;
import java.lang.reflect.Modifier;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

public class ListCellRendererTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.ListCellRenderer");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        ListCellRenderer<String> r = new ListCellRenderer<String>() {
            public Component getListCellRendererComponent(JList<? extends String> list,
                    String value, int index, boolean selected, boolean focused) {
                return new JLabel(value);
            }
        };
        Component c = r.getListCellRendererComponent(null, "item", 1, true, false);
        check(c instanceof JLabel, "returns a component");
        check("item".equals(((JLabel) c).getText()), "component carries value");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
