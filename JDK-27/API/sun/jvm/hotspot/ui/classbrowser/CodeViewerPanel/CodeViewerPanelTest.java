/*
 * @test
 * @summary sun.jvm.hotspot.ui.classbrowser.CodeViewerPanel conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.ui.classbrowser:+open
 * @run main CodeViewerPanelTest
 */
import java.lang.reflect.*;

public class CodeViewerPanelTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.ui.classbrowser.CodeViewerPanel", false, CodeViewerPanelTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("javax.swing.JPanel".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "viewAddress"), "method viewAddress");
        check(hasField(c, "contentEditor"), "field contentEditor");
        check(hasField(c, "address"), "field address");
        check(hasField(c, "htmlGen"), "field htmlGen");
        check(hasField(c, "scrollPane"), "field scrollPane");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
