/*
 * @test
 * @summary sun.jvm.hotspot.utilities.HeapHprofBinWriter conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.utilities:+open
 * @run main HeapHprofBinWriterTest
 */
import java.lang.reflect.*;

public class HeapHprofBinWriterTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.utilities.HeapHprofBinWriter", false, HeapHprofBinWriterTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("sun.jvm.hotspot.utilities.AbstractHeapGraphWriter".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "write"), "method write");
        check(hasMethod(c, "writeHeapRecordPrologue"), "method writeHeapRecordPrologue");
        check(hasMethod(c, "writeStickyClasses"), "method writeStickyClasses");
        check(hasMethod(c, "writeJavaThread"), "method writeJavaThread");
        check(hasMethod(c, "writeInstance"), "method writeInstance");
        check(hasMethod(c, "calculateOopDumpRecordSize"), "method calculateOopDumpRecordSize");
        check(hasMethod(c, "writeGlobalJNIHandle"), "method writeGlobalJNIHandle");
        check(hasMethod(c, "writePrimitiveArray"), "method writePrimitiveArray");
        check(hasMethod(c, "writeObjectArray"), "method writeObjectArray");
        check(hasMethod(c, "writeClass"), "method writeClass");
        check(hasMethod(c, "writeLocalJNIHandles"), "method writeLocalJNIHandles");
        check(hasMethod(c, "writeStackRefs"), "method writeStackRefs");
        check(hasMethod(c, "signatureToHprofKind"), "method signatureToHprofKind");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
