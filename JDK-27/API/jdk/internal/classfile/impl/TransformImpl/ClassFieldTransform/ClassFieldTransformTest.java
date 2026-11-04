/*
 * @test
 * @summary jdk.internal.classfile.impl.TransformImpl.ClassFieldTransform conformance: internal class reflection.
 * @modules java.base/jdk.internal.classfile.impl:+open
 * @run main ClassFieldTransformTest
 */
public class ClassFieldTransformTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasM(java.lang.reflect.Method[] ms, String n) { for (java.lang.reflect.Method m : ms) if (m.getName().equals(n)) return true; return false; }
    static boolean hasF(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.getName().equals(n)) return true; return false; }
    static boolean hasI(Class<?>[] is, String n) { for (Class<?> i : is) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnum(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.classfile.impl.TransformImpl$ClassFieldTransform", false, ClassFieldTransformTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.classfile.impl.TransformImpl$ClassFieldTransform"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Record"), "superclass java.lang.Record");
        check(c.getInterfaces().length >= 1, "direct interfaces >= 1");
        check(hasI(c.getInterfaces(), "jdk.internal.classfile.impl.TransformImpl$UnresolvedClassTransform"), "interface jdk.internal.classfile.impl.TransformImpl$UnresolvedClassTransform");
        java.lang.reflect.Method[] ms = c.getDeclaredMethods();
        check(ms.length >= 10, "declared methods >= 10");
        check(hasM(ms, "andThen"), "method andThen");
        check(hasM(ms, "equals"), "method equals");
        check(hasM(ms, "filter"), "method filter");
        check(hasM(ms, "hashCode"), "method hashCode");
        check(hasM(ms, "resolve"), "method resolve");
        check(hasM(ms, "toString"), "method toString");
        check(hasM(ms, "transform"), "method transform");
        java.lang.reflect.Field[] fs = c.getDeclaredFields();
        check(fs.length >= 2, "declared fields >= 2");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
