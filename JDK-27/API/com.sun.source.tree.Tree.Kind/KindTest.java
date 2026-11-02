/*
 * @test
 * @summary com.sun.source.tree.Tree.Kind conformance: reflection shape checks.
 * @run main KindTest
 */

import java.lang.reflect.*;

public class KindTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static java.util.Set<String> ifaceNames(Class<?> c) {
        java.util.Set<String> s = new java.util.HashSet<>();
        for (Class<?> i : c.getInterfaces()) s.add(i.getName());
        return s;
    }
    static boolean declares(Class<?> c, String name) {
        for (Method m : c.getDeclaredMethods())
            if (m.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.source.tree.Tree$Kind");
        check(c != null, "com.sun.source.tree.Tree.Kind loads");
        check(c.getName().equals("com.sun.source.tree.Tree$Kind"), "binary name is com.sun.source.tree.Tree$Kind");
        check(Modifier.isPublic(c.getModifiers()), "com.sun.source.tree.Tree.Kind is public");
        check(c.isEnum(), "com.sun.source.tree.Tree.Kind is an enum");
        check(c.getSuperclass().getName().equals("java.lang.Enum"), "superclass is java.lang.Enum");
        Object[] enums = c.getEnumConstants();
        check(enums.length == 118, "118 enum constants");
        java.util.Set<String> names = new java.util.HashSet<>();
        for (Object e : enums) names.add(((Enum<?>) e).name());
        check(names.equals(java.util.Set.of("ANNOTATED_TYPE", "ANNOTATION", "TYPE_ANNOTATION", "ARRAY_ACCESS", "ARRAY_TYPE", "ASSERT", "ASSIGNMENT", "BLOCK", "BREAK", "CASE", "CATCH", "CLASS", "COMPILATION_UNIT", "CONDITIONAL_EXPRESSION", "CONTINUE", "DO_WHILE_LOOP", "ENHANCED_FOR_LOOP", "EXPRESSION_STATEMENT", "MEMBER_SELECT", "MEMBER_REFERENCE", "FOR_LOOP", "IDENTIFIER", "IF", "IMPORT", "INSTANCE_OF", "LABELED_STATEMENT", "METHOD", "METHOD_INVOCATION", "MODIFIERS", "NEW_ARRAY", "NEW_CLASS", "LAMBDA_EXPRESSION", "PACKAGE", "PARENTHESIZED", "ANY_PATTERN", "BINDING_PATTERN", "DEFAULT_CASE_LABEL", "CONSTANT_CASE_LABEL", "PATTERN_CASE_LABEL", "DECONSTRUCTION_PATTERN", "PRIMITIVE_TYPE", "VAR_TYPE", "RETURN", "EMPTY_STATEMENT", "SWITCH", "SWITCH_EXPRESSION", "SYNCHRONIZED", "THROW", "TRY", "PARAMETERIZED_TYPE", "UNION_TYPE", "INTERSECTION_TYPE", "TYPE_CAST", "TYPE_PARAMETER", "VARIABLE", "WHILE_LOOP", "POSTFIX_INCREMENT", "POSTFIX_DECREMENT", "PREFIX_INCREMENT", "PREFIX_DECREMENT", "UNARY_PLUS", "UNARY_MINUS", "BITWISE_COMPLEMENT", "LOGICAL_COMPLEMENT", "MULTIPLY", "DIVIDE", "REMAINDER", "PLUS", "MINUS", "LEFT_SHIFT", "RIGHT_SHIFT", "UNSIGNED_RIGHT_SHIFT", "LESS_THAN", "GREATER_THAN", "LESS_THAN_EQUAL", "GREATER_THAN_EQUAL", "EQUAL_TO", "NOT_EQUAL_TO", "AND", "XOR", "OR", "CONDITIONAL_AND", "CONDITIONAL_OR", "MULTIPLY_ASSIGNMENT", "DIVIDE_ASSIGNMENT", "REMAINDER_ASSIGNMENT", "PLUS_ASSIGNMENT", "MINUS_ASSIGNMENT", "LEFT_SHIFT_ASSIGNMENT", "RIGHT_SHIFT_ASSIGNMENT", "UNSIGNED_RIGHT_SHIFT_ASSIGNMENT", "AND_ASSIGNMENT", "XOR_ASSIGNMENT", "OR_ASSIGNMENT", "INT_LITERAL", "LONG_LITERAL", "FLOAT_LITERAL", "DOUBLE_LITERAL", "BOOLEAN_LITERAL", "CHAR_LITERAL", "STRING_LITERAL", "NULL_LITERAL", "UNBOUNDED_WILDCARD", "EXTENDS_WILDCARD", "SUPER_WILDCARD", "ERRONEOUS", "INTERFACE", "ENUM", "ANNOTATION_TYPE", "MODULE", "EXPORTS", "OPENS", "PROVIDES", "RECORD", "REQUIRES", "USES", "OTHER", "YIELD")), "exact enum constant names");
        check(Enum.valueOf((Class<? extends Enum>) c, "ANNOTATED_TYPE") != null, "Enum.valueOf works");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
