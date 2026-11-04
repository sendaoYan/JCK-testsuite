/*
 * @test
 * @summary javax.swing.plaf.LayerUI conformance: white-box structure + black-box behavior.
 * @run main LayerUITest
 */

import java.lang.reflect.Modifier;

public class LayerUITest {
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
        Class<?> c = Class.forName("javax.swing.plaf.LayerUI");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.plaf.ComponentUI"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("java.io.Serializable"), "interface 0");
    }

    static void testBlackBox() throws Exception {
        javax.swing.plaf.LayerUI<javax.swing.JPanel> ui = new javax.swing.plaf.LayerUI<javax.swing.JPanel>() {};
        check(ui instanceof java.io.Serializable, "serializable");
        javax.swing.JLayer<javax.swing.JPanel> layer = new javax.swing.JLayer<>(new javax.swing.JPanel());
        check(ui.getBaseline(layer, 100, 100) == -1, "baseline -1");
        check(ui.getBaselineResizeBehavior(layer) == java.awt.Component.BaselineResizeBehavior.OTHER, "baseline resize OTHER");
        java.beans.PropertyChangeListener l = e -> {};
        ui.addPropertyChangeListener(l);
        check(ui.getPropertyChangeListeners().length == 1, "pcl count 1");
        ui.removePropertyChangeListener(l);
        check(ui.getPropertyChangeListeners().length == 0, "pcl removed");
        java.beans.PropertyChangeListener l2 = e -> {};
        ui.addPropertyChangeListener("foo", l2);
        check(ui.getPropertyChangeListeners("foo").length == 1, "named pcl 1");
        ui.removePropertyChangeListener("foo", l2);
        check(ui.getPropertyChangeListeners("foo").length == 0, "named pcl removed");
    }
}
