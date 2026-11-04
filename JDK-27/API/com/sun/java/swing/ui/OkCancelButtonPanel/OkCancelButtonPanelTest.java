/*
 * @test
 * @summary com.sun.java.swing.ui.OkCancelButtonPanel conformance: internal package reflection.
 * @modules jdk.hotspot.agent/com.sun.java.swing.ui:+open
 * @run main OkCancelButtonPanelTest
 */
import java.lang.reflect.*;

public class OkCancelButtonPanelTest {
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
        Class<?> c = Class.forName("com.sun.java.swing.ui.OkCancelButtonPanel", false, OkCancelButtonPanelTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("javax.swing.JPanel".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasField(c, "OK_COMMAND"), "field OK_COMMAND");
        check(hasField(c, "CANCEL_COMMAND"), "field CANCEL_COMMAND");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
