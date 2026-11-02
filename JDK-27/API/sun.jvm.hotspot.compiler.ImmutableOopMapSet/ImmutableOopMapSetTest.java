/*
 * @test
 * @summary sun.jvm.hotspot.compiler.ImmutableOopMapSet conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.compiler:+open
 * @run main ImmutableOopMapSetTest
 */
import java.lang.reflect.*;

public class ImmutableOopMapSetTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.compiler.ImmutableOopMapSet", false, ImmutableOopMapSetTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("sun.jvm.hotspot.runtime.VMObject".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "toString"), "method toString");
        check(hasMethod(c, "getMap"), "method getMap");
        check(hasMethod(c, "getCount"), "method getCount");
        check(hasMethod(c, "oopsDo"), "method oopsDo");
        check(hasMethod(c, "findMapAtOffset"), "method findMapAtOffset");
        check(hasMethod(c, "pairAt"), "method pairAt");
        check(hasMethod(c, "getMapAt"), "method getMapAt");
        check(hasMethod(c, "allDo"), "method allDo");
        check(hasMethod(c, "getPairAt"), "method getPairAt");
        check(hasMethod(c, "updateRegisterMap"), "method updateRegisterMap");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
