/*
 * @test
 * @summary sun.jvm.hotspot.runtime.JNIHandleBlock conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.runtime:+open
 * @run main JNIHandleBlockTest
 */
import java.lang.reflect.*;

public class JNIHandleBlockTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.runtime.JNIHandleBlock", false, JNIHandleBlockTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("sun.jvm.hotspot.runtime.VMObject".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "toString"), "method toString");
        check(hasMethod(c, "next"), "method next");
        check(hasMethod(c, "top"), "method top");
        check(hasMethod(c, "oopsDo"), "method oopsDo");
        check(hasMethod(c, "getOopHandle"), "method getOopHandle");
        check(hasMethod(c, "indexOfHandle"), "method indexOfHandle");
        check(hasMethod(c, "blockContainingHandle"), "method blockContainingHandle");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
