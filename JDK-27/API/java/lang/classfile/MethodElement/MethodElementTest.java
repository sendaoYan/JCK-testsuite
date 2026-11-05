/*
 * @test
 * @summary java.lang.classfile.MethodElement conformance: white-box structure + black-box behavior.
 * @run main MethodElementTest
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

public class MethodElementTest {
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
        Class<?> c = Class.forName("java.lang.classfile.MethodElement");
        check(c != null, "java.lang.classfile.MethodElement loads");
        check(c.getName().equals("java.lang.classfile.MethodElement"), "binary name java.lang.classfile.MethodElement");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.MethodElement is public");
        check(c.isInterface(), "java.lang.classfile.MethodElement is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.MethodElement has no superclass");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        byte[] b = ClassFile.of().build(ClassDesc.of("pkg.T"), cb -> cb.withMethodBody("m", MethodTypeDesc.of(ConstantDescs.CD_void), 0, cob -> cob.return_()));
        MethodElement e = ClassFile.of().parse(b).methods().get(0).code().get();
        check(e instanceof MethodElement, "CodeModel is MethodElement");
        check(DeprecatedAttribute.of() instanceof MethodElement, "DeprecatedAttribute is MethodElement");

    }


    // ---- real-class members ----
    @Deprecated static void depMethod() {}
    static void throwsM() throws IllegalStateException {}
    static <T> T genericM(T t) { return t; }
    static class MEattr extends CustomAttribute<MEattr> { MEattr(AttributeMapper<MEattr> m) { super(m); } }
    static final AttributeMapper<MEattr> MEMAP = new AttributeMapper<MEattr>() {
        public String name() { return "MEAttr"; }
        public MEattr readAttribute(AttributedElement e, ClassReader cr, int pos) { return new MEattr(MEMAP); }
        public void writeAttribute(BufWriter bw, MEattr a) { bw.writeIndex(bw.constantPool().utf8Entry(name())); bw.writeInt(0); }
        public AttributeStability stability() { return AttributeStability.STATELESS; }
    };

    static MethodModel find(ClassModel cm, String name) {
        for (MethodModel m : cm.methods()) if (m.methodName().stringValue().equals(name)) return m;
        return null;
    }

    // ---- Part 3: 正例家族矩阵（方法级元素全 instanceof MethodElement） ----
    static void testPositiveFamily() throws Exception {
        // 工厂产物
        check(CodeModel.class.isAssignableFrom(java.lang.classfile.attribute.CodeAttribute.class), "CodeAttribute <- CodeModel");
        check(DeprecatedAttribute.of() instanceof MethodElement, "DeprecatedAttribute is MethodElement");
        check(ExceptionsAttribute.ofSymbols(ClassDesc.of("java.io.IOException")) instanceof MethodElement, "ExceptionsAttribute is MethodElement");
        check(SignatureAttribute.of(Signature.parseFrom("I")) instanceof MethodElement, "SignatureAttribute is MethodElement");
        check(MethodParametersAttribute.of(MethodParameterInfo.ofParameter(java.util.Optional.of("a"), 0)) instanceof MethodElement, "MethodParametersAttribute is MethodElement");
        check(new MEattr(MEMAP) instanceof MethodElement, "CustomAttribute is MethodElement");
        // 模型产物
        ClassFile cf = ClassFile.of();
        byte[] b = cf.build(ClassDesc.of("pkg.ME"), cb -> cb
                .withMethodBody("m", MethodTypeDesc.ofDescriptor("(I)I"), ClassFile.ACC_STATIC, c -> {
                    Label l = c.newLabel();
                    c.iconst_0().iload(0).if_icmpge(l);
                    c.iconst_1().ireturn();
                    c.labelBinding(l).iconst_m1().ireturn();
                }));
        ClassModel cm = cf.parse(b);
        MethodModel mm = cm.methods().get(0);
        CodeModel code = mm.code().get();
        check(code instanceof MethodElement, "CodeModel is MethodElement");
        check(mm.flags() instanceof MethodElement, "method AccessFlags is MethodElement");
        check(code.findAttribute(Attributes.stackMapTable()).isPresent(), "branch method has StackMapTable");
        // 方法 elementList 直方图：[AccessFlags, CodeModel]
        int af = 0, cmc = 0;
        for (MethodElement e : mm.elementList()) { if (e instanceof AccessFlags) af++; if (e instanceof CodeModel) cmc++; }
        check(af == 1 && cmc == 1, "method elementList [AccessFlags, CodeModel] (" + af + "," + cmc + ")");
        for (MethodElement e : mm.elementList()) check(e != null, "method element non-null");
        // 真类解析：@Deprecated/throws/泛型方法的 bound 属性均为 MethodElement
        byte[] self = MethodElementTest.class.getResourceAsStream("MethodElementTest.class").readAllBytes();
        ClassModel sm = cf.parse(self);
        check(find(sm, "depMethod").findAttribute(Attributes.deprecated()).get() instanceof MethodElement, "bound Deprecated is MethodElement");
        check(find(sm, "throwsM").findAttribute(Attributes.exceptions()).get() instanceof MethodElement, "bound Exceptions is MethodElement");
        check(find(sm, "genericM").findAttribute(Attributes.signature()).get() instanceof MethodElement, "bound Signature is MethodElement");
        check(!MethodElement.class.isInstance(sm.findAttribute(Attributes.sourceFile()).get()), "bound SourceFile NOT MethodElement");
    }

    // ---- Part 4: 负例矩阵（类级/字段级元素 NOT MethodElement） ----
    static void testNegativeFamily() throws Exception {
        check(!MethodElement.class.isInstance(Superclass.of(ConstantPoolBuilder.of().classEntry(ClassDesc.of("java.lang.Object")))), "Superclass NOT MethodElement");
        check(!MethodElement.class.isInstance(ClassFileVersion.of(52, 0)), "ClassFileVersion NOT MethodElement");
        check(!MethodElement.class.isInstance(Interfaces.ofSymbols()), "Interfaces NOT MethodElement");
        check(!MethodElement.class.isInstance(SourceFileAttribute.of("X.java")), "SourceFileAttribute NOT MethodElement");
        check(!MethodElement.class.isInstance(ConstantValueAttribute.of(1)), "ConstantValueAttribute NOT MethodElement");
        check(!MethodElement.class.isInstance(NopInstruction.of()), "Instruction NOT MethodElement");
        check(!MethodElement.class.isInstance(LineNumber.of(1)), "LineNumber NOT MethodElement");
        ClassFile cf = ClassFile.of();
        byte[] b = cf.build(ClassDesc.of("pkg.ME2"), cb -> cb.withField("f", ConstantDescs.CD_int, 0).withMethodBody("m", ConstantDescs.MTD_void, 0, c -> c.return_()));
        ClassModel cm = cf.parse(b);
        check(!MethodElement.class.isInstance(cm.fields().get(0)), "FieldModel NOT MethodElement");
        check(!MethodElement.class.isInstance(cm.methods().get(0)), "MethodModel NOT MethodElement (它是 ClassElement，仅元素层是 MethodElement)");
    }

    // ---- Part 5: MethodElement 谓词通道 transform + defineClass 运行闭环 ----
    static int dynSeq;
    static void testTransformChannel() throws Exception {
        ClassFile cf = ClassFile.of();
        String name = "DynMethodElement" + (++dynSeq);
        byte[] b = cf.build(ClassDesc.of(name), cb -> cb
                .withMethodBody("go", MethodTypeDesc.ofDescriptor("()I"), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC, c -> c.iconst_2().ireturn()));
        ClassModel src = cf.parse(b);
        byte[] out = cf.transformClass(src, new ClassTransform() {
            public void accept(ClassBuilder cb, ClassElement e) {
                if (e instanceof MethodModel mm && mm.methodName().stringValue().equals("go")) cb.with(e);
            }
        });
        ClassModel dst = cf.parse(out);
        check(dst.methods().size() == 1, "kept 1 method");
        Class<?> dc = MethodHandles.lookup().defineClass(out);
        check(dc.getMethod("go").invoke(null).equals(2), "dyn go()==2 运行闭环");
        check(cf.verify(dst).isEmpty(), "transform verify clean");
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
