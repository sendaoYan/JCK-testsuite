/*
 * @test
 * @summary jdk.internal.foreign.layout.AbstractLayout conformance: internal abstract class reflection.
 * @modules java.base/jdk.internal.foreign.layout:+open
 * @run main AbstractLayoutTest
 */
public class AbstractLayoutTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasM(java.lang.reflect.Method[] ms, String n) { for (java.lang.reflect.Method m : ms) if (m.getName().equals(n)) return true; return false; }
    static boolean hasF(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.getName().equals(n)) return true; return false; }
    static boolean hasI(Class<?>[] is, String n) { for (Class<?> i : is) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnum(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.foreign.layout.AbstractLayout", false, AbstractLayoutTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.foreign.layout.AbstractLayout"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.lang.reflect.Method[] ms = c.getDeclaredMethods();
        check(ms.length >= 23, "declared methods >= 23");
        check(hasM(ms, "arrayElementVarHandle"), "method arrayElementVarHandle");
        check(hasM(ms, "byteAlignment"), "method byteAlignment");
        check(hasM(ms, "byteOffset"), "method byteOffset");
        check(hasM(ms, "byteOffsetHandle"), "method byteOffsetHandle");
        check(hasM(ms, "byteSize"), "method byteSize");
        check(hasM(ms, "equals"), "method equals");
        check(hasM(ms, "hasNaturalAlignment"), "method hasNaturalAlignment");
        check(hasM(ms, "hashCode"), "method hashCode");
        check(hasM(ms, "name"), "method name");
        check(hasM(ms, "scale"), "method scale");
        check(hasM(ms, "scaleHandle"), "method scaleHandle");
        check(hasM(ms, "select"), "method select");
        check(hasM(ms, "sliceHandle"), "method sliceHandle");
        check(hasM(ms, "toString"), "method toString");
        check(hasM(ms, "varHandle"), "method varHandle");
        java.lang.reflect.Field[] fs = c.getDeclaredFields();
        check(fs.length >= 3, "declared fields >= 3");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
