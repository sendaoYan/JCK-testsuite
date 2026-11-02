/*
 * @test
 * @summary sun.jvm.hotspot.debugger.win32.coff.ExportDirectoryTable conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.debugger.win32.coff:+open
 * @run main ExportDirectoryTableTest
 */
import java.lang.reflect.*;

public class ExportDirectoryTableTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.debugger.win32.coff.ExportDirectoryTable", false, ExportDirectoryTableTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == true, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "getMajorVersion"), "method getMajorVersion");
        check(hasMethod(c, "getMinorVersion"), "method getMinorVersion");
        check(hasMethod(c, "getTimeDateStamp"), "method getTimeDateStamp");
        check(hasMethod(c, "getDLLName"), "method getDLLName");
        check(hasMethod(c, "getNumberOfNamePointers"), "method getNumberOfNamePointers");
        check(hasMethod(c, "getExportOrdinal"), "method getExportOrdinal");
        check(hasMethod(c, "getExportName"), "method getExportName");
        check(hasMethod(c, "getExportAddress"), "method getExportAddress");
        check(hasMethod(c, "isExportAddressForwarder"), "method isExportAddressForwarder");
        check(hasMethod(c, "getExportAddressForwarder"), "method getExportAddressForwarder");
        check(hasMethod(c, "getExportFlags"), "method getExportFlags");
        check(hasMethod(c, "getNameRVA"), "method getNameRVA");
        check(hasMethod(c, "getOrdinalBase"), "method getOrdinalBase");
        check(hasMethod(c, "getNumberOfAddressTableEntries"), "method getNumberOfAddressTableEntries");
        check(hasMethod(c, "getExportAddressTableRVA"), "method getExportAddressTableRVA");
        check(hasMethod(c, "getNamePointerTableRVA"), "method getNamePointerTableRVA");
        check(hasMethod(c, "getOrdinalTableRVA"), "method getOrdinalTableRVA");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
