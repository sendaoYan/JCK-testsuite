/*
 * @test
 * @summary java.lang.classfile.TypeAnnotation.TypePathComponent.Kind conformance: white-box structure + black-box behavior.
 * @run main KindTest
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

public class KindTest {
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
        Class<?> c = Class.forName("java.lang.classfile.TypeAnnotation$TypePathComponent$Kind");
        check(c != null, "java.lang.classfile.TypeAnnotation.TypePathComponent.Kind loads");
        check(c.getName().equals("java.lang.classfile.TypeAnnotation$TypePathComponent$Kind"), "binary name java.lang.classfile.TypeAnnotation$TypePathComponent$Kind");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.TypeAnnotation.TypePathComponent.Kind is public");
        check(c.isEnum(), "java.lang.classfile.TypeAnnotation.TypePathComponent.Kind is an enum");
        check(c.getSuperclass() == Enum.class, "java.lang.classfile.TypeAnnotation.TypePathComponent.Kind superclass is java.lang.Enum");
        Object[] ec = c.getEnumConstants();
        check(ec.length == 4, "4 enum constants");
        java.util.Set<String> es = new java.util.HashSet<>();
        for (Object e : ec) es.add(((Enum<?>) e).name());
        check(es.equals(java.util.Set.of("ARRAY", "INNER_TYPE", "WILDCARD", "TYPE_ARGUMENT")), "exact enum constant names");
        check(declares(c, "values"), "declares values");
        check(declares(c, "valueOf"), "declares valueOf");
        check(declares(c, "tag"), "declares tag");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        TypeAnnotation.TypePathComponent.Kind k = TypeAnnotation.TypePathComponent.Kind.ARRAY;
        check(k.tag() == 0, "ARRAY tag 0");
        check(TypeAnnotation.TypePathComponent.Kind.INNER_TYPE.tag() == 1, "INNER_TYPE tag 1");
        check(TypeAnnotation.TypePathComponent.Kind.WILDCARD.tag() == 2, "WILDCARD tag 2");
        check(TypeAnnotation.TypePathComponent.Kind.TYPE_ARGUMENT.tag() == 3, "TYPE_ARGUMENT tag 3");
        check(TypeAnnotation.TypePathComponent.Kind.valueOf("ARRAY") == k, "valueOf");

    }

    // ---- Part 3: 枚举全值矩阵（顺序 + tag + valueOf 双向 + 负例 + Enum 契约） ----
    static void testEnumMatrix() {
        TypeAnnotation.TypePathComponent.Kind[] vs = TypeAnnotation.TypePathComponent.Kind.values();
        check(vs.length == 4, "values() length 4");
        String[] names = {"ARRAY", "INNER_TYPE", "WILDCARD", "TYPE_ARGUMENT"};
        int[] tags = {0, 1, 2, 3};
        for (int i = 0; i < 4; i++) {
            check(vs[i].name().equals(names[i]), "ordinal " + i + " name " + names[i]);
            check(vs[i].ordinal() == i, names[i] + " ordinal " + i);
            check(vs[i].tag() == tags[i], names[i] + " tag " + tags[i]);
            check(TypeAnnotation.TypePathComponent.Kind.valueOf(names[i]) == vs[i], "valueOf(" + names[i] + ") round-trip");
            check(vs[i].toString().contains(names[i]), names[i] + " toString contains name");
            check(vs[i].compareTo(vs[i]) == 0, names[i] + " self compareTo 0");
            check(vs[i].getDeclaringClass() == TypeAnnotation.TypePathComponent.Kind.class, names[i] + " declaringClass");
        }
        for (int i = 0; i < 3; i++)
            check(vs[i].compareTo(vs[i + 1]) < 0, "compareTo ordering at " + i);
        check(vs[0].equals(vs[0]) && !vs[0].equals(vs[1]), "equals same/different");
        check(vs[0].hashCode() == vs[0].hashCode(), "hashCode stable");
        try { TypeAnnotation.TypePathComponent.Kind.valueOf("NOPE"); check(false, "valueOf(NOPE) must IAE"); }
        catch (IllegalArgumentException e) { check(e.getMessage().contains("No enum constant"), "valueOf(NOPE) IAE msg"); }
        try { TypeAnnotation.TypePathComponent.Kind.valueOf(null); check(false, "valueOf(null) must NPE"); }
        catch (NullPointerException e) { check(true, "valueOf(null) NPE"); }
        // values() 每次返回新数组（可安全修改）
        TypeAnnotation.TypePathComponent.Kind[] v2 = TypeAnnotation.TypePathComponent.Kind.values();
        check(v2 != vs, "values() fresh array");
        check(java.util.Arrays.equals(v2, vs), "values() content equal");
        // tag 唯一性：4 个 tag 互不相同
        java.util.Set<Integer> tagSet = new java.util.HashSet<>();
        for (TypeAnnotation.TypePathComponent.Kind k : vs) tagSet.add(k.tag());
        check(tagSet.size() == 4, "4 distinct tags");
        // Kind 与 TypePathComponent 工厂互通
        check(TypeAnnotation.TypePathComponent.of(TypeAnnotation.TypePathComponent.Kind.WILDCARD, 0).typePathKind()
                == TypeAnnotation.TypePathComponent.Kind.WILDCARD, "Kind feeds TypePathComponent.of");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testEnumMatrix();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
