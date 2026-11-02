/*
 * @test
 * @summary sun.jvm.hotspot.debugger.DebuggerBase conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.debugger:+open
 * @run main DebuggerBaseTest
 */
import java.lang.reflect.*;

public class DebuggerBaseTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.debugger.DebuggerBase", false, DebuggerBaseTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 1, "interface count");
        check(hasInterface(c, "sun.jvm.hotspot.debugger.Debugger"), "interface sun.jvm.hotspot.debugger.Debugger");
        check(hasMethod(c, "readBytes"), "method readBytes");
        check(hasMethod(c, "initCache"), "method initCache");
        check(hasMethod(c, "clearCache"), "method clearCache");
        check(hasMethod(c, "configureJavaPrimitiveTypeSizes"), "method configureJavaPrimitiveTypeSizes");
        check(hasMethod(c, "putHeapConst"), "method putHeapConst");
        check(hasMethod(c, "findSymbol"), "method findSymbol");
        check(hasMethod(c, "getJBooleanSize"), "method getJBooleanSize");
        check(hasMethod(c, "getJByteSize"), "method getJByteSize");
        check(hasMethod(c, "getJCharSize"), "method getJCharSize");
        check(hasMethod(c, "getJDoubleSize"), "method getJDoubleSize");
        check(hasMethod(c, "getJFloatSize"), "method getJFloatSize");
        check(hasMethod(c, "getJIntSize"), "method getJIntSize");
        check(hasMethod(c, "getJLongSize"), "method getJLongSize");
        check(hasMethod(c, "getJShortSize"), "method getJShortSize");
        check(hasMethod(c, "getHeapOopSize"), "method getHeapOopSize");
        check(hasMethod(c, "getNarrowOopBase"), "method getNarrowOopBase");
        check(hasMethod(c, "getNarrowOopShift"), "method getNarrowOopShift");
        check(hasMethod(c, "getKlassPtrSize"), "method getKlassPtrSize");
        check(hasMethod(c, "getNarrowKlassBase"), "method getNarrowKlassBase");
        check(hasMethod(c, "getNarrowKlassShift"), "method getNarrowKlassShift");
        check(hasMethod(c, "checkJavaConfigured"), "method checkJavaConfigured");
        check(hasMethod(c, "checkConfigured"), "method checkConfigured");
        check(hasMethod(c, "readCInteger"), "method readCInteger");
        check(hasMethod(c, "setBigEndian"), "method setBigEndian");
        check(hasMethod(c, "disableCache"), "method disableCache");
        check(hasMethod(c, "enableCache"), "method enableCache");
        check(hasMethod(c, "readJBoolean"), "method readJBoolean");
        check(hasMethod(c, "readJByte"), "method readJByte");
        check(hasMethod(c, "readJChar"), "method readJChar");
        check(hasMethod(c, "readJDouble"), "method readJDouble");
        check(hasMethod(c, "readJFloat"), "method readJFloat");
        check(hasMethod(c, "readJInt"), "method readJInt");
        check(hasMethod(c, "readJLong"), "method readJLong");
        check(hasMethod(c, "readJShort"), "method readJShort");
        check(hasMethod(c, "readAddressValue"), "method readAddressValue");
        check(hasMethod(c, "readCompOopAddressValue"), "method readCompOopAddressValue");
        check(hasMethod(c, "readCompKlassAddressValue"), "method readCompKlassAddressValue");
        check(hasMethod(c, "parseCacheNumPagesProperty"), "method parseCacheNumPagesProperty");
        check(hasMethod(c, "invalidatePageCache"), "method invalidatePageCache");
        check(hasField(c, "machDesc"), "field machDesc");
        check(hasField(c, "utils"), "field utils");
        check(hasField(c, "jbooleanSize"), "field jbooleanSize");
        check(hasField(c, "jbyteSize"), "field jbyteSize");
        check(hasField(c, "jcharSize"), "field jcharSize");
        check(hasField(c, "jdoubleSize"), "field jdoubleSize");
        check(hasField(c, "jfloatSize"), "field jfloatSize");
        check(hasField(c, "jintSize"), "field jintSize");
        check(hasField(c, "jlongSize"), "field jlongSize");
        check(hasField(c, "jshortSize"), "field jshortSize");
        check(hasField(c, "javaPrimitiveTypesConfigured"), "field javaPrimitiveTypesConfigured");
        check(hasField(c, "oopSize"), "field oopSize");
        check(hasField(c, "heapOopSize"), "field heapOopSize");
        check(hasField(c, "narrowOopBase"), "field narrowOopBase");
        check(hasField(c, "narrowOopShift"), "field narrowOopShift");
        check(hasField(c, "klassPtrSize"), "field klassPtrSize");
        check(hasField(c, "narrowKlassBase"), "field narrowKlassBase");
        check(hasField(c, "narrowKlassShift"), "field narrowKlassShift");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
