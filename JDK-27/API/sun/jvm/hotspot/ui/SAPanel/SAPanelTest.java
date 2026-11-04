/*
 * @test
 * @summary sun.jvm.hotspot.ui.SAPanel conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.ui:+open
 * @run main SAPanelTest
 */
import java.lang.reflect.*;

public class SAPanelTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.ui.SAPanel", false, SAPanelTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("javax.swing.JPanel".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "showInspector"), "method showInspector");
        check(hasMethod(c, "addPanelListener"), "method addPanelListener");
        check(hasMethod(c, "showThreadOopInspector"), "method showThreadOopInspector");
        check(hasMethod(c, "showThreadStackMemory"), "method showThreadStackMemory");
        check(hasMethod(c, "showThreadInfo"), "method showThreadInfo");
        check(hasMethod(c, "showJavaStackTrace"), "method showJavaStackTrace");
        check(hasMethod(c, "showCodeViewer"), "method showCodeViewer");
        check(hasMethod(c, "removePanelListener"), "method removePanelListener");
        check(hasField(c, "listeners"), "field listeners");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
