/*
 * @test
 * @summary java.lang.classfile.instruction.ReturnInstruction conformance: white-box structure + black-box behavior.
 * @run main ReturnInstructionTest
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

public class ReturnInstructionTest {
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
        byte[] b = ClassFile.of().build(ClassDesc.of("EmRET" + m), cb -> cb.withMethodBody(m, mtd, ClassFile.ACC_STATIC, body));
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
        Class<?> c = Class.forName("java.lang.classfile.instruction.ReturnInstruction");
        check(c != null, "java.lang.classfile.instruction.ReturnInstruction loads");
        check(c.getName().equals("java.lang.classfile.instruction.ReturnInstruction"), "binary name java.lang.classfile.instruction.ReturnInstruction");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.instruction.ReturnInstruction is public");
        check(c.isInterface(), "java.lang.classfile.instruction.ReturnInstruction is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.instruction.ReturnInstruction has no superclass");
        check(declares(c, "typeKind"), "declares typeKind");
        check(declares(c, "of"), "declares of");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        ReturnInstruction i = ReturnInstruction.of(TypeKind.VOID);
        check(i.opcode() == Opcode.RETURN && i.typeKind() == TypeKind.VOID && i.sizeInBytes() == 1, "of(VOID)");
        check(ReturnInstruction.of(Opcode.IRETURN).typeKind() == TypeKind.INT, "of(IRETURN) INT");
        check(ReturnInstruction.of(Opcode.LRETURN).typeKind() == TypeKind.LONG, "of(LRETURN) LONG");
        check(ReturnInstruction.of(Opcode.ARETURN).typeKind() == TypeKind.REFERENCE, "of(ARETURN) REFERENCE");

    }


    // ---- Part 3: 发射-回读（核心）：六族按方法描述符发射 ----
    static void testEmitReadback() throws Exception {
        Object[][] rows = {
                {MethodTypeDesc.ofDescriptor("()I"), Opcode.IRETURN, TypeKind.INT},
                {MethodTypeDesc.ofDescriptor("()J"), Opcode.LRETURN, TypeKind.LONG},
                {MethodTypeDesc.ofDescriptor("()F"), Opcode.FRETURN, TypeKind.FLOAT},
                {MethodTypeDesc.ofDescriptor("()D"), Opcode.DRETURN, TypeKind.DOUBLE},
                {MethodTypeDesc.ofDescriptor("()Ljava/lang/Object;"), Opcode.ARETURN, TypeKind.REFERENCE},
                {ConstantDescs.MTD_void, Opcode.RETURN, TypeKind.VOID},
        };
        String[] pushers = {"iconst", "lconst", "fconst", "dconst", "aconst", "none"};
        String mname = "m";
        for (int r = 0; r < rows.length; r++) {
            final int row = r;
            java.util.List<CodeElement> els = emit(mname + r, (MethodTypeDesc) rows[r][0], cob -> {
                switch (row) {
                    case 0 -> cob.iconst_1().ireturn();
                    case 1 -> cob.lconst_0().lreturn();
                    case 2 -> cob.fconst_0().freturn();
                    case 3 -> cob.dconst_0().dreturn();
                    case 4 -> cob.aconst_null().areturn();
                    default -> cob.return_();
                }
            });
            ReturnInstruction i = null;
            for (CodeElement e : els) if (e instanceof ReturnInstruction x) i = x;
            check(i != null, "return family " + rows[r][1] + " emitted");
            check(i.opcode() == rows[r][1], rows[r][1] + " opcode");
            check(i.typeKind() == rows[r][2], rows[r][1] + " typeKind " + rows[r][2]);
            check(i.sizeInBytes() == 1, rows[r][1] + " sizeInBytes 1");
            check(i.opcode().kind() == Opcode.Kind.RETURN, rows[r][1] + " kind RETURN");
            check(i instanceof Instruction && i instanceof CodeElement, rows[r][1] + " Instruction+CodeElement");
        }
    }

    // ---- Part 4: 工厂全表 + 负例 ----
    static void testFactoryNegative() {
        check(ReturnInstruction.of(TypeKind.INT).opcode() == Opcode.IRETURN, "of(INT)");
        check(ReturnInstruction.of(TypeKind.LONG).opcode() == Opcode.LRETURN, "of(LONG)");
        check(ReturnInstruction.of(TypeKind.FLOAT).opcode() == Opcode.FRETURN, "of(FLOAT)");
        check(ReturnInstruction.of(TypeKind.DOUBLE).opcode() == Opcode.DRETURN, "of(DOUBLE)");
        check(ReturnInstruction.of(TypeKind.REFERENCE).opcode() == Opcode.ARETURN, "of(REFERENCE)");
        check(ReturnInstruction.of(TypeKind.VOID).opcode() == Opcode.RETURN, "of(VOID)");
        // byte/short/char/boolean 窄化到 IRETURN
        check(ReturnInstruction.of(TypeKind.BYTE).opcode() == Opcode.IRETURN, "of(BYTE) narrows IRETURN");
        check(ReturnInstruction.of(TypeKind.SHORT).opcode() == Opcode.IRETURN, "of(SHORT) narrows IRETURN");
        check(ReturnInstruction.of(TypeKind.CHAR).opcode() == Opcode.IRETURN, "of(CHAR) narrows IRETURN");
        check(ReturnInstruction.of(TypeKind.BOOLEAN).opcode() == Opcode.IRETURN, "of(BOOLEAN) narrows IRETURN");
        check(ReturnInstruction.of(Opcode.IRETURN).typeKind() == TypeKind.INT, "of(IRETURN) INT");
        check(ReturnInstruction.of(Opcode.RETURN).typeKind() == TypeKind.VOID, "of(RETURN) VOID");
        try { ReturnInstruction.of((TypeKind) null); check(false, "of((TypeKind)null) must throw"); }
        catch (NullPointerException e) { check(true, "of((TypeKind)null) NPE"); }
        try { ReturnInstruction.of((Opcode) null); check(false, "of((Opcode)null) must throw"); }
        catch (NullPointerException e) { check(true, "of((Opcode)null) NPE"); }
        try { ReturnInstruction.of(Opcode.NOP); check(false, "of(NOP) must throw"); }
        catch (IllegalArgumentException e) { check(true, "of(NOP) IAE wrong kind"); }
        try { ReturnInstruction.of(Opcode.IADD); check(false, "of(IADD) must throw"); }
        catch (IllegalArgumentException e) { check(true, "of(IADD) IAE wrong kind"); }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testEmitReadback();
        testFactoryNegative();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
