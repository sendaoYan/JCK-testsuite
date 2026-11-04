/*
 * @test
 * @summary sun.jvm.hotspot.interpreter.BytecodeStream conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.interpreter:+open
 * @run main BytecodeStreamTest
 */
import java.lang.reflect.*;

public class BytecodeStreamTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.interpreter.BytecodeStream", false, BytecodeStreamTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "method"), "method method");
        check(hasMethod(c, "next"), "method next");
        check(hasMethod(c, "code"), "method code");
        check(hasMethod(c, "getIndex"), "method getIndex");
        check(hasMethod(c, "bci"), "method bci");
        check(hasMethod(c, "dest"), "method dest");
        check(hasMethod(c, "isWide"), "method isWide");
        check(hasMethod(c, "getIndexU1"), "method getIndexU1");
        check(hasMethod(c, "getIndexU2"), "method getIndexU2");
        check(hasMethod(c, "setStart"), "method setStart");
        check(hasMethod(c, "codeAt"), "method codeAt");
        check(hasMethod(c, "getIndexU4"), "method getIndexU4");
        check(hasMethod(c, "hasIndexU4"), "method hasIndexU4");
        check(hasMethod(c, "getIndexU1Cpcache"), "method getIndexU1Cpcache");
        check(hasMethod(c, "getIndexU2Cpcache"), "method getIndexU2Cpcache");
        check(hasMethod(c, "setInterval"), "method setInterval");
        check(hasMethod(c, "isLastBytecode"), "method isLastBytecode");
        check(hasMethod(c, "nextBCI"), "method nextBCI");
        check(hasMethod(c, "endBCI"), "method endBCI");
        check(hasMethod(c, "isActiveBreakpoint"), "method isActiveBreakpoint");
        check(hasMethod(c, "setNextBCI"), "method setNextBCI");
        check(hasMethod(c, "dest_w"), "method dest_w");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
