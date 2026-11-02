/*
 * @test
 * @summary sun.jvm.hotspot.debugger.posix.elf.ELFFile conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.debugger.posix.elf:+open
 * @run main ELFFileTest
 */
import java.lang.reflect.*;

public class ELFFileTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.debugger.posix.elf.ELFFile", false, ELFFileTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == true, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "close"), "method close");
        check(hasMethod(c, "getEncoding"), "method getEncoding");
        check(hasMethod(c, "getVersion"), "method getVersion");
        check(hasMethod(c, "getHeader"), "method getHeader");
        check(hasMethod(c, "getObjectSize"), "method getObjectSize");
        check(hasMethod(c, "getMagicNumber"), "method getMagicNumber");
        check(hasField(c, "ELF_MAGIC_NUMBER"), "field ELF_MAGIC_NUMBER");
        check(hasField(c, "CLASS_INVALID"), "field CLASS_INVALID");
        check(hasField(c, "CLASS_32"), "field CLASS_32");
        check(hasField(c, "CLASS_64"), "field CLASS_64");
        check(hasField(c, "DATA_INVALID"), "field DATA_INVALID");
        check(hasField(c, "DATA_LSB"), "field DATA_LSB");
        check(hasField(c, "DATA_MSB"), "field DATA_MSB");
        check(hasField(c, "VERSION_INVALID"), "field VERSION_INVALID");
        check(hasField(c, "VERSION_CURRENT"), "field VERSION_CURRENT");
        check(hasField(c, "NDX_MAGIC_0"), "field NDX_MAGIC_0");
        check(hasField(c, "NDX_MAGIC_1"), "field NDX_MAGIC_1");
        check(hasField(c, "NDX_MAGIC_2"), "field NDX_MAGIC_2");
        check(hasField(c, "NDX_MAGIC_3"), "field NDX_MAGIC_3");
        check(hasField(c, "NDX_OBJECT_SIZE"), "field NDX_OBJECT_SIZE");
        check(hasField(c, "NDX_ENCODING"), "field NDX_ENCODING");
        check(hasField(c, "NDX_VERSION"), "field NDX_VERSION");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
