/*
 * @test
 * @summary com.sun.java.swing.ui.TabsDlg conformance: internal package reflection.
 * @modules jdk.hotspot.agent/com.sun.java.swing.ui:+open
 * @run main TabsDlgTest
 */
import java.lang.reflect.*;

public class TabsDlgTest {
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
        Class<?> c = Class.forName("com.sun.java.swing.ui.TabsDlg", false, TabsDlgTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("javax.swing.JDialog".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "main"), "method main");
        check(hasMethod(c, "addOkListener"), "method addOkListener");
        check(hasMethod(c, "addCancelListener"), "method addCancelListener");
        check(hasMethod(c, "enableApplyButton"), "method enableApplyButton");
        check(hasMethod(c, "removeOkListener"), "method removeOkListener");
        check(hasMethod(c, "removeCancelListener"), "method removeCancelListener");
        check(hasMethod(c, "addApplyListener"), "method addApplyListener");
        check(hasMethod(c, "removeApplyListener"), "method removeApplyListener");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
