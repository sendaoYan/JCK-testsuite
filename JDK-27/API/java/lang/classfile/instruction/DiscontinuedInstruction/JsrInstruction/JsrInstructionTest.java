/*
 * @test
 * @summary java.lang.classfile.instruction.DiscontinuedInstruction.JsrInstruction conformance: white-box structure + black-box behavior.
 * @run main JsrInstructionTest
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

public class JsrInstructionTest {
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

    // 触发 ConstantDescs 完整初始化，规避本 JDK 构建的 MethodTypeDesc.ofDescriptor 静态初始化环
    static final ClassDesc TOUCH = ConstantDescs.CD_Object;

    // 发射-回读辅助：build 唯一类名 + parse 回全部 CodeElement
    static java.util.List<CodeElement> emit(String m, MethodTypeDesc mtd, java.util.function.Consumer<CodeBuilder> body) throws Exception {
        byte[] b = ClassFile.of().build(ClassDesc.of("EmJSI" + m), cb -> cb.withMethodBody(m, mtd, ClassFile.ACC_STATIC, body));
        java.util.List<CodeElement> els = new ArrayList<>();
        for (MethodModel mm : ClassFile.of().parse(b).methods())
            if (mm.code().isPresent()) els.addAll(mm.code().get().elementList());
        return els;
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
        Class<?> c = Class.forName("java.lang.classfile.instruction.DiscontinuedInstruction$JsrInstruction");
        check(c != null, "java.lang.classfile.instruction.DiscontinuedInstruction.JsrInstruction loads");
        check(c.getName().equals("java.lang.classfile.instruction.DiscontinuedInstruction$JsrInstruction"), "binary name java.lang.classfile.instruction.DiscontinuedInstruction$JsrInstruction");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.instruction.DiscontinuedInstruction.JsrInstruction is public");
        check(c.isInterface(), "java.lang.classfile.instruction.DiscontinuedInstruction.JsrInstruction is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.instruction.DiscontinuedInstruction.JsrInstruction has no superclass");
        check(declares(c, "target"), "declares target");
        check(declares(c, "of"), "declares of");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        final Label[] tgt = new Label[1];
        ClassFile.of().build(ClassDesc.of("pkg.T"), cb -> cb.withMethodBody("m", MethodTypeDesc.of(ConstantDescs.CD_void), 0, cob -> {
            tgt[0] = cob.newLabel();
            DiscontinuedInstruction.JsrInstruction i = DiscontinuedInstruction.JsrInstruction.of(Opcode.JSR, tgt[0]);
            check(i.opcode() == Opcode.JSR, "opcode");
            check(i.target() == tgt[0], "target");
            cob.return_();
        }));

    }


    // ---- Part 3: v50 发射-回读（核心） ----
    static void testEmitReadbackV50() throws Exception {
        byte[] b = ClassFile.of().build(ClassDesc.of("EmJSI"), cb -> cb
                .with(ClassFileVersion.of(50, 0)).withSuperclass(ConstantDescs.CD_Object)
                .withMethodBody("m", MethodTypeDesc.ofDescriptor("()I"), ClassFile.ACC_PUBLIC, cob -> {
                    Label sub = cob.newLabel(), cont = cob.newLabel();
                    cob.with(DiscontinuedInstruction.JsrInstruction.of(Opcode.JSR_W, sub));
                    cob.labelBinding(cont).iconst_1().ireturn();
                    cob.labelBinding(sub).astore(1);
                    cob.with(DiscontinuedInstruction.RetInstruction.of(1));
                }));
        DiscontinuedInstruction.JsrInstruction jsr = null;
        for (CodeElement e : ClassFile.of().parse(b).methods().get(0).code().get().elementList())
            if (e instanceof DiscontinuedInstruction.JsrInstruction x) jsr = x;
        check(jsr != null, "jsr_w emitted at v50 + read back");
        check(jsr.opcode() == Opcode.JSR_W, "JSR_W opcode");
        check(jsr.sizeInBytes() == 5, "JSR_W sizeInBytes 5");
        check(jsr.target() != null, "JSR_W target non-null");
        check(jsr instanceof DiscontinuedInstruction, "instanceof DiscontinuedInstruction");
        check(jsr instanceof Instruction && jsr instanceof CodeElement, "Instruction+CodeElement");
        // JSR（非 wide）
        byte[] b2 = ClassFile.of().build(ClassDesc.of("EmJSI2"), cb -> cb
                .with(ClassFileVersion.of(50, 0)).withSuperclass(ConstantDescs.CD_Object)
                .withMethodBody("m", MethodTypeDesc.ofDescriptor("()I"), ClassFile.ACC_PUBLIC, cob -> {
                    Label sub = cob.newLabel(), cont = cob.newLabel();
                    cob.with(DiscontinuedInstruction.JsrInstruction.of(sub));
                    cob.labelBinding(cont).iconst_1().ireturn();
                    cob.labelBinding(sub).astore(1);
                    cob.with(DiscontinuedInstruction.RetInstruction.of(1));
                }));
        for (CodeElement e : ClassFile.of().parse(b2).methods().get(0).code().get().elementList())
            if (e instanceof DiscontinuedInstruction.JsrInstruction x) {
                check(x.opcode() == Opcode.JSR, "JSR opcode");
                check(x.sizeInBytes() == 3, "JSR sizeInBytes 3");
            }
        // v71 拒绝
        try {
            ClassFile.of().build(ClassDesc.of("EmJSI3"), cb -> cb.withMethodBody("m", ConstantDescs.MTD_void, ClassFile.ACC_STATIC, cob -> {
                Label l = cob.newLabel();
                cob.with(DiscontinuedInstruction.JsrInstruction.of(l));
                cob.labelBinding(l).return_();
            }));
            check(false, "jsr at v71 must throw");
        } catch (IllegalArgumentException e) {
            check(e.getMessage() != null && e.getMessage().contains("jsr"), "jsr v71 IAE mentions jsr");
        }
    }

    // ---- Part 4: 工厂 + 负例 ----
    static void testFactoryNegative() throws Exception {
        final Label[] l = new Label[1];
        ClassFile.of().build(ClassDesc.of("EmJSI4"), cb -> cb.withMethodBody("m", ConstantDescs.MTD_void, ClassFile.ACC_STATIC, cob -> {
            l[0] = cob.newLabel();
            DiscontinuedInstruction.JsrInstruction i = DiscontinuedInstruction.JsrInstruction.of(l[0]);
            check(i.opcode() == Opcode.JSR, "of(Label) JSR");
            check(i.target() == l[0], "of(Label) target identity ==");
            check(i.sizeInBytes() == 3, "of(Label) size 3");
            check(DiscontinuedInstruction.JsrInstruction.of(Opcode.JSR_W, l[0]).opcode() == Opcode.JSR_W, "of(JSR_W,l)");
            check(DiscontinuedInstruction.JsrInstruction.of(Opcode.JSR_W, l[0]).sizeInBytes() == 5, "of(JSR_W,l) size 5");
            check(DiscontinuedInstruction.JsrInstruction.of(Opcode.JSR_W, l[0]).target() == l[0], "of(JSR_W,l) target");
            cob.return_();
        }));
        try { DiscontinuedInstruction.JsrInstruction.of(null); check(false, "of(null) must throw"); }
        catch (NullPointerException e) { check(true, "of(null) NPE"); }
        try { DiscontinuedInstruction.JsrInstruction.of(Opcode.JSR, null); check(false, "of(JSR,null) must throw"); }
        catch (NullPointerException e) { check(true, "of(JSR,null) NPE"); }
        try { DiscontinuedInstruction.JsrInstruction.of(Opcode.GOTO, l[0]); check(false, "of(GOTO,l) must throw"); }
        catch (IllegalArgumentException e) { check(true, "of(GOTO,l) IAE wrong kind"); }
        try { DiscontinuedInstruction.JsrInstruction.of(Opcode.NOP, l[0]); check(false, "of(NOP,l) must throw"); }
        catch (IllegalArgumentException e) { check(true, "of(NOP,l) IAE wrong kind"); }
        try { DiscontinuedInstruction.JsrInstruction.of(Opcode.RET, l[0]); check(false, "of(RET,l) must throw"); }
        catch (IllegalArgumentException e) { check(true, "of(RET,l) IAE wrong kind"); }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testEmitReadbackV50();
        testFactoryNegative();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
