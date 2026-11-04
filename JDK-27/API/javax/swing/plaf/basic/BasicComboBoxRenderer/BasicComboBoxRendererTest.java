/*
 * @test
 * @summary javax.swing.plaf.basic.BasicComboBoxRenderer conformance: white-box structure + black-box behavior.
 * @run main BasicComboBoxRendererTest
 */

import java.lang.reflect.Modifier;

public class BasicComboBoxRendererTest {
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
        Class<?> c = Class.forName("javax.swing.plaf.basic.BasicComboBoxRenderer");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.JLabel"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 2, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.swing.ListCellRenderer"), "interface 0");
        check(c.getInterfaces()[1].getName().equals("java.io.Serializable"), "interface 1");
    }

    static void testBlackBox() throws Exception {
        javax.swing.plaf.basic.BasicComboBoxRenderer r = new javax.swing.plaf.basic.BasicComboBoxRenderer();
        check(r instanceof javax.swing.JLabel, "is JLabel");
        javax.swing.JList<Object> list = new javax.swing.JList<>();
        java.awt.Component c = r.getListCellRendererComponent(list, "item", 0, false, false);
        check(c == r, "returns itself");
        check("item".equals(r.getText()), "sets text");
        check(r.getPreferredSize() != null, "preferred size non-null");
    }
}
