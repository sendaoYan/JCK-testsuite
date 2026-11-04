/*
 * @test
 * @summary java.lang.classfile.TypeAnnotation.TargetType conformance: white-box structure + black-box behavior.
 * @run main TargetTypeTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.invoke.*;
import java.lang.reflect.*;
import java.util.*;
import java.nio.file.*;

public class TargetTypeTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static boolean declares(Class<?> c, String name) {
        for (Method m : c.getDeclaredMethods())
            if (m.getName().equals(name)) return true;
        return false;
    }
    static final ClassReader[] cap = new ClassReader[1];
    static final BufWriter[] cap2 = new BufWriter[1];
    static final BranchInstruction[] br = new BranchInstruction[1];
    static final SwitchCase[] sc = new SwitchCase[1];
    static final TableSwitchInstruction[] ts = new TableSwitchInstruction[1];
    static final LookupSwitchInstruction[] ls = new LookupSwitchInstruction[1];
    static final ExceptionCatch[] ec = new ExceptionCatch[1];
    static final TypeAnnotation.TypeArgumentTarget[] ta = new TypeAnnotation.TypeArgumentTarget[1];
    static final TypeAnnotation.LocalVarTarget[] lvt = new TypeAnnotation.LocalVarTarget[1];
    static final TypeAnnotation.LocalVarTargetInfo[] lvi = new TypeAnnotation.LocalVarTargetInfo[1];
    static final StackMapFrameInfo.UninitializedVerificationTypeInfo[] uv = new StackMapFrameInfo.UninitializedVerificationTypeInfo[1];
    static final TypeAnnotation.OffsetTarget[] ot = new TypeAnnotation.OffsetTarget[1];

    // ---- custom attribute support for black-box above ----
    static class CA extends CustomAttribute<CA> { CA(AttributeMapper<CA> m) { super(m); } }
    static class CA2 extends CustomAttribute<CA2> { CA2(AttributeMapper<CA2> m) { super(m); } }
    static class CA3 extends CustomAttribute<CA3> { CA3(AttributeMapper<CA3> m) { super(m); } }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.classfile.TypeAnnotation$TargetType");
        check(c != null, "java.lang.classfile.TypeAnnotation.TargetType loads");
        check(c.getName().equals("java.lang.classfile.TypeAnnotation$TargetType"), "binary name java.lang.classfile.TypeAnnotation$TargetType");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.TypeAnnotation.TargetType is public");
        check(c.isEnum(), "java.lang.classfile.TypeAnnotation.TargetType is an enum");
        check(c.getSuperclass() == Enum.class, "java.lang.classfile.TypeAnnotation.TargetType superclass is java.lang.Enum");
        Object[] ec = c.getEnumConstants();
        check(ec.length == 22, "22 enum constants");
        java.util.Set<String> es = new java.util.HashSet<>();
        for (Object e : ec) es.add(((Enum<?>) e).name());
        check(es.equals(java.util.Set.of("CLASS_TYPE_PARAMETER", "METHOD_TYPE_PARAMETER", "CLASS_EXTENDS", "CLASS_TYPE_PARAMETER_BOUND", "METHOD_TYPE_PARAMETER_BOUND", "FIELD", "METHOD_RETURN", "METHOD_RECEIVER", "METHOD_FORMAL_PARAMETER", "THROWS", "LOCAL_VARIABLE", "RESOURCE_VARIABLE", "EXCEPTION_PARAMETER", "INSTANCEOF", "NEW", "CONSTRUCTOR_REFERENCE", "METHOD_REFERENCE", "CAST", "CONSTRUCTOR_INVOCATION_TYPE_ARGUMENT", "METHOD_INVOCATION_TYPE_ARGUMENT", "CONSTRUCTOR_REFERENCE_TYPE_ARGUMENT", "METHOD_REFERENCE_TYPE_ARGUMENT")), "exact enum constant names");
        check(declares(c, "values"), "declares values");
        check(declares(c, "valueOf"), "declares valueOf");
        check(declares(c, "targetTypeValue"), "declares targetTypeValue");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        check(TypeAnnotation.TargetType.FIELD.targetTypeValue() == 0x13, "FIELD value 0x13");
        check(TypeAnnotation.TargetType.CLASS_TYPE_PARAMETER.targetTypeValue() == 0x00, "CLASS_TYPE_PARAMETER 0");
        check(TypeAnnotation.TargetType.THROWS.targetTypeValue() == 0x17, "THROWS 0x17");
        check(TypeAnnotation.TargetType.values().length == 22, "22 target types");
        check(TypeAnnotation.TargetType.valueOf("FIELD") == TypeAnnotation.TargetType.FIELD, "valueOf");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
