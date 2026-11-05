/*
 * @test
 * @summary java.lang.classfile.instruction.ConvertInstruction conformance: white-box structure + black-box behavior.
 * @run main ConvertInstructionTest
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

public class ConvertInstructionTest {
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
        byte[] b = ClassFile.of().build(ClassDesc.of("EmCVI" + m), cb -> cb.withMethodBody(m, mtd, ClassFile.ACC_STATIC, body));
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
        Class<?> c = Class.forName("java.lang.classfile.instruction.ConvertInstruction");
        check(c != null, "java.lang.classfile.instruction.ConvertInstruction loads");
        check(c.getName().equals("java.lang.classfile.instruction.ConvertInstruction"), "binary name java.lang.classfile.instruction.ConvertInstruction");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.instruction.ConvertInstruction is public");
        check(c.isInterface(), "java.lang.classfile.instruction.ConvertInstruction is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.instruction.ConvertInstruction has no superclass");
        check(declares(c, "fromType"), "declares fromType");
        check(declares(c, "toType"), "declares toType");
        check(declares(c, "of"), "declares of");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        ConvertInstruction i = ConvertInstruction.of(TypeKind.INT, TypeKind.LONG);
        check(i.opcode() == Opcode.I2L, "opcode I2L");
        check(i.fromType() == TypeKind.INT, "fromType");
        check(i.toType() == TypeKind.LONG, "toType");
        check(ConvertInstruction.of(Opcode.D2I).fromType() == TypeKind.DOUBLE, "of(D2I) from DOUBLE");

    }


    // ---- Part 3: 发射-回读矩阵（核心）：i2x/l2x/f2x/d2x 全表 ----
    static void testEmitReadback() throws Exception {
        java.util.List<CodeElement> els = emit("m", MethodTypeDesc.ofDescriptor("(IJFD)V"), cob -> {
            cob.iload(0).i2l().pop2();
            cob.iload(0).i2f().pop();
            cob.iload(0).i2d().pop2();
            cob.iload(0).i2b().pop();
            cob.iload(0).i2c().pop();
            cob.iload(0).i2s().pop();
            cob.lload(1).l2i().pop();
            cob.lload(1).l2f().pop();
            cob.lload(1).l2d().pop2();
            cob.fload(3).f2i().pop();
            cob.fload(3).f2l().pop2();
            cob.fload(3).f2d().pop2();
            cob.dload(4).d2i().pop();
            cob.dload(4).d2l().pop2();
            cob.dload(4).d2f().pop();
            cob.return_();
        });
        Opcode[] ops = {Opcode.I2L, Opcode.I2F, Opcode.I2D, Opcode.I2B, Opcode.I2C, Opcode.I2S,
                Opcode.L2I, Opcode.L2F, Opcode.L2D, Opcode.F2I, Opcode.F2L, Opcode.F2D,
                Opcode.D2I, Opcode.D2L, Opcode.D2F};
        TypeKind[] from = {TypeKind.INT, TypeKind.INT, TypeKind.INT, TypeKind.INT, TypeKind.INT, TypeKind.INT,
                TypeKind.LONG, TypeKind.LONG, TypeKind.LONG, TypeKind.FLOAT, TypeKind.FLOAT, TypeKind.FLOAT,
                TypeKind.DOUBLE, TypeKind.DOUBLE, TypeKind.DOUBLE};
        TypeKind[] to = {TypeKind.LONG, TypeKind.FLOAT, TypeKind.DOUBLE, TypeKind.BYTE, TypeKind.CHAR, TypeKind.SHORT,
                TypeKind.INT, TypeKind.FLOAT, TypeKind.DOUBLE, TypeKind.INT, TypeKind.LONG, TypeKind.DOUBLE,
                TypeKind.INT, TypeKind.LONG, TypeKind.FLOAT};
        java.util.List<Opcode> got = new ArrayList<>();
        for (CodeElement e : els) if (e instanceof ConvertInstruction x) {
            got.add(x.opcode());
            check(x.sizeInBytes() == 1, x.opcode() + " sizeInBytes 1");
            check(x.opcode().kind() == Opcode.Kind.CONVERT, x.opcode() + " kind CONVERT");
            check(x instanceof Instruction && x instanceof CodeElement, x.opcode() + " Instruction+CodeElement");
        }
        check(got.equals(java.util.List.of(ops)), "15 convert opcodes in order: " + got);
        for (int i = 0; i < ops.length; i++)
            for (CodeElement e : els) if (e instanceof ConvertInstruction x && x.opcode() == ops[i]) {
                check(x.fromType() == from[i], ops[i] + " fromType " + from[i]);
                check(x.toType() == to[i], ops[i] + " toType " + to[i]);
            }
        // javac 真产物：helper 方法 (int)->long 强转产生 I2L
        byte[] self = ConvertInstructionTest.class.getResourceAsStream("ConvertInstructionTest.class").readAllBytes();
        boolean found = false;
        for (MethodModel mm : ClassFile.of().parse(self).methods())
            if (mm.methodName().stringValue().equals("i2lHelper") && mm.code().isPresent())
                for (CodeElement e : mm.code().get().elementList())
                    if (e instanceof ConvertInstruction x && x.opcode() == Opcode.I2L && x.fromType() == TypeKind.INT && x.toType() == TypeKind.LONG) found = true;
        check(found, "javac i2lHelper emits I2L from/to correct");
    }

    // ---- Part 4: 工厂 + 负例 ----
    static void testFactoryNegative() {
        check(ConvertInstruction.of(TypeKind.INT, TypeKind.LONG).opcode() == Opcode.I2L, "of(INT,LONG) I2L");
        check(ConvertInstruction.of(Opcode.D2I).fromType() == TypeKind.DOUBLE, "of(D2I) from DOUBLE");
        check(ConvertInstruction.of(Opcode.D2I).toType() == TypeKind.INT, "of(D2I) to INT");
        check(ConvertInstruction.of(Opcode.I2L).sizeInBytes() == 1, "of(I2L) size 1");
        try { ConvertInstruction.of(TypeKind.INT, TypeKind.INT); check(false, "of(INT,INT) must throw"); }
        catch (IllegalArgumentException e) { check(true, "of(INT,INT) IAE no-op convert"); }
        try { ConvertInstruction.of(TypeKind.REFERENCE, TypeKind.INT); check(false, "of(REF,INT) must throw"); }
        catch (IllegalArgumentException e) { check(true, "of(REF,INT) IAE no such conversion"); }
        try { ConvertInstruction.of(null, TypeKind.LONG); check(false, "of(null,LONG) must throw"); }
        catch (NullPointerException e) { check(true, "of(null,LONG) NPE"); }
        try { ConvertInstruction.of(TypeKind.INT, null); check(false, "of(INT,null) must throw"); }
        catch (NullPointerException e) { check(true, "of(INT,null) NPE"); }
        try { ConvertInstruction.of(Opcode.NOP); check(false, "of(NOP) must throw"); }
        catch (IllegalArgumentException e) { check(true, "of(NOP) IAE wrong kind"); }
        try { ConvertInstruction.of((Opcode) null); check(false, "of((Opcode)null) must throw"); }
        catch (NullPointerException e) { check(true, "of((Opcode)null) NPE"); }
    }

    static long i2lHelper(int x) { return (long) x; }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testEmitReadback();
        testFactoryNegative();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
