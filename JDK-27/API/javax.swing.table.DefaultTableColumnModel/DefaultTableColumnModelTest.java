/*
 * @test
 * @summary javax.swing.table.DefaultTableColumnModel conformance: type shape and supertypes.
 * @run main DefaultTableColumnModelTest
 */

import java.lang.reflect.Modifier;

public class DefaultTableColumnModelTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.table.DefaultTableColumnModel");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 4, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.swing.table.TableColumnModel"), "interface 0");
        check(c.getInterfaces()[1].getName().equals("java.beans.PropertyChangeListener"), "interface 1");
        check(c.getInterfaces()[2].getName().equals("javax.swing.event.ListSelectionListener"), "interface 2");
        check(c.getInterfaces()[3].getName().equals("java.io.Serializable"), "interface 3");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
