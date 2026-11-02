/*
 * @test
 * @summary sun.reflect.annotation.TypeAnnotation.TypeAnnotationTarget conformance: enum constants and members.
 * @run main TypeAnnotationTargetTest
 */

public class TypeAnnotationTargetTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.reflect.annotation.TypeAnnotation$TypeAnnotationTarget", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.reflect.annotation.TypeAnnotation$TypeAnnotationTarget"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.isEnum(), "is enum");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass java.lang.Enum");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("valueOf"), "declares method valueOf");
        check(mnames.contains("values"), "declares method values");
        java.util.Set<String> consts = new java.util.HashSet<String>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant()) consts.add(f.getName());
        check(consts.contains("CLASS_TYPE_PARAMETER"), "enum constant CLASS_TYPE_PARAMETER");
        check(consts.contains("METHOD_TYPE_PARAMETER"), "enum constant METHOD_TYPE_PARAMETER");
        check(consts.contains("CLASS_EXTENDS"), "enum constant CLASS_EXTENDS");
        check(consts.contains("CLASS_IMPLEMENTS"), "enum constant CLASS_IMPLEMENTS");
        check(consts.contains("CLASS_TYPE_PARAMETER_BOUND"), "enum constant CLASS_TYPE_PARAMETER_BOUND");
        check(consts.contains("METHOD_TYPE_PARAMETER_BOUND"), "enum constant METHOD_TYPE_PARAMETER_BOUND");
        check(consts.contains("FIELD"), "enum constant FIELD");
        check(consts.contains("METHOD_RETURN"), "enum constant METHOD_RETURN");
        check(consts.contains("METHOD_RECEIVER"), "enum constant METHOD_RECEIVER");
        check(consts.contains("METHOD_FORMAL_PARAMETER"), "enum constant METHOD_FORMAL_PARAMETER");
        check(consts.contains("THROWS"), "enum constant THROWS");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
