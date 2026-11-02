/*
 * @test
 * @summary jdk.internal.foreign.abi.SharedUtils conformance: internal class reflection.
 * @modules java.base/jdk.internal.foreign.abi:+open
 * @run main SharedUtilsTest
 */
public class SharedUtilsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasM(java.lang.reflect.Method[] ms, String n) { for (java.lang.reflect.Method m : ms) if (m.getName().equals(n)) return true; return false; }
    static boolean hasF(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.getName().equals(n)) return true; return false; }
    static boolean hasI(Class<?>[] is, String n) { for (Class<?> i : is) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnum(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.foreign.abi.SharedUtils", false, SharedUtilsTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.foreign.abi.SharedUtils"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.lang.reflect.Method[] ms = c.getDeclaredMethods();
        check(ms.length >= 35, "declared methods >= 35");
        check(hasM(ms, "adaptDowncallForIMR"), "method adaptDowncallForIMR");
        check(hasM(ms, "alignUp"), "method alignUp");
        check(hasM(ms, "arrangeUpcallHelper"), "method arrangeUpcallHelper");
        check(hasM(ms, "canonicalLayouts"), "method canonicalLayouts");
        check(hasM(ms, "checkCaptureSegment"), "method checkCaptureSegment");
        check(hasM(ms, "checkExceptions"), "method checkExceptions");
        check(hasM(ms, "checkNative"), "method checkNative");
        check(hasM(ms, "checkSymbol"), "method checkSymbol");
        check(hasM(ms, "getSystemLinker"), "method getSystemLinker");
        check(hasM(ms, "handleUncaughtException"), "method handleUncaughtException");
        check(hasM(ms, "isPowerOfTwo"), "method isPowerOfTwo");
        check(hasM(ms, "maybeCheckCaptureSegment"), "method maybeCheckCaptureSegment");
        check(hasM(ms, "maybeInsertAllocator"), "method maybeInsertAllocator");
        check(hasM(ms, "newBoundedArena"), "method newBoundedArena");
        check(hasM(ms, "newEmptyArena"), "method newEmptyArena");
        java.lang.reflect.Field[] fs = c.getDeclaredFields();
        check(fs.length >= 11, "declared fields >= 11");
        check(hasF(fs, "C_POINTER"), "field C_POINTER");
        check(hasF(fs, "DUMMY_ARENA"), "field DUMMY_ARENA");
        check(hasF(fs, "MH_CHECK_SYMBOL"), "field MH_CHECK_SYMBOL");
        check(hasF(fs, "THROWING_ALLOCATOR"), "field THROWING_ALLOCATOR");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
