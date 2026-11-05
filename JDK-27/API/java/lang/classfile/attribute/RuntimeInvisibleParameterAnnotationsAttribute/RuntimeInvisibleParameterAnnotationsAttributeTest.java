/*
 * @test
 * @summary java.lang.classfile.attribute.RuntimeInvisibleParameterAnnotationsAttribute conformance: white-box structure + black-box behavior.
 * @run main RuntimeInvisibleParameterAnnotationsAttributeTest
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

public class RuntimeInvisibleParameterAnnotationsAttributeTest {
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
        Class<?> c = Class.forName("java.lang.classfile.attribute.RuntimeInvisibleParameterAnnotationsAttribute");
        check(c != null, "java.lang.classfile.attribute.RuntimeInvisibleParameterAnnotationsAttribute loads");
        check(c.getName().equals("java.lang.classfile.attribute.RuntimeInvisibleParameterAnnotationsAttribute"), "binary name java.lang.classfile.attribute.RuntimeInvisibleParameterAnnotationsAttribute");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.attribute.RuntimeInvisibleParameterAnnotationsAttribute is public");
        check(c.isInterface(), "java.lang.classfile.attribute.RuntimeInvisibleParameterAnnotationsAttribute is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.attribute.RuntimeInvisibleParameterAnnotationsAttribute has no superclass");
        check(declares(c, "parameterAnnotations"), "declares parameterAnnotations");
        check(declares(c, "of"), "declares of");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        java.util.List<java.util.List<Annotation>> pas = java.util.List.of(java.util.List.of(Annotation.of(ClassDesc.of("java.lang.Deprecated"))));
        RuntimeInvisibleParameterAnnotationsAttribute a = RuntimeInvisibleParameterAnnotationsAttribute.of(pas);
        check(a.parameterAnnotations().size() == 1, "parameter count");
        check(a.parameterAnnotations().get(0).size() == 1, "annotation count");

    }


    // ---- real-product member（CLASS 保留参数注解 → RIPA）----
    @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.CLASS)
    @java.lang.annotation.Target(java.lang.annotation.ElementType.PARAMETER)
    @interface RIPAnno {}
    void ripaM(@RIPAnno int x, int y) {}

    // ---- Part 3: javac 真产物 ----
    static void testRealProduct() throws Exception {
        byte[] self = RuntimeInvisibleParameterAnnotationsAttributeTest.class.getResourceAsStream("RuntimeInvisibleParameterAnnotationsAttributeTest.class").readAllBytes();
        ClassModel cm = ClassFile.of().parse(self);
        MethodModel mm = null;
        for (MethodModel m : cm.methods()) if (m.methodName().stringValue().equals("ripaM")) mm = m;
        check(mm != null, "ripaM found");
        var at = mm.findAttribute(Attributes.runtimeInvisibleParameterAnnotations()).get();
        check(at.parameterAnnotations().size() == 2, "2 params -> 2 lists");
        check(at.parameterAnnotations().get(0).size() == 1, "param0 annotated");
        check(at.parameterAnnotations().get(0).get(0).className().stringValue().equals("LRuntimeInvisibleParameterAnnotationsAttributeTest$RIPAnno;"), "param0 className");
        check(at.parameterAnnotations().get(1).isEmpty(), "param1 empty");
        check(at.attributeMapper() == Attributes.runtimeInvisibleParameterAnnotations(), "parsed mapper identity");
        check(mm.findAttribute(Attributes.runtimeVisibleParameterAnnotations()).isEmpty(), "no RVPA twin");
    }

    // ---- Part 4: 工厂（仅 of(List<List>)）+ null + 属性面 ----
    static void testFactoryOverloads() {
        Annotation a = Annotation.of(ClassDesc.of("X"), List.of());
        RuntimeInvisibleParameterAnnotationsAttribute at = RuntimeInvisibleParameterAnnotationsAttribute.of(
                List.of(List.of(a), List.of()));
        check(at.parameterAnnotations().size() == 2 && at.parameterAnnotations().get(0).get(0) == a, "of identity");
        check(RuntimeInvisibleParameterAnnotationsAttribute.of(List.of()).parameterAnnotations().isEmpty(), "of(empty)");
        check(at.attributeName().stringValue().equals("RuntimeInvisibleParameterAnnotations")
                && at.attributeName().stringValue().equals(Attributes.NAME_RUNTIME_INVISIBLE_PARAMETER_ANNOTATIONS), "NAME");
        check(at.attributeMapper() == Attributes.runtimeInvisibleParameterAnnotations(), "mapper identity");
        check(at.attributeMapper().stability() == AttributeMapper.AttributeStability.CP_REFS, "stability CP_REFS");
        check(!at.attributeMapper().allowMultiple(), "allowMultiple false");
        int ofCount = 0;
        for (Method m : RuntimeInvisibleParameterAnnotationsAttribute.class.getMethods())
            if (m.getName().equals("of")) ofCount++;
        check(ofCount == 1, "single of overload");
        try { RuntimeInvisibleParameterAnnotationsAttribute.of(null); check(false, "of(null) must throw"); }
        catch (NullPointerException e) { check(true, "of(null) NPE"); }
    }

    // ---- Part 5: 方法级注入→re-parse 回读 ----
    static void testInjectRoundTrip() throws Exception {
        Annotation a = Annotation.of(ClassDesc.of("IP"), List.of(AnnotationElement.ofString("k", "v")));
        byte[] b = ClassFile.of().build(ClassDesc.of("DynRIPA"), cb -> cb
                .withSuperclass(ConstantDescs.CD_Object)
                .withMethod("m", MethodTypeDesc.ofDescriptor("(Ljava/lang/String;I)V"), 0, mb -> mb
                        .with(RuntimeInvisibleParameterAnnotationsAttribute.of(List.of(List.of(a), List.of())))
                        .withCode(cob -> cob.return_())));
        ClassModel cm = ClassFile.of().parse(b);
        var at = cm.methods().get(0).findAttribute(Attributes.runtimeInvisibleParameterAnnotations()).get();
        check(at.parameterAnnotations().size() == 2, "inject size");
        check(at.parameterAnnotations().get(0).get(0).className().stringValue().equals("LIP;"), "inject className");
        check(at.parameterAnnotations().get(1).isEmpty(), "inject empty param");
        check(at.attributeMapper() == Attributes.runtimeInvisibleParameterAnnotations(), "parsed mapper identity");
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
