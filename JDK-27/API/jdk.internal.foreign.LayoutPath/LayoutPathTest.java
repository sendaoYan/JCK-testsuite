/*
 * @test
 * @summary jdk.internal.foreign.LayoutPath conformance: internal class reflection.
 * @modules java.base/jdk.internal.foreign:+open
 * @run main LayoutPathTest
 */
public class LayoutPathTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasM(java.lang.reflect.Method[] ms, String n) { for (java.lang.reflect.Method m : ms) if (m.getName().equals(n)) return true; return false; }
    static boolean hasF(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.getName().equals(n)) return true; return false; }
    static boolean hasI(Class<?>[] is, String n) { for (Class<?> i : is) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnum(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.foreign.LayoutPath", false, LayoutPathTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.foreign.LayoutPath"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.lang.reflect.Method[] ms = c.getDeclaredMethods();
        check(ms.length >= 28, "declared methods >= 28");
        check(hasM(ms, "derefElement"), "method derefElement");
        check(hasM(ms, "dereferenceHandle"), "method dereferenceHandle");
        check(hasM(ms, "groupElement"), "method groupElement");
        check(hasM(ms, "layout"), "method layout");
        check(hasM(ms, "offset"), "method offset");
        check(hasM(ms, "offsetHandle"), "method offsetHandle");
        check(hasM(ms, "rootPath"), "method rootPath");
        check(hasM(ms, "sequenceElement"), "method sequenceElement");
        check(hasM(ms, "sliceHandle"), "method sliceHandle");
        java.lang.reflect.Field[] fs = c.getDeclaredFields();
        check(fs.length >= 16, "declared fields >= 16");
        check(hasF(fs, "EMPTY_PATH_ELEMENTS"), "field EMPTY_PATH_ELEMENTS");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
