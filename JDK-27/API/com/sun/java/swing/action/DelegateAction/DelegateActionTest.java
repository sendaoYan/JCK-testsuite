/*
 * @test
 * @summary com.sun.java.swing.action.DelegateAction conformance: internal package reflection.
 * @modules jdk.hotspot.agent/com.sun.java.swing.action:+open
 * @run main DelegateActionTest
 */
import java.lang.reflect.*;

public class DelegateActionTest {
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
        Class<?> c = Class.forName("com.sun.java.swing.action.DelegateAction", false, DelegateActionTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("javax.swing.AbstractAction".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "actionPerformed"), "method actionPerformed");
        check(hasMethod(c, "addActionListener"), "method addActionListener");
        check(hasMethod(c, "removeActionListener"), "method removeActionListener");
        check(hasMethod(c, "getActionListeners"), "method getActionListeners");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
