/*
 * @test
 * @summary sun.jvm.hotspot.debugger.win32.coff.WindowsNTSubsystem conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.debugger.win32.coff:+open
 * @run main WindowsNTSubsystemTest
 */
import java.lang.reflect.*;

public class WindowsNTSubsystemTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.debugger.win32.coff.WindowsNTSubsystem", false, WindowsNTSubsystemTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == true, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasField(c, "IMAGE_SUBSYSTEM_UNKNOWN"), "field IMAGE_SUBSYSTEM_UNKNOWN");
        check(hasField(c, "IMAGE_SUBSYSTEM_NATIVE"), "field IMAGE_SUBSYSTEM_NATIVE");
        check(hasField(c, "IMAGE_SUBSYSTEM_WINDOWS_GUI"), "field IMAGE_SUBSYSTEM_WINDOWS_GUI");
        check(hasField(c, "IMAGE_SUBSYSTEM_WINDOWS_CUI"), "field IMAGE_SUBSYSTEM_WINDOWS_CUI");
        check(hasField(c, "IMAGE_SUBSYSTEM_POSIX_CUI"), "field IMAGE_SUBSYSTEM_POSIX_CUI");
        check(hasField(c, "IMAGE_SUBSYSTEM_WINDOWS_CE_GUI"), "field IMAGE_SUBSYSTEM_WINDOWS_CE_GUI");
        check(hasField(c, "IMAGE_SUBSYSTEM_EFI_APPLICATION"), "field IMAGE_SUBSYSTEM_EFI_APPLICATION");
        check(hasField(c, "IMAGE_SUBSYSTEM_EFI_BOOT_SERVICE_DRIVER"), "field IMAGE_SUBSYSTEM_EFI_BOOT_SERVICE_DRIVER");
        check(hasField(c, "IMAGE_SUBSYSTEM_EFI_RUNTIME_DRIVER"), "field IMAGE_SUBSYSTEM_EFI_RUNTIME_DRIVER");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
