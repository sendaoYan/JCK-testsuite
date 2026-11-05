/*
 * @test
 * @summary java.lang.classfile.instruction.NewPrimitiveArrayInstruction conformance: white-box structure + black-box behavior.
 * @run main NewPrimitiveArrayInstructionTest
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

public class NewPrimitiveArrayInstructionTest {
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
        byte[] b = ClassFile.of().build(ClassDesc.of("EmNPA" + m), cb -> cb.withMethodBody(m, mtd, ClassFile.ACC_STATIC, body));
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
        Class<?> c = Class.forName("java.lang.classfile.instruction.NewPrimitiveArrayInstruction");
        check(c != null, "java.lang.classfile.instruction.NewPrimitiveArrayInstruction loads");
        check(c.getName().equals("java.lang.classfile.instruction.NewPrimitiveArrayInstruction"), "binary name java.lang.classfile.instruction.NewPrimitiveArrayInstruction");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.instruction.NewPrimitiveArrayInstruction is public");
        check(c.isInterface(), "java.lang.classfile.instruction.NewPrimitiveArrayInstruction is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.instruction.NewPrimitiveArrayInstruction has no superclass");
        check(declares(c, "typeKind"), "declares typeKind");
        check(declares(c, "of"), "declares of");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        NewPrimitiveArrayInstruction i = NewPrimitiveArrayInstruction.of(TypeKind.INT);
        check(i.opcode() == Opcode.NEWARRAY, "opcode");
        check(i.typeKind() == TypeKind.INT, "typeKind INT");
        check(NewPrimitiveArrayInstruction.of(TypeKind.BYTE).typeKind() == TypeKind.BYTE, "BYTE");

    }


    // ---- Part 3: 发射-回读（核心） ----
    static void testEmitReadback() throws Exception {
        java.util.List<CodeElement> els = emit("m", MethodTypeDesc.ofDescriptor("()[I"),
                cob -> cob.iconst_5().newarray(TypeKind.INT).areturn());
        NewPrimitiveArrayInstruction i = null;
        for (CodeElement e : els) if (e instanceof NewPrimitiveArrayInstruction x) i = x;
        check(i != null, "newarray(INT) emitted + read back");
        check(i.opcode() == Opcode.NEWARRAY, "NEWARRAY opcode");
        check(i.sizeInBytes() == 2, "NEWARRAY sizeInBytes 2");
        check(i.opcode().kind() == Opcode.Kind.NEW_PRIMITIVE_ARRAY, "kind NEW_PRIMITIVE_ARRAY");
        check(i.typeKind() == TypeKind.INT, "typeKind INT");
        check(i instanceof Instruction && i instanceof CodeElement, "Instruction+CodeElement");
        // BOOLEAN
        els = emit("b", MethodTypeDesc.ofDescriptor("()[Z"), cob -> cob.iconst_2().newarray(TypeKind.BOOLEAN).areturn());
        for (CodeElement e : els) if (e instanceof NewPrimitiveArrayInstruction x) {
            check(x.opcode() == Opcode.NEWARRAY && x.typeKind() == TypeKind.BOOLEAN, "newarray(BOOLEAN)");
        }
        // newarrayCode 联动（TypeKind ↔ JVM atype）
        check(TypeKind.INT.newarrayCode() == 10, "INT newarrayCode 10");
        check(TypeKind.BOOLEAN.newarrayCode() == 4, "BOOLEAN newarrayCode 4");
        check(TypeKind.LONG.newarrayCode() == 11, "LONG newarrayCode 11");
        check(TypeKind.DOUBLE.newarrayCode() == 7, "DOUBLE newarrayCode 7");
        check(TypeKind.fromNewarrayCode(10) == TypeKind.INT, "fromNewarrayCode(10) INT");
        check(TypeKind.fromNewarrayCode(4) == TypeKind.BOOLEAN, "fromNewarrayCode(4) BOOLEAN");
    }

    // ---- Part 4: 工厂全 8 原始类型矩阵 + 负例 ----
    static void testFactoryNegative() {
        TypeKind[] prim = {TypeKind.BOOLEAN, TypeKind.BYTE, TypeKind.CHAR, TypeKind.SHORT,
                TypeKind.INT, TypeKind.LONG, TypeKind.FLOAT, TypeKind.DOUBLE};
        for (TypeKind t : prim) {
            NewPrimitiveArrayInstruction x = NewPrimitiveArrayInstruction.of(t);
            check(x.opcode() == Opcode.NEWARRAY, t + " opcode NEWARRAY");
            check(x.typeKind() == t, t + " typeKind roundtrip");
            check(x.sizeInBytes() == 2, t + " sizeInBytes 2");
        }
        try { NewPrimitiveArrayInstruction.of(null); check(false, "of(null) must throw"); }
        catch (NullPointerException e) { check(true, "of(null) NPE"); }
        try { NewPrimitiveArrayInstruction.of(TypeKind.VOID); check(false, "of(VOID) must throw"); }
        catch (IllegalArgumentException e) { check(true, "of(VOID) IAE"); }
        try { NewPrimitiveArrayInstruction.of(TypeKind.REFERENCE); check(false, "of(REFERENCE) must throw"); }
        catch (IllegalArgumentException e) { check(true, "of(REFERENCE) IAE (anewarray domain)"); }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testEmitReadback();
        testFactoryNegative();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
