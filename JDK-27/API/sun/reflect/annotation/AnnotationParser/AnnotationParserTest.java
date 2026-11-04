/*
 * @test
 * @summary sun.reflect.annotation.AnnotationParser conformance: internal package reflection.
 * @modules java.base/sun.reflect.annotation:+open
 * @run main AnnotationParserTest
 */
public class AnnotationParserTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.reflect.annotation.AnnotationParser", false, AnnotationParserTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "annotationForMap"), "method annotationForMap");
        check(hasMethod(c, "parseAnnotations"), "method parseAnnotations");
        check(hasMethod(c, "parseMemberValue"), "method parseMemberValue");
        check(hasMethod(c, "parseParameterAnnotations"), "method parseParameterAnnotations");
        check(hasMethod(c, "parseSelectAnnotations"), "method parseSelectAnnotations");
        check(hasMethod(c, "toArray"), "method toArray");
        check(hasField(c, "EMPTY_ANNOTATIONS_ARRAY"), "field EMPTY_ANNOTATIONS_ARRAY");
        check(hasField(c, "EMPTY_ANNOTATION_ARRAY"), "field EMPTY_ANNOTATION_ARRAY");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
