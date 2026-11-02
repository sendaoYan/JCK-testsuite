/*
 * @test
 * @summary sun.jvm.hotspot.HSDB conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot:+open
 * @run main HSDBTest
 */
import java.lang.reflect.*;

public class HSDBTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.HSDB", false, HSDBTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 2, "interface count");
        check(hasInterface(c, "sun.jvm.hotspot.ui.ObjectHistogramPanel$Listener"), "interface sun.jvm.hotspot.ui.ObjectHistogramPanel$Listener");
        check(hasInterface(c, "sun.jvm.hotspot.ui.SAListener"), "interface sun.jvm.hotspot.ui.SAListener");
        check(hasMethod(c, "main"), "method main");
        check(hasMethod(c, "run"), "method run");
        check(hasMethod(c, "showInspector"), "method showInspector");
        check(hasMethod(c, "showThreadOopInspector"), "method showThreadOopInspector");
        check(hasMethod(c, "showLiveness"), "method showLiveness");
        check(hasMethod(c, "showThreadStackMemory"), "method showThreadStackMemory");
        check(hasMethod(c, "showObjectsOfType"), "method showObjectsOfType");
        check(hasMethod(c, "showThreadInfo"), "method showThreadInfo");
        check(hasMethod(c, "showJavaStackTrace"), "method showJavaStackTrace");
        check(hasMethod(c, "showClassBrowser"), "method showClassBrowser");
        check(hasMethod(c, "showCodeViewer"), "method showCodeViewer");
        check(hasMethod(c, "showMemoryViewer"), "method showMemoryViewer");
        check(hasMethod(c, "showAnnotatedMemoryViewer"), "method showAnnotatedMemoryViewer");
        check(hasMethod(c, "showCommandLineFlags"), "method showCommandLineFlags");
        check(hasMethod(c, "showVMVersion"), "method showVMVersion");
        check(hasMethod(c, "showSystemProperties"), "method showSystemProperties");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
