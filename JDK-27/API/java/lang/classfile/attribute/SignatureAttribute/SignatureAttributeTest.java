/*
 * @test
 * @summary java.lang.classfile.attribute.SignatureAttribute conformance: white-box structure + black-box behavior.
 * @run main SignatureAttributeTest
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

public class SignatureAttributeTest {
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
        Class<?> c = Class.forName("java.lang.classfile.attribute.SignatureAttribute");
        check(c != null, "java.lang.classfile.attribute.SignatureAttribute loads");
        check(c.getName().equals("java.lang.classfile.attribute.SignatureAttribute"), "binary name java.lang.classfile.attribute.SignatureAttribute");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.attribute.SignatureAttribute is public");
        check(c.isInterface(), "java.lang.classfile.attribute.SignatureAttribute is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.attribute.SignatureAttribute has no superclass");
        check(declares(c, "signature"), "declares signature");
        check(declares(c, "asClassSignature"), "declares asClassSignature");
        check(declares(c, "asMethodSignature"), "declares asMethodSignature");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        SignatureAttribute a = SignatureAttribute.of(ClassSignature.parseFrom("Ljava/lang/Object;Ljava/lang/Runnable;"));
        check(a.signature().stringValue().equals("Ljava/lang/Object;Ljava/lang/Runnable;"), "signature");
        check(a.asClassSignature().superclassSignature().className().equals("java/lang/Object"), "asClassSignature");
        SignatureAttribute m = SignatureAttribute.of(MethodSignature.parseFrom("(I)I"));
        check(m.asMethodSignature().result().signatureString().equals("I"), "asMethodSignature");
        SignatureAttribute t = SignatureAttribute.of(Signature.parseFrom("Ljava/lang/String;"));
        check(t.asTypeSignature().signatureString().equals("Ljava/lang/String;"), "asTypeSignature");

    }


    // ---- real-product members ----
    static class SGClass<T extends java.io.Serializable> { java.util.List<String> sgField; }
    <T extends java.util.List<java.lang.String>> T sgPick(java.util.List<T> l) { return null; }

    // ---- Part 3: 工厂 4 重载（ClassSignature/MethodSignature/Signature/Utf8Entry）----
    static void testFactoryOverloads() {
        ClassSignature cs = ClassSignature.parseFrom("<T:Ljava/lang/Object;>Ljava/lang/Object;");
        MethodSignature ms = MethodSignature.parseFrom("(I)V");
        Signature ts = Signature.parseFrom("Ljava/util/List<Ljava/lang/String;>;");
        ConstantPoolBuilder cpb = ConstantPoolBuilder.of();
        Utf8Entry raw = cpb.utf8Entry("Ljava/lang/Object;");
        SignatureAttribute a1 = SignatureAttribute.of(cs);
        check(a1.signature().stringValue().equals("<T:Ljava/lang/Object;>Ljava/lang/Object;"), "of(ClassSignature)");
        SignatureAttribute a2 = SignatureAttribute.of(ms);
        check(a2.signature().stringValue().equals("(I)V"), "of(MethodSignature)");
        SignatureAttribute a3 = SignatureAttribute.of(ts);
        check(a3.signature().stringValue().equals("Ljava/util/List<Ljava/lang/String;>;"), "of(Signature)");
        check(a3.signature() != raw, "of(Signature) 不复用既有 Utf8");
        SignatureAttribute a4 = SignatureAttribute.of(raw);
        check(a4.signature() == raw, "of(Utf8Entry) entry identity");
        check(a1.attributeName().stringValue().equals("Signature")
                && a1.attributeName().stringValue().equals(Attributes.NAME_SIGNATURE), "NAME_SIGNATURE");
        check(a1.attributeMapper() == Attributes.signature(), "mapper identity");
        check(a1.attributeMapper().stability() == AttributeMapper.AttributeStability.CP_REFS, "stability CP_REFS");
        check(!a1.attributeMapper().allowMultiple(), "allowMultiple false");
        try { SignatureAttribute.of((Utf8Entry) null); check(false, "of(null) must throw"); }
        catch (NullPointerException e) { check(true, "of(null Utf8Entry) NPE"); }
    }

    // ---- Part 4: as* 视图方法 + 错型精确 IAE ----
    static void testAsViews() {
        SignatureAttribute a1 = SignatureAttribute.of(ClassSignature.parseFrom("<T:Ljava/lang/Object;>Ljava/lang/Object;"));
        check(a1.asClassSignature().typeParameters().size() == 1, "asClassSignature");
        SignatureAttribute a2 = SignatureAttribute.of(MethodSignature.parseFrom("(I)V"));
        check(a2.asMethodSignature().result().signatureString().equals("V"), "asMethodSignature result");
        check(a2.asMethodSignature().arguments().size() == 1, "asMethodSignature args");
        SignatureAttribute a3 = SignatureAttribute.of(Signature.parseFrom("Ljava/util/List<Ljava/lang/String;>;"));
        check(a3.asTypeSignature() != null, "asTypeSignature");
        try { a2.asClassSignature(); check(false, "method sig asClass must throw"); }
        catch (IllegalArgumentException e) { check(e.getMessage() != null, "asClassSignature on method sig IAE"); }
        try { a2.asTypeSignature(); check(false, "method sig asType must throw"); }
        catch (IllegalArgumentException e) { check(e.getMessage() != null, "asTypeSignature on method sig IAE"); }
        try { a1.asMethodSignature(); check(false, "class sig asMethod must throw"); }
        catch (IllegalArgumentException e) { check(e.getMessage() != null, "asMethodSignature on class sig IAE"); }
    }

    // ---- Part 5: javac 真产物（类/字段/方法三处精确串）----
    static void testRealProduct() throws Exception {
        byte[] cb = SignatureAttributeTest.class.getResourceAsStream("SignatureAttributeTest$SGClass.class").readAllBytes();
        ClassModel cm2 = ClassFile.of().parse(cb);
        check(cm2.findAttribute(Attributes.signature()).get().signature().stringValue().equals("<T::Ljava/io/Serializable;>Ljava/lang/Object;"),
                "class signature exact");
        FieldModel fm = null;
        for (FieldModel f : cm2.fields()) if (f.fieldName().stringValue().equals("sgField")) fm = f;
        check(fm.findAttribute(Attributes.signature()).get().signature().stringValue().equals("Ljava/util/List<Ljava/lang/String;>;"),
                "field signature exact");
        byte[] self = SignatureAttributeTest.class.getResourceAsStream("SignatureAttributeTest.class").readAllBytes();
        MethodModel mm = null;
        for (MethodModel m : ClassFile.of().parse(self).methods()) if (m.methodName().stringValue().equals("sgPick")) mm = m;
        check(mm.findAttribute(Attributes.signature()).get().signature().stringValue()
                .equals("<T::Ljava/util/List<Ljava/lang/String;>;>(Ljava/util/List<TT;>;)TT;"), "method signature exact");
        check(mm.findAttribute(Attributes.signature()).get().attributeMapper() == Attributes.signature(), "parsed mapper identity");
    }

    // ---- Part 6: 类/字段/方法三级注入→re-parse 回读 ----
    static void testInjectRoundTrip() throws Exception {
        SignatureAttribute cs = SignatureAttribute.of(ClassSignature.parseFrom("<U:Ljava/lang/Object;>Ljava/lang/Object;"));
        SignatureAttribute ms = SignatureAttribute.of(MethodSignature.parseFrom("(J)V"));
        SignatureAttribute ts = SignatureAttribute.of(Signature.parseFrom("Ljava/lang/Integer;"));
        byte[] b = ClassFile.of().build(ClassDesc.of("DynSG"), cb2 -> cb2
                .withSuperclass(ConstantDescs.CD_Object)
                .with(cs)
                .withField("f", ConstantDescs.CD_Object, fb -> fb.with(ts))
                .withMethod("m", MethodTypeDesc.ofDescriptor("(J)V"), 0, mb -> mb.with(ms).withCode(cob -> cob.return_())));
        ClassModel m2 = ClassFile.of().parse(b);
        check(m2.findAttribute(Attributes.signature()).get().signature().stringValue().equals("<U:Ljava/lang/Object;>Ljava/lang/Object;"), "class inject");
        check(m2.fields().get(0).findAttribute(Attributes.signature()).get().signature().stringValue().equals("Ljava/lang/Integer;"), "field inject");
        check(m2.methods().get(0).findAttribute(Attributes.signature()).get().signature().stringValue().equals("(J)V"), "method inject");
        // signature 串幂等 round-trip：再 parseFrom 得同串
        check(ClassSignature.parseFrom(m2.findAttribute(Attributes.signature()).get().signature().stringValue()).toString() != null, "re-parse idempotent");
        check(ClassFile.of().verify(m2).isEmpty(), "verify clean");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testFactoryOverloads();
        testAsViews();
        testRealProduct();
        testInjectRoundTrip();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
