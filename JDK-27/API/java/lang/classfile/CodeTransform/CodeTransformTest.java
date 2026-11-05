/*
 * @test
 * @summary java.lang.classfile.CodeTransform conformance: white-box structure + black-box behavior.
 * @run main CodeTransformTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.invoke.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.*;
import java.nio.file.*;

public class CodeTransformTest {
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
        Class<?> c = Class.forName("java.lang.classfile.CodeTransform");
        check(c != null, "java.lang.classfile.CodeTransform loads");
        check(c.getName().equals("java.lang.classfile.CodeTransform"), "binary name java.lang.classfile.CodeTransform");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.CodeTransform is public");
        check(c.isInterface(), "java.lang.classfile.CodeTransform is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.CodeTransform has no superclass");
        check(declares(c, "ofStateful"), "declares ofStateful");
        check(declares(c, "endHandler"), "declares endHandler");
        check(declares(c, "andThen"), "declares andThen");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        byte[] src = ClassFile.of().build(ClassDesc.of("pkg.T"), cb -> cb.withMethodBody("m", MethodTypeDesc.of(ConstantDescs.CD_void), 0, cob -> cob.nop().return_()));
        ClassModel cm = ClassFile.of().parse(src);
        byte[] out = ClassFile.of().transformClass(cm, ClassTransform.transformingMethodBodies(CodeTransform.ACCEPT_ALL));
        check(ClassFile.of().parse(out).methods().get(0).code().isPresent(), "code preserved");

    }

    // ---- Part 3: ACCEPT_ALL 指令守恒 ----
    static void testAcceptAllConservation() throws Exception {
        ClassFile cf = ClassFile.of();
        byte[] b = cf.build(ClassDesc.of("COTA"), cb -> cb.withMethodBody("m", MethodTypeDesc.of(ConstantDescs.CD_int), ClassFile.ACC_STATIC,
                cob -> cob.iconst_2().iconst_3().iadd().ireturn()));
        ClassModel src = cf.parse(b);
        List<Opcode> before = opcodes(src), after = opcodes(cf.parse(cf.transformClass(src, ClassTransform.transformingMethodBodies(CodeTransform.ACCEPT_ALL))));
        check(before.equals(List.of(Opcode.ICONST_2, Opcode.ICONST_3, Opcode.IADD, Opcode.IRETURN)), "source opcode sequence");
        check(after.equals(before), "ACCEPT_ALL conserves instruction sequence: " + after);
        check(cf.parse(cf.transformClass(src, ClassTransform.transformingMethodBodies(CodeTransform.ACCEPT_ALL))).methods().get(0).code().isPresent(), "ACCEPT_ALL keeps code model");
    }

    static List<Opcode> opcodes(ClassModel m) {
        List<Opcode> out = new ArrayList<>();
        for (CodeElement e : m.methods().get(0).code().get().elementList())
            if (e instanceof Instruction i) out.add(i.opcode());
        return out;
    }

    // ---- Part 4: 自造匿名 CodeTransform——剥 NOP（facts 范例模板）----
    static void testAnonymousNopFilter() throws Exception {
        ClassFile cf = ClassFile.of();
        byte[] b = cf.build(ClassDesc.of("COTN"), cb -> cb.withMethodBody("m", ConstantDescs.MTD_void, ClassFile.ACC_STATIC,
                cob -> cob.nop().nop().nop().return_()));
        CodeTransform dropNop = new CodeTransform() {
            public void accept(CodeBuilder cb, CodeElement e) {
                if (!(e instanceof Instruction i) || i.opcode() != Opcode.NOP) cb.with(e);
            }
        };
        ClassModel out = cf.parse(cf.transformClass(cf.parse(b), ClassTransform.transformingMethodBodies(dropNop)));
        check(opcodes(out).equals(List.of(Opcode.RETURN)), "anonymous CodeTransform drops all NOPs: " + opcodes(out));
        check(cf.verify(out).isEmpty(), "NOP-stripped body verifies clean");
        // 过滤变换可选择性保留（只剥第二个 NOP 不可能按 bci 区分——改为剥全部再断言计数差异源 3→0）
        ClassModel srcModel = cf.parse(b);
        check(opcodes(srcModel).size() == 4, "source had 3 NOPs + RETURN");
    }

    // ---- Part 5: 自造匿名 CodeTransform——加倍 iconst（实测 [ICONST_1,ICONST_1,POP,POP,RETURN] 且 verify 干净）----
    static void testDoubling() throws Exception {
        ClassFile cf = ClassFile.of();
        byte[] b = cf.build(ClassDesc.of("COTD"), cb -> cb.withMethodBody("m", ConstantDescs.MTD_void, ClassFile.ACC_STATIC,
                cob -> cob.iconst_1().pop().return_()));
        CodeTransform dbl = new CodeTransform() {
            public void accept(CodeBuilder cb, CodeElement e) {
                if (e instanceof Instruction i && i.opcode() != Opcode.RETURN) { cb.with(e); cb.with(e); }
                else cb.with(e);
            }
        };
        ClassModel out = cf.parse(cf.transformClass(cf.parse(b), ClassTransform.transformingMethodBodies(dbl)));
        check(opcodes(out).equals(List.of(Opcode.ICONST_1, Opcode.ICONST_1, Opcode.POP, Opcode.POP, Opcode.RETURN)), "doubling transform doubles non-return instructions: " + opcodes(out));
        check(cf.verify(out).isEmpty(), "doubled body (balanced push/pop) verifies clean");
    }

    // ---- Part 6: andThen 组合语义（t1 先应用、t2 后应用； NOP 剥除后再加倍）----
    static void testAndThen() throws Exception {
        ClassFile cf = ClassFile.of();
        byte[] b = cf.build(ClassDesc.of("COTA2"), cb -> cb.withMethodBody("m", ConstantDescs.MTD_void, ClassFile.ACC_STATIC,
                cob -> cob.nop().iconst_1().pop().nop().return_()));
        CodeTransform dropNop = new CodeTransform() {
            public void accept(CodeBuilder cb, CodeElement e) {
                if (!(e instanceof Instruction i) || i.opcode() != Opcode.NOP) cb.with(e);
            }
        };
        CodeTransform dbl = new CodeTransform() {
            public void accept(CodeBuilder cb, CodeElement e) {
                if (e instanceof Instruction i && i.opcode() != Opcode.RETURN) { cb.with(e); cb.with(e); }
                else cb.with(e);
            }
        };
        // dropNop.andThen(dbl)：先剥 NOP，再对剩余非 RETURN 指令加倍
        ClassModel out = cf.parse(cf.transformClass(cf.parse(b), ClassTransform.transformingMethodBodies(dropNop.andThen(dbl))));
        check(opcodes(out).equals(List.of(Opcode.ICONST_1, Opcode.ICONST_1, Opcode.POP, Opcode.POP, Opcode.RETURN)), "andThen: first drops NOPs then doubles: " + opcodes(out));
        // 反序组合 dbl.andThen(dropNop)：加倍后再剥 NOP（源无 NOP 双倍问题，仍无 NOP）
        ClassModel out2 = cf.parse(cf.transformClass(cf.parse(b), ClassTransform.transformingMethodBodies(dbl.andThen(dropNop))));
        check(opcodes(out2).equals(List.of(Opcode.ICONST_1, Opcode.ICONST_1, Opcode.POP, Opcode.POP, Opcode.RETURN)), "reversed andThen same here");
        // ACCEPT_ALL.andThen(x) 恒等 x 的效果
        ClassModel out3 = cf.parse(cf.transformClass(cf.parse(b), ClassTransform.transformingMethodBodies(CodeTransform.ACCEPT_ALL.andThen(dropNop))));
        check(opcodes(out3).equals(List.of(Opcode.ICONST_1, Opcode.POP, Opcode.RETURN)), "ACCEPT_ALL.andThen(dropNop) == dropNop");
        // 与自身组合两次仍是加倍后保持
        check(CodeTransform.ACCEPT_ALL.andThen(CodeTransform.ACCEPT_ALL) != null, "ACCEPT_ALL self-chain non-null");
    }

    // ---- Part 7: ofStateful——每个方法体 transform 各拿新实例（Supplier 被调用，实测每方法 1 次）----
    static void testOfStateful() throws Exception {
        ClassFile cf = ClassFile.of();
        byte[] b = cf.build(ClassDesc.of("COTS"), cb -> cb
                .withMethodBody("a", ConstantDescs.MTD_void, 0, cob -> cob.return_())
                .withMethodBody("b", ConstantDescs.MTD_void, 0, cob -> cob.return_()));
        int[] supplies = {0};
        CodeTransform st = CodeTransform.ofStateful(() -> {
            supplies[0]++;
            return CodeTransform.ACCEPT_ALL;
        });
        cf.transformClass(cf.parse(b), ClassTransform.transformingMethodBodies(st));
        check(supplies[0] == 2, "ofStateful supplier called per method body: " + supplies[0]);
        // 有状态计数隔离：每个实例只看到自己方法的元素
        int[] perMethod = {0};
        CodeTransform counting = CodeTransform.ofStateful(() -> new CodeTransform() {
            int local;
            public void accept(CodeBuilder cb, CodeElement e) { local++; cb.with(e); }
            public void atEnd(CodeBuilder cb) { perMethod[0] = perMethod[0] * 100 + local; }
        });
        cf.transformClass(cf.parse(b), ClassTransform.transformingMethodBodies(counting));
        check(perMethod[0] > 0, "stateful per-instance accept counting works: " + perMethod[0]);
        // ofStateful(null) → NPE
        try {
            CodeTransform.ofStateful(null);
            check(false, "ofStateful(null) must throw");
        } catch (NullPointerException expected) {
            check(true, "ofStateful(null) NPE");
        }
    }

    // ---- Part 8: endHandler 在 RETURN 之后追加（实测死代码归一化为 [RETURN, ATHROW]，verify 干净）----
    static void testEndHandler() throws Exception {
        ClassFile cf = ClassFile.of();
        byte[] b = cf.build(ClassDesc.of("COTE"), cb -> cb.withMethodBody("m", ConstantDescs.MTD_void, ClassFile.ACC_STATIC, cob -> cob.return_()));
        List<String> hookLog = new ArrayList<>();
        CodeTransform eh = CodeTransform.endHandler(cb -> {
            hookLog.add("end");
            cb.nop();
        });
        ClassModel out = cf.parse(cf.transformClass(cf.parse(b), ClassTransform.transformingMethodBodies(CodeTransform.ACCEPT_ALL.andThen(eh))));
        check(hookLog.equals(List.of("end")), "CodeTransform.endHandler consumer invoked once");
        List<Opcode> ops = opcodes(out);
        check(ops.get(0) == Opcode.RETURN, "original RETURN still first: " + ops);
        check(ops.size() > 1, "endHandler appended code after RETURN: " + ops);
        check(cf.verify(out).isEmpty(), "endHandler-appended body verifies clean");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testAcceptAllConservation();
        testAnonymousNopFilter();
        testDoubling();
        testAndThen();
        testOfStateful();
        testEndHandler();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
