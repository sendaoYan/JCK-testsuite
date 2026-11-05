/*
 * @test
 * @summary java.lang.classfile.attribute.MethodParameterInfo conformance: white-box structure + black-box behavior.
 * @run main MethodParameterInfoTest
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

public class MethodParameterInfoTest {
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
        Class<?> c = Class.forName("java.lang.classfile.attribute.MethodParameterInfo");
        check(c != null, "java.lang.classfile.attribute.MethodParameterInfo loads");
        check(c.getName().equals("java.lang.classfile.attribute.MethodParameterInfo"), "binary name java.lang.classfile.attribute.MethodParameterInfo");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.attribute.MethodParameterInfo is public");
        check(c.isInterface(), "java.lang.classfile.attribute.MethodParameterInfo is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.attribute.MethodParameterInfo has no superclass");
        check(declares(c, "name"), "declares name");
        check(declares(c, "flagsMask"), "declares flagsMask");
        check(declares(c, "flags"), "declares flags");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        MethodParameterInfo p = MethodParameterInfo.ofParameter(java.util.Optional.of("x"), 0);
        check(p.name().get().stringValue().equals("x"), "name");
        check(p.flagsMask() == 0, "flagsMask");
        check(!p.has(AccessFlag.FINAL), "no FINAL");
        MethodParameterInfo p2 = MethodParameterInfo.of(java.util.Optional.empty(), AccessFlag.FINAL);
        check(p2.name().isEmpty() && p2.has(AccessFlag.FINAL), "FINAL flag");

    }

    // ---- Part 3: 三工厂 + flags 语义矩阵 ----
    static void testFactories() {
        ConstantPoolBuilder cp = ConstantPoolBuilder.of();
        // ofParameter(Optional<String>, int)：命名 + 0 flags
        MethodParameterInfo a = MethodParameterInfo.ofParameter(Optional.of("a"), 0);
        check(a.name().get().stringValue().equals("a"), "ofParameter name");
        check(a.flagsMask() == 0 && a.flags().isEmpty(), "ofParameter zero flags");
        // ofParameter：未命名 + FINAL
        MethodParameterInfo b = MethodParameterInfo.ofParameter(Optional.empty(), ClassFile.ACC_FINAL);
        check(b.name().isEmpty(), "unnamed");
        check(b.flagsMask() == 0x0010, "FINAL mask 0x10");
        check(b.flags().equals(Set.of(AccessFlag.FINAL)), "flags [FINAL]");
        check(b.has(AccessFlag.FINAL) && !b.has(AccessFlag.SYNTHETIC), "has()");
        // ofParameter：SYNTHETIC / MANDATED
        MethodParameterInfo s = MethodParameterInfo.ofParameter(Optional.of("s"), ClassFile.ACC_SYNTHETIC);
        check(s.flagsMask() == 0x1000 && s.has(AccessFlag.SYNTHETIC), "SYNTHETIC 0x1000");
        MethodParameterInfo md = MethodParameterInfo.ofParameter(Optional.empty(), ClassFile.ACC_MANDATED);
        check(md.flagsMask() == 0x8000 && md.has(AccessFlag.MANDATED), "MANDATED 0x8000");
        // 组合 flags
        MethodParameterInfo combo = MethodParameterInfo.ofParameter(Optional.of("c"), ClassFile.ACC_FINAL | ClassFile.ACC_SYNTHETIC);
        check(combo.flags().equals(Set.of(AccessFlag.FINAL, AccessFlag.SYNTHETIC)), "combo flags set");
        // of(Optional<Utf8Entry>, int)：池条目名
        MethodParameterInfo u = MethodParameterInfo.of(Optional.of(cp.utf8Entry("u")), ClassFile.ACC_MANDATED);
        check(u.name().get().stringValue().equals("u") && u.has(AccessFlag.MANDATED), "Utf8Entry name factory");
        // of(Optional<String>, AccessFlag...)
        MethodParameterInfo f = MethodParameterInfo.of(Optional.of("f"), AccessFlag.SYNTHETIC, AccessFlag.MANDATED);
        check(f.flagsMask() == (ClassFile.ACC_SYNTHETIC | ClassFile.ACC_MANDATED), "flag-varargs mask");
        check(f.flags().equals(Set.of(AccessFlag.SYNTHETIC, AccessFlag.MANDATED)), "flag-varargs set");
        check(MethodParameterInfo.of(Optional.empty()).flagsMask() == 0, "empty flag-varargs -> 0");
    }

    // ---- Part 4: null / u2 负例 ----
    static void testNegatives() {
        try { MethodParameterInfo.ofParameter(Optional.of("a"), -1); check(false, "neg flags must IAE"); }
        catch (IllegalArgumentException e) {
            check(e.getMessage() != null && e.getMessage().contains("flags"), "flags IAE msg: " + e.getMessage());
        }
        try { MethodParameterInfo.ofParameter(Optional.of("a"), 0x10000); check(false, "flags 65536 must IAE"); }
        catch (IllegalArgumentException e) { check(true, "flags 65536 IAE"); }
        check(MethodParameterInfo.ofParameter(Optional.of("a"), 0xFFFF).flagsMask() == 0xFFFF, "u2 max ok");
        try { MethodParameterInfo.of(null, 0); check(false, "of((Optional<Utf8Entry>)null,0) must NPE"); }
        catch (NullPointerException e) { check(true, "Optional null NPE"); }
    }

    // ---- Part 5: 注入 MethodParameters → re-parse 回读 ----
    static void testBuildRoundtrip() {
        ClassFile cf = ClassFile.of();
        MethodParametersAttribute attr = MethodParametersAttribute.of(
                MethodParameterInfo.ofParameter(Optional.of("a"), 0),
                MethodParameterInfo.ofParameter(Optional.empty(), ClassFile.ACC_FINAL),
                MethodParameterInfo.of(Optional.of("c"), AccessFlag.SYNTHETIC));
        byte[] b = cf.build(ClassDesc.of("MPIT1"), cb -> cb
                .withMethod("m", MethodTypeDesc.ofDescriptor("(III)I"), ClassFile.ACC_STATIC, mb -> {
                    mb.withCode(c -> c.iconst_1().ireturn());
                    mb.with(attr);
                }));
        MethodParametersAttribute r = cf.parse(b).methods().get(0).findAttribute(Attributes.methodParameters()).get();
        check(r.parameters().size() == 3, "3 params roundtrip");
        check(r.parameters().get(0).name().get().stringValue().equals("a") && r.parameters().get(0).flagsMask() == 0, "param a");
        check(r.parameters().get(1).name().isEmpty() && r.parameters().get(1).has(AccessFlag.FINAL), "param unnamed FINAL");
        check(r.parameters().get(2).name().get().stringValue().equals("c") && r.parameters().get(2).flagsMask() == 0x1000, "param c SYNTHETIC");
        check(r.attributeName().stringValue().equals(Attributes.NAME_METHOD_PARAMETERS), "attributeName==NAME_METHOD_PARAMETERS");
        check(r.attributeMapper() == Attributes.methodParameters(), "attributeMapper==Attributes.methodParameters()");
        check(r.attributeMapper().stability() == AttributeMapper.AttributeStability.CP_REFS, "stability CP_REFS");
        check(!r.attributeMapper().allowMultiple(), "allowMultiple false");
    }

    // ---- Part 6: javac 默认不产 MethodParameters（需 -parameters）----
    static void testRealClass() throws Exception {
        byte[] self = MethodParameterInfoTest.class.getResourceAsStream("MethodParameterInfoTest.class").readAllBytes();
        boolean any = false;
        for (MethodModel mm : ClassFile.of().parse(self).methods())
            any |= mm.findAttribute(Attributes.methodParameters()).isPresent();
        check(!any, "javac without -parameters emits no MethodParameters");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testFactories();
        testNegatives();
        testBuildRoundtrip();
        testRealClass();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
