/*
 * @test
 * @summary java.lang.classfile.attribute.RuntimeVisibleTypeAnnotationsAttribute conformance: white-box structure + black-box behavior.
 * @run main RuntimeVisibleTypeAnnotationsAttributeTest
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

public class RuntimeVisibleTypeAnnotationsAttributeTest {
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
        Class<?> c = Class.forName("java.lang.classfile.attribute.RuntimeVisibleTypeAnnotationsAttribute");
        check(c != null, "java.lang.classfile.attribute.RuntimeVisibleTypeAnnotationsAttribute loads");
        check(c.getName().equals("java.lang.classfile.attribute.RuntimeVisibleTypeAnnotationsAttribute"), "binary name java.lang.classfile.attribute.RuntimeVisibleTypeAnnotationsAttribute");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.attribute.RuntimeVisibleTypeAnnotationsAttribute is public");
        check(c.isInterface(), "java.lang.classfile.attribute.RuntimeVisibleTypeAnnotationsAttribute is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.attribute.RuntimeVisibleTypeAnnotationsAttribute has no superclass");
        check(declares(c, "annotations"), "declares annotations");
        check(declares(c, "of"), "declares of");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        TypeAnnotation ta = TypeAnnotation.of(TypeAnnotation.TargetInfo.ofField(), java.util.List.of(), Annotation.of(ClassDesc.of("java.lang.Deprecated")));
        RuntimeVisibleTypeAnnotationsAttribute a = RuntimeVisibleTypeAnnotationsAttribute.of(ta);
        check(a.annotations().size() == 1, "size");
        check(a.annotations().get(0).targetInfo().targetType() == TypeAnnotation.TargetType.FIELD, "target FIELD");

    }


    // ---- real-product members（TYPE_USE + RUNTIME → RVTA）----
    @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
    @java.lang.annotation.Target(java.lang.annotation.ElementType.TYPE_USE)
    @interface RVTAnno { int v() default 1; }
    static <@RVTAnno U> java.util.List<U> rvtM(U u) { return null; }
    @RVTAnno String rvtField;

    // ---- Part 3: javac 真产物（方法类型参数 + 字段两个 target）----
    static void testRealProduct() throws Exception {
        byte[] self = RuntimeVisibleTypeAnnotationsAttributeTest.class.getResourceAsStream("RuntimeVisibleTypeAnnotationsAttributeTest.class").readAllBytes();
        ClassModel cm = ClassFile.of().parse(self);
        MethodModel mm = null;
        for (MethodModel m : cm.methods()) if (m.methodName().stringValue().equals("rvtM")) mm = m;
        check(mm != null, "rvtM found");
        var at = mm.findAttribute(Attributes.runtimeVisibleTypeAnnotations()).get();
        check(at.annotations().size() == 1, "method RVTA count");
        check(at.annotations().get(0).targetInfo().targetType() == TypeAnnotation.TargetType.METHOD_TYPE_PARAMETER, "METHOD_TYPE_PARAMETER target");
        check(at.annotations().get(0).targetPath().isEmpty(), "empty target path");
        check(at.annotations().get(0).annotation().className().stringValue().equals("LRuntimeVisibleTypeAnnotationsAttributeTest$RVTAnno;"), "className");
        check(at.attributeMapper() == Attributes.runtimeVisibleTypeAnnotations(), "parsed mapper identity");
        check(mm.findAttribute(Attributes.runtimeInvisibleTypeAnnotations()).isEmpty(), "no RITA twin");
        FieldModel fm = null;
        for (FieldModel f : cm.fields()) if (f.fieldName().stringValue().equals("rvtField")) fm = f;
        var fat = fm.findAttribute(Attributes.runtimeVisibleTypeAnnotations()).get();
        check(fat.annotations().get(0).targetInfo().targetType() == TypeAnnotation.TargetType.FIELD, "FIELD target");
        check(fat.annotations().get(0).targetInfo() instanceof TypeAnnotation.EmptyTarget, "FIELD EmptyTarget instance");
    }

    // ---- Part 4: 工厂 of(List)/of(varargs) + null + 属性面（UNSTABLE 稳定性）----
    static void testFactoryOverloads() {
        TypeAnnotation t1 = TypeAnnotation.of(TypeAnnotation.TargetInfo.ofField(), List.of(),
                Annotation.of(ClassDesc.of("T1"), List.of()));
        TypeAnnotation t2 = TypeAnnotation.of(TypeAnnotation.TargetInfo.ofMethodReturn(),
                List.of(TypeAnnotation.TypePathComponent.of(TypeAnnotation.TypePathComponent.Kind.ARRAY, 0)),
                Annotation.of(ClassDesc.of("T2"), List.of(AnnotationElement.ofInt("n", 2))));
        RuntimeVisibleTypeAnnotationsAttribute at = RuntimeVisibleTypeAnnotationsAttribute.of(List.of(t1, t2));
        check(at.annotations().size() == 2 && at.annotations().get(0) == t1, "of(List) identity");
        check(RuntimeVisibleTypeAnnotationsAttribute.of(t1).annotations().get(0) == t1, "of(varargs)");
        check(RuntimeVisibleTypeAnnotationsAttribute.of(List.of()).annotations().isEmpty(), "of(empty)");
        check(at.attributeName().stringValue().equals("RuntimeVisibleTypeAnnotations")
                && at.attributeName().stringValue().equals(Attributes.NAME_RUNTIME_VISIBLE_TYPE_ANNOTATIONS), "NAME");
        check(at.attributeMapper() == Attributes.runtimeVisibleTypeAnnotations(), "mapper identity");
        check(at.attributeMapper().stability() == AttributeMapper.AttributeStability.UNSTABLE, "stability UNSTABLE");
        try { RuntimeVisibleTypeAnnotationsAttribute.of((List<TypeAnnotation>) null); check(false, "of(null List) must throw"); }
        catch (NullPointerException e) { check(true, "of(null List) NPE"); }
        try { RuntimeVisibleTypeAnnotationsAttribute.of((TypeAnnotation[]) null); check(false, "of(null varargs) must throw"); }
        catch (NullPointerException e) { check(true, "of(null varargs) NPE"); }
        // TypePathComponent 常量与 of(Kind,int)
        check(TypeAnnotation.TypePathComponent.ARRAY.typePathKind() == TypeAnnotation.TypePathComponent.Kind.ARRAY, "TPC.ARRAY constant");
        check(TypeAnnotation.TypePathComponent.INNER_TYPE.typePathKind() == TypeAnnotation.TypePathComponent.Kind.INNER_TYPE, "TPC.INNER_TYPE constant");
        check(TypeAnnotation.TypePathComponent.WILDCARD.typePathKind() == TypeAnnotation.TypePathComponent.Kind.WILDCARD, "TPC.WILDCARD constant");
        check(TypeAnnotation.TypePathComponent.Kind.values().length == 4, "Kind 4 values");
        check(TypeAnnotation.TypePathComponent.of(TypeAnnotation.TypePathComponent.Kind.TYPE_ARGUMENT, 1).typeArgumentIndex() == 1, "TPC.of(Kind,int)");
    }

    // ---- Part 5: 类/字段/方法三级注入→re-parse 回读 ----
    static void testInjectRoundTrip() throws Exception {
        TypeAnnotation tf = TypeAnnotation.of(TypeAnnotation.TargetInfo.ofField(), List.of(),
                Annotation.of(ClassDesc.of("F"), List.of()));
        TypeAnnotation tr = TypeAnnotation.of(TypeAnnotation.TargetInfo.ofMethodReturn(),
                List.of(TypeAnnotation.TypePathComponent.ARRAY),
                Annotation.of(ClassDesc.of("R"), List.of(AnnotationElement.ofInt("q", 3))));
        byte[] b = ClassFile.of().build(ClassDesc.of("DynRVTA"), cb -> cb
                .withSuperclass(ConstantDescs.CD_Object)
                .with(RuntimeVisibleTypeAnnotationsAttribute.of(List.of(tf, tr)))
                .withField("f", ConstantDescs.CD_Object, fb -> fb.with(RuntimeVisibleTypeAnnotationsAttribute.of(tf)))
                .withMethod("m", MethodTypeDesc.ofDescriptor("()V"), 0, mb -> mb
                        .with(RuntimeVisibleTypeAnnotationsAttribute.of(tr)).withCode(cob -> cob.return_())));
        ClassModel cm = ClassFile.of().parse(b);
        var cls = cm.findAttribute(Attributes.runtimeVisibleTypeAnnotations()).get().annotations();
        check(cls.size() == 2, "class inject count");
        check(cls.get(0).targetInfo().targetType() == TypeAnnotation.TargetType.FIELD, "t0 FIELD");
        check(cls.get(0).targetPath().isEmpty(), "t0 empty path");
        check(cls.get(1).targetInfo().targetType() == TypeAnnotation.TargetType.METHOD_RETURN, "t1 METHOD_RETURN");
        check(cls.get(1).targetPath().size() == 1
                && cls.get(1).targetPath().get(0).typePathKind() == TypeAnnotation.TypePathComponent.Kind.ARRAY, "t1 ARRAY path");
        check(cls.get(1).annotation().elements().get(0).value() instanceof AnnotationValue.OfInt, "t1 element OfInt");
        check(cm.fields().get(0).findAttribute(Attributes.runtimeVisibleTypeAnnotations()).get().annotations().size() == 1, "field inject");
        check(cm.methods().get(0).findAttribute(Attributes.runtimeVisibleTypeAnnotations()).get().annotations().get(0).targetInfo().targetType() == TypeAnnotation.TargetType.METHOD_RETURN, "method inject");
        check(cm.findAttribute(Attributes.runtimeVisibleTypeAnnotations()).get().attributeMapper() == Attributes.runtimeVisibleTypeAnnotations(), "parsed mapper identity");
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
