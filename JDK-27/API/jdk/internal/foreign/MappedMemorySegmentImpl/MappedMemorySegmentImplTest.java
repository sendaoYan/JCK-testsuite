/*
 * @test
 * @summary jdk.internal.foreign.MappedMemorySegmentImpl conformance: internal class reflection.
 * @modules java.base/jdk.internal.foreign:+open
 * @run main MappedMemorySegmentImplTest
 */
public class MappedMemorySegmentImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasM(java.lang.reflect.Method[] ms, String n) { for (java.lang.reflect.Method m : ms) if (m.getName().equals(n)) return true; return false; }
    static boolean hasF(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.getName().equals(n)) return true; return false; }
    static boolean hasI(Class<?>[] is, String n) { for (Class<?> i : is) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnum(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.foreign.MappedMemorySegmentImpl", false, MappedMemorySegmentImplTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.foreign.MappedMemorySegmentImpl"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("jdk.internal.foreign.NativeMemorySegmentImpl"), "superclass jdk.internal.foreign.NativeMemorySegmentImpl");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.lang.reflect.Method[] ms = c.getDeclaredMethods();
        check(ms.length >= 12, "declared methods >= 12");
        check(hasM(ms, "asSlice"), "method asSlice");
        check(hasM(ms, "force"), "method force");
        check(hasM(ms, "isLoaded"), "method isLoaded");
        check(hasM(ms, "isMapped"), "method isMapped");
        check(hasM(ms, "load"), "method load");
        check(hasM(ms, "unload"), "method unload");
        java.lang.reflect.Field[] fs = c.getDeclaredFields();
        check(fs.length >= 2, "declared fields >= 2");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
