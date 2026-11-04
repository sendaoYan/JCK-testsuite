/*
 * @test
 * @summary sun.jvm.hotspot.debugger.win32.coff.Characteristics conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.debugger.win32.coff:+open
 * @run main CharacteristicsTest
 */
import java.lang.reflect.*;

public class CharacteristicsTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.debugger.win32.coff.Characteristics", false, CharacteristicsTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == true, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasField(c, "IMAGE_FILE_RELOCS_STRIPPED"), "field IMAGE_FILE_RELOCS_STRIPPED");
        check(hasField(c, "IMAGE_FILE_EXECUTABLE_IMAGE"), "field IMAGE_FILE_EXECUTABLE_IMAGE");
        check(hasField(c, "IMAGE_FILE_LINE_NUMS_STRIPPED"), "field IMAGE_FILE_LINE_NUMS_STRIPPED");
        check(hasField(c, "IMAGE_FILE_LOCAL_SYMS_STRIPPED"), "field IMAGE_FILE_LOCAL_SYMS_STRIPPED");
        check(hasField(c, "IMAGE_FILE_AGGRESSIVE_WS_TRIM"), "field IMAGE_FILE_AGGRESSIVE_WS_TRIM");
        check(hasField(c, "IMAGE_FILE_LARGE_ADDRESS_AWARE"), "field IMAGE_FILE_LARGE_ADDRESS_AWARE");
        check(hasField(c, "IMAGE_FILE_16BIT_MACHINE"), "field IMAGE_FILE_16BIT_MACHINE");
        check(hasField(c, "IMAGE_FILE_BYTES_REVERSED_LO"), "field IMAGE_FILE_BYTES_REVERSED_LO");
        check(hasField(c, "IMAGE_FILE_32BIT_MACHINE"), "field IMAGE_FILE_32BIT_MACHINE");
        check(hasField(c, "IMAGE_FILE_DEBUG_STRIPPED"), "field IMAGE_FILE_DEBUG_STRIPPED");
        check(hasField(c, "IMAGE_FILE_REMOVABLE_RUN_FROM_SWAP"), "field IMAGE_FILE_REMOVABLE_RUN_FROM_SWAP");
        check(hasField(c, "IMAGE_FILE_SYSTEM"), "field IMAGE_FILE_SYSTEM");
        check(hasField(c, "IMAGE_FILE_DLL"), "field IMAGE_FILE_DLL");
        check(hasField(c, "IMAGE_FILE_UP_SYSTEM_ONLY"), "field IMAGE_FILE_UP_SYSTEM_ONLY");
        check(hasField(c, "IMAGE_FILE_BYTES_REVERSED_HI"), "field IMAGE_FILE_BYTES_REVERSED_HI");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
