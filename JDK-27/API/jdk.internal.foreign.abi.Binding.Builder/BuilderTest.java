/*
 * @test
 * @summary jdk.internal.foreign.abi.Binding.Builder conformance: internal class reflection.
 * @modules java.base/jdk.internal.foreign.abi:+open
 * @run main BuilderTest
 */
public class BuilderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasM(java.lang.reflect.Method[] ms, String n) { for (java.lang.reflect.Method m : ms) if (m.getName().equals(n)) return true; return false; }
    static boolean hasF(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.getName().equals(n)) return true; return false; }
    static boolean hasI(Class<?>[] is, String n) { for (Class<?> i : is) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnum(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.foreign.abi.Binding$Builder", false, BuilderTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.foreign.abi.Binding$Builder"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.lang.reflect.Method[] ms = c.getDeclaredMethods();
        check(ms.length >= 19, "declared methods >= 19");
        check(hasM(ms, "allocate"), "method allocate");
        check(hasM(ms, "boxAddress"), "method boxAddress");
        check(hasM(ms, "boxAddressRaw"), "method boxAddressRaw");
        check(hasM(ms, "bufferLoad"), "method bufferLoad");
        check(hasM(ms, "bufferStore"), "method bufferStore");
        check(hasM(ms, "build"), "method build");
        check(hasM(ms, "copy"), "method copy");
        check(hasM(ms, "dup"), "method dup");
        check(hasM(ms, "segmentBase"), "method segmentBase");
        check(hasM(ms, "segmentOffsetAllowHeap"), "method segmentOffsetAllowHeap");
        check(hasM(ms, "segmentOffsetNoAllowHeap"), "method segmentOffsetNoAllowHeap");
        check(hasM(ms, "shiftLeft"), "method shiftLeft");
        check(hasM(ms, "shiftRight"), "method shiftRight");
        check(hasM(ms, "unboxAddress"), "method unboxAddress");
        check(hasM(ms, "vmLoad"), "method vmLoad");
        java.lang.reflect.Field[] fs = c.getDeclaredFields();
        check(fs.length >= 1, "declared fields >= 1");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
