/*
 * @test
 * @summary sun.reflect.annotation.TypeAnnotation.TypeAnnotationTarget conformance: internal package reflection.
 * @modules java.base/sun.reflect.annotation:+open
 * @run main TypeAnnotationTargetTest
 */
public class TypeAnnotationTargetTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.reflect.annotation.TypeAnnotation$TypeAnnotationTarget", false, TypeAnnotationTargetTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == true, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "valueOf"), "method valueOf");
        check(hasMethod(c, "values"), "method values");
        check(hasField(c, "$VALUES"), "field $VALUES");
        check(hasField(c, "CLASS_EXTENDS"), "field CLASS_EXTENDS");
        check(hasField(c, "CLASS_IMPLEMENTS"), "field CLASS_IMPLEMENTS");
        check(hasField(c, "CLASS_TYPE_PARAMETER"), "field CLASS_TYPE_PARAMETER");
        check(hasField(c, "CLASS_TYPE_PARAMETER_BOUND"), "field CLASS_TYPE_PARAMETER_BOUND");
        check(hasField(c, "FIELD"), "field FIELD");
        check(hasField(c, "METHOD_FORMAL_PARAMETER"), "field METHOD_FORMAL_PARAMETER");
        check(hasField(c, "METHOD_RECEIVER"), "field METHOD_RECEIVER");
        check(hasField(c, "METHOD_RETURN"), "field METHOD_RETURN");
        check(hasField(c, "METHOD_TYPE_PARAMETER"), "field METHOD_TYPE_PARAMETER");
        check(hasField(c, "METHOD_TYPE_PARAMETER_BOUND"), "field METHOD_TYPE_PARAMETER_BOUND");
        check(hasField(c, "RECORD_COMPONENT"), "field RECORD_COMPONENT");
        check(hasField(c, "THROWS"), "field THROWS");
        check(hasEnumConstant(c, "CLASS_EXTENDS"), "enum CLASS_EXTENDS");
        check(hasEnumConstant(c, "CLASS_IMPLEMENTS"), "enum CLASS_IMPLEMENTS");
        check(hasEnumConstant(c, "CLASS_TYPE_PARAMETER"), "enum CLASS_TYPE_PARAMETER");
        check(hasEnumConstant(c, "CLASS_TYPE_PARAMETER_BOUND"), "enum CLASS_TYPE_PARAMETER_BOUND");
        check(hasEnumConstant(c, "FIELD"), "enum FIELD");
        check(hasEnumConstant(c, "METHOD_FORMAL_PARAMETER"), "enum METHOD_FORMAL_PARAMETER");
        check(hasEnumConstant(c, "METHOD_RECEIVER"), "enum METHOD_RECEIVER");
        check(hasEnumConstant(c, "METHOD_RETURN"), "enum METHOD_RETURN");
        check(hasEnumConstant(c, "METHOD_TYPE_PARAMETER"), "enum METHOD_TYPE_PARAMETER");
        check(hasEnumConstant(c, "METHOD_TYPE_PARAMETER_BOUND"), "enum METHOD_TYPE_PARAMETER_BOUND");
        check(hasEnumConstant(c, "RECORD_COMPONENT"), "enum RECORD_COMPONENT");
        check(hasEnumConstant(c, "THROWS"), "enum THROWS");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
