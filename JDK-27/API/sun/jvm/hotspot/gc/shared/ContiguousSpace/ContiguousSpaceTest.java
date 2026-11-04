/*
 * @test
 * @summary sun.jvm.hotspot.gc.shared.ContiguousSpace conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.gc.shared:+open
 * @run main ContiguousSpaceTest
 */
import java.lang.reflect.*;

public class ContiguousSpaceTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.gc.shared.ContiguousSpace", false, ContiguousSpaceTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("sun.jvm.hotspot.runtime.VMObject".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 1, "interface count");
        check(hasInterface(c, "sun.jvm.hotspot.gc.shared.LiveRegionsProvider"), "interface sun.jvm.hotspot.gc.shared.LiveRegionsProvider");
        check(hasMethod(c, "bottom"), "method bottom");
        check(hasMethod(c, "contains"), "method contains");
        check(hasMethod(c, "end"), "method end");
        check(hasMethod(c, "print"), "method print");
        check(hasMethod(c, "capacity"), "method capacity");
        check(hasMethod(c, "top"), "method top");
        check(hasMethod(c, "free"), "method free");
        check(hasMethod(c, "used"), "method used");
        check(hasMethod(c, "printOn"), "method printOn");
        check(hasMethod(c, "getLiveRegions"), "method getLiveRegions");
        check(hasMethod(c, "bottomAsOopHandle"), "method bottomAsOopHandle");
        check(hasMethod(c, "nextOopHandle"), "method nextOopHandle");
        check(hasMethod(c, "usedRegion"), "method usedRegion");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
