/*
 * @test
 * @summary java.lang.classfile.attribute.RuntimeInvisibleAnnotationsAttribute conformance: white-box structure + black-box behavior.
 * @run main RuntimeInvisibleAnnotationsAttributeTest
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

public class RuntimeInvisibleAnnotationsAttributeTest {
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
        Class<?> c = Class.forName("java.lang.classfile.attribute.RuntimeInvisibleAnnotationsAttribute");
        check(c != null, "java.lang.classfile.attribute.RuntimeInvisibleAnnotationsAttribute loads");
        check(c.getName().equals("java.lang.classfile.attribute.RuntimeInvisibleAnnotationsAttribute"), "binary name java.lang.classfile.attribute.RuntimeInvisibleAnnotationsAttribute");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.attribute.RuntimeInvisibleAnnotationsAttribute is public");
        check(c.isInterface(), "java.lang.classfile.attribute.RuntimeInvisibleAnnotationsAttribute is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.attribute.RuntimeInvisibleAnnotationsAttribute has no superclass");
        check(declares(c, "annotations"), "declares annotations");
        check(declares(c, "of"), "declares of");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        Annotation an = Annotation.of(ClassDesc.of("java.lang.SuppressWarnings"), AnnotationElement.ofString("value", "unchecked"));
        RuntimeInvisibleAnnotationsAttribute a = RuntimeInvisibleAnnotationsAttribute.of(an);
        check(a.annotations().size() == 1, "size");
        check(a.annotations().get(0).classSymbol().equals(ClassDesc.of("java.lang.SuppressWarnings")), "classSymbol");

    }


    // ---- real-product members（CLASS 保留（默认）→ RuntimeInvisibleAnnotations）----
    @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.CLASS)
    @interface RIAAnno { int n() default 5; }
    @RIAAnno void riDep() {}
    @RIAAnno static int riField;
    @RIAAnno static class RINested {}

    // ---- Part 3: javac 真产物 ----
    static void testRealProduct() throws Exception {
        byte[] self = RuntimeInvisibleAnnotationsAttributeTest.class.getResourceAsStream("RuntimeInvisibleAnnotationsAttributeTest.class").readAllBytes();
        ClassModel cm = ClassFile.of().parse(self);
        MethodModel mm = null;
        for (MethodModel m : cm.methods()) if (m.methodName().stringValue().equals("riDep")) mm = m;
        check(mm != null, "riDep found");
        var at = mm.findAttribute(Attributes.runtimeInvisibleAnnotations()).get();
        check(at.annotations().size() == 1, "method RIA count");
        check(at.annotations().get(0).className().stringValue().equals("LRuntimeInvisibleAnnotationsAttributeTest$RIAAnno;"), "method RIA className");
        check(at.attributeMapper() == Attributes.runtimeInvisibleAnnotations(), "parsed mapper identity");
        check(mm.findAttribute(Attributes.runtimeVisibleAnnotations()).isEmpty(), "CLASS retention not visible");
        FieldModel fm = null;
        for (FieldModel f : cm.fields()) if (f.fieldName().stringValue().equals("riField")) fm = f;
        check(fm.findAttribute(Attributes.runtimeInvisibleAnnotations()).get().annotations().size() == 1, "field RIA");
        byte[] nb = RuntimeInvisibleAnnotationsAttributeTest.class.getResourceAsStream("RuntimeInvisibleAnnotationsAttributeTest$RINested.class").readAllBytes();
        check(ClassFile.of().parse(nb).findAttribute(Attributes.runtimeInvisibleAnnotations()).get().annotations().size() == 1, "class-level RIA");
    }

    // ---- Part 4: 工厂 + null + 属性面 ----
    static void testFactoryOverloads() {
        Annotation a1 = Annotation.of(ClassDesc.of("T1"), List.of(AnnotationElement.ofLong("l", 9L)));
        RuntimeInvisibleAnnotationsAttribute at = RuntimeInvisibleAnnotationsAttribute.of(List.of(a1));
        check(at.annotations().get(0) == a1, "of(List) identity");
        check(RuntimeInvisibleAnnotationsAttribute.of(a1).annotations().get(0) == a1, "of(varargs)");
        check(RuntimeInvisibleAnnotationsAttribute.of(List.of()).annotations().isEmpty(), "of(empty)");
        check(at.attributeName().stringValue().equals("RuntimeInvisibleAnnotations")
                && at.attributeName().stringValue().equals(Attributes.NAME_RUNTIME_INVISIBLE_ANNOTATIONS), "NAME");
        check(at.attributeMapper() == Attributes.runtimeInvisibleAnnotations(), "mapper identity");
        check(at.attributeMapper().stability() == AttributeMapper.AttributeStability.CP_REFS, "stability CP_REFS");
        check(!at.attributeMapper().allowMultiple(), "allowMultiple false");
        try { RuntimeInvisibleAnnotationsAttribute.of((List<Annotation>) null); check(false, "of(null List) must throw"); }
        catch (NullPointerException e) { check(true, "of(null List) NPE"); }
        try { RuntimeInvisibleAnnotationsAttribute.of((Annotation[]) null); check(false, "of(null varargs) must throw"); }
        catch (NullPointerException e) { check(true, "of(null varargs) NPE"); }
    }

    // ---- Part 5: 三级注入→re-parse 全回读 ----
    static void testInjectRoundTrip() throws Exception {
        Annotation ann = Annotation.of(ClassDesc.of("IInj"), List.of(AnnotationElement.ofBoolean("b", true)));
        byte[] b = ClassFile.of().build(ClassDesc.of("DynRIA"), cb -> cb
                .withSuperclass(ConstantDescs.CD_Object)
                .with(RuntimeInvisibleAnnotationsAttribute.of(List.of(ann)))
                .withField("f", ConstantDescs.CD_int, fb -> fb.with(RuntimeInvisibleAnnotationsAttribute.of(ann)))
                .withMethod("m", MethodTypeDesc.ofDescriptor("()V"), 0, mb -> mb
                        .with(RuntimeInvisibleAnnotationsAttribute.of(ann)).withCode(cob -> cob.return_())));
        ClassModel cm = ClassFile.of().parse(b);
        var cls = cm.findAttribute(Attributes.runtimeInvisibleAnnotations()).get().annotations().get(0);
        check(cls.className().stringValue().equals("LIInj;"), "class inject className");
        check(cls.elements().get(0).name().stringValue().equals("b"), "element name");
        check(cls.elements().get(0).value() instanceof AnnotationValue.OfBoolean
                && ((AnnotationValue.OfBoolean) cls.elements().get(0).value()).booleanValue(), "element OfBoolean value");
        check(cm.fields().get(0).findAttribute(Attributes.runtimeInvisibleAnnotations()).get().annotations().size() == 1, "field inject");
        check(cm.methods().get(0).findAttribute(Attributes.runtimeInvisibleAnnotations()).get().annotations().size() == 1, "method inject");
        check(cm.findAttribute(Attributes.runtimeInvisibleAnnotations()).get().attributeMapper() == Attributes.runtimeInvisibleAnnotations(), "parsed mapper identity");
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
