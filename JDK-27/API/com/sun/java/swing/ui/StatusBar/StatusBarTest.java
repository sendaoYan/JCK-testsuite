/*
 * @test
 * @summary com.sun.java.swing.ui.StatusBar conformance: internal package reflection.
 * @modules jdk.hotspot.agent/com.sun.java.swing.ui:+open
 * @run main StatusBarTest
 */
import java.lang.reflect.*;

public class StatusBarTest {
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
        Class<?> c = Class.forName("com.sun.java.swing.ui.StatusBar", false, StatusBarTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("javax.swing.JPanel".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 2, "interface count");
        check(hasInterface(c, "java.awt.event.ActionListener"), "interface java.awt.event.ActionListener");
        check(hasInterface(c, "java.awt.event.MouseListener"), "interface java.awt.event.MouseListener");
        check(hasMethod(c, "getInstance"), "method getInstance");
        check(hasMethod(c, "getWidth"), "method getWidth");
        check(hasMethod(c, "getPreferredSize"), "method getPreferredSize");
        check(hasMethod(c, "mousePressed"), "method mousePressed");
        check(hasMethod(c, "mouseReleased"), "method mouseReleased");
        check(hasMethod(c, "mouseClicked"), "method mouseClicked");
        check(hasMethod(c, "mouseExited"), "method mouseExited");
        check(hasMethod(c, "mouseEntered"), "method mouseEntered");
        check(hasMethod(c, "actionPerformed"), "method actionPerformed");
        check(hasMethod(c, "setInstance"), "method setInstance");
        check(hasMethod(c, "getFontHeight"), "method getFontHeight");
        check(hasMethod(c, "setMessage"), "method setMessage");
        check(hasMethod(c, "startBusyBar"), "method startBusyBar");
        check(hasMethod(c, "stopBusyBar"), "method stopBusyBar");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
