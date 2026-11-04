/*
 * @test
 * @summary sun.jvm.hotspot.ui.action.ShowAction conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.ui.action:+open
 * @run main ShowActionTest
 */
import java.lang.reflect.*;

public class ShowActionTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.ui.action.ShowAction", false, ShowActionTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("com.sun.java.swing.action.DelegateAction".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasField(c, "VALUE_COMMAND"), "field VALUE_COMMAND");
        check(hasField(c, "VALUE_NAME"), "field VALUE_NAME");
        check(hasField(c, "VALUE_SMALL_ICON"), "field VALUE_SMALL_ICON");
        check(hasField(c, "VALUE_MNEMONIC"), "field VALUE_MNEMONIC");
        check(hasField(c, "VALUE_SHORT_DESCRIPTION"), "field VALUE_SHORT_DESCRIPTION");
        check(hasField(c, "VALUE_LONG_DESCRIPTION"), "field VALUE_LONG_DESCRIPTION");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
