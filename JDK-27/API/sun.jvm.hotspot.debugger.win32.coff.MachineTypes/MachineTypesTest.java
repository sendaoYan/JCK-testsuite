/*
 * @test
 * @summary sun.jvm.hotspot.debugger.win32.coff.MachineTypes conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.debugger.win32.coff:+open
 * @run main MachineTypesTest
 */
import java.lang.reflect.*;

public class MachineTypesTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.debugger.win32.coff.MachineTypes", false, MachineTypesTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == true, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasField(c, "IMAGE_FILE_MACHINE_UNKNOWN"), "field IMAGE_FILE_MACHINE_UNKNOWN");
        check(hasField(c, "IMAGE_FILE_MACHINE_ALPHA"), "field IMAGE_FILE_MACHINE_ALPHA");
        check(hasField(c, "IMAGE_FILE_MACHINE_ARM"), "field IMAGE_FILE_MACHINE_ARM");
        check(hasField(c, "IMAGE_FILE_MACHINE_ALPHA64"), "field IMAGE_FILE_MACHINE_ALPHA64");
        check(hasField(c, "IMAGE_FILE_MACHINE_I386"), "field IMAGE_FILE_MACHINE_I386");
        check(hasField(c, "IMAGE_FILE_MACHINE_M68K"), "field IMAGE_FILE_MACHINE_M68K");
        check(hasField(c, "IMAGE_FILE_MACHINE_MIPS16"), "field IMAGE_FILE_MACHINE_MIPS16");
        check(hasField(c, "IMAGE_FILE_MACHINE_MIPSFPU"), "field IMAGE_FILE_MACHINE_MIPSFPU");
        check(hasField(c, "IMAGE_FILE_MACHINE_MIPSFPU16"), "field IMAGE_FILE_MACHINE_MIPSFPU16");
        check(hasField(c, "IMAGE_FILE_MACHINE_POWERPC"), "field IMAGE_FILE_MACHINE_POWERPC");
        check(hasField(c, "IMAGE_FILE_MACHINE_R3000"), "field IMAGE_FILE_MACHINE_R3000");
        check(hasField(c, "IMAGE_FILE_MACHINE_R4000"), "field IMAGE_FILE_MACHINE_R4000");
        check(hasField(c, "IMAGE_FILE_MACHINE_R10000"), "field IMAGE_FILE_MACHINE_R10000");
        check(hasField(c, "IMAGE_FILE_MACHINE_SH3"), "field IMAGE_FILE_MACHINE_SH3");
        check(hasField(c, "IMAGE_FILE_MACHINE_SH4"), "field IMAGE_FILE_MACHINE_SH4");
        check(hasField(c, "IMAGE_FILE_MACHINE_THUMB"), "field IMAGE_FILE_MACHINE_THUMB");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
