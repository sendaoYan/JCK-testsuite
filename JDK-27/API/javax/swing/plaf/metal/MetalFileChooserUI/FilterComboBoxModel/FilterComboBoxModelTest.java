/*
 * @test
 * @summary javax.swing.plaf.metal.MetalFileChooserUI.FilterComboBoxModel conformance: white-box structure + black-box behavior.
 * @run main FilterComboBoxModelTest
 */

import java.beans.PropertyChangeListener;
import java.lang.reflect.Modifier;
import javax.swing.ComboBoxModel;

public class FilterComboBoxModelTest {
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
        Class<?> c = Class.forName("javax.swing.plaf.metal.MetalFileChooserUI$FilterComboBoxModel");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == false, "public modifier");
        check(Modifier.isProtected(mods) == true, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.AbstractListModel"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 2, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.swing.ComboBoxModel"), "interface 0");
        check(c.getInterfaces()[1].getName().equals("java.beans.PropertyChangeListener"), "interface 1");
    }

    static void testBlackBox() throws Exception {
        // The sole constructor is protected, so instantiation is not possible
        // from outside the package; verify behavior via type relationships.
        Class<?> c = Class.forName("javax.swing.plaf.metal.MetalFileChooserUI$FilterComboBoxModel");
        check(ComboBoxModel.class.isAssignableFrom(c), "is a ComboBoxModel");
        check(PropertyChangeListener.class.isAssignableFrom(c), "is a PropertyChangeListener");
    }
}
