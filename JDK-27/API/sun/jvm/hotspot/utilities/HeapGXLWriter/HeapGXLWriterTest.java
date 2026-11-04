/*
 * @test
 * @summary sun.jvm.hotspot.utilities.HeapGXLWriter conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.utilities:+open
 * @run main HeapGXLWriterTest
 */
import java.lang.reflect.*;

public class HeapGXLWriterTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.utilities.HeapGXLWriter", false, HeapGXLWriterTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("sun.jvm.hotspot.utilities.AbstractHeapGraphWriter".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "write"), "method write");
        check(hasMethod(c, "writeObjectHeader"), "method writeObjectHeader");
        check(hasMethod(c, "writeObjectFooter"), "method writeObjectFooter");
        check(hasMethod(c, "calculateOopDumpRecordSize"), "method calculateOopDumpRecordSize");
        check(hasMethod(c, "writeHeapHeader"), "method writeHeapHeader");
        check(hasMethod(c, "writePrimitiveArray"), "method writePrimitiveArray");
        check(hasMethod(c, "writeObjectArray"), "method writeObjectArray");
        check(hasMethod(c, "writeClass"), "method writeClass");
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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
