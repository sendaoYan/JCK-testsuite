/*
 * @test
 * @summary sun.jvm.hotspot.ui.treetable.JTreeTable conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.ui.treetable:+open
 * @run main JTreeTableTest
 */
import java.lang.reflect.*;

public class JTreeTableTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.ui.treetable.JTreeTable", false, JTreeTableTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("javax.swing.JTable".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "getTree"), "method getTree");
        check(hasMethod(c, "updateUI"), "method updateUI");
        check(hasMethod(c, "setRootVisible"), "method setRootVisible");
        check(hasMethod(c, "setShowsRootHandles"), "method setShowsRootHandles");
        check(hasMethod(c, "setRowHeight"), "method setRowHeight");
        check(hasMethod(c, "getShowsRootHandles"), "method getShowsRootHandles");
        check(hasMethod(c, "sizeColumnsToFit"), "method sizeColumnsToFit");
        check(hasMethod(c, "editCellAt"), "method editCellAt");
        check(hasMethod(c, "getEditingRow"), "method getEditingRow");
        check(hasMethod(c, "getTreeEditable"), "method getTreeEditable");
        check(hasMethod(c, "setTreeEditable"), "method setTreeEditable");
        check(hasMethod(c, "getShowsIcons"), "method getShowsIcons");
        check(hasMethod(c, "setShowsIcons"), "method setShowsIcons");
        check(hasField(c, "tree"), "field tree");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
