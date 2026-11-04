/*
 * @test
 * @summary sun.jvm.hotspot.oops.AccessFlags conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.oops:+open
 * @run main AccessFlagsTest
 */
import java.lang.reflect.*;

public class AccessFlagsTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.oops.AccessFlags", false, AccessFlagsTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 1, "interface count");
        check(hasInterface(c, "sun.jvm.hotspot.runtime.ClassConstants"), "interface sun.jvm.hotspot.runtime.ClassConstants");
        check(hasMethod(c, "getValue"), "method getValue");
        check(hasMethod(c, "isInterface"), "method isInterface");
        check(hasMethod(c, "isAbstract"), "method isAbstract");
        check(hasMethod(c, "isStatic"), "method isStatic");
        check(hasMethod(c, "isFinal"), "method isFinal");
        check(hasMethod(c, "isAnnotation"), "method isAnnotation");
        check(hasMethod(c, "isEnum"), "method isEnum");
        check(hasMethod(c, "isPublic"), "method isPublic");
        check(hasMethod(c, "isSynthetic"), "method isSynthetic");
        check(hasMethod(c, "isProtected"), "method isProtected");
        check(hasMethod(c, "isTransient"), "method isTransient");
        check(hasMethod(c, "isVolatile"), "method isVolatile");
        check(hasMethod(c, "isVarArgs"), "method isVarArgs");
        check(hasMethod(c, "isSynchronized"), "method isSynchronized");
        check(hasMethod(c, "isNative"), "method isNative");
        check(hasMethod(c, "isStrict"), "method isStrict");
        check(hasMethod(c, "isBridge"), "method isBridge");
        check(hasMethod(c, "isPrivate"), "method isPrivate");
        check(hasMethod(c, "isSuper"), "method isSuper");
        check(hasMethod(c, "printOn"), "method printOn");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
