/*
 * @test
 * @summary java.lang.classfile.attribute.RuntimeInvisibleTypeAnnotationsAttribute conformance: white-box structure + black-box behavior.
 * @run main RuntimeInvisibleTypeAnnotationsAttributeTest
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

public class RuntimeInvisibleTypeAnnotationsAttributeTest {
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
        Class<?> c = Class.forName("java.lang.classfile.attribute.RuntimeInvisibleTypeAnnotationsAttribute");
        check(c != null, "java.lang.classfile.attribute.RuntimeInvisibleTypeAnnotationsAttribute loads");
        check(c.getName().equals("java.lang.classfile.attribute.RuntimeInvisibleTypeAnnotationsAttribute"), "binary name java.lang.classfile.attribute.RuntimeInvisibleTypeAnnotationsAttribute");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.attribute.RuntimeInvisibleTypeAnnotationsAttribute is public");
        check(c.isInterface(), "java.lang.classfile.attribute.RuntimeInvisibleTypeAnnotationsAttribute is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.attribute.RuntimeInvisibleTypeAnnotationsAttribute has no superclass");
        check(declares(c, "annotations"), "declares annotations");
        check(declares(c, "of"), "declares of");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        TypeAnnotation ta = TypeAnnotation.of(TypeAnnotation.TargetInfo.ofMethodReturn(), java.util.List.of(), Annotation.of(ClassDesc.of("java.lang.Deprecated")));
        RuntimeInvisibleTypeAnnotationsAttribute a = RuntimeInvisibleTypeAnnotationsAttribute.of(ta);
        check(a.annotations().size() == 1, "size");
        check(a.annotations().get(0).targetInfo().targetType() == TypeAnnotation.TargetType.METHOD_RETURN, "target METHOD_RETURN");

    }


    // ---- real-product member（TYPE_USE + CLASS 保留 → RITA）----
    @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.CLASS)
    @java.lang.annotation.Target(java.lang.annotation.ElementType.TYPE_USE)
    @interface RITAnno {}
    @RITAnno String ritField;

    // ---- Part 3: javac 真产物 ----
    static void testRealProduct() throws Exception {
        byte[] self = RuntimeInvisibleTypeAnnotationsAttributeTest.class.getResourceAsStream("RuntimeInvisibleTypeAnnotationsAttributeTest.class").readAllBytes();
        ClassModel cm = ClassFile.of().parse(self);
        FieldModel fm = null;
        for (FieldModel f : cm.fields()) if (f.fieldName().stringValue().equals("ritField")) fm = f;
        check(fm != null, "ritField found");
        var at = fm.findAttribute(Attributes.runtimeInvisibleTypeAnnotations()).get();
        check(at.annotations().size() == 1, "field RITA count");
        check(at.annotations().get(0).targetInfo().targetType() == TypeAnnotation.TargetType.FIELD, "FIELD target");
        check(at.annotations().get(0).annotation().className().stringValue().equals("LRuntimeInvisibleTypeAnnotationsAttributeTest$RITAnno;"), "className");
        check(at.attributeMapper() == Attributes.runtimeInvisibleTypeAnnotations(), "parsed mapper identity");
        check(fm.findAttribute(Attributes.runtimeVisibleTypeAnnotations()).isEmpty(), "no RVTA twin");
    }

    // ---- Part 4: 工厂 + null + 属性面（UNSTABLE）----
    static void testFactoryOverloads() {
        TypeAnnotation t1 = TypeAnnotation.of(TypeAnnotation.TargetInfo.ofField(), List.of(),
                Annotation.of(ClassDesc.of("IT1"), List.of()));
        RuntimeInvisibleTypeAnnotationsAttribute at = RuntimeInvisibleTypeAnnotationsAttribute.of(List.of(t1));
        check(at.annotations().get(0) == t1, "of(List) identity");
        check(RuntimeInvisibleTypeAnnotationsAttribute.of(t1).annotations().get(0) == t1, "of(varargs)");
        check(RuntimeInvisibleTypeAnnotationsAttribute.of(List.of()).annotations().isEmpty(), "of(empty)");
        check(at.attributeName().stringValue().equals("RuntimeInvisibleTypeAnnotations")
                && at.attributeName().stringValue().equals(Attributes.NAME_RUNTIME_INVISIBLE_TYPE_ANNOTATIONS), "NAME");
        check(at.attributeMapper() == Attributes.runtimeInvisibleTypeAnnotations(), "mapper identity");
        check(at.attributeMapper().stability() == AttributeMapper.AttributeStability.UNSTABLE, "stability UNSTABLE");
        try { RuntimeInvisibleTypeAnnotationsAttribute.of((List<TypeAnnotation>) null); check(false, "of(null List) must throw"); }
        catch (NullPointerException e) { check(true, "of(null List) NPE"); }
        try { RuntimeInvisibleTypeAnnotationsAttribute.of((TypeAnnotation[]) null); check(false, "of(null varargs) must throw"); }
        catch (NullPointerException e) { check(true, "of(null varargs) NPE"); }
    }

    // ---- Part 5: 类/字段/方法三级注入→re-parse 回读 ----
    static void testInjectRoundTrip() throws Exception {
        TypeAnnotation t = TypeAnnotation.of(TypeAnnotation.TargetInfo.ofField(), List.of(),
                Annotation.of(ClassDesc.of("IF"), List.of(AnnotationElement.ofDouble("d", 1.5))));
        TypeAnnotation tr = TypeAnnotation.of(TypeAnnotation.TargetInfo.ofThrows(0), List.of(),
                Annotation.of(ClassDesc.of("IT"), List.of()));
        byte[] b = ClassFile.of().build(ClassDesc.of("DynRITA"), cb -> cb
                .withSuperclass(ConstantDescs.CD_Object)
                .with(RuntimeInvisibleTypeAnnotationsAttribute.of(List.of(t, tr)))
                .withField("f", ConstantDescs.CD_Object, fb -> fb.with(RuntimeInvisibleTypeAnnotationsAttribute.of(t)))
                .withMethod("m", MethodTypeDesc.ofDescriptor("()V"), 0, mb -> mb
                        .with(RuntimeInvisibleTypeAnnotationsAttribute.of(tr)).withCode(cob -> cob.return_())));
        ClassModel cm = ClassFile.of().parse(b);
        var cls = cm.findAttribute(Attributes.runtimeInvisibleTypeAnnotations()).get().annotations();
        check(cls.size() == 2, "class inject count");
        check(cls.get(0).targetInfo().targetType() == TypeAnnotation.TargetType.FIELD, "t0 FIELD");
        check(cls.get(0).annotation().elements().get(0).value() instanceof AnnotationValue.OfDouble, "t0 element OfDouble");
        check(cls.get(1).targetInfo().targetType() == TypeAnnotation.TargetType.THROWS, "t1 THROWS");
        check(cls.get(1).targetInfo() instanceof TypeAnnotation.ThrowsTarget, "t1 ThrowsTarget instance");
        check(cm.fields().get(0).findAttribute(Attributes.runtimeInvisibleTypeAnnotations()).get().annotations().size() == 1, "field inject");
        check(cm.methods().get(0).findAttribute(Attributes.runtimeInvisibleTypeAnnotations()).get().annotations().size() == 1, "method inject");
        check(cm.findAttribute(Attributes.runtimeInvisibleTypeAnnotations()).get().attributeMapper() == Attributes.runtimeInvisibleTypeAnnotations(), "parsed mapper identity");
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
