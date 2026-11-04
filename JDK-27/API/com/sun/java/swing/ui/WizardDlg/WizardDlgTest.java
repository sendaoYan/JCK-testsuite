/*
 * @test
 * @summary com.sun.java.swing.ui.WizardDlg conformance: internal package reflection.
 * @modules jdk.hotspot.agent/com.sun.java.swing.ui:+open
 * @run main WizardDlgTest
 */
import java.lang.reflect.*;

public class WizardDlgTest {
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
        Class<?> c = Class.forName("com.sun.java.swing.ui.WizardDlg", false, WizardDlgTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("javax.swing.JDialog".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "main"), "method main");
        check(hasMethod(c, "reset"), "method reset");
        check(hasMethod(c, "addCancelListener"), "method addCancelListener");
        check(hasMethod(c, "removeCancelListener"), "method removeCancelListener");
        check(hasMethod(c, "setPanels"), "method setPanels");
        check(hasMethod(c, "addFinishListener"), "method addFinishListener");
        check(hasMethod(c, "setWestPanel"), "method setWestPanel");
        check(hasMethod(c, "removeFinishListener"), "method removeFinishListener");
        check(hasMethod(c, "addNextListener"), "method addNextListener");
        check(hasMethod(c, "removeNextListener"), "method removeNextListener");
        check(hasMethod(c, "addBackListener"), "method addBackListener");
        check(hasMethod(c, "removeBackListener"), "method removeBackListener");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
