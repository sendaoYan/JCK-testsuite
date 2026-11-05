/*
 * @test
 * @summary java.lang.classfile.CodeBuilder.BlockCodeBuilder conformance: white-box structure + black-box behavior.
 * @run main BlockCodeBuilderTest
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

public class BlockCodeBuilderTest {
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
        Class<?> c = Class.forName("java.lang.classfile.CodeBuilder$BlockCodeBuilder");
        check(c != null, "java.lang.classfile.CodeBuilder.BlockCodeBuilder loads");
        check(c.getName().equals("java.lang.classfile.CodeBuilder$BlockCodeBuilder"), "binary name java.lang.classfile.CodeBuilder$BlockCodeBuilder");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.CodeBuilder.BlockCodeBuilder is public");
        check(c.isInterface(), "java.lang.classfile.CodeBuilder.BlockCodeBuilder is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.CodeBuilder.BlockCodeBuilder has no superclass");
        check(declares(c, "breakLabel"), "declares breakLabel");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        byte[] b = ClassFile.of().build(ClassDesc.of("pkg.T"), cb -> cb.withMethodBody("abs", MethodTypeDesc.of(ConstantDescs.CD_int, ConstantDescs.CD_int), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC, cob -> cob.iload(0).ifThen(Opcode.IFLT, blk -> blk.iload(0).ineg().ireturn()).iload(0).ireturn()));
        ClassModel cm = ClassFile.of().parse(b);
        CodeModel code = cm.methods().get(0).code().get();
        boolean hasBranch = false;
        for (CodeElement ce : code) { if (ce instanceof Instruction i && i.opcode().kind() == Opcode.Kind.BRANCH) hasBranch = true; }
        check(hasBranch, "branch emitted by block builder");
        check(code.elementList().size() >= 5, "block body has instructions");

    }

    static List<String> ops(CodeModel code) {
        List<String> r = new ArrayList<>();
        for (CodeElement e : code.elementList()) if (e instanceof Instruction i) r.add(i.opcode().name());
        return r;
    }
    static CodeModel codeOf(ClassModel m, String name) {
        return m.methods().stream().filter(x -> x.methodName().stringValue().equals(name)).findFirst().orElseThrow().code().orElseThrow();
    }

    // ---- Part 3: 嵌套 block（三层）+ breakLabel 语义 + 死代码 NOP/ATHROW（实测锁定） ----
    static void testNestedBlocks() throws Exception {
        ClassFile cf = ClassFile.of();
        byte[] b = cf.build(ClassDesc.of("BcbT"), cb -> cb.withMethodBody("m", MethodTypeDesc.ofDescriptor("(I)I"), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC, c -> c
                .block(l1 -> l1
                        .block(l2 -> l2
                                .block(l3 -> l3.iload(0).ifgt(l3.breakLabel()))
                                .iconst_1().ireturn())
                        .iconst_2().ireturn())
                .iconst_3().ireturn()));
        ClassModel cm = cf.parse(b);
        check(cf.verify(cm).isEmpty(), "nested blocks verify clean");
        // 实测：内层块提前 ireturn 后，外层剩余指令成死代码 → builder 以 NOP+ATHROW 填充
        check(ops(codeOf(cm, "m")).equals(List.of("ILOAD_0", "IFGT", "ICONST_1", "IRETURN", "NOP", "ATHROW", "NOP", "ATHROW")),
                "dead code becomes NOP/ATHROW: " + ops(codeOf(cm, "m")));
        Class<?> dc = MethodHandles.lookup().defineClass(b);
        check(dc.getMethod("m", int.class).invoke(null, -1).equals(1), "m(-1)==1 (l2 body)");
        check(dc.getMethod("m", int.class).invoke(null, 1).equals(1), "m(1)==1 (break out of l3 → l2 body)");
        // breakLabel() 在各层均非 null 且可跳（跳到本层块尾 = 继续执行块后代码）
        final boolean[] bl = new boolean[3];
        cf.build(ClassDesc.of("BcbU"), cb -> cb.withMethodBody("m", MethodTypeDesc.ofDescriptor("()I"), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC, c -> c
                .block(l1 -> {
                    bl[0] = l1.breakLabel() != null;
                    l1.block(l2 -> {
                        bl[1] = l2.breakLabel() != null;
                        l2.block(l3 -> { bl[2] = l3.breakLabel() != null; l3.goto_(l3.breakLabel()); });
                        l2.iconst_2().ireturn();
                    });
                    c.iconst_3().ireturn();
                }).iconst_4().ireturn()));
        check(bl[0] && bl[1] && bl[2], "breakLabel() non-null at all 3 levels");
    }

    // ---- Part 4: 嵌套 block 内发射跳转到外层 label（block 与 labelBinding 协作，实测） ----
    static void testGotoOuterLabel() throws Exception {
        ClassFile cf = ClassFile.of();
        byte[] b = cf.build(ClassDesc.of("BcbV"), cb -> cb.withMethodBody("m", MethodTypeDesc.ofDescriptor("(I)I"), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC, c -> {
            Label outer = c.newLabel();
            c.block(l1 -> {
                c.block(l2 -> c.goto_(outer));   // 内层块跳到外层 label
                c.nop();
            });
            c.labelBinding(outer);
            c.bipush(7).ireturn();
        }));
        ClassModel cm = cf.parse(b);
        check(cf.verify(cm).isEmpty(), "goto outer label verify clean");
        // 内层块余下 nop 成为死代码（NOP+ATHROW 替代），最终落到 outer 返回 7
        check(ops(codeOf(cm, "m")).contains("GOTO"), "goto emitted inside nested block");
        check(MethodHandles.lookup().defineClass(b).getMethod("m", int.class).invoke(null, 0).equals(7), "inner block goto outer label → 7");
    }

    // ---- Part 5: ifThen/ifThenElse 两分支运行闭环 ----
    static void testIfThenElse() throws Exception {
        ClassFile cf = ClassFile.of();
        byte[] b = cf.build(ClassDesc.of("BcbW"), cb -> cb
                .withMethodBody("ie", MethodTypeDesc.ofDescriptor("(I)I"), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC, c -> c
                        .iload(0).ifThenElse(Opcode.IFNE, t -> t.iconst_1().ireturn(), e -> e.iconst_2().ireturn())
                        .iconst_3().ireturn())
                .withMethodBody("it", MethodTypeDesc.ofDescriptor("(I)I"), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC, c -> c
                        .iload(0).ifThen(Opcode.IFLT, t -> t.iconst_m1().ireturn())
                        .iconst_1().ireturn()));
        ClassModel cm = cf.parse(b);
        check(cf.verify(cm).isEmpty(), "ifThen(Else) verify clean");
        int branches = 0;
        for (CodeElement e : codeOf(cm, "ie").elementList()) if (e instanceof Instruction i && i.opcode().kind() == Opcode.Kind.BRANCH) branches++;
        check(branches == 1, "ifThenElse emits 1 conditional branch (then 后接 else 落空，双分支都 ireturn 时无需 goto): " + branches);
        Class<?> dc = MethodHandles.lookup().defineClass(b);
        check(dc.getMethod("ie", int.class).invoke(null, 5).equals(1), "ifThenElse(5) then==1");
        check(dc.getMethod("ie", int.class).invoke(null, 0).equals(2), "ifThenElse(0) else==2");
        check(dc.getMethod("it", int.class).invoke(null, -3).equals(-1), "ifThen(-3) then==-1");
        check(dc.getMethod("it", int.class).invoke(null, 3).equals(1), "ifThen(3) fallthrough==1");
    }

    // ---- Part 6: trying(try 块, CatchBuilder) 在 BlockCodeBuilder 体系内的运行闭环 ----
    static void testTryingRuntime() throws Exception {
        ClassFile cf = ClassFile.of();
        byte[] b = cf.build(ClassDesc.of("BcbX"), cb -> cb.withMethodBody("safe", MethodTypeDesc.ofDescriptor("(I)I"), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC, c -> c
                .trying(t -> t.iload(0).ifeq(t.breakLabel()).iconst_1().iconst_0().idiv().pop().iconst_0().ireturn(),
                        cat -> cat.catching(ClassDesc.of("java.lang.ArithmeticException"), h -> h.pop().bipush(42).ireturn()))
                .bipush(9).ireturn()));
        ClassModel cm = cf.parse(b);
        check(cf.verify(cm).isEmpty(), "trying verify clean");
        List<ExceptionCatch> eh = codeOf(cm, "safe").exceptionHandlers();
        check(eh.size() == 1, "trying handler count 1");
        check(eh.get(0).catchType().get().asInternalName().equals("java/lang/ArithmeticException"), "handler catchType internal name");
        Class<?> dc = MethodHandles.lookup().defineClass(b);
        check(dc.getMethod("safe", int.class).invoke(null, 1).equals(42), "safe(1): try 内除零被捕获 → 42");
        check(dc.getMethod("safe", int.class).invoke(null, 0).equals(9), "safe(0): breakLabel 跳出 try → 9");
        // catch 块内 breakLabel 属于 catch 块自身（h is BlockCodeBuilder）
        byte[] b2 = cf.build(ClassDesc.of("BcbY"), cb -> cb.withMethodBody("m", MethodTypeDesc.ofDescriptor("()I"), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC, c -> c
                .trying(t -> t.aconst_null().athrow(),
                        cat -> cat.catchingAll(h -> { check(h.breakLabel() != null, "catch block breakLabel non-null"); h.pop().bipush(8).ireturn(); }))
                .iconst_0().ireturn()));
        check(MethodHandles.lookup().defineClass(b2).getMethod("m").invoke(null).equals(8), "catchingAll runtime 8");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testNestedBlocks();
        testGotoOuterLabel();
        testIfThenElse();
        testTryingRuntime();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
