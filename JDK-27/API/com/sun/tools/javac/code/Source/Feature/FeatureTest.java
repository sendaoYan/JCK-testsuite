/*
 * @test
 * @summary com.sun.tools.javac.code.Source.Feature conformance: internal enum reflection.
 * @modules jdk.compiler/com.sun.tools.javac.code:+open
 * @run main FeatureTest
 */
public class FeatureTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.tools.javac.code.Source$Feature", false, FeatureTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("com.sun.tools.javac.code.Source$Feature"), "name");
        check(c.isEnum(), "enum");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass");
        check(c.getInterfaces().length == 0, "interfaces count");
        java.util.Set<String> ec = new java.util.HashSet<>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant()) ec.add(f.getName());
        check(ec.size() == 35, "enum constants count");
        check(ec.contains("CAPTURE_MREF_RETURN_TYPE"), "enum constant CAPTURE_MREF_RETURN_TYPE");
        check(ec.contains("CASE_NULL"), "enum constant CASE_NULL");
        check(ec.contains("DEPRECATION_ON_IMPORT"), "enum constant DEPRECATION_ON_IMPORT");
        check(ec.contains("DIAMOND_WITH_ANONYMOUS_CLASS_CREATION"), "enum constant DIAMOND_WITH_ANONYMOUS_CLASS_CREATION");
        check(ec.contains("EFFECTIVELY_FINAL_VARIABLES_IN_TRY_WITH_RESOURCES"), "enum constant EFFECTIVELY_FINAL_VARIABLES_IN_TRY_WITH_RESOURCES");
        check(ec.contains("ERASE_POLY_SIG_RETURN_TYPE"), "enum constant ERASE_POLY_SIG_RETURN_TYPE");
        check(ec.contains("FLEXIBLE_CONSTRUCTORS"), "enum constant FLEXIBLE_CONSTRUCTORS");
        check(ec.contains("IMPLICIT_CLASSES"), "enum constant IMPLICIT_CLASSES");
        check(ec.contains("IMPORT_ON_DEMAND_OBSERVABLE_PACKAGES"), "enum constant IMPORT_ON_DEMAND_OBSERVABLE_PACKAGES");
        check(ec.contains("JAVA_BASE_TRANSITIVE"), "enum constant JAVA_BASE_TRANSITIVE");
        check(ec.contains("LOCAL_VARIABLE_TYPE_INFERENCE"), "enum constant LOCAL_VARIABLE_TYPE_INFERENCE");
        check(ec.contains("MODULES"), "enum constant MODULES");
        check(ec.contains("MODULE_IMPORTS"), "enum constant MODULE_IMPORTS");
        check(ec.contains("NO_TARGET_ANNOTATION_APPLICABILITY"), "enum constant NO_TARGET_ANNOTATION_APPLICABILITY");
        check(ec.contains("PATTERN_MATCHING_IN_INSTANCEOF"), "enum constant PATTERN_MATCHING_IN_INSTANCEOF");
        check(ec.contains("PATTERN_SWITCH"), "enum constant PATTERN_SWITCH");
        check(ec.contains("PRIMITIVE_PATTERNS"), "enum constant PRIMITIVE_PATTERNS");
        check(ec.contains("PRIVATE_INTERFACE_METHODS"), "enum constant PRIVATE_INTERFACE_METHODS");
        check(ec.contains("PRIVATE_MEMBERS_IN_PERMITS_CLAUSE"), "enum constant PRIVATE_MEMBERS_IN_PERMITS_CLAUSE");
        check(ec.contains("PRIVATE_SAFE_VARARGS"), "enum constant PRIVATE_SAFE_VARARGS");
        check(ec.contains("RECORDS"), "enum constant RECORDS");
        check(ec.contains("RECORD_PATTERNS"), "enum constant RECORD_PATTERNS");
        check(ec.contains("REDUNDANT_STRICTFP"), "enum constant REDUNDANT_STRICTFP");
        check(ec.contains("REIFIABLE_TYPES_INSTANCEOF"), "enum constant REIFIABLE_TYPES_INSTANCEOF");
        check(ec.contains("SEALED_CLASSES"), "enum constant SEALED_CLASSES");
        check(ec.contains("SWITCH_EXPRESSION"), "enum constant SWITCH_EXPRESSION");
        check(ec.contains("SWITCH_MULTIPLE_CASE_LABELS"), "enum constant SWITCH_MULTIPLE_CASE_LABELS");
        check(ec.contains("SWITCH_RULE"), "enum constant SWITCH_RULE");
        check(ec.contains("TEXT_BLOCKS"), "enum constant TEXT_BLOCKS");
        check(ec.contains("TYPE_ANNOTATIONS_ON_VAR_LAMBDA_PARAMETER"), "enum constant TYPE_ANNOTATIONS_ON_VAR_LAMBDA_PARAMETER");
        check(ec.contains("UNCONDITIONAL_PATTERN_IN_INSTANCEOF"), "enum constant UNCONDITIONAL_PATTERN_IN_INSTANCEOF");
        check(ec.contains("UNDERSCORE_IDENTIFIER"), "enum constant UNDERSCORE_IDENTIFIER");
        check(ec.contains("UNNAMED_VARIABLES"), "enum constant UNNAMED_VARIABLES");
        check(ec.contains("VAR_SYNTAX_IMPLICIT_LAMBDAS"), "enum constant VAR_SYNTAX_IMPLICIT_LAMBDAS");
        check(ec.contains("WARN_ON_ILLEGAL_UTF8"), "enum constant WARN_ON_ILLEGAL_UTF8");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
