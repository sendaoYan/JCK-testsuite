/*
 * @test
 * @summary sun.jvm.hotspot.debugger.Page conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.debugger:+open
 * @run main PageTest
 */
import java.lang.reflect.*;

public class PageTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.debugger.Page", false, PageTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "getBoolean"), "method getBoolean");
        check(hasMethod(c, "getByte"), "method getByte");
        check(hasMethod(c, "getShort"), "method getShort");
        check(hasMethod(c, "getChar"), "method getChar");
        check(hasMethod(c, "getInt"), "method getInt");
        check(hasMethod(c, "getLong"), "method getLong");
        check(hasMethod(c, "getFloat"), "method getFloat");
        check(hasMethod(c, "getDouble"), "method getDouble");
        check(hasMethod(c, "getSize"), "method getSize");
        check(hasMethod(c, "getData"), "method getData");
        check(hasMethod(c, "getNext"), "method getNext");
        check(hasMethod(c, "getPrev"), "method getPrev");
        check(hasMethod(c, "getDataAsBytes"), "method getDataAsBytes");
        check(hasMethod(c, "getBaseAddress"), "method getBaseAddress");
        check(hasMethod(c, "isMapped"), "method isMapped");
        check(hasMethod(c, "setNext"), "method setNext");
        check(hasMethod(c, "setPrev"), "method setPrev");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
