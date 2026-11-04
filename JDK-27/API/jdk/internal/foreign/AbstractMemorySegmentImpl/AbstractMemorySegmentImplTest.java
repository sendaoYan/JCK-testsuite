/*
 * @test
 * @summary jdk.internal.foreign.AbstractMemorySegmentImpl conformance: internal abstract class reflection.
 * @modules java.base/jdk.internal.foreign:+open
 * @run main AbstractMemorySegmentImplTest
 */
public class AbstractMemorySegmentImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasM(java.lang.reflect.Method[] ms, String n) { for (java.lang.reflect.Method m : ms) if (m.getName().equals(n)) return true; return false; }
    static boolean hasF(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.getName().equals(n)) return true; return false; }
    static boolean hasI(Class<?>[] is, String n) { for (Class<?> i : is) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnum(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.foreign.AbstractMemorySegmentImpl", false, AbstractMemorySegmentImplTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.foreign.AbstractMemorySegmentImpl"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length >= 2, "direct interfaces >= 2");
        check(hasI(c.getInterfaces(), "java.lang.foreign.MemorySegment"), "interface java.lang.foreign.MemorySegment");
        check(hasI(c.getInterfaces(), "java.lang.foreign.SegmentAllocator"), "interface java.lang.foreign.SegmentAllocator");
        java.lang.reflect.Method[] ms = c.getDeclaredMethods();
        check(ms.length >= 122, "declared methods >= 122");
        check(hasM(ms, "allocate"), "method allocate");
        check(hasM(ms, "asByteBuffer"), "method asByteBuffer");
        check(hasM(ms, "asOverlappingSlice"), "method asOverlappingSlice");
        check(hasM(ms, "asReadOnly"), "method asReadOnly");
        check(hasM(ms, "asSlice"), "method asSlice");
        check(hasM(ms, "byteSize"), "method byteSize");
        check(hasM(ms, "checkAccess"), "method checkAccess");
        check(hasM(ms, "checkEnclosingLayout"), "method checkEnclosingLayout");
        check(hasM(ms, "checkReadOnly"), "method checkReadOnly");
        check(hasM(ms, "copy"), "method copy");
        check(hasM(ms, "copyFrom"), "method copyFrom");
        check(hasM(ms, "elements"), "method elements");
        check(hasM(ms, "equals"), "method equals");
        check(hasM(ms, "fill"), "method fill");
        check(hasM(ms, "force"), "method force");
        java.lang.reflect.Field[] fs = c.getDeclaredFields();
        check(fs.length >= 4, "declared fields >= 4");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
