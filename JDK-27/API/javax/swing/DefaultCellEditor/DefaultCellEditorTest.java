/*
 * @test
 * @summary javax.swing.DefaultCellEditor conformance: white-box structure + black-box behavior.
 * @run main DefaultCellEditorTest
 */

import java.lang.reflect.Modifier;

import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JTextField;

public class DefaultCellEditorTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.DefaultCellEditor");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isProtected(mods), "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.AbstractCellEditor"), "superclass");
        check(!Modifier.isAbstract(mods), "abstract modifier");
        check(c.getInterfaces().length == 2, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.swing.table.TableCellEditor"), "interface 0");
        check(c.getInterfaces()[1].getName().equals("javax.swing.tree.TreeCellEditor"), "interface 1");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // text-field editor
        JTextField field = new JTextField("abc");
        DefaultCellEditor textEd = new DefaultCellEditor(field);
        check(textEd.getComponent() == field, "getComponent (text)");
        check("abc".equals(textEd.getCellEditorValue()), "text editor value");

        // click count to start
        textEd.setClickCountToStart(3);
        check(textEd.getClickCountToStart() == 3, "click count");
        check(textEd.isCellEditable(null), "text editable");
        check(textEd.shouldSelectCell(null), "text shouldSelect");
        check(textEd.stopCellEditing(), "text stop");

        // check-box editor returns Boolean selection state
        JCheckBox box = new JCheckBox();
        DefaultCellEditor checkEd = new DefaultCellEditor(box);
        box.setSelected(true);
        check(Boolean.TRUE.equals(checkEd.getCellEditorValue()), "checkbox value");

        // combo-box editor
        JComboBox<String> combo = new JComboBox<>(new String[]{"x", "y"});
        DefaultCellEditor comboEd = new DefaultCellEditor(combo);
        check(comboEd.getComponent() == combo, "getComponent (combo)");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
