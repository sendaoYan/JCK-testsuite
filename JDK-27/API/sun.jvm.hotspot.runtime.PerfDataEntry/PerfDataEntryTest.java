/*
 * @test
 * @summary sun.jvm.hotspot.runtime.PerfDataEntry conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.runtime:+open
 * @run main PerfDataEntryTest
 */
import java.lang.reflect.*;

public class PerfDataEntryTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.runtime.PerfDataEntry", false, PerfDataEntryTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("sun.jvm.hotspot.runtime.VMObject".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "name"), "method name");
        check(hasMethod(c, "flags"), "method flags");
        check(hasMethod(c, "booleanValue"), "method booleanValue");
        check(hasMethod(c, "byteValue"), "method byteValue");
        check(hasMethod(c, "charValue"), "method charValue");
        check(hasMethod(c, "shortValue"), "method shortValue");
        check(hasMethod(c, "intValue"), "method intValue");
        check(hasMethod(c, "longValue"), "method longValue");
        check(hasMethod(c, "floatValue"), "method floatValue");
        check(hasMethod(c, "doubleValue"), "method doubleValue");
        check(hasMethod(c, "valueAsString"), "method valueAsString");
        check(hasMethod(c, "dataOffset"), "method dataOffset");
        check(hasMethod(c, "nameOffset"), "method nameOffset");
        check(hasMethod(c, "vectorLength"), "method vectorLength");
        check(hasMethod(c, "dataType"), "method dataType");
        check(hasMethod(c, "booleanArrayValue"), "method booleanArrayValue");
        check(hasMethod(c, "charArrayValue"), "method charArrayValue");
        check(hasMethod(c, "shortArrayValue"), "method shortArrayValue");
        check(hasMethod(c, "intArrayValue"), "method intArrayValue");
        check(hasMethod(c, "longArrayValue"), "method longArrayValue");
        check(hasMethod(c, "floatArrayValue"), "method floatArrayValue");
        check(hasMethod(c, "doubleArrayValue"), "method doubleArrayValue");
        check(hasMethod(c, "dataUnits"), "method dataUnits");
        check(hasMethod(c, "entryLength"), "method entryLength");
        check(hasMethod(c, "supported"), "method supported");
        check(hasMethod(c, "dataVariability"), "method dataVariability");
        check(hasMethod(c, "byteArrayValue"), "method byteArrayValue");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
