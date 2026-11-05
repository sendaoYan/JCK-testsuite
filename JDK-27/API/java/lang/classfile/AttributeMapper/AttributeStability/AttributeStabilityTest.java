/*
 * @test
 * @summary java.lang.classfile.AttributeMapper.AttributeStability conformance: white-box structure + black-box behavior.
 * @run main AttributeStabilityTest
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

public class AttributeStabilityTest {
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
        Class<?> c = Class.forName("java.lang.classfile.AttributeMapper$AttributeStability");
        check(c != null, "java.lang.classfile.AttributeMapper.AttributeStability loads");
        check(c.getName().equals("java.lang.classfile.AttributeMapper$AttributeStability"), "binary name java.lang.classfile.AttributeMapper$AttributeStability");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.AttributeMapper.AttributeStability is public");
        check(c.isEnum(), "java.lang.classfile.AttributeMapper.AttributeStability is an enum");
        check(c.getSuperclass() == Enum.class, "java.lang.classfile.AttributeMapper.AttributeStability superclass is java.lang.Enum");
        Object[] ec = c.getEnumConstants();
        check(ec.length == 5, "5 enum constants");
        java.util.Set<String> es = new java.util.HashSet<>();
        for (Object e : ec) es.add(((Enum<?>) e).name());
        check(es.equals(java.util.Set.of("STATELESS", "CP_REFS", "LABELS", "UNSTABLE", "UNKNOWN")), "exact enum constant names");
        check(declares(c, "values"), "declares values");
        check(declares(c, "valueOf"), "declares valueOf");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        check(AttributeMapper.AttributeStability.values().length == 5, "5 constants");
        check(AttributeMapper.AttributeStability.valueOf("STATELESS") == AttributeMapper.AttributeStability.STATELESS, "valueOf");
        check(AttributeMapper.AttributeStability.CP_REFS != AttributeMapper.AttributeStability.UNSTABLE, "distinct");

    }

    // ---- Part 3: 枚举次序（STATELESS 最稳定 → UNKNOWN 最不稳定，ordinal 递增）+ valueOf 双向 + 假名负例 ----
    static void testEnumOrderAndValueOf() {
        AttributeMapper.AttributeStability[] vs = AttributeMapper.AttributeStability.values();
        check(vs.length == 5, "5 constants");
        // 声明次序即 ordinal 次序（javap 锁定）：STATELESS(0) CP_REFS(1) LABELS(2) UNSTABLE(3) UNKNOWN(4)
        check(vs[0].name().equals("STATELESS") && vs[0].ordinal() == 0, "STATELESS ordinal 0");
        check(vs[1].name().equals("CP_REFS") && vs[1].ordinal() == 1, "CP_REFS ordinal 1");
        check(vs[2].name().equals("LABELS") && vs[2].ordinal() == 2, "LABELS ordinal 2");
        check(vs[3].name().equals("UNSTABLE") && vs[3].ordinal() == 3, "UNSTABLE ordinal 3");
        check(vs[4].name().equals("UNKNOWN") && vs[4].ordinal() == 4, "UNKNOWN ordinal 4");
        // compareTo 严格全序（稳定性从高到低）
        for (int i = 0; i < vs.length - 1; i++)
            check(vs[i].compareTo(vs[i + 1]) < 0, vs[i] + " < " + vs[i + 1]);
        // valueOf 双向逐值
        for (AttributeMapper.AttributeStability v : vs)
            check(AttributeMapper.AttributeStability.valueOf(v.name()) == v, "valueOf round-trip " + v);
        // 假名 → IllegalArgumentException
        try {
            AttributeMapper.AttributeStability.valueOf("RELAXED");
            check(false, "valueOf(RELAXED) must throw");
        } catch (IllegalArgumentException e) {
            check(e.getMessage() != null && e.getMessage().contains("RELAXED"), "valueOf bogus IAE message: " + e.getMessage());
        }
        try {
            AttributeMapper.AttributeStability.valueOf(null);
            check(false, "valueOf(null) must throw");
        } catch (NullPointerException expected) {
            check(true, "valueOf(null) NPE");
        }
        // Enum 基础面
        check(AttributeMapper.AttributeStability.STATELESS.getDeclaringClass() == AttributeMapper.AttributeStability.class, "declaring class");
    }

    // ---- Part 4: 每个标准属性 stability 落点（实测表；抽 12 个断言非 UNSTABLE/UNKNOWN）----
    static void testStandardStabilities() {
        // 非 UNSTABLE 组：Deprecated/SourceFile/Signature/ConstantValue/Synthetic/Code/Exceptions/NestHost/Record/Module/InnerClasses
        check(Attributes.deprecated().stability() != AttributeMapper.AttributeStability.UNSTABLE
                && Attributes.deprecated().stability() != AttributeMapper.AttributeStability.UNKNOWN, "deprecated not UNSTABLE/UNKNOWN");
        check(Attributes.sourceFile().stability() == AttributeMapper.AttributeStability.CP_REFS, "sourceFile CP_REFS");
        check(Attributes.signature().stability() == AttributeMapper.AttributeStability.CP_REFS, "signature CP_REFS");
        check(Attributes.constantValue().stability() == AttributeMapper.AttributeStability.CP_REFS, "constantValue CP_REFS");
        check(Attributes.synthetic().stability() == AttributeMapper.AttributeStability.STATELESS, "synthetic STATELESS");
        check(Attributes.code().stability() == AttributeMapper.AttributeStability.CP_REFS, "code CP_REFS");
        check(Attributes.exceptions().stability() == AttributeMapper.AttributeStability.CP_REFS, "exceptions CP_REFS");
        check(Attributes.nestHost().stability() == AttributeMapper.AttributeStability.CP_REFS, "nestHost CP_REFS");
        check(Attributes.record().stability() == AttributeMapper.AttributeStability.CP_REFS, "record CP_REFS");
        check(Attributes.module().stability() == AttributeMapper.AttributeStability.CP_REFS, "module CP_REFS");
        check(Attributes.innerClasses().stability() == AttributeMapper.AttributeStability.CP_REFS, "innerClasses CP_REFS");
        // StackMapTable 实测落点：LABELS（码内 label 偏移敏感；非 UNSTABLE）
        check(Attributes.stackMapTable().stability() == AttributeMapper.AttributeStability.LABELS, "stackMapTable LABELS (offset-sensitive)");
        // 唯二 UNSTABLE：TypeAnnotation 族（引用码内偏移）
        check(Attributes.runtimeVisibleTypeAnnotations().stability() == AttributeMapper.AttributeStability.UNSTABLE, "runtimeVisibleTypeAnnotations UNSTABLE");
        check(Attributes.runtimeInvisibleTypeAnnotations().stability() == AttributeMapper.AttributeStability.UNSTABLE, "runtimeInvisibleTypeAnnotations UNSTABLE");
    }

    // ---- Part 5: 未注册自定义属性 → UNKNOWN（实测：parse 以 UnknownAttribute 保留，mapper.stability()==UNKNOWN）----
    static void testUnknownStabilityFromUnregisteredParse() throws Exception {
        check(AttributeMapper.AttributeStability.class.getDeclaringClass() == AttributeMapper.class, "AttributeStability nested in AttributeMapper");
        // 未注册自定义属性 parse 不抛、保留、stability UNKNOWN
        AttributeMapper<CA> unreg = new AttributeMapper<>() {
            public String name() { return "UnregAttr"; }
            public CA readAttribute(AttributedElement e, ClassReader cr, int p) { return new CA(this); }
            public void writeAttribute(BufWriter bw, CA a) { bw.writeIndex(bw.constantPool().utf8Entry("UnregAttr")); bw.writeInt(0); }
            public AttributeMapper.AttributeStability stability() { return AttributeMapper.AttributeStability.STATELESS; }
        };
        byte[] b = ClassFile.of(ClassFile.AttributeMapperOption.of(u -> u.equalsString("UnregAttr") ? unreg : null))
                .build(ClassDesc.of("ASU"), cb -> cb.with(new CA(unreg)));
        ClassModel cm = ClassFile.of().parse(b); // 未注册环境 parse
        check(!cm.attributes().isEmpty(), "unregistered parse keeps attribute");
        Attribute<?> kept = cm.attributes().get(0);
        check(kept.attributeMapper().name().equals("UnregAttr"), "unknown attr keeps its name");
        check(kept.attributeMapper().stability() == AttributeMapper.AttributeStability.UNKNOWN, "unregistered attr stability UNKNOWN: " + kept.attributeMapper().stability());
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testEnumOrderAndValueOf();
        testStandardStabilities();
        testUnknownStabilityFromUnregisteredParse();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
