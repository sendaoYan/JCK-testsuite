/*
 * @test
 * @summary java.lang.classfile.attribute.RuntimeVisibleAnnotationsAttribute conformance: white-box structure + black-box behavior.
 * @run main RuntimeVisibleAnnotationsAttributeTest
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

public class RuntimeVisibleAnnotationsAttributeTest {
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
        Class<?> c = Class.forName("java.lang.classfile.attribute.RuntimeVisibleAnnotationsAttribute");
        check(c != null, "java.lang.classfile.attribute.RuntimeVisibleAnnotationsAttribute loads");
        check(c.getName().equals("java.lang.classfile.attribute.RuntimeVisibleAnnotationsAttribute"), "binary name java.lang.classfile.attribute.RuntimeVisibleAnnotationsAttribute");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.attribute.RuntimeVisibleAnnotationsAttribute is public");
        check(c.isInterface(), "java.lang.classfile.attribute.RuntimeVisibleAnnotationsAttribute is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.attribute.RuntimeVisibleAnnotationsAttribute has no superclass");
        check(declares(c, "annotations"), "declares annotations");
        check(declares(c, "of"), "declares of");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        Annotation an = Annotation.of(ClassDesc.of("java.lang.Deprecated"), AnnotationElement.ofBoolean("forRemoval", true));
        RuntimeVisibleAnnotationsAttribute a = RuntimeVisibleAnnotationsAttribute.of(an);
        check(a.annotations().size() == 1, "size");
        check(a.annotations().get(0).classSymbol().equals(ClassDesc.of("java.lang.Deprecated")), "classSymbol");

    }


    // ---- real-product members（@Deprecated 为 RUNTIME 保留 → RuntimeVisibleAnnotations）----
    @Deprecated void rvaDep() {}
    @Deprecated static int rvaDepField;
    @Deprecated static class RVANested {}

    // ---- Part 3: javac 真产物 ----
    static void testRealProduct() throws Exception {
        byte[] self = RuntimeVisibleAnnotationsAttributeTest.class.getResourceAsStream("RuntimeVisibleAnnotationsAttributeTest.class").readAllBytes();
        ClassModel cm = ClassFile.of().parse(self);
        MethodModel mm = null;
        for (MethodModel m : cm.methods()) if (m.methodName().stringValue().equals("rvaDep")) mm = m;
        check(mm != null, "rvaDep found");
        var at = mm.findAttribute(Attributes.runtimeVisibleAnnotations()).get();
        check(at.annotations().size() == 1, "method RVA count");
        check(at.annotations().get(0).className().stringValue().equals("Ljava/lang/Deprecated;"), "method RVA className");
        check(at.annotations().get(0).elements().isEmpty(), "@Deprecated() bare form no elements");
        check(at.annotations().get(0).classSymbol().descriptorString().equals("Ljava/lang/Deprecated;"), "classSymbol");
        check(at.attributeMapper() == Attributes.runtimeVisibleAnnotations(), "parsed mapper identity");
        FieldModel fm = null;
        for (FieldModel f : cm.fields()) if (f.fieldName().stringValue().equals("rvaDepField")) fm = f;
        check(fm.findAttribute(Attributes.runtimeVisibleAnnotations()).get().annotations().get(0).className().stringValue().equals("Ljava/lang/Deprecated;"), "field RVA");
        byte[] nb = RuntimeVisibleAnnotationsAttributeTest.class.getResourceAsStream("RuntimeVisibleAnnotationsAttributeTest$RVANested.class").readAllBytes();
        check(ClassFile.of().parse(nb).findAttribute(Attributes.runtimeVisibleAnnotations()).get().annotations().size() == 1, "class-level RVA");
        // RUNTIME 注解不进 invisible
        check(mm.findAttribute(Attributes.runtimeInvisibleAnnotations()).isEmpty(), "no RIA for RUNTIME anno");
    }

    // ---- Part 4: 工厂 of(List)/of(varargs) + null + 属性面 ----
    static void testFactoryOverloads() {
        Annotation a1 = Annotation.of(ClassDesc.of("T1"),
                List.of(AnnotationElement.ofInt("n", 3), AnnotationElement.ofString("s", "x")));
        Annotation a2 = Annotation.of(ClassDesc.of("T2"), List.of());
        RuntimeVisibleAnnotationsAttribute at = RuntimeVisibleAnnotationsAttribute.of(List.of(a1, a2));
        check(at.annotations().size() == 2 && at.annotations().get(0) == a1, "of(List) identity");
        check(RuntimeVisibleAnnotationsAttribute.of(a1).annotations().get(0) == a1, "of(varargs)");
        check(RuntimeVisibleAnnotationsAttribute.of(List.of()).annotations().isEmpty(), "of(empty)");
        check(at.attributeName().stringValue().equals("RuntimeVisibleAnnotations")
                && at.attributeName().stringValue().equals(Attributes.NAME_RUNTIME_VISIBLE_ANNOTATIONS), "NAME");
        check(at.attributeMapper() == Attributes.runtimeVisibleAnnotations(), "mapper identity");
        check(at.attributeMapper().stability() == AttributeMapper.AttributeStability.CP_REFS, "stability CP_REFS");
        check(!at.attributeMapper().allowMultiple(), "allowMultiple false");
        try { RuntimeVisibleAnnotationsAttribute.of((List<Annotation>) null); check(false, "of(null List) must throw"); }
        catch (NullPointerException e) { check(true, "of(null List) NPE"); }
        try { RuntimeVisibleAnnotationsAttribute.of((Annotation[]) null); check(false, "of(null varargs) must throw"); }
        catch (NullPointerException e) { check(true, "of(null varargs) NPE"); }
    }

    // ---- Part 5: 类/字段/方法三级注入→re-parse 全回读 ----
    static void testInjectRoundTrip() throws Exception {
        Annotation ann = Annotation.of(ClassDesc.of("Inj"),
                List.of(AnnotationElement.ofInt("v", 7), AnnotationElement.ofString("t", "str")));
        byte[] b = ClassFile.of().build(ClassDesc.of("DynRVA"), cb -> cb
                .withSuperclass(ConstantDescs.CD_Object)
                .with(RuntimeVisibleAnnotationsAttribute.of(List.of(ann)))
                .withField("f", ConstantDescs.CD_int, fb -> fb.withFlags(ClassFile.ACC_PUBLIC).with(RuntimeVisibleAnnotationsAttribute.of(ann)))
                .withMethod("m", MethodTypeDesc.ofDescriptor("()V"), 0, mb -> mb
                        .with(RuntimeVisibleAnnotationsAttribute.of(ann)).withCode(cob -> cob.return_())));
        ClassModel cm = ClassFile.of().parse(b);
        var cls = cm.findAttribute(Attributes.runtimeVisibleAnnotations()).get().annotations().get(0);
        check(cls.className().stringValue().equals("LInj;"), "class inject className");
        check(cls.elements().size() == 2, "class inject element count");
        check(cls.elements().get(0).name().stringValue().equals("v"), "element name");
        check(cls.elements().get(0).value() instanceof AnnotationValue.OfInt
                && ((AnnotationValue.OfInt) cls.elements().get(0).value()).intValue() == 7, "element OfInt value");
        check(cls.elements().get(1).value() instanceof AnnotationValue.OfString
                && ((AnnotationValue.OfString) cls.elements().get(1).value()).stringValue().equals("str"), "element OfString value");
        check(cm.fields().get(0).findAttribute(Attributes.runtimeVisibleAnnotations()).get().annotations().size() == 1, "field inject");
        check(cm.methods().get(0).findAttribute(Attributes.runtimeVisibleAnnotations()).get().annotations().size() == 1, "method inject");
        check(cm.findAttribute(Attributes.runtimeVisibleAnnotations()).get().attributeMapper() == Attributes.runtimeVisibleAnnotations(), "parsed mapper identity");
        check(ClassFile.of().verify(cm).isEmpty(), "verify clean");
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
