/*
 * @test
 * @summary sun.jvm.hotspot.runtime.RegisterMap conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.runtime:+open
 * @run main RegisterMapTest
 */
import java.lang.reflect.*;

public class RegisterMapTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.runtime.RegisterMap", false, RegisterMapTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 1, "interface count");
        check(hasInterface(c, "java.lang.Cloneable"), "interface java.lang.Cloneable");
        check(hasMethod(c, "clone"), "method clone");
        check(hasMethod(c, "clear"), "method clear");
        check(hasMethod(c, "copy"), "method copy");
        check(hasMethod(c, "getLocation"), "method getLocation");
        check(hasMethod(c, "print"), "method print");
        check(hasMethod(c, "setLocation"), "method setLocation");
        check(hasMethod(c, "printOn"), "method printOn");
        check(hasMethod(c, "getThread"), "method getThread");
        check(hasMethod(c, "setIncludeArgumentOops"), "method setIncludeArgumentOops");
        check(hasMethod(c, "getIncludeArgumentOops"), "method getIncludeArgumentOops");
        check(hasMethod(c, "getUpdateMap"), "method getUpdateMap");
        check(hasMethod(c, "initializeFromPD"), "method initializeFromPD");
        check(hasMethod(c, "clearPD"), "method clearPD");
        check(hasMethod(c, "initializePD"), "method initializePD");
        check(hasMethod(c, "getLocationPD"), "method getLocationPD");
        check(hasField(c, "location"), "field location");
        check(hasField(c, "locationValid"), "field locationValid");
        check(hasField(c, "includeArgumentOops"), "field includeArgumentOops");
        check(hasField(c, "thread"), "field thread");
        check(hasField(c, "updateMap"), "field updateMap");
        check(hasField(c, "regCount"), "field regCount");
        check(hasField(c, "locationValidTypeSize"), "field locationValidTypeSize");
        check(hasField(c, "locationValidSize"), "field locationValidSize");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
