/*
 * @test
 * @summary sun.jvm.hotspot.debugger.win32.coff.OptionalHeaderDataDirectories conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.debugger.win32.coff:+open
 * @run main OptionalHeaderDataDirectoriesTest
 */
import java.lang.reflect.*;

public class OptionalHeaderDataDirectoriesTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.debugger.win32.coff.OptionalHeaderDataDirectories", false, OptionalHeaderDataDirectoriesTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == true, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "getExceptionTable"), "method getExceptionTable");
        check(hasMethod(c, "getExportTable"), "method getExportTable");
        check(hasMethod(c, "getExportDirectoryTable"), "method getExportDirectoryTable");
        check(hasMethod(c, "getImportTable"), "method getImportTable");
        check(hasMethod(c, "getResourceTable"), "method getResourceTable");
        check(hasMethod(c, "getCertificateTable"), "method getCertificateTable");
        check(hasMethod(c, "getBaseRelocationTable"), "method getBaseRelocationTable");
        check(hasMethod(c, "getDebug"), "method getDebug");
        check(hasMethod(c, "getDebugDirectory"), "method getDebugDirectory");
        check(hasMethod(c, "getArchitecture"), "method getArchitecture");
        check(hasMethod(c, "getGlobalPtr"), "method getGlobalPtr");
        check(hasMethod(c, "getTLSTable"), "method getTLSTable");
        check(hasMethod(c, "getLoadConfigTable"), "method getLoadConfigTable");
        check(hasMethod(c, "getBoundImportTable"), "method getBoundImportTable");
        check(hasMethod(c, "getImportAddressTable"), "method getImportAddressTable");
        check(hasMethod(c, "getDelayImportDescriptor"), "method getDelayImportDescriptor");
        check(hasMethod(c, "getCOMPlusRuntimeHeader"), "method getCOMPlusRuntimeHeader");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
