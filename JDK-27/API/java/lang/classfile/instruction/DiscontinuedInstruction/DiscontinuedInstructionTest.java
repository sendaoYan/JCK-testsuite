/*
 * @test
 * @summary java.lang.classfile.instruction.DiscontinuedInstruction conformance: white-box structure + black-box behavior.
 * @run main DiscontinuedInstructionTest
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

public class DiscontinuedInstructionTest {
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
        byte[] b = ClassFile.of().build(ClassDesc.of("EmDCI" + m), cb -> cb.withMethodBody(m, mtd, ClassFile.ACC_STATIC, body));
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
        Class<?> c = Class.forName("java.lang.classfile.instruction.DiscontinuedInstruction");
        check(c != null, "java.lang.classfile.instruction.DiscontinuedInstruction loads");
        check(c.getName().equals("java.lang.classfile.instruction.DiscontinuedInstruction"), "binary name java.lang.classfile.instruction.DiscontinuedInstruction");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.instruction.DiscontinuedInstruction is public");
        check(c.isInterface(), "java.lang.classfile.instruction.DiscontinuedInstruction is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.instruction.DiscontinuedInstruction has no superclass");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        final Label[] tgt = new Label[1];
        ClassFile.of().build(ClassDesc.of("pkg.T"), cb -> cb.withMethodBody("m", MethodTypeDesc.of(ConstantDescs.CD_void), 0, cob -> {
            tgt[0] = cob.newLabel();
            check(DiscontinuedInstruction.JsrInstruction.of(Opcode.JSR, tgt[0]).opcode() == Opcode.JSR, "jsr opcode");
            check(DiscontinuedInstruction.JsrInstruction.of(tgt[0]).target() == tgt[0], "jsr of(Label) target");
            check(DiscontinuedInstruction.RetInstruction.of(Opcode.RET, 1).opcode() == Opcode.RET, "ret opcode");
            check(DiscontinuedInstruction.RetInstruction.of(1).opcode() == Opcode.RET, "ret of(int) opcode");
            cob.return_();
        }));

    }


    // ---- Part 3: v50 发射-回读（jsr/ret 仅允许 class 版本 <51） ----
    static void testEmitReadbackV50() throws Exception {
        byte[] b = ClassFile.of().build(ClassDesc.of("EmDCI"), cb -> cb
                .with(ClassFileVersion.of(50, 0)).withSuperclass(ConstantDescs.CD_Object)
                .withMethodBody("m", MethodTypeDesc.ofDescriptor("()I"), ClassFile.ACC_PUBLIC, cob -> {
                    Label sub = cob.newLabel(), cont = cob.newLabel();
                    cob.with(DiscontinuedInstruction.JsrInstruction.of(sub));
                    cob.labelBinding(cont).iconst_1().ireturn();
                    cob.labelBinding(sub).astore(1);
                    cob.with(DiscontinuedInstruction.RetInstruction.of(1));
                }));
        check(b.length > 8, "v50 jsr/ret build succeeds");
        int jsr = 0, ret = 0;
        for (CodeElement e : ClassFile.of().parse(b).methods().get(0).code().get().elementList()) {
            if (e instanceof DiscontinuedInstruction.JsrInstruction x) {
                jsr++;
                check(x.opcode() == Opcode.JSR, "JSR opcode");
                check(x.opcode().kind() == Opcode.Kind.DISCONTINUED_JSR, "JSR kind DISCONTINUED_JSR");
                check(x.sizeInBytes() == 3, "JSR sizeInBytes 3");
                check(x.target() != null, "JSR target non-null");
            }
            if (e instanceof DiscontinuedInstruction.RetInstruction x) {
                ret++;
                check(x.opcode() == Opcode.RET, "RET opcode");
                check(x.opcode().kind() == Opcode.Kind.DISCONTINUED_RET, "RET kind DISCONTINUED_RET");
                check(x.slot() == 1, "RET slot 1");
            }
            if (e instanceof DiscontinuedInstruction d) {
                check(d instanceof Instruction && d instanceof CodeElement, "DiscontinuedInstruction is Instruction+CodeElement");
                check(!((Object) d instanceof PseudoInstruction), "not PseudoInstruction");
            }
        }
        check(jsr == 1 && ret == 1, "1 jsr + 1 ret read back, got " + jsr + "/" + ret);
    }

    // ---- Part 4: v71 拒绝语义 + 层次 ----
    static void testV51RejectionAndHierarchy() throws Exception {
        // v71（默认版本）发射 jsr → 精确 IAE
        try {
            ClassFile.of().build(ClassDesc.of("EmDCI2"), cb -> cb.withMethodBody("m", ConstantDescs.MTD_void, ClassFile.ACC_STATIC, cob -> {
                Label l = cob.newLabel();
                cob.with(DiscontinuedInstruction.JsrInstruction.of(l));
                cob.labelBinding(l).return_();
            }));
            check(false, "jsr at v71 must throw");
        } catch (IllegalArgumentException e) {
            check(e.getMessage() != null && e.getMessage().contains("jsr") && e.getMessage().contains("51.0"),
                    "jsr v71 IAE message: " + e.getMessage());
        }
        // v71 发射 ret → 同语义拒绝
        try {
            ClassFile.of().build(ClassDesc.of("EmDCI3"), cb -> cb.withMethodBody("m", ConstantDescs.MTD_void, ClassFile.ACC_STATIC,
                    cob -> cob.with(DiscontinuedInstruction.RetInstruction.of(1))));
            check(false, "ret at v71 must throw");
        } catch (IllegalArgumentException e) {
            check(e.getMessage() != null && e.getMessage().contains("ret"), "ret v71 IAE message: " + e.getMessage());
        }
        // CodeBuilder 无 jsr()/ret() 便捷方法（规范层 discontinued）
        boolean hasJsr = false, hasRet = false;
        for (Method m : CodeBuilder.class.getMethods()) {
            if (m.getName().equals("jsr")) hasJsr = true;
            if (m.getName().equals("ret")) hasRet = true;
        }
        check(!hasJsr, "CodeBuilder has no jsr() method");
        check(!hasRet, "CodeBuilder has no ret() method");
        // sealed 层次
        Class<?> c = DiscontinuedInstruction.class;
        check(c.isSealed(), "DiscontinuedInstruction sealed");
        Class<?>[] perm = c.getPermittedSubclasses();
        check(perm.length == 2, "2 permitted subclasses");
        java.util.List<String> names = new ArrayList<>();
        for (Class<?> p : perm) names.add(p.getSimpleName());
        check(names.contains("JsrInstruction") && names.contains("RetInstruction"), "permitted " + names);
        // Kind 归属
        check(Opcode.JSR.kind() == Opcode.Kind.DISCONTINUED_JSR && Opcode.JSR_W.kind() == Opcode.Kind.DISCONTINUED_JSR, "JSR/JSR_W kind");
        check(Opcode.RET.kind() == Opcode.Kind.DISCONTINUED_RET && Opcode.RET_W.kind() == Opcode.Kind.DISCONTINUED_RET, "RET/RET_W kind");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testEmitReadbackV50();
        testV51RejectionAndHierarchy();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
