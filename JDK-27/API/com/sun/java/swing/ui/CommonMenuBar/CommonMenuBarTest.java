/*
 * @test
 * @summary com.sun.java.swing.ui.CommonMenuBar conformance: internal package reflection.
 * @modules jdk.hotspot.agent/com.sun.java.swing.ui:+open
 * @run main CommonMenuBarTest
 */
import java.lang.reflect.*;

public class CommonMenuBarTest {
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
        Class<?> c = Class.forName("com.sun.java.swing.ui.CommonMenuBar", false, CommonMenuBarTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("javax.swing.JMenuBar".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "configureMenu"), "method configureMenu");
        check(hasMethod(c, "configureMenuItem"), "method configureMenuItem");
        check(hasMethod(c, "addCheckBoxMenuItem"), "method addCheckBoxMenuItem");
        check(hasMethod(c, "configureToggleMenuItem"), "method configureToggleMenuItem");
        check(hasMethod(c, "addRadioButtonMenuItem"), "method addRadioButtonMenuItem");
        check(hasMethod(c, "createMenu"), "method createMenu");
        check(hasMethod(c, "addMenuItem"), "method addMenuItem");
        check(hasField(c, "manager"), "field manager");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
