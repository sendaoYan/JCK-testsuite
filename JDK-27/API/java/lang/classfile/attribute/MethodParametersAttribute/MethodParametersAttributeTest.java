/*
 * @test
 * @summary java.lang.classfile.attribute.MethodParametersAttribute conformance: white-box structure + black-box behavior.
 * @run main MethodParametersAttributeTest
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

public class MethodParametersAttributeTest {
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
        Class<?> c = Class.forName("java.lang.classfile.attribute.MethodParametersAttribute");
        check(c != null, "java.lang.classfile.attribute.MethodParametersAttribute loads");
        check(c.getName().equals("java.lang.classfile.attribute.MethodParametersAttribute"), "binary name java.lang.classfile.attribute.MethodParametersAttribute");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.attribute.MethodParametersAttribute is public");
        check(c.isInterface(), "java.lang.classfile.attribute.MethodParametersAttribute is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.attribute.MethodParametersAttribute has no superclass");
        check(declares(c, "parameters"), "declares parameters");
        check(declares(c, "of"), "declares of");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        MethodParameterInfo p = MethodParameterInfo.ofParameter(java.util.Optional.of("x"), 0);
        MethodParametersAttribute a = MethodParametersAttribute.of(p);
        check(a.parameters().size() == 1, "size");
        check(a.parameters().get(0).name().get().stringValue().equals("x"), "name");

    }

    // ---- Part 3: 双工厂 + null/u1 负例 + 元数据 ----
    static void testFactories() {
        MethodParameterInfo p0 = MethodParameterInfo.ofParameter(Optional.empty(), 0);
        MethodParameterInfo p1 = MethodParameterInfo.ofParameter(Optional.of("x"), ClassFile.ACC_FINAL);
        MethodParametersAttribute a1 = MethodParametersAttribute.of(List.of(p0, p1));
        check(a1.parameters().size() == 2 && a1.parameters().get(0) == p0, "of(List) identity");
        MethodParametersAttribute a2 = MethodParametersAttribute.of(p0, p1);
        check(a2.parameters().size() == 2, "of(varargs) size");
        check(a2.parameters().get(1).name().get().stringValue().equals("x"), "varargs entry readback");
        check(MethodParametersAttribute.of(List.of()).parameters().isEmpty(), "empty list ok");
        check(MethodParametersAttribute.of().parameters().isEmpty(), "empty varargs ok");
        // null 负例
        try { MethodParametersAttribute.of((List<MethodParameterInfo>) null); check(false, "of((List)null) must NPE"); }
        catch (NullPointerException e) { check(true, "list null NPE"); }
        // 条目数 u1 边界（method_parameters 计数是 u1，最多 255）
        check(MethodParametersAttribute.of(Collections.nCopies(255, p0)).parameters().size() == 255, "255 ok");
        try {
            MethodParametersAttribute.of(Collections.nCopies(300, p0));
            check(false, "300 must IAE");
        } catch (IllegalArgumentException e) {
            check(e.getMessage() != null && e.getMessage().contains("u1"), "u1 IAE msg: " + e.getMessage());
        }
        // 元数据
        check(a1.attributeName().stringValue().equals(Attributes.NAME_METHOD_PARAMETERS), "attributeName==NAME_METHOD_PARAMETERS");
        check(a1.attributeName().stringValue().equals("MethodParameters"), "attributeName literal");
        check(a1.attributeMapper() == Attributes.methodParameters(), "attributeMapper==Attributes.methodParameters()");
        check(a1.attributeMapper().stability() == AttributeMapper.AttributeStability.CP_REFS, "stability CP_REFS");
        check(!a1.attributeMapper().allowMultiple(), "allowMultiple false");
    }

    // ---- Part 4: build 注入 → re-parse 参数序 + flags 全表 ----
    static void testBuildRoundtrip() {
        ClassFile cf = ClassFile.of();
        ConstantPoolBuilder cp = ConstantPoolBuilder.of();
        MethodParametersAttribute attr = MethodParametersAttribute.of(
                MethodParameterInfo.ofParameter(Optional.of("a"), 0),
                MethodParameterInfo.ofParameter(Optional.empty(), ClassFile.ACC_FINAL),
                MethodParameterInfo.of(Optional.of("c"), AccessFlag.SYNTHETIC),
                MethodParameterInfo.of(Optional.of(cp.utf8Entry("d")), ClassFile.ACC_MANDATED));
        byte[] b = cf.build(ClassDesc.of("MPAT1"), cb -> cb
                .withMethod("m", MethodTypeDesc.ofDescriptor("(IIII)V"), ClassFile.ACC_STATIC, mb -> {
                    mb.withCode(c -> c.return_());
                    mb.with(attr);
                }));
        MethodModel mm = cf.parse(b).methods().get(0);
        var found = mm.findAttribute(Attributes.methodParameters());
        check(found.isPresent(), "injected present");
        List<MethodParameterInfo> ps = found.get().parameters();
        check(ps.size() == 4, "4 params");
        check(ps.stream().map(p -> p.name().map(Utf8Entry::stringValue).orElse("-")).toList()
                .equals(List.of("a", "-", "c", "d")), "param order+names");
        check(ps.stream().map(MethodParameterInfo::flagsMask).toList()
                .equals(List.of(0, 0x0010, 0x1000, 0x8000)), "flags mask table 0/10/1000/8000");
        check(ps.get(1).flags().equals(Set.of(AccessFlag.FINAL)), "FINAL set");
        check(ps.get(2).flags().equals(Set.of(AccessFlag.SYNTHETIC)), "SYNTHETIC set");
        check(ps.get(3).flags().equals(Set.of(AccessFlag.MANDATED)), "MANDATED set");
        // MethodElement 通道
        boolean asElem = false;
        for (MethodElement el : mm.elementList()) if (el instanceof MethodParametersAttribute) asElem = true;
        check(asElem, "visible as MethodElement");
        check(cf.verify(b).isEmpty(), "injected class verifies");
    }

    // ---- Part 5: javac 默认不产（-parameters 才产）----
    static void testRealClass() throws Exception {
        byte[] self = MethodParametersAttributeTest.class.getResourceAsStream("MethodParametersAttributeTest.class").readAllBytes();
        boolean any = false;
        for (MethodModel mm : ClassFile.of().parse(self).methods())
            any |= mm.findAttribute(Attributes.methodParameters()).isPresent();
        check(!any, "javac without -parameters emits no MethodParameters");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testFactories();
        testBuildRoundtrip();
        testRealClass();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
