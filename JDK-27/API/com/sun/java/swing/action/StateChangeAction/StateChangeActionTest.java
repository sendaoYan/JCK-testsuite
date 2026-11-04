/*
 * @test
 * @summary com.sun.java.swing.action.StateChangeAction conformance: internal package reflection.
 * @modules jdk.hotspot.agent/com.sun.java.swing.action:+open
 * @run main StateChangeActionTest
 */
import java.lang.reflect.*;

public class StateChangeActionTest {
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
        Class<?> c = Class.forName("com.sun.java.swing.action.StateChangeAction", false, StateChangeActionTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("com.sun.java.swing.action.DelegateAction".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 1, "interface count");
        check(hasInterface(c, "java.awt.event.ItemListener"), "interface java.awt.event.ItemListener");
        check(hasMethod(c, "isSelected"), "method isSelected");
        check(hasMethod(c, "setSelected"), "method setSelected");
        check(hasMethod(c, "itemStateChanged"), "method itemStateChanged");
        check(hasMethod(c, "setItemListener"), "method setItemListener");
        check(hasMethod(c, "getItemListener"), "method getItemListener");
        check(hasField(c, "selected"), "field selected");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
