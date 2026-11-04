/*
 * @test
 * @summary javax.swing.colorchooser.ColorSelectionModel conformance: white-box structure + black-box behavior.
 * @run main ColorSelectionModelTest
 */

import java.awt.Color;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import javax.swing.colorchooser.ColorSelectionModel;
import javax.swing.event.ChangeListener;

public class ColorSelectionModelTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // Minimal conforming implementation used to exercise the interface contract.
    static class Model implements ColorSelectionModel {
        private Color color;
        private final java.util.List<ChangeListener> listeners = new java.util.ArrayList<ChangeListener>();
        Model(Color c) { color = c; }
        public Color getSelectedColor() { return color; }
        public void setSelectedColor(Color c) {
            if (c != null && !c.equals(color)) { color = c; fire(); }
        }
        private void fire() {
            javax.swing.event.ChangeEvent ev = new javax.swing.event.ChangeEvent(this);
            for (ChangeListener l : listeners) l.stateChanged(ev);
        }
        public void addChangeListener(ChangeListener l) { listeners.add(l); }
        public void removeChangeListener(ChangeListener l) { listeners.remove(l); }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.colorchooser.ColorSelectionModel");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == true, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 0, "no interfaces");
        Method[] methods = c.getDeclaredMethods();
        check(methods.length == 4, "four interface methods");
        check(c.getMethod("getSelectedColor") != null, "getSelectedColor declared");
        check(c.getMethod("setSelectedColor", Color.class) != null, "setSelectedColor declared");
        check(c.getMethod("addChangeListener", ChangeListener.class) != null, "addChangeListener declared");
        check(c.getMethod("removeChangeListener", ChangeListener.class) != null, "removeChangeListener declared");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        ColorSelectionModel model = new Model(Color.blue);
        check(Color.blue.equals(model.getSelectedColor()), "getSelectedColor returns initial color");

        // set/get round-trip through the interface reference.
        model.setSelectedColor(Color.green);
        check(Color.green.equals(model.getSelectedColor()), "setSelectedColor updates color");

        // ChangeListener fires on change.
        final int[] fired = { 0 };
        final Object[] source = { null };
        ChangeListener l = new ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent e) {
                fired[0]++;
                source[0] = e.getSource();
            }
        };
        model.addChangeListener(l);
        model.setSelectedColor(Color.red);
        check(fired[0] == 1, "setSelectedColor fires listener");
        check(source[0] == model, "change event source is the model");

        // removeChangeListener stops delivery.
        model.removeChangeListener(l);
        model.setSelectedColor(Color.black);
        check(fired[0] == 1, "removed listener not notified");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // Appended contract: custom model drives JColorChooser color.
    static void testContract() throws Exception {
        javax.swing.colorchooser.ColorSelectionModel m =
                new javax.swing.colorchooser.DefaultColorSelectionModel(java.awt.Color.WHITE);
        javax.swing.JColorChooser cc = new javax.swing.JColorChooser(m);
        check(java.awt.Color.WHITE.equals(cc.getColor()), "JColorChooser reads custom model color");
        m.setSelectedColor(java.awt.Color.CYAN);
        check(java.awt.Color.CYAN.equals(cc.getColor()), "model changes propagate to chooser getColor");
        m.setSelectedColor(null);
        check(java.awt.Color.CYAN.equals(m.getSelectedColor()), "null color ignored by default model");
    }
}
