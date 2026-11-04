/*
 * @test
 * @summary sun.jvm.hotspot.debugger.DebuggerUtilities conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.debugger:+open
 * @run main DebuggerUtilitiesTest
 */
import java.lang.reflect.*;

public class DebuggerUtilitiesTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.debugger.DebuggerUtilities", false, DebuggerUtilitiesTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "checkAlignment"), "method checkAlignment");
        check(hasMethod(c, "dataToJBoolean"), "method dataToJBoolean");
        check(hasMethod(c, "dataToJByte"), "method dataToJByte");
        check(hasMethod(c, "dataToJChar"), "method dataToJChar");
        check(hasMethod(c, "dataToJDouble"), "method dataToJDouble");
        check(hasMethod(c, "dataToJFloat"), "method dataToJFloat");
        check(hasMethod(c, "dataToJInt"), "method dataToJInt");
        check(hasMethod(c, "dataToJLong"), "method dataToJLong");
        check(hasMethod(c, "dataToJShort"), "method dataToJShort");
        check(hasMethod(c, "dataToCInteger"), "method dataToCInteger");
        check(hasMethod(c, "addressValueToString"), "method addressValueToString");
        check(hasMethod(c, "charToNibble"), "method charToNibble");
        check(hasMethod(c, "jlongToData"), "method jlongToData");
        check(hasMethod(c, "jintToData"), "method jintToData");
        check(hasMethod(c, "scanAddress"), "method scanAddress");
        check(hasMethod(c, "dataToAddressValue"), "method dataToAddressValue");
        check(hasMethod(c, "jbooleanToData"), "method jbooleanToData");
        check(hasMethod(c, "jbyteToData"), "method jbyteToData");
        check(hasMethod(c, "jcharToData"), "method jcharToData");
        check(hasMethod(c, "jdoubleToData"), "method jdoubleToData");
        check(hasMethod(c, "jfloatToData"), "method jfloatToData");
        check(hasMethod(c, "jshortToData"), "method jshortToData");
        check(hasMethod(c, "cIntegerToData"), "method cIntegerToData");
        check(hasField(c, "addressSize"), "field addressSize");
        check(hasField(c, "isBigEndian"), "field isBigEndian");
        check(hasField(c, "supports32bitAlignmentOf64bitTypes"), "field supports32bitAlignmentOf64bitTypes");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
