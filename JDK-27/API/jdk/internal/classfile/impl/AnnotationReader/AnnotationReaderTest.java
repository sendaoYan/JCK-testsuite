/*
 * @test
 * @summary jdk.internal.classfile.impl.AnnotationReader conformance: internal class reflection.
 * @modules java.base/jdk.internal.classfile.impl:+open
 * @run main AnnotationReaderTest
 */
public class AnnotationReaderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasM(java.lang.reflect.Method[] ms, String n) { for (java.lang.reflect.Method m : ms) if (m.getName().equals(n)) return true; return false; }
    static boolean hasF(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.getName().equals(n)) return true; return false; }
    static boolean hasI(Class<?>[] is, String n) { for (Class<?> i : is) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnum(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.classfile.impl.AnnotationReader", false, AnnotationReaderTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.classfile.impl.AnnotationReader"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.lang.reflect.Method[] ms = c.getDeclaredMethods();
        check(ms.length >= 20, "declared methods >= 20");
        check(hasM(ms, "readAnnotations"), "method readAnnotations");
        check(hasM(ms, "readElementValue"), "method readElementValue");
        check(hasM(ms, "readParameterAnnotations"), "method readParameterAnnotations");
        check(hasM(ms, "readTypeAnnotations"), "method readTypeAnnotations");
        check(hasM(ms, "writeAnnotation"), "method writeAnnotation");
        check(hasM(ms, "writeAnnotationValue"), "method writeAnnotationValue");
        check(hasM(ms, "writeAnnotations"), "method writeAnnotations");
        check(hasM(ms, "writeTypeAnnotation"), "method writeTypeAnnotation");
        check(hasM(ms, "writeTypeAnnotations"), "method writeTypeAnnotations");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
