/*
 * @test
 * @summary sun.jvm.hotspot.gc.shared.G1HeapRegionType conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.gc.shared:+open
 * @run main G1HeapRegionTypeTest
 */
import java.lang.reflect.*;

public class G1HeapRegionTypeTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.gc.shared.G1HeapRegionType", false, G1HeapRegionTypeTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == true, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check("java.lang.Enum".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "value"), "method value");
        check(hasMethod(c, "values"), "method values");
        check(hasMethod(c, "valueOf"), "method valueOf");
        check(hasField(c, "Free"), "field Free");
        check(hasField(c, "Eden"), "field Eden");
        check(hasField(c, "Survivor"), "field Survivor");
        check(hasField(c, "StartsHumongous"), "field StartsHumongous");
        check(hasField(c, "ContinuesHumongous"), "field ContinuesHumongous");
        check(hasField(c, "Old"), "field Old");
        check(hasField(c, "Archive"), "field Archive");
        check(hasField(c, "G1HeapRegionTypeEndSentinel"), "field G1HeapRegionTypeEndSentinel");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
