/*
 * @test
 * @summary sun.jvm.hotspot.utilities.PointerLocation conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.utilities:+open
 * @run main PointerLocationTest
 */
import java.lang.reflect.*;

public class PointerLocationTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.utilities.PointerLocation", false, PointerLocationTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "toString"), "method toString");
        check(hasMethod(c, "print"), "method print");
        check(hasMethod(c, "printOn"), "method printOn");
        check(hasMethod(c, "isUnknown"), "method isUnknown");
        check(hasMethod(c, "isInNewGen"), "method isInNewGen");
        check(hasMethod(c, "isInOldGen"), "method isInOldGen");
        check(hasMethod(c, "isInLocalJNIHandleBlock"), "method isInLocalJNIHandleBlock");
        check(hasMethod(c, "isMetadata"), "method isMetadata");
        check(hasMethod(c, "isCtype"), "method isCtype");
        check(hasMethod(c, "isInJavaStack"), "method isInJavaStack");
        check(hasMethod(c, "isNativeSymbol"), "method isNativeSymbol");
        check(hasMethod(c, "isInHeap"), "method isInHeap");
        check(hasMethod(c, "isInInterpreter"), "method isInInterpreter");
        check(hasMethod(c, "isInCodeCache"), "method isInCodeCache");
        check(hasMethod(c, "isInStrongGlobalJNIHandles"), "method isInStrongGlobalJNIHandles");
        check(hasMethod(c, "isInWeakGlobalJNIHandles"), "method isInWeakGlobalJNIHandles");
        check(hasMethod(c, "isInTLAB"), "method isInTLAB");
        check(hasMethod(c, "getTLABThread"), "method getTLABThread");
        check(hasMethod(c, "getTLAB"), "method getTLAB");
        check(hasMethod(c, "getGeneration"), "method getGeneration");
        check(hasMethod(c, "getG1HeapRegion"), "method getG1HeapRegion");
        check(hasMethod(c, "getCodeBlob"), "method getCodeBlob");
        check(hasMethod(c, "isInBlobCode"), "method isInBlobCode");
        check(hasMethod(c, "isInBlobData"), "method isInBlobData");
        check(hasMethod(c, "isInBlobOops"), "method isInBlobOops");
        check(hasMethod(c, "inOtherGen"), "method inOtherGen");
        check(hasMethod(c, "getInterpreterCodelet"), "method getInterpreterCodelet");
        check(hasMethod(c, "isInBlobUnknownLocation"), "method isInBlobUnknownLocation");
        check(hasMethod(c, "getJNIHandleBlock"), "method getJNIHandleBlock");
        check(hasMethod(c, "getJNIHandleThread"), "method getJNIHandleThread");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
