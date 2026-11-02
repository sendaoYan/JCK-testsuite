/*
 * @test
 * @summary sun.jvm.hotspot.code.ScopeDesc conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.code:+open
 * @run main ScopeDescTest
 */
import java.lang.reflect.*;

public class ScopeDescTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.code.ScopeDesc", false, ScopeDescTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "equals"), "method equals");
        check(hasMethod(c, "getMethod"), "method getMethod");
        check(hasMethod(c, "getMonitors"), "method getMonitors");
        check(hasMethod(c, "getLocals"), "method getLocals");
        check(hasMethod(c, "getBCI"), "method getBCI");
        check(hasMethod(c, "sender"), "method sender");
        check(hasMethod(c, "printValueOn"), "method printValueOn");
        check(hasMethod(c, "getExpressions"), "method getExpressions");
        check(hasMethod(c, "isTop"), "method isTop");
        check(hasMethod(c, "printValue"), "method printValue");
        check(hasMethod(c, "getReexecute"), "method getReexecute");
        check(hasMethod(c, "getNMethod"), "method getNMethod");
        check(hasMethod(c, "getObjects"), "method getObjects");
        check(hasMethod(c, "getDecodeOffset"), "method getDecodeOffset");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
