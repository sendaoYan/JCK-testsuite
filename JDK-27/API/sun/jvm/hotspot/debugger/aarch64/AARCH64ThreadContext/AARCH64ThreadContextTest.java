/*
 * @test
 * @summary sun.jvm.hotspot.debugger.aarch64.AARCH64ThreadContext conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.debugger.aarch64:+open
 * @run main AARCH64ThreadContextTest
 */
import java.lang.reflect.*;

public class AARCH64ThreadContextTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.debugger.aarch64.AARCH64ThreadContext", false, AARCH64ThreadContextTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 1, "interface count");
        check(hasInterface(c, "sun.jvm.hotspot.debugger.ThreadContext"), "interface sun.jvm.hotspot.debugger.ThreadContext");
        check(hasMethod(c, "getNumRegisters"), "method getNumRegisters");
        check(hasMethod(c, "getRegisterAsAddress"), "method getRegisterAsAddress");
        check(hasMethod(c, "getRegisterName"), "method getRegisterName");
        check(hasMethod(c, "getRegister"), "method getRegister");
        check(hasMethod(c, "setRegister"), "method setRegister");
        check(hasMethod(c, "setRegisterAsAddress"), "method setRegisterAsAddress");
        check(hasMethod(c, "getTopFrame"), "method getTopFrame");
        check(hasField(c, "R0"), "field R0");
        check(hasField(c, "R1"), "field R1");
        check(hasField(c, "R2"), "field R2");
        check(hasField(c, "R3"), "field R3");
        check(hasField(c, "R4"), "field R4");
        check(hasField(c, "R5"), "field R5");
        check(hasField(c, "R6"), "field R6");
        check(hasField(c, "R7"), "field R7");
        check(hasField(c, "R8"), "field R8");
        check(hasField(c, "R9"), "field R9");
        check(hasField(c, "R10"), "field R10");
        check(hasField(c, "R11"), "field R11");
        check(hasField(c, "R12"), "field R12");
        check(hasField(c, "R13"), "field R13");
        check(hasField(c, "R14"), "field R14");
        check(hasField(c, "R15"), "field R15");
        check(hasField(c, "R16"), "field R16");
        check(hasField(c, "R17"), "field R17");
        check(hasField(c, "R18"), "field R18");
        check(hasField(c, "R19"), "field R19");
        check(hasField(c, "R20"), "field R20");
        check(hasField(c, "R21"), "field R21");
        check(hasField(c, "R22"), "field R22");
        check(hasField(c, "R23"), "field R23");
        check(hasField(c, "R24"), "field R24");
        check(hasField(c, "R25"), "field R25");
        check(hasField(c, "R26"), "field R26");
        check(hasField(c, "R27"), "field R27");
        check(hasField(c, "R28"), "field R28");
        check(hasField(c, "FP"), "field FP");
        check(hasField(c, "LR"), "field LR");
        check(hasField(c, "SP"), "field SP");
        check(hasField(c, "PC"), "field PC");
        check(hasField(c, "PSTATE"), "field PSTATE");
        check(hasField(c, "NPRGREG"), "field NPRGREG");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
