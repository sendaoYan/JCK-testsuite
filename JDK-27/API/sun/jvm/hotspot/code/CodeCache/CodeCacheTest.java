/*
 * @test
 * @summary sun.jvm.hotspot.code.CodeCache conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.code:+open
 * @run main CodeCacheTest
 */
import java.lang.reflect.*;

public class CodeCacheTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.code.CodeCache", false, CodeCacheTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "contains"), "method contains");
        check(hasMethod(c, "iterate"), "method iterate");
        check(hasMethod(c, "findBlobUnsafe"), "method findBlobUnsafe");
        check(hasMethod(c, "findBlob"), "method findBlob");
        check(hasMethod(c, "createCodeBlobWrapper"), "method createCodeBlobWrapper");
        check(hasMethod(c, "findNMethod"), "method findNMethod");
        check(hasMethod(c, "findNMethodUnsafe"), "method findNMethodUnsafe");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
