/*
 * @test
 * @summary java.lang.classfile.ClassElement conformance: white-box structure + black-box behavior.
 * @run main ClassElementTest
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

public class ClassElementTest {
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
        Class<?> c = Class.forName("java.lang.classfile.ClassElement");
        check(c != null, "java.lang.classfile.ClassElement loads");
        check(c.getName().equals("java.lang.classfile.ClassElement"), "binary name java.lang.classfile.ClassElement");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.ClassElement is public");
        check(c.isInterface(), "java.lang.classfile.ClassElement is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.ClassElement has no superclass");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        ClassElement e = Superclass.of(ConstantPoolBuilder.of().classEntry(ClassDesc.of("java.lang.Object")));
        check(e != null, "Superclass is ClassElement");
        ClassElement v = ClassFileVersion.of(52, 0);
        check(v instanceof ClassElement, "ClassFileVersion is ClassElement");

    }


    // ---- real-class members ----
    @Deprecated static int depField;
    @Deprecated static void depMethod() {}
    static class CEattr extends CustomAttribute<CEattr> { CEattr(AttributeMapper<CEattr> m) { super(m); } }
    static final AttributeMapper<CEattr> CEMAP = new AttributeMapper<CEattr>() {
        public String name() { return "CEAttr"; }
        public CEattr readAttribute(AttributedElement e, ClassReader cr, int pos) { return new CEattr(CEMAP); }
        public void writeAttribute(BufWriter bw, CEattr a) { bw.writeIndex(bw.constantPool().utf8Entry(name())); bw.writeInt(0); }
        public AttributeStability stability() { return AttributeStability.STATELESS; }
    };

    // ---- Part 3: 正例家族矩阵（类级元素全 instanceof ClassElement） ----
    static void testPositiveFamily() throws Exception {
        ClassFile cf = ClassFile.of();
        // 工厂产物
        check(Superclass.of(ConstantPoolBuilder.of().classEntry(ClassDesc.of("java.lang.Object"))) instanceof ClassElement, "Superclass is ClassElement");
        check(ClassFileVersion.of(52, 0) instanceof ClassElement, "ClassFileVersion is ClassElement");
        check(Interfaces.ofSymbols(ClassDesc.of("java.lang.Runnable")) instanceof ClassElement, "Interfaces is ClassElement");
        check(DeprecatedAttribute.of() instanceof ClassElement, "DeprecatedAttribute is ClassElement");
        check(SourceFileAttribute.of("X.java") instanceof ClassElement, "SourceFileAttribute is ClassElement");
        check(SignatureAttribute.of(Signature.parseFrom("Ljava/lang/Object;")) instanceof ClassElement, "SignatureAttribute is ClassElement");
        check(new CEattr(CEMAP) instanceof ClassElement, "CustomAttribute is ClassElement");
        // 模型产物
        byte[] b = cf.build(ClassDesc.of("pkg.CE"), cb -> cb
                .withField("f", ConstantDescs.CD_int, 0)
                .withMethodBody("m", ConstantDescs.MTD_void, ClassFile.ACC_STATIC, c -> c.return_()));
        ClassModel cm = cf.parse(b);
        FieldModel fm = cm.fields().get(0);
        MethodModel mm = cm.methods().get(0);
        check(fm instanceof ClassElement, "FieldModel is ClassElement");
        check(mm instanceof ClassElement, "MethodModel is ClassElement");
        check(cm.flags() instanceof ClassElement, "class AccessFlags is ClassElement");
        check(ClassElement.class.isAssignableFrom(MethodModel.class), "MethodModel assignable to ClassElement");
        check(ClassElement.class.isAssignableFrom(FieldModel.class), "FieldModel assignable to ClassElement");
        check(ClassFileElement.class.isAssignableFrom(ClassElement.class), "ClassElement <- ClassFileElement");
        // 真类解析产物（bound 属性）
        byte[] self = ClassElementTest.class.getResourceAsStream("ClassElementTest.class").readAllBytes();
        ClassModel sm = cf.parse(self);
        check(sm.findAttribute(Attributes.sourceFile()).get() instanceof ClassElement, "bound SourceFile is ClassElement");
        for (FieldModel f : sm.fields())
            if (f.fieldName().stringValue().equals("depField")) check(f.findAttribute(Attributes.deprecated()).get() instanceof ClassElement, "bound Deprecated is ClassElement");
        // elementList 全体
        for (ClassElement e : sm.elementList()) check(e instanceof ClassElement, "self class element is ClassElement");
    }

    // ---- Part 4: 负例矩阵（方法/代码级元素 NOT ClassElement，实测编译器即拒绝） ----
    static void testNegativeFamily() throws Exception {
        ClassFile cf = ClassFile.of();
        byte[] b = cf.build(ClassDesc.of("pkg.CE2"), cb -> cb
                .withMethodBody("m", MethodTypeDesc.ofDescriptor("(I)I"), ClassFile.ACC_STATIC, c -> {
                    Label l = c.newLabel();
                    c.iconst_0().iload(0).if_icmpge(l);
                    c.iconst_1().ireturn();
                    c.labelBinding(l).iconst_m1().ireturn();
                }));
        ClassModel cm = cf.parse(b);
        MethodModel mm = cm.methods().get(0);
        CodeModel code = mm.code().get();
        check(!ClassElement.class.isInstance(code), "CodeModel NOT ClassElement");
        check(MethodElement.class.isInstance(code), "CodeModel IS MethodElement (层级正确定位)");
        check(!ClassElement.class.isInstance(NopInstruction.of()), "Instruction NOT ClassElement");
        check(!ClassElement.class.isInstance(LineNumber.of(1)), "PseudoInstruction NOT ClassElement");
        check(!ClassElement.class.isInstance(ExceptionsAttribute.ofSymbols(ClassDesc.of("java.io.IOException"))), "ExceptionsAttribute NOT ClassElement");
        check(!ClassElement.class.isInstance(ConstantValueAttribute.of(1)), "ConstantValueAttribute NOT ClassElement");
        for (CodeElement e : code.elementList()) {
            if (Instruction.class.isInstance(e)) check(!ClassElement.class.isInstance(e), "code instruction NOT ClassElement");
        }
        // StackMapTable 属性（实现 CodeElement，非 ClassElement）
        var smt = code.findAttribute(Attributes.stackMapTable());
        check(smt.isPresent() && CodeElement.class.isInstance(smt.get()) && !ClassElement.class.isInstance(smt.get()), "StackMapTableAttribute is CodeElement only");
    }

    // ---- Part 5: defineClass 运行闭环（经 ClassElement 通道过滤 transform） ----
    static int dynSeq;
    static void testTransformChannel() throws Exception {
        ClassFile cf = ClassFile.of();
        String name = "DynClassElement" + (++dynSeq);
        byte[] b = cf.build(ClassDesc.of(name), cb -> cb
                .withField("keep", ConstantDescs.CD_int, ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC)
                .withMethodBody("go", MethodTypeDesc.ofDescriptor("()I"), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC,
                        c -> c.iconst_5().ireturn()));
        ClassModel src = cf.parse(b);
        // 只保留 MethodModel（ClassElement 谓词通道）
        byte[] out = cf.transformClass(src, new ClassTransform() {
            public void accept(ClassBuilder cb, ClassElement e) { if (e instanceof MethodModel) cb.with(e); }
        });
        ClassModel dst = cf.parse(out);
        check(dst.methods().size() == 1, "filtered methods 1");
        check(dst.fields().isEmpty(), "filtered fields 0");
        check(dst.superclass().isPresent(), "transform keeps superclass");
        Class<?> dc = MethodHandles.lookup().defineClass(out);
        check(dc.getMethod("go").invoke(null).equals(5), "dyn go()==5 运行闭环（剥字段后仍可运行）");
        check(cf.verify(dst).isEmpty(), "filtered verify clean");
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
