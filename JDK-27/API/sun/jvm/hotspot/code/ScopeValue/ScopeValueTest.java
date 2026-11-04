/*
 * @test
 * @summary sun.jvm.hotspot.code.ScopeValue conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.code:+open
 * @run main ScopeValueTest
 */
import java.lang.reflect.*;

public class ScopeValueTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.code.ScopeValue", false, ScopeValueTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "toString"), "method toString");
        check(hasMethod(c, "isObject"), "method isObject");
        check(hasMethod(c, "readFrom"), "method readFrom");
        check(hasMethod(c, "printOn"), "method printOn");
        check(hasMethod(c, "isConstantDouble"), "method isConstantDouble");
        check(hasMethod(c, "isLocation"), "method isLocation");
        check(hasMethod(c, "isConstantInt"), "method isConstantInt");
        check(hasMethod(c, "isConstantLong"), "method isConstantLong");
        check(hasMethod(c, "isConstantOop"), "method isConstantOop");
        check(hasMethod(c, "isMarker"), "method isMarker");
        check(hasMethod(c, "isObjectMerge"), "method isObjectMerge");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
