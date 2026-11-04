/*
 * @test
 * @summary sun.jvm.hotspot.gc.shared.GCName conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.gc.shared:+open
 * @run main GCNameTest
 */
import java.lang.reflect.*;

public class GCNameTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.gc.shared.GCName", false, GCNameTest.class.getClassLoader());
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
        check(hasField(c, "ParallelOld"), "field ParallelOld");
        check(hasField(c, "SerialOld"), "field SerialOld");
        check(hasField(c, "PSMarkSweep"), "field PSMarkSweep");
        check(hasField(c, "ParallelScavenge"), "field ParallelScavenge");
        check(hasField(c, "DefNew"), "field DefNew");
        check(hasField(c, "G1New"), "field G1New");
        check(hasField(c, "G1Old"), "field G1Old");
        check(hasField(c, "G1Full"), "field G1Full");
        check(hasField(c, "Z"), "field Z");
        check(hasField(c, "Shenandoah"), "field Shenandoah");
        check(hasField(c, "NA"), "field NA");
        check(hasField(c, "GCNameEndSentinel"), "field GCNameEndSentinel");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
