/*
 * @test
 * @summary java.lang.classfile.attribute.RuntimeVisibleParameterAnnotationsAttribute conformance: white-box structure + black-box behavior.
 * @run main RuntimeVisibleParameterAnnotationsAttributeTest
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

public class RuntimeVisibleParameterAnnotationsAttributeTest {
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
        Class<?> c = Class.forName("java.lang.classfile.attribute.RuntimeVisibleParameterAnnotationsAttribute");
        check(c != null, "java.lang.classfile.attribute.RuntimeVisibleParameterAnnotationsAttribute loads");
        check(c.getName().equals("java.lang.classfile.attribute.RuntimeVisibleParameterAnnotationsAttribute"), "binary name java.lang.classfile.attribute.RuntimeVisibleParameterAnnotationsAttribute");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.attribute.RuntimeVisibleParameterAnnotationsAttribute is public");
        check(c.isInterface(), "java.lang.classfile.attribute.RuntimeVisibleParameterAnnotationsAttribute is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.attribute.RuntimeVisibleParameterAnnotationsAttribute has no superclass");
        check(declares(c, "parameterAnnotations"), "declares parameterAnnotations");
        check(declares(c, "of"), "declares of");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        java.util.List<java.util.List<Annotation>> pas = java.util.List.of(java.util.List.of(Annotation.of(ClassDesc.of("java.lang.Deprecated"))));
        RuntimeVisibleParameterAnnotationsAttribute a = RuntimeVisibleParameterAnnotationsAttribute.of(pas);
        check(a.parameterAnnotations().size() == 1, "parameter count");
        check(a.parameterAnnotations().get(0).size() == 1, "annotation count");

    }


    // ---- real-product member（参数声明注解进 RVPA；未注解参数为空列表）----
    void rvpaM(@Deprecated int a, int b, @Deprecated String c) {}

    // ---- Part 3: javac 真产物 ----
    static void testRealProduct() throws Exception {
        byte[] self = RuntimeVisibleParameterAnnotationsAttributeTest.class.getResourceAsStream("RuntimeVisibleParameterAnnotationsAttributeTest.class").readAllBytes();
        ClassModel cm = ClassFile.of().parse(self);
        MethodModel mm = null;
        for (MethodModel m : cm.methods()) if (m.methodName().stringValue().equals("rvpaM")) mm = m;
        check(mm != null, "rvpaM found");
        var at = mm.findAttribute(Attributes.runtimeVisibleParameterAnnotations()).get();
        check(at.parameterAnnotations().size() == 3, "3 params -> 3 lists");
        check(at.parameterAnnotations().get(0).size() == 1, "param0 annotated");
        check(at.parameterAnnotations().get(1).isEmpty(), "param1 empty list");
        check(at.parameterAnnotations().get(2).size() == 1, "param2 annotated");
        check(at.parameterAnnotations().get(0).get(0).className().stringValue().equals("Ljava/lang/Deprecated;"), "param0 className");
        check(at.attributeMapper() == Attributes.runtimeVisibleParameterAnnotations(), "parsed mapper identity");
        check(mm.findAttribute(Attributes.runtimeInvisibleParameterAnnotations()).isEmpty(), "no RIPA twin");
        check(mm.findAttribute(Attributes.runtimeVisibleAnnotations()).isEmpty(), "param annos not in method RVA");
    }

    // ---- Part 4: 工厂（仅 of(List<List>)）+ null + 属性面 ----
    static void testFactoryOverloads() {
        Annotation d = Annotation.of(ClassDesc.of("D"), List.of());
        RuntimeVisibleParameterAnnotationsAttribute at = RuntimeVisibleParameterAnnotationsAttribute.of(
                List.of(List.of(d), List.of(), List.of(d, d)));
        check(at.parameterAnnotations().size() == 3, "of matrix size");
        check(at.parameterAnnotations().get(0).get(0) == d, "of identity");
        check(at.parameterAnnotations().get(1).isEmpty(), "empty param list kept");
        check(at.parameterAnnotations().get(2).size() == 2, "two annos per param kept");
        check(RuntimeVisibleParameterAnnotationsAttribute.of(List.of()).parameterAnnotations().isEmpty(), "of(empty)");
        check(at.attributeName().stringValue().equals("RuntimeVisibleParameterAnnotations")
                && at.attributeName().stringValue().equals(Attributes.NAME_RUNTIME_VISIBLE_PARAMETER_ANNOTATIONS), "NAME");
        check(at.attributeMapper() == Attributes.runtimeVisibleParameterAnnotations(), "mapper identity");
        check(at.attributeMapper().stability() == AttributeMapper.AttributeStability.CP_REFS, "stability CP_REFS");
        check(!at.attributeMapper().allowMultiple(), "allowMultiple false");
        // 无 varargs 重载（javap 锁定唯一 of）
        int ofCount = 0;
        for (Method m : RuntimeVisibleParameterAnnotationsAttribute.class.getMethods())
            if (m.getName().equals("of")) ofCount++;
        check(ofCount == 1, "single of overload");
        try { RuntimeVisibleParameterAnnotationsAttribute.of(null); check(false, "of(null) must throw"); }
        catch (NullPointerException e) { check(true, "of(null) NPE"); }
    }

    // ---- Part 5: 方法级注入→re-parse 矩阵回读 ----
    static void testInjectRoundTrip() throws Exception {
        Annotation d = Annotation.of(ClassDesc.of("P"), List.of(AnnotationElement.ofInt("i", 1)));
        byte[] b = ClassFile.of().build(ClassDesc.of("DynRVPA"), cb -> cb
                .withSuperclass(ConstantDescs.CD_Object)
                .withMethod("m", MethodTypeDesc.ofDescriptor("(II)I"), 0, mb -> mb
                        .with(RuntimeVisibleParameterAnnotationsAttribute.of(List.of(List.of(d), List.of())))
                        .withCode(cob -> cob.iconst_1().ireturn())));
        ClassModel cm = ClassFile.of().parse(b);
        var at = cm.methods().get(0).findAttribute(Attributes.runtimeVisibleParameterAnnotations()).get();
        check(at.parameterAnnotations().size() == 2, "inject 2x2 size");
        check(at.parameterAnnotations().get(0).size() == 1, "inject p0 count");
        check(at.parameterAnnotations().get(0).get(0).className().stringValue().equals("LP;"), "inject p0 className");
        check(at.parameterAnnotations().get(0).get(0).elements().get(0).name().stringValue().equals("i"), "inject element name");
        check(at.parameterAnnotations().get(1).isEmpty(), "inject p1 empty");
        check(at.attributeMapper() == Attributes.runtimeVisibleParameterAnnotations(), "parsed mapper identity");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testRealProduct();
        testFactoryOverloads();
        testInjectRoundTrip();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
