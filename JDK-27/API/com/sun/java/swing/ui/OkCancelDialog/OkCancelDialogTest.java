/*
 * @test
 * @summary com.sun.java.swing.ui.OkCancelDialog conformance: internal package reflection.
 * @modules jdk.hotspot.agent/com.sun.java.swing.ui:+open
 * @run main OkCancelDialogTest
 */
import java.lang.reflect.*;

public class OkCancelDialogTest {
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
        Class<?> c = Class.forName("com.sun.java.swing.ui.OkCancelDialog", false, OkCancelDialogTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("javax.swing.JDialog".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 1, "interface count");
        check(hasInterface(c, "java.awt.event.ActionListener"), "interface java.awt.event.ActionListener");
        check(hasMethod(c, "actionPerformed"), "method actionPerformed");
        check(hasMethod(c, "isOk"), "method isOk");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
