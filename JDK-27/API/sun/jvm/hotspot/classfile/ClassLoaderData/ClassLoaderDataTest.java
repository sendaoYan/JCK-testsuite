/*
 * @test
 * @summary sun.jvm.hotspot.classfile.ClassLoaderData conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.classfile:+open
 * @run main ClassLoaderDataTest
 */
import java.lang.reflect.*;

public class ClassLoaderDataTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.classfile.ClassLoaderData", false, ClassLoaderDataTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("sun.jvm.hotspot.runtime.VMObject".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "next"), "method next");
        check(hasMethod(c, "getClassLoader"), "method getClassLoader");
        check(hasMethod(c, "find"), "method find");
        check(hasMethod(c, "instantiateWrapperFor"), "method instantiateWrapperFor");
        check(hasMethod(c, "getKlasses"), "method getKlasses");
        check(hasMethod(c, "gethasClassMirrorHolder"), "method gethasClassMirrorHolder");
        check(hasMethod(c, "theNullClassLoaderData"), "method theNullClassLoaderData");
        check(hasMethod(c, "classesDo"), "method classesDo");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
