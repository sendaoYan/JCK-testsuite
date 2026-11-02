/*
 * @test
 * @summary sun.jvm.hotspot.ui.SAListener conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.ui:+open
 * @run main SAListenerTest
 */
import java.lang.reflect.*;

public class SAListenerTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.ui.SAListener", false, SAListenerTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == true, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "showInspector"), "method showInspector");
        check(hasMethod(c, "showThreadOopInspector"), "method showThreadOopInspector");
        check(hasMethod(c, "showLiveness"), "method showLiveness");
        check(hasMethod(c, "showThreadStackMemory"), "method showThreadStackMemory");
        check(hasMethod(c, "showThreadInfo"), "method showThreadInfo");
        check(hasMethod(c, "showJavaStackTrace"), "method showJavaStackTrace");
        check(hasMethod(c, "showCodeViewer"), "method showCodeViewer");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
