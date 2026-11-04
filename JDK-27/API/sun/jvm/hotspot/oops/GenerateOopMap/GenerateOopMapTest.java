/*
 * @test
 * @summary sun.jvm.hotspot.oops.GenerateOopMap conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.oops:+open
 * @run main GenerateOopMapTest
 */
import java.lang.reflect.*;

public class GenerateOopMapTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.oops.GenerateOopMap", false, GenerateOopMapTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "method"), "method method");
        check(hasMethod(c, "maxLocals"), "method maxLocals");
        check(hasMethod(c, "computeMap"), "method computeMap");
        check(hasMethod(c, "possibleGCPoint"), "method possibleGCPoint");
        check(hasMethod(c, "fillStackmapForOpcodes"), "method fillStackmapForOpcodes");
        check(hasMethod(c, "fillStackmapProlog"), "method fillStackmapProlog");
        check(hasMethod(c, "fillStackmapEpilog"), "method fillStackmapEpilog");
        check(hasMethod(c, "fillInitVars"), "method fillInitVars");
        check(hasMethod(c, "reportResults"), "method reportResults");
        check(hasMethod(c, "resultForBasicblock"), "method resultForBasicblock");
        check(hasMethod(c, "monitorSafe"), "method monitorSafe");
        check(hasMethod(c, "getMonitorMatch"), "method getMonitorMatch");
        check(hasMethod(c, "allowRewrites"), "method allowRewrites");
        check(hasMethod(c, "reportInitVars"), "method reportInitVars");
        check(hasField(c, "bad_monitors"), "field bad_monitors");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
