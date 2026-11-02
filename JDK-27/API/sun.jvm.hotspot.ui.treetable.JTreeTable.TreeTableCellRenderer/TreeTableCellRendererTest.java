/*
 * @test
 * @summary sun.jvm.hotspot.ui.treetable.JTreeTable.TreeTableCellRenderer conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.ui.treetable:+open
 * @run main TreeTableCellRendererTest
 */
import java.lang.reflect.*;

public class TreeTableCellRendererTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    static boolean hasField(Class<?> c, String n) {
        for (Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true;
        return false;
    }
    static boolean hasInterface(Class<?> c, String n) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.jvm.hotspot.ui.treetable.JTreeTable$TreeTableCellRenderer", false, TreeTableCellRendererTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("javax.swing.JTree".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 1, "interface count");
        check(hasInterface(c, "javax.swing.table.TableCellRenderer"), "interface javax.swing.table.TableCellRenderer");
        check(hasMethod(c, "updateUI"), "method updateUI");
        check(hasMethod(c, "paint"), "method paint");
        check(hasMethod(c, "setBounds"), "method setBounds");
        check(hasMethod(c, "setRowHeight"), "method setRowHeight");
        check(hasMethod(c, "getTableCellRendererComponent"), "method getTableCellRendererComponent");
        check(hasField(c, "visibleRow"), "field visibleRow");
        check(hasField(c, "highlightBorder"), "field highlightBorder");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
