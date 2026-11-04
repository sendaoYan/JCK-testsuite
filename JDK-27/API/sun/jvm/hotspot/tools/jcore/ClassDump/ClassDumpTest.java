/*
 * @test
 * @summary sun.jvm.hotspot.tools.jcore.ClassDump conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.tools.jcore:+open
 * @run main ClassDumpTest
 */
import java.lang.reflect.*;

public class ClassDumpTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.tools.jcore.ClassDump", false, ClassDumpTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("sun.jvm.hotspot.tools.Tool".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "main"), "method main");
        check(hasMethod(c, "run"), "method run");
        check(hasMethod(c, "getName"), "method getName");
        check(hasMethod(c, "setOutputDirectory"), "method setOutputDirectory");
        check(hasMethod(c, "setClassFilter"), "method setClassFilter");
        check(hasMethod(c, "setJarOutput"), "method setJarOutput");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
