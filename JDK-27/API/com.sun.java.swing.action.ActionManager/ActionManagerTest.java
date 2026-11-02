/*
 * @test
 * @summary com.sun.java.swing.action.ActionManager conformance: internal package reflection.
 * @modules jdk.hotspot.agent/com.sun.java.swing.action:+open
 * @run main ActionManagerTest
 */
import java.lang.reflect.*;

public class ActionManagerTest {
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
        Class<?> c = Class.forName("com.sun.java.swing.action.ActionManager", false, ActionManagerTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "getInstance"), "method getInstance");
        check(hasMethod(c, "getIcon"), "method getIcon");
        check(hasMethod(c, "addActions"), "method addActions");
        check(hasMethod(c, "getAction"), "method getAction");
        check(hasMethod(c, "setInstance"), "method setInstance");
        check(hasMethod(c, "addAction"), "method addAction");
        check(hasMethod(c, "getDelegateAction"), "method getDelegateAction");
        check(hasMethod(c, "getStateChangeAction"), "method getStateChangeAction");
        check(hasMethod(c, "setActionEnabled"), "method setActionEnabled");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
