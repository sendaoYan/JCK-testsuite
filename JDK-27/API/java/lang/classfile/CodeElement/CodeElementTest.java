/*
 * @test
 * @summary java.lang.classfile.CodeElement conformance: white-box structure + black-box behavior.
 * @run main CodeElementTest
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

public class CodeElementTest {
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
        Class<?> c = Class.forName("java.lang.classfile.CodeElement");
        check(c != null, "java.lang.classfile.CodeElement loads");
        check(c.getName().equals("java.lang.classfile.CodeElement"), "binary name java.lang.classfile.CodeElement");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.CodeElement is public");
        check(c.isInterface(), "java.lang.classfile.CodeElement is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.CodeElement has no superclass");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        CodeElement e = NopInstruction.of();
        check(e instanceof CodeElement, "NopInstruction is CodeElement");
        CodeElement ln = LineNumber.of(1);
        check(ln instanceof CodeElement, "LineNumber is CodeElement");

    }


    // ---- Part 3: 正例家族矩阵（指令 + 全部伪指令族 + StackMapTable + 自定义属性） ----
    static class CDattr extends CustomAttribute<CDattr> { CDattr(AttributeMapper<CDattr> m) { super(m); } }
    static final AttributeMapper<CDattr> CDMAP = new AttributeMapper<CDattr>() {
        public String name() { return "CDAttr"; }
        public CDattr readAttribute(AttributedElement e, ClassReader cr, int pos) { return new CDattr(CDMAP); }
        public void writeAttribute(BufWriter bw, CDattr a) { bw.writeIndex(bw.constantPool().utf8Entry(name())); bw.writeInt(0); }
        public AttributeStability stability() { return AttributeStability.STATELESS; }
    };
    static void testPositiveFamily() throws Exception {
        // 工厂产物
        check(NopInstruction.of() instanceof CodeElement, "NopInstruction is CodeElement");
        check(ReturnInstruction.of(TypeKind.INT) instanceof CodeElement, "ReturnInstruction is CodeElement");
        check(LineNumber.of(1) instanceof CodeElement, "LineNumber is CodeElement");
        try { LocalVariable.of(0, "x", ConstantDescs.CD_int, null, null); check(false, "LocalVariable.of(null labels) must throw"); }
        catch (NullPointerException e) { check(true, "LocalVariable.of null labels NPE (labels required)"); }
        check(new CDattr(CDMAP) instanceof CodeElement, "CustomAttribute is CodeElement");
        // 发射产物：Instruction 族 + PseudoInstruction 族 + LabelTarget + ExceptionCatch + StackMapTable
        ClassFile cf = ClassFile.of();
        byte[] b = cf.build(ClassDesc.of("pkg.CD"), cb -> cb.withMethodBody("m", MethodTypeDesc.ofDescriptor("(I)I"), ClassFile.ACC_STATIC, cob -> {
            Label s = cob.newLabel(), e2 = cob.newLabel(), h = cob.newLabel(), end = cob.newLabel();
            cob.labelBinding(s).iconst_1().pop().goto_(end);
            cob.labelBinding(e2);
            cob.labelBinding(h).pop().iconst_m1().ireturn();
            cob.labelBinding(end).iconst_0().ireturn();
            cob.exceptionCatchAll(s, e2, h);
            cob.localVariable(0, "lv", ConstantDescs.CD_int, s, e2);
            cob.lineNumber(3);
        }));
        ClassModel cm = cf.parse(b);
        CodeModel code = cm.methods().get(0).code().get();
        int instr = 0, pseudo = 0, lt = 0, ec = 0, lv = 0, ln = 0;
        for (CodeElement el : code.elementList()) {
            if (Instruction.class.isInstance(el)) instr++;
            if (el instanceof PseudoInstruction) pseudo++;
            if (el instanceof LabelTarget) lt++;
            if (el instanceof ExceptionCatch) ec++;
            if (el instanceof LocalVariable) lv++;
            if (el instanceof LineNumber) ln++;
        }
        check(instr >= 7, ">= 7 Instruction elements: " + instr);
        check(pseudo >= 4, "pseudo elements (LabelTarget+EC+LV+LN): " + pseudo);
        check(instr + pseudo == code.elementList().size(), "Instruction + PseudoInstruction partitions elementList (" + (instr + pseudo) + ")");
        check(lt >= 1 && ec == 1 && lv == 1 && ln == 1, "per-pseudo counts (lt/ec/lv/ln): " + lt + "/" + ec + "/" + lv + "/" + ln);
        // StackMapTableAttribute 实现 CodeElement（不在 elementList，但类型层可作 CodeElement）
        var smt = code.findAttribute(Attributes.stackMapTable());
        check(smt.isPresent() && CodeElement.class.isInstance(smt.get()), "StackMapTableAttribute is CodeElement");
        check(cf.verify(cm).isEmpty(), "verify clean");
        // 真类解析：main 方法 code 元素全为 CodeElement
        byte[] self = CodeElementTest.class.getResourceAsStream("CodeElementTest.class").readAllBytes();
        ClassModel sm = cf.parse(self);
        for (MethodModel m : sm.methods()) {
            if (m.code().isEmpty()) continue;
            for (CodeElement el : m.code().get().elementList()) check(CodeElement.class.isInstance(el), "self code element is CodeElement");
        }
    }

    // ---- Part 4: 负例矩阵（类/方法/字段级元素 NOT CodeElement） ----
    static void testNegativeFamily() throws Exception {
        check(!CodeElement.class.isInstance(SourceFileAttribute.of("X.java")), "SourceFileAttribute NOT CodeElement");
        check(!CodeElement.class.isInstance(DeprecatedAttribute.of()), "DeprecatedAttribute NOT CodeElement");
        check(!CodeElement.class.isInstance(ConstantValueAttribute.of(1)), "ConstantValueAttribute NOT CodeElement");
        check(!CodeElement.class.isInstance(ExceptionsAttribute.ofSymbols(ClassDesc.of("java.io.IOException"))), "ExceptionsAttribute NOT CodeElement");
        check(!CodeElement.class.isInstance(ClassFileVersion.of(52, 0)), "ClassFileVersion NOT CodeElement");
        check(!CodeElement.class.isInstance(Interfaces.ofSymbols()), "Interfaces NOT CodeElement");
        ClassFile cf = ClassFile.of();
        byte[] b = cf.build(ClassDesc.of("pkg.CD2"), cb -> cb.withField("f", ConstantDescs.CD_int, 0).withMethodBody("m", ConstantDescs.MTD_void, 0, c -> c.return_()));
        ClassModel cm = cf.parse(b);
        check(!CodeElement.class.isInstance(cm.fields().get(0)), "FieldModel NOT CodeElement");
        check(!CodeElement.class.isInstance(cm.methods().get(0)), "MethodModel NOT CodeElement");
        check(!CodeElement.class.isInstance(cm), "ClassModel NOT CodeElement");
        check(CodeElement.class.isAssignableFrom(Instruction.class), "Instruction <- CodeElement");
        check(CodeElement.class.isAssignableFrom(PseudoInstruction.class), "PseudoInstruction <- CodeElement");
        check(ClassFileElement.class.isAssignableFrom(CodeElement.class), "CodeElement <- ClassFileElement");
    }

    // ---- Part 5: CodeElement 谓词通道 transform（剥指令保留伪指令）+ defineClass ----
    static int dynSeq;
    static void testTransformChannel() throws Exception {
        ClassFile cf = ClassFile.of();
        String name = "DynCodeElement" + (++dynSeq);
        byte[] b = cf.build(ClassDesc.of(name), cb -> cb.withMethodBody("go", MethodTypeDesc.ofDescriptor("()I"), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC,
                c -> c.nop().nop().iconst_2().ireturn()));
        ClassModel src = cf.parse(b);
        int before = 0;
        for (CodeElement e : src.methods().get(0).code().get().elementList()) if (Instruction.class.isInstance(e)) before++;
        check(before == 4, "4 instructions before transform");
        byte[] out = cf.transformClass(src, ClassTransform.transformingMethodBodies(new CodeTransform() {
            public void accept(CodeBuilder cb, CodeElement e) {
                if (Instruction.class.isInstance(e) && ((Instruction) e).opcode() != Opcode.NOP) cb.with(e);
            }
        }));
        ClassModel dst = cf.parse(out);
        List<Opcode> ops = new ArrayList<>();
        for (CodeElement e : dst.methods().get(0).code().get().elementList()) if (Instruction.class.isInstance(e)) ops.add(((Instruction) e).opcode());
        check(ops.equals(List.of(Opcode.ICONST_2, Opcode.IRETURN)), "NOP stripped via CodeElement channel: " + ops);
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
