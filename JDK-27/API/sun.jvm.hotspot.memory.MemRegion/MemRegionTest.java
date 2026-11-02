/*
 * @test
 * @summary sun.jvm.hotspot.memory.MemRegion conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.memory:+open
 * @run main MemRegionTest
 */
import java.lang.reflect.*;

public class MemRegionTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.memory.MemRegion", false, MemRegionTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 1, "interface count");
        check(hasInterface(c, "java.lang.Cloneable"), "interface java.lang.Cloneable");
        check(hasMethod(c, "clone"), "method clone");
        check(hasMethod(c, "copy"), "method copy");
        check(hasMethod(c, "contains"), "method contains");
        check(hasMethod(c, "end"), "method end");
        check(hasMethod(c, "start"), "method start");
        check(hasMethod(c, "union"), "method union");
        check(hasMethod(c, "setStart"), "method setStart");
        check(hasMethod(c, "intersection"), "method intersection");
        check(hasMethod(c, "setWordSize"), "method setWordSize");
        check(hasMethod(c, "byteSize"), "method byteSize");
        check(hasMethod(c, "setEnd"), "method setEnd");
        check(hasMethod(c, "wordSize"), "method wordSize");
        check(hasMethod(c, "startAsOopHandle"), "method startAsOopHandle");
        check(hasMethod(c, "endAsOopHandle"), "method endAsOopHandle");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
