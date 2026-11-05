/*
 * @test
 * @summary java.lang.classfile.FieldElement conformance: white-box structure + black-box behavior.
 * @run main FieldElementTest
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

public class FieldElementTest {
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
        Class<?> c = Class.forName("java.lang.classfile.FieldElement");
        check(c != null, "java.lang.classfile.FieldElement loads");
        check(c.getName().equals("java.lang.classfile.FieldElement"), "binary name java.lang.classfile.FieldElement");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.FieldElement is public");
        check(c.isInterface(), "java.lang.classfile.FieldElement is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.FieldElement has no superclass");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        byte[] b = ClassFile.of().build(ClassDesc.of("pkg.T"), cb -> cb.withField("x", ConstantDescs.CD_int, fb -> fb.with(ConstantValueAttribute.of(1))));
        FieldModel f = ClassFile.of().parse(b).fields().get(0);
        FieldElement cve = f.findAttribute(Attributes.constantValue()).get();
        check(cve instanceof FieldElement, "ConstantValueAttribute is FieldElement");
        check(f.elementList().size() >= 1, "field element list");
        check(f.elementList().get(0) instanceof FieldElement, "field elementList member is FieldElement");

    }


    // ---- real-class members ----
    static final int CONST = 7;
    java.util.List<String> gfield;
    @Deprecated static int depField;
    static class FEattr extends CustomAttribute<FEattr> { FEattr(AttributeMapper<FEattr> m) { super(m); } }
    static final AttributeMapper<FEattr> FEMAP = new AttributeMapper<FEattr>() {
        public String name() { return "FEAttr"; }
        public FEattr readAttribute(AttributedElement e, ClassReader cr, int pos) { return new FEattr(FEMAP); }
        public void writeAttribute(BufWriter bw, FEattr a) { bw.writeIndex(bw.constantPool().utf8Entry(name())); bw.writeInt(0); }
        public AttributeStability stability() { return AttributeStability.STATELESS; }
    };

    static FieldModel find(ClassModel cm, String name) {
        for (FieldModel f : cm.fields()) if (f.fieldName().stringValue().equals(name)) return f;
        return null;
    }

    // ---- Part 3: 正例家族矩阵（字段级元素全 instanceof FieldElement） ----
    static void testPositiveFamily() throws Exception {
        // 工厂产物
        check(ConstantValueAttribute.of(1) instanceof FieldElement, "ConstantValueAttribute is FieldElement");
        check(DeprecatedAttribute.of() instanceof FieldElement, "DeprecatedAttribute is FieldElement");
        check(SignatureAttribute.of(Signature.parseFrom("Ljava/util/List<Ljava/lang/String;>;")) instanceof FieldElement, "SignatureAttribute is FieldElement");
        check(new FEattr(FEMAP) instanceof FieldElement, "CustomAttribute is FieldElement");
        // 模型产物
        ClassFile cf = ClassFile.of();
        byte[] self = FieldElementTest.class.getResourceAsStream("FieldElementTest.class").readAllBytes();
        ClassModel sm = cf.parse(self);
        FieldModel konst = find(sm, "CONST");
        check(konst != null, "self CONST located");
        check(konst.flags() instanceof FieldElement, "field AccessFlags is FieldElement");
        check(konst.findAttribute(Attributes.constantValue()).get() instanceof FieldElement, "bound ConstantValue is FieldElement");
        check(find(sm, "gfield").findAttribute(Attributes.signature()).get() instanceof FieldElement, "bound Signature is FieldElement");
        check(find(sm, "depField").findAttribute(Attributes.deprecated()).get() instanceof FieldElement, "bound Deprecated is FieldElement");
        check(find(sm, "depField").findAttribute(Attributes.runtimeVisibleAnnotations()).get() instanceof FieldElement, "bound RuntimeVisibleAnnotations is FieldElement");
        // elementList 直方图：含 ConstantValue 的字段 = [AccessFlags, ConstantValueAttribute]
        int af = 0, cv = 0;
        for (FieldElement e : konst.elementList()) { if (e instanceof AccessFlags) af++; if (e instanceof ConstantValueAttribute) cv++; }
        check(af == 1 && cv == 1, "CONST elementList [AccessFlags, ConstantValue] (" + af + "," + cv + ")");
        for (FieldElement e : konst.elementList()) check(e != null, "field element non-null");
        check(cf.verify(sm).isEmpty(), "self verify clean");
    }

    // ---- Part 4: 负例矩阵 + 无属性字段 elementList ----
    static void testNegativeFamily() throws Exception {
        check(!FieldElement.class.isInstance(SourceFileAttribute.of("X.java")), "SourceFileAttribute NOT FieldElement");
        check(!FieldElement.class.isInstance(ExceptionsAttribute.ofSymbols(ClassDesc.of("java.io.IOException"))), "ExceptionsAttribute NOT FieldElement");
        check(!FieldElement.class.isInstance(Superclass.of(ConstantPoolBuilder.of().classEntry(ClassDesc.of("java.lang.Object")))), "Superclass NOT FieldElement");
        check(!FieldElement.class.isInstance(ClassFileVersion.of(52, 0)), "ClassFileVersion NOT FieldElement");
        check(!FieldElement.class.isInstance(NopInstruction.of()), "Instruction NOT FieldElement");
        check(!FieldElement.class.isInstance(LineNumber.of(1)), "LineNumber NOT FieldElement");
        ClassFile cf = ClassFile.of();
        byte[] b = cf.build(ClassDesc.of("pkg.FE"), cb -> cb
                .withField("plain", ConstantDescs.CD_int, ClassFile.ACC_PRIVATE)
                .withMethodBody("m", ConstantDescs.MTD_void, 0, c -> c.return_()));
        ClassModel cm = cf.parse(b);
        FieldModel plain = cm.fields().get(0);
        check(plain.elementList().size() == 1 && plain.elementList().get(0) instanceof AccessFlags, "plain field elementList [AccessFlags] only");
        check(!FieldElement.class.isInstance(cm.methods().get(0)), "MethodModel NOT FieldElement");
        check(!FieldElement.class.isInstance(cm.fields().get(0)), "FieldModel NOT FieldElement (它是 ClassElement，仅元素层是 FieldElement)");
    }

    // ---- Part 5: FieldElement 谓词通道 transform + defineClass 运行闭环 ----
    static int dynSeq;
    static void testTransformChannel() throws Exception {
        ClassFile cf = ClassFile.of();
        String name = "DynFieldElement" + (++dynSeq);
        byte[] b = cf.build(ClassDesc.of(name), cb -> cb
                .withField("V", ConstantDescs.CD_int,
                        fb -> fb.withFlags(ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC).with(ConstantValueAttribute.of(11)))
                .withMethodBody("get", MethodTypeDesc.ofDescriptor("()I"), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC,
                        c -> c.getstatic(ClassDesc.of(name), "V", ConstantDescs.CD_int).ireturn()));
        ClassModel src = cf.parse(b);
        byte[] out = cf.transformClass(src, new ClassTransform() {
            public void accept(ClassBuilder cb, ClassElement e) { if (e instanceof MethodModel) cb.with(e); }
        });
        ClassModel dst = cf.parse(out);
        check(dst.fields().isEmpty(), "fields stripped via ClassElement channel");
        Class<?> dc = MethodHandles.lookup().defineClass(b);
        check(dc.getMethod("get").invoke(null).equals(11), "dyn get()==11 运行闭环");
        check(cf.verify(src).isEmpty(), "dyn verify clean");
    }
    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testPositiveFamily();
        testNegativeFamily();
        testTransformChannel();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
