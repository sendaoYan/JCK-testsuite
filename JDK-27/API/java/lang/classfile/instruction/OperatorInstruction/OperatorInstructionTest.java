/*
 * @test
 * @summary java.lang.classfile.instruction.OperatorInstruction conformance: white-box structure + black-box behavior.
 * @run main OperatorInstructionTest
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

public class OperatorInstructionTest {
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
        byte[] b = ClassFile.of().build(ClassDesc.of("EmOPI" + m), cb -> cb.withMethodBody(m, mtd, ClassFile.ACC_STATIC, body));
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
        Class<?> c = Class.forName("java.lang.classfile.instruction.OperatorInstruction");
        check(c != null, "java.lang.classfile.instruction.OperatorInstruction loads");
        check(c.getName().equals("java.lang.classfile.instruction.OperatorInstruction"), "binary name java.lang.classfile.instruction.OperatorInstruction");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.instruction.OperatorInstruction is public");
        check(c.isInterface(), "java.lang.classfile.instruction.OperatorInstruction is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.instruction.OperatorInstruction has no superclass");
        check(declares(c, "typeKind"), "declares typeKind");
        check(declares(c, "of"), "declares of");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        OperatorInstruction i = OperatorInstruction.of(Opcode.IADD);
        check(i.opcode() == Opcode.IADD, "opcode");
        check(i.typeKind() == TypeKind.INT, "typeKind INT");
        check(OperatorInstruction.of(Opcode.DSUB).typeKind() == TypeKind.DOUBLE, "DSUB DOUBLE");

    }


    // ---- Part 3: 发射-回读（核心）：int/long/float/double 四族全表 ----
    static void testEmitReadback() throws Exception {
        java.util.List<CodeElement> els = emit("m", MethodTypeDesc.ofDescriptor("(IIJJFFDD)V"), cob -> {
            cob.iload(0).iload(1).iadd().pop();
            cob.iload(0).iload(1).isub().pop();
            cob.iload(0).iload(1).imul().pop();
            cob.iload(0).iload(1).idiv().pop();
            cob.iload(0).iload(1).irem().pop();
            cob.iload(0).iload(1).iand().pop();
            cob.iload(0).iload(1).ior().pop();
            cob.iload(0).iload(1).ixor().pop();
            cob.iload(0).iload(1).ishl().pop();
            cob.iload(0).iload(1).ishr().pop();
            cob.iload(0).iload(1).iushr().pop();
            cob.iload(0).ineg().pop();
            cob.lload(2).lload(4).ladd().pop2();
            cob.lload(2).lload(4).lsub().pop2();
            cob.lload(2).lload(4).lmul().pop2();
            cob.lload(2).lload(4).ldiv().pop2();
            cob.lload(2).lload(4).lrem().pop2();
            cob.lload(2).lload(4).land().pop2();
            cob.lload(2).lload(4).lor().pop2();
            cob.lload(2).lload(4).lxor().pop2();
            cob.lload(2).lload(4).lshl().pop2();
            cob.lload(2).lload(4).lshr().pop2();
            cob.lload(2).lload(4).lushr().pop2();
            cob.lload(2).lneg().pop2();
            cob.lload(2).lload(4).lcmp().pop();
            cob.fload(6).fload(7).fadd().pop();
            cob.fload(6).fload(7).fsub().pop();
            cob.fload(6).fload(7).fmul().pop();
            cob.fload(6).fload(7).fdiv().pop();
            cob.fload(6).fload(7).frem().pop();
            cob.fload(6).fneg().pop();
            cob.fload(6).fload(7).fcmpl().pop();
            cob.fload(6).fload(7).fcmpg().pop();
            cob.dload(8).dload(10).dadd().pop2();
            cob.dload(8).dload(10).dsub().pop2();
            cob.dload(8).dload(10).dmul().pop2();
            cob.dload(8).dload(10).ddiv().pop2();
            cob.dload(8).dload(10).drem().pop2();
            cob.dload(8).dneg().pop2();
            cob.dload(8).dload(10).dcmpl().pop();
            cob.dload(8).dload(10).dcmpg().pop();
            cob.return_();
        });
        Opcode[] intOps = {Opcode.IADD, Opcode.ISUB, Opcode.IMUL, Opcode.IDIV, Opcode.IREM,
                Opcode.IAND, Opcode.IOR, Opcode.IXOR, Opcode.ISHL, Opcode.ISHR, Opcode.IUSHR, Opcode.INEG};
        Opcode[] longOps = {Opcode.LADD, Opcode.LSUB, Opcode.LMUL, Opcode.LDIV, Opcode.LREM,
                Opcode.LAND, Opcode.LOR, Opcode.LXOR, Opcode.LSHL, Opcode.LSHR, Opcode.LUSHR, Opcode.LNEG, Opcode.LCMP};
        Opcode[] floatOps = {Opcode.FADD, Opcode.FSUB, Opcode.FMUL, Opcode.FDIV, Opcode.FREM,
                Opcode.FNEG, Opcode.FCMPL, Opcode.FCMPG};
        Opcode[] doubleOps = {Opcode.DADD, Opcode.DSUB, Opcode.DMUL, Opcode.DDIV, Opcode.DREM,
                Opcode.DNEG, Opcode.DCMPL, Opcode.DCMPG};
        java.util.List<Opcode> got = new ArrayList<>();
        for (CodeElement e : els) if (e instanceof OperatorInstruction x) {
            got.add(x.opcode());
            check(x.sizeInBytes() == 1, x.opcode() + " sizeInBytes 1");
            check(x.opcode().kind() == Opcode.Kind.OPERATOR, x.opcode() + " kind OPERATOR");
            check(x instanceof Instruction && x instanceof CodeElement, x.opcode() + " Instruction+CodeElement");
        }
        java.util.List<Opcode> expect = new ArrayList<>();
        for (Opcode o : intOps) expect.add(o);
        for (Opcode o : longOps) expect.add(o);
        for (Opcode o : floatOps) expect.add(o);
        for (Opcode o : doubleOps) expect.add(o);
        check(got.equals(expect), "41 operators in order (got " + got.size() + ")");
        // typeKind 分族
        for (CodeElement e : els) if (e instanceof OperatorInstruction x) {
            TypeKind want = x.opcode().name().startsWith("I") || x.opcode() == Opcode.INEG ? TypeKind.INT
                    : x.opcode().name().startsWith("L") ? TypeKind.LONG
                    : x.opcode().name().startsWith("F") ? TypeKind.FLOAT : TypeKind.DOUBLE;
            check(x.typeKind() == want, x.opcode() + " typeKind " + want);
        }
    }

    // ---- Part 4: 工厂 + 负例 ----
    static void testFactoryNegative() {
        check(OperatorInstruction.of(Opcode.IADD).typeKind() == TypeKind.INT, "of(IADD) INT");
        check(OperatorInstruction.of(Opcode.LADD).typeKind() == TypeKind.LONG, "of(LADD) LONG");
        check(OperatorInstruction.of(Opcode.FADD).typeKind() == TypeKind.FLOAT, "of(FADD) FLOAT");
        check(OperatorInstruction.of(Opcode.DADD).typeKind() == TypeKind.DOUBLE, "of(DADD) DOUBLE");
        check(OperatorInstruction.of(Opcode.INEG).typeKind() == TypeKind.INT, "of(INEG) INT");
        check(OperatorInstruction.of(Opcode.LCMP).typeKind() == TypeKind.LONG, "of(LCMP) LONG");
        try { OperatorInstruction.of(null); check(false, "of(null) must throw"); }
        catch (NullPointerException e) { check(true, "of(null) NPE"); }
        for (Opcode bad : new Opcode[]{Opcode.NOP, Opcode.IRETURN, Opcode.GETSTATIC}) {
            try { OperatorInstruction.of(bad); check(false, "of(" + bad + ") must throw"); }
            catch (IllegalArgumentException e) { check(true, "of(" + bad + ") IAE wrong kind"); }
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testEmitReadback();
        testFactoryNegative();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
