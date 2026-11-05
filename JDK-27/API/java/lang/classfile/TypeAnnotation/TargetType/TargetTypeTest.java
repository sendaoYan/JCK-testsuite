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

    // ---- Part 3: 枚举全值矩阵（javap/探针实测锁定：顺序/值/编码长度全表） ----
    static void testEnumMatrix() throws Exception {
        String[] names = {"CLASS_TYPE_PARAMETER", "METHOD_TYPE_PARAMETER", "CLASS_EXTENDS", "CLASS_TYPE_PARAMETER_BOUND",
                "METHOD_TYPE_PARAMETER_BOUND", "FIELD", "METHOD_RETURN", "METHOD_RECEIVER", "METHOD_FORMAL_PARAMETER",
                "THROWS", "LOCAL_VARIABLE", "RESOURCE_VARIABLE", "EXCEPTION_PARAMETER", "INSTANCEOF", "NEW",
                "CONSTRUCTOR_REFERENCE", "METHOD_REFERENCE", "CAST", "CONSTRUCTOR_INVOCATION_TYPE_ARGUMENT",
                "METHOD_INVOCATION_TYPE_ARGUMENT", "CONSTRUCTOR_REFERENCE_TYPE_ARGUMENT", "METHOD_REFERENCE_TYPE_ARGUMENT"};
        int[] vals = {0x00, 0x01, 0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17,
                0x40, 0x41, 0x42, 0x43, 0x44, 0x45, 0x46, 0x47, 0x48, 0x49, 0x4A, 0x4B};
        int[] sizes = {1, 1, 2, 2, 2, 0, 0, 0, 1, 2, -1, -1, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3};
        TypeAnnotation.TargetType[] vs = TypeAnnotation.TargetType.values();
        check(vs.length == 22 && names.length == 22, "values length 22");
        for (int i = 0; i < 22; i++) {
            check(vs[i].name().equals(names[i]), "order#" + i + " is " + vs[i].name());
            check(vs[i].targetTypeValue() == vals[i], names[i] + " targetTypeValue");
            check(vs[i].sizeIfFixed() == sizes[i], names[i] + " sizeIfFixed");
            check(TypeAnnotation.TargetType.valueOf(names[i]) == vs[i], names[i] + " valueOf round-trip");
            check(vs[i].ordinal() == i, names[i] + " ordinal");
        }
        // JVMS 4.7.20.1 target_type 数值与 TargetInfo.TARGET_* 常量反射对照（22/22）
        for (TypeAnnotation.TargetType t : vs) {
            java.lang.reflect.Field f = TypeAnnotation.TargetInfo.class.getField("TARGET_" + t.name());
            check(f.getInt(null) == t.targetTypeValue(), "TARGET_" + t.name() + " int constant matches enum value");
        }
        // 假名 → IAE 且消息含 No enum constant
        try {
            TypeAnnotation.TargetType.valueOf("NO_SUCH_TARGET");
            check(false, "valueOf(bogus) must throw IAE");
        } catch (IllegalArgumentException e) {
            check(e.getMessage() != null && e.getMessage().contains("No enum constant"), "valueOf(bogus) IAE message: " + e.getMessage());
        }
        // null 名 → NPE
        try {
            TypeAnnotation.TargetType.valueOf((String) null);
            check(false, "valueOf(null) must throw NPE");
        } catch (NullPointerException e) {
            check(true, "valueOf(null) NPE");
        }
        // values() 每次返回新数组（内容等价）
        check(TypeAnnotation.TargetType.values() != vs, "values() fresh array instance");
        check(java.util.Arrays.equals(TypeAnnotation.TargetType.values(), vs), "values() equal contents");
        // Enum 契约
        check(Enum.class.isAssignableFrom(TypeAnnotation.TargetType.class), "is java.lang.Enum subtype");
        check(TypeAnnotation.TargetType.FIELD.getDeclaringClass() == TypeAnnotation.TargetType.class, "getDeclaringClass");
        check(TypeAnnotation.TargetType.CLASS_TYPE_PARAMETER.compareTo(TypeAnnotation.TargetType.METHOD_REFERENCE_TYPE_ARGUMENT) < 0, "compareTo ascending");
        check(TypeAnnotation.TargetType.METHOD_REFERENCE_TYPE_ARGUMENT.compareTo(TypeAnnotation.TargetType.CLASS_TYPE_PARAMETER) > 0, "compareTo reverse");
        check(TypeAnnotation.TargetType.LOCAL_VARIABLE.compareTo(TypeAnnotation.TargetType.RESOURCE_VARIABLE) < 0, "adjacent compareTo");
        check(TypeAnnotation.TargetType.FIELD.equals(TypeAnnotation.TargetType.FIELD), "equals reflexive");
        check(!TypeAnnotation.TargetType.FIELD.equals(TypeAnnotation.TargetType.METHOD_RETURN), "equals distinct");
        for (TypeAnnotation.TargetType t : vs) {
            check(t.toString() != null && t.toString().contains(t.name()), "toString contains name: " + t);
        }
        // 变长编码语义：LOCAL_VARIABLE/RESOURCE_VARIABLE 不可定长
        check(TypeAnnotation.TargetType.LOCAL_VARIABLE.sizeIfFixed() == -1, "LOCAL_VARIABLE sizeIfFixed -1");
        check(TypeAnnotation.TargetType.RESOURCE_VARIABLE.sizeIfFixed() == -1, "RESOURCE_VARIABLE sizeIfFixed -1");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testEnumMatrix();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
