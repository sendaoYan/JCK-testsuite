/*
 * @test
 * @summary sun.reflect.annotation.TypeAnnotationParser conformance: internal package reflection.
 * @modules java.base/sun.reflect.annotation:+open
 * @run main TypeAnnotationParserTest
 */
public class TypeAnnotationParserTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.reflect.annotation.TypeAnnotationParser", false, TypeAnnotationParserTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "buildAnnotatedInterfaces"), "method buildAnnotatedInterfaces");
        check(hasMethod(c, "buildAnnotatedSuperclass"), "method buildAnnotatedSuperclass");
        check(hasMethod(c, "buildAnnotatedType"), "method buildAnnotatedType");
        check(hasMethod(c, "buildAnnotatedTypes"), "method buildAnnotatedTypes");
        check(hasMethod(c, "parseAnnotatedBounds"), "method parseAnnotatedBounds");
        check(hasMethod(c, "parseTypeVariableAnnotations"), "method parseTypeVariableAnnotations");
        check(hasField(c, "CAST"), "field CAST");
        check(hasField(c, "CLASS_EXTENDS"), "field CLASS_EXTENDS");
        check(hasField(c, "CLASS_TYPE_PARAMETER"), "field CLASS_TYPE_PARAMETER");
        check(hasField(c, "CLASS_TYPE_PARAMETER_BOUND"), "field CLASS_TYPE_PARAMETER_BOUND");
        check(hasField(c, "CONSTRUCTOR_INVOCATION_TYPE_ARGUMENT"), "field CONSTRUCTOR_INVOCATION_TYPE_ARGUMENT");
        check(hasField(c, "CONSTRUCTOR_REFERENCE"), "field CONSTRUCTOR_REFERENCE");
        check(hasField(c, "CONSTRUCTOR_REFERENCE_TYPE_ARGUMENT"), "field CONSTRUCTOR_REFERENCE_TYPE_ARGUMENT");
        check(hasField(c, "EMPTY_TYPE_ANNOTATION_ARRAY"), "field EMPTY_TYPE_ANNOTATION_ARRAY");
        check(hasField(c, "EXCEPTION_PARAMETER"), "field EXCEPTION_PARAMETER");
        check(hasField(c, "FIELD"), "field FIELD");
        check(hasField(c, "INSTANCEOF"), "field INSTANCEOF");
        check(hasField(c, "LOCAL_VARIABLE"), "field LOCAL_VARIABLE");
        check(hasField(c, "METHOD_FORMAL_PARAMETER"), "field METHOD_FORMAL_PARAMETER");
        check(hasField(c, "METHOD_INVOCATION_TYPE_ARGUMENT"), "field METHOD_INVOCATION_TYPE_ARGUMENT");
        check(hasField(c, "METHOD_RECEIVER"), "field METHOD_RECEIVER");
        check(hasField(c, "METHOD_REFERENCE"), "field METHOD_REFERENCE");
        check(hasField(c, "METHOD_REFERENCE_TYPE_ARGUMENT"), "field METHOD_REFERENCE_TYPE_ARGUMENT");
        check(hasField(c, "METHOD_RETURN"), "field METHOD_RETURN");
        check(hasField(c, "METHOD_TYPE_PARAMETER"), "field METHOD_TYPE_PARAMETER");
        check(hasField(c, "METHOD_TYPE_PARAMETER_BOUND"), "field METHOD_TYPE_PARAMETER_BOUND");
        check(hasField(c, "NEW"), "field NEW");
        check(hasField(c, "RESOURCE_VARIABLE"), "field RESOURCE_VARIABLE");
        check(hasField(c, "THROWS"), "field THROWS");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
