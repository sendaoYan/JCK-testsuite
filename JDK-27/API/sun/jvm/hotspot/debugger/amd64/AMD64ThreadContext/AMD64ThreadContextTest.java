/*
 * @test
 * @summary sun.jvm.hotspot.debugger.amd64.AMD64ThreadContext conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.debugger.amd64:+open
 * @run main AMD64ThreadContextTest
 */
import java.lang.reflect.*;

public class AMD64ThreadContextTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.debugger.amd64.AMD64ThreadContext", false, AMD64ThreadContextTest.class.getClassLoader());
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
        check(hasField(c, "R15"), "field R15");
        check(hasField(c, "R14"), "field R14");
        check(hasField(c, "R13"), "field R13");
        check(hasField(c, "R12"), "field R12");
        check(hasField(c, "R11"), "field R11");
        check(hasField(c, "R10"), "field R10");
        check(hasField(c, "R9"), "field R9");
        check(hasField(c, "R8"), "field R8");
        check(hasField(c, "RDI"), "field RDI");
        check(hasField(c, "RSI"), "field RSI");
        check(hasField(c, "RBP"), "field RBP");
        check(hasField(c, "RBX"), "field RBX");
        check(hasField(c, "RDX"), "field RDX");
        check(hasField(c, "RCX"), "field RCX");
        check(hasField(c, "RAX"), "field RAX");
        check(hasField(c, "TRAPNO"), "field TRAPNO");
        check(hasField(c, "ERR"), "field ERR");
        check(hasField(c, "RIP"), "field RIP");
        check(hasField(c, "CS"), "field CS");
        check(hasField(c, "RFL"), "field RFL");
        check(hasField(c, "RSP"), "field RSP");
        check(hasField(c, "SS"), "field SS");
        check(hasField(c, "FS"), "field FS");
        check(hasField(c, "GS"), "field GS");
        check(hasField(c, "ES"), "field ES");
        check(hasField(c, "DS"), "field DS");
        check(hasField(c, "FSBASE"), "field FSBASE");
        check(hasField(c, "GSBASE"), "field GSBASE");
        check(hasField(c, "NPRGREG"), "field NPRGREG");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
