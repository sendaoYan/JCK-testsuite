/*
 * @test
 * @summary sun.jvm.hotspot.utilities.AbstractHeapGraphWriter conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.utilities:+open
 * @run main AbstractHeapGraphWriterTest
 */
import java.lang.reflect.*;

public class AbstractHeapGraphWriterTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.utilities.AbstractHeapGraphWriter", false, AbstractHeapGraphWriterTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 1, "interface count");
        check(hasInterface(c, "sun.jvm.hotspot.utilities.HeapGraphWriter"), "interface sun.jvm.hotspot.utilities.HeapGraphWriter");
        check(hasMethod(c, "writeObject"), "method writeObject");
        check(hasMethod(c, "write"), "method write");
        check(hasMethod(c, "writeString"), "method writeString");
        check(hasMethod(c, "writeHeapRecordPrologue"), "method writeHeapRecordPrologue");
        check(hasMethod(c, "writeJavaThreads"), "method writeJavaThreads");
        check(hasMethod(c, "writeGlobalJNIHandles"), "method writeGlobalJNIHandles");
        check(hasMethod(c, "writeStickyClasses"), "method writeStickyClasses");
        check(hasMethod(c, "handleRuntimeException"), "method handleRuntimeException");
        check(hasMethod(c, "writeJavaThread"), "method writeJavaThread");
        check(hasMethod(c, "writeInstance"), "method writeInstance");
        check(hasMethod(c, "writeObjectHeader"), "method writeObjectHeader");
        check(hasMethod(c, "writeObjectFields"), "method writeObjectFields");
        check(hasMethod(c, "writeObjectFooter"), "method writeObjectFooter");
        check(hasMethod(c, "calculateOopDumpRecordSize"), "method calculateOopDumpRecordSize");
        check(hasMethod(c, "writeGlobalJNIHandle"), "method writeGlobalJNIHandle");
        check(hasMethod(c, "writeHeapHeader"), "method writeHeapHeader");
        check(hasMethod(c, "writeInternalObject"), "method writeInternalObject");
        check(hasMethod(c, "writePrimitiveArray"), "method writePrimitiveArray");
        check(hasMethod(c, "writeObjectArray"), "method writeObjectArray");
        check(hasMethod(c, "writeClass"), "method writeClass");
        check(hasMethod(c, "writeThread"), "method writeThread");
        check(hasMethod(c, "writeReferenceField"), "method writeReferenceField");
        check(hasMethod(c, "writeByteField"), "method writeByteField");
        check(hasMethod(c, "writeCharField"), "method writeCharField");
        check(hasMethod(c, "writeBooleanField"), "method writeBooleanField");
        check(hasMethod(c, "writeShortField"), "method writeShortField");
        check(hasMethod(c, "writeIntField"), "method writeIntField");
        check(hasMethod(c, "writeLongField"), "method writeLongField");
        check(hasMethod(c, "writeFloatField"), "method writeFloatField");
        check(hasMethod(c, "writeDoubleField"), "method writeDoubleField");
        check(hasMethod(c, "writeHeapFooter"), "method writeHeapFooter");
        check(hasMethod(c, "writeHeapRecordEpilogue"), "method writeHeapRecordEpilogue");
        check(hasMethod(c, "isJavaVisible"), "method isJavaVisible");
        check(hasField(c, "javaLangClass"), "field javaLangClass");
        check(hasField(c, "javaLangString"), "field javaLangString");
        check(hasField(c, "javaLangThread"), "field javaLangThread");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
