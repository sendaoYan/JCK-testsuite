/*
 * @test
 * @summary com.sun.tools.javac.code.FlagsEnum conformance: internal enum reflection.
 * @modules jdk.compiler/com.sun.tools.javac.code:+open
 * @run main FlagsEnumTest
 */
public class FlagsEnumTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.tools.javac.code.FlagsEnum", false, FlagsEnumTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("com.sun.tools.javac.code.FlagsEnum"), "name");
        check(c.isEnum(), "enum");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass");
        check(c.getInterfaces().length == 0, "interfaces count");
        java.util.Set<String> ec = new java.util.HashSet<>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant()) ec.add(f.getName());
        check(ec.size() == 62, "enum constants count");
        check(ec.contains("ABSTRACT"), "enum constant ABSTRACT");
        check(ec.contains("ACYCLIC"), "enum constant ACYCLIC");
        check(ec.contains("ACYCLIC_ANN"), "enum constant ACYCLIC_ANN");
        check(ec.contains("ANNOTATION"), "enum constant ANNOTATION");
        check(ec.contains("ANONCONSTR_BASED"), "enum constant ANONCONSTR_BASED");
        check(ec.contains("ANONCONSTR_OR_SUPER_OWNER_ATTRIBUTED"), "enum constant ANONCONSTR_OR_SUPER_OWNER_ATTRIBUTED");
        check(ec.contains("AUTOMATIC_MODULE_OR_HAS_RESOURCE_OR_NAME_FILLED"), "enum constant AUTOMATIC_MODULE_OR_HAS_RESOURCE_OR_NAME_FILLED");
        check(ec.contains("AUXILIARY"), "enum constant AUXILIARY");
        check(ec.contains("BLOCK_OR_LAMBDA_PARAMETER"), "enum constant BLOCK_OR_LAMBDA_PARAMETER");
        check(ec.contains("BRIDGE"), "enum constant BRIDGE");
        check(ec.contains("CLASH"), "enum constant CLASH");
        check(ec.contains("CLASS_SEEN"), "enum constant CLASS_SEEN");
        check(ec.contains("COMPOUND_OR_GENERATED_MEMBER"), "enum constant COMPOUND_OR_GENERATED_MEMBER");
        check(ec.contains("DEFAULT"), "enum constant DEFAULT");
        check(ec.contains("DEPRECATED_ANNOTATION"), "enum constant DEPRECATED_ANNOTATION");
        check(ec.contains("DEPRECATED_OR_BODY_ONLY_FINALIZE"), "enum constant DEPRECATED_OR_BODY_ONLY_FINALIZE");
        check(ec.contains("DEPRECATED_REMOVAL"), "enum constant DEPRECATED_REMOVAL");
        check(ec.contains("EFFECTIVELY_FINAL"), "enum constant EFFECTIVELY_FINAL");
        check(ec.contains("ENUM"), "enum constant ENUM");
        check(ec.contains("EXISTS"), "enum constant EXISTS");
        check(ec.contains("FINAL"), "enum constant FINAL");
        check(ec.contains("FROM_SOURCE"), "enum constant FROM_SOURCE");
        check(ec.contains("GENERATEDCONSTR"), "enum constant GENERATEDCONSTR");
        check(ec.contains("HASINIT"), "enum constant HASINIT");
        check(ec.contains("HYPOTHETICAL"), "enum constant HYPOTHETICAL");
        check(ec.contains("IMPLICIT_CLASS_OR_VAR_VARIABLE"), "enum constant IMPLICIT_CLASS_OR_VAR_VARIABLE");
        check(ec.contains("INTERFACE"), "enum constant INTERFACE");
        check(ec.contains("LAMBDA_METHOD_OR_LOCAL_CAPTURE_FIELD"), "enum constant LAMBDA_METHOD_OR_LOCAL_CAPTURE_FIELD");
        check(ec.contains("LOCKED"), "enum constant LOCKED");
        check(ec.contains("MANDATED_OR_ACC_MODULE"), "enum constant MANDATED_OR_ACC_MODULE");
        check(ec.contains("MATCH_BINDING"), "enum constant MATCH_BINDING");
        check(ec.contains("MATCH_BINDING_TO_OUTER"), "enum constant MATCH_BINDING_TO_OUTER");
        check(ec.contains("MODULE_OR_COMPACT_RECORD_CONSTRUCTOR_OR_UNINITIALIZED_FIELD"), "enum constant MODULE_OR_COMPACT_RECORD_CONSTRUCTOR_OR_UNINITIALIZED_FIELD");
        check(ec.contains("NATIVE"), "enum constant NATIVE");
        check(ec.contains("NON_SEALED"), "enum constant NON_SEALED");
        check(ec.contains("NOOUTERTHIS"), "enum constant NOOUTERTHIS");
        check(ec.contains("NOT_IN_PROFILE_OR_BAD_OVERRIDE"), "enum constant NOT_IN_PROFILE_OR_BAD_OVERRIDE");
        check(ec.contains("PARAMETER"), "enum constant PARAMETER");
        check(ec.contains("PREVIEW_API"), "enum constant PREVIEW_API");
        check(ec.contains("PREVIEW_REFLECTIVE"), "enum constant PREVIEW_REFLECTIVE");
        check(ec.contains("PRIVATE"), "enum constant PRIVATE");
        check(ec.contains("PROPRIETARY"), "enum constant PROPRIETARY");
        check(ec.contains("PROTECTED"), "enum constant PROTECTED");
        check(ec.contains("PUBLIC"), "enum constant PUBLIC");
        check(ec.contains("RECORD"), "enum constant RECORD");
        check(ec.contains("RECOVERABLE"), "enum constant RECOVERABLE");
        check(ec.contains("RESTRICTED_OR_REQUIRES_IDENTITY"), "enum constant RESTRICTED_OR_REQUIRES_IDENTITY");
        check(ec.contains("SEALED"), "enum constant SEALED");
        check(ec.contains("SIGNATURE_POLYMORPHIC"), "enum constant SIGNATURE_POLYMORPHIC");
        check(ec.contains("SOURCE_SEEN"), "enum constant SOURCE_SEEN");
        check(ec.contains("STATIC"), "enum constant STATIC");
        check(ec.contains("STRICTFP"), "enum constant STRICTFP");
        check(ec.contains("SYNCHRONIZED_OR_ACC_SUPER"), "enum constant SYNCHRONIZED_OR_ACC_SUPER");
        check(ec.contains("SYNTHETIC"), "enum constant SYNTHETIC");
        check(ec.contains("SYSTEM_MODULE_OR_VALUE_BASED_OR_FIELD_INIT_TYPE_ANNOTATIONS_QUEUED"), "enum constant SYSTEM_MODULE_OR_VALUE_BASED_OR_FIELD_INIT_TYPE_ANNOTATIONS_QUEUED");
        check(ec.contains("THROWS"), "enum constant THROWS");
        check(ec.contains("TRANSIENT_OR_ACC_VARARGS"), "enum constant TRANSIENT_OR_ACC_VARARGS");
        check(ec.contains("TYPE_TRANSLATED"), "enum constant TYPE_TRANSLATED");
        check(ec.contains("UNATTRIBUTED"), "enum constant UNATTRIBUTED");
        check(ec.contains("UNION"), "enum constant UNION");
        check(ec.contains("VARARGS"), "enum constant VARARGS");
        check(ec.contains("VOLATILE_OR_ACC_BRIDGE"), "enum constant VOLATILE_OR_ACC_BRIDGE");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
