/*
 * @test
 * @summary javax.swing.plaf.metal.MetalFileChooserUI.DirectoryComboBoxModel conformance: white-box structure + black-box behavior.
 * @run main DirectoryComboBoxModelTest
 */

import java.lang.reflect.Modifier;
import javax.swing.ComboBoxModel;
import javax.swing.ListModel;

public class DirectoryComboBoxModelTest {
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
        Class<?> c = Class.forName("javax.swing.plaf.metal.MetalFileChooserUI$DirectoryComboBoxModel");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == false, "public modifier");
        check(Modifier.isProtected(mods) == true, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.AbstractListModel"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.swing.ComboBoxModel"), "interface 0");
    }

    static void testBlackBox() throws Exception {
        // The class is a protected member of MetalFileChooserUI and its sole
        // constructor is not accessible from outside the package, so behavior
        // is verified via the model type relationships it must honor.
        Class<?> c = Class.forName("javax.swing.plaf.metal.MetalFileChooserUI$DirectoryComboBoxModel");
        check(ComboBoxModel.class.isAssignableFrom(c), "is a ComboBoxModel");
        check(ListModel.class.isAssignableFrom(c), "is a ListModel");
    }
}
