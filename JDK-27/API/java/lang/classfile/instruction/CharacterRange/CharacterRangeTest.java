/*
 * @test
 * @summary java.lang.classfile.instruction.CharacterRange conformance: white-box structure + black-box behavior.
 * @run main CharacterRangeTest
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

public class CharacterRangeTest {
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
        byte[] b = ClassFile.of().build(ClassDesc.of("EmCHR" + m), cb -> cb.withMethodBody(m, mtd, ClassFile.ACC_STATIC, body));
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
        Class<?> c = Class.forName("java.lang.classfile.instruction.CharacterRange");
        check(c != null, "java.lang.classfile.instruction.CharacterRange loads");
        check(c.getName().equals("java.lang.classfile.instruction.CharacterRange"), "binary name java.lang.classfile.instruction.CharacterRange");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.instruction.CharacterRange is public");
        check(c.isInterface(), "java.lang.classfile.instruction.CharacterRange is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.instruction.CharacterRange has no superclass");
        check(declares(c, "startScope"), "declares startScope");
        check(declares(c, "endScope"), "declares endScope");
        check(declares(c, "characterRangeStart"), "declares characterRangeStart");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        final Label[] scopes = new Label[2];
        ClassFile.of().build(ClassDesc.of("pkg.T"), cb -> cb.withMethodBody("m", MethodTypeDesc.of(ConstantDescs.CD_void), 0, cob -> {
            scopes[0] = cob.newLabel();
            scopes[1] = cob.newLabel();
            CharacterRange cr = CharacterRange.of(scopes[0], scopes[1], 0, 10, CharacterRange.FLAG_BLOCK);
            check(cr.characterRangeStart() == 0, "start");
            check(cr.characterRangeEnd() == 10, "end");
            check(cr.flags() == CharacterRange.FLAG_BLOCK, "flags");
            check(cr.startScope() == scopes[0] && cr.endScope() == scopes[1], "scopes");
            check(CharacterRange.FLAG_STATEMENT == 0x0001 && CharacterRange.FLAG_BLOCK == 0x0002 && CharacterRange.FLAG_ASSIGNMENT == 0x0004, "flag constants");
            cob.return_();
        }));

    }


    // ---- Part 3: 发射-回读矩阵（核心） ----
    static void testEmitReadback() throws Exception {
        // 发射 characterRange(s,e,100,200,FLAG_STATEMENT|FLAG_ASSIGNMENT) → 回读全部访问器
        byte[] b = ClassFile.of().build(ClassDesc.of("EmCHR"), cb -> cb.withMethodBody("m", ConstantDescs.MTD_void, ClassFile.ACC_STATIC, cob -> {
            Label s = cob.newLabel(), e = cob.newLabel();
            cob.labelBinding(s);
            cob.nop();
            cob.labelBinding(e);
            cob.characterRange(s, e, 100, 200, CharacterRange.FLAG_STATEMENT | CharacterRange.FLAG_ASSIGNMENT);
            cob.return_();
        }));
        CodeAttribute ca = (CodeAttribute) ClassFile.of().parse(b).methods().get(0).code().get();
        CharacterRange got = null;
        for (CodeElement el : ca.elementList()) if (el instanceof CharacterRange x) got = x;
        check(got != null, "characterRange emitted + read back");
        check(got.characterRangeStart() == 100, "characterRangeStart 100");
        check(got.characterRangeEnd() == 200, "characterRangeEnd 200");
        check(got.flags() == (CharacterRange.FLAG_STATEMENT | CharacterRange.FLAG_ASSIGNMENT), "flags OR combination 5");
        check(got.startScope() != null && got.endScope() != null, "scope labels non-null");
        check(got instanceof PseudoInstruction, "PseudoInstruction");
        check(!((Object) got instanceof Instruction), "not Instruction");
        check(got instanceof CodeElement, "CodeElement");
        check(ca.labelToBci(got.startScope()) == 0, "startScope bci 0");
        check(ca.labelToBci(got.endScope()) == 1, "endScope bci 1");
        // 属性面：CharacterRangeTable 存在
        check(ca.findAttribute(Attributes.characterRangeTable()).isPresent(), "CharacterRangeTable attribute present");
        var crt = ca.findAttribute(Attributes.characterRangeTable()).get();
        check(crt.characterRangeTable().size() == 1, "CRT 1 entry");
        check(crt.characterRangeTable().get(0).characterRangeStart() == 100 && crt.characterRangeTable().get(0).characterRangeEnd() == 200,
                "CRT entry range 100-200");
        check(crt.characterRangeTable().get(0).flags() == 5, "CRT entry flags 5");
    }

    // ---- Part 4: 工厂 + 常量 + 负例 ----
    static void testFactoryNegative() throws Exception {
        // 9 个 FLAG 常量精确值表
        check(CharacterRange.FLAG_STATEMENT == 0x0001, "FLAG_STATEMENT 0x1");
        check(CharacterRange.FLAG_BLOCK == 0x0002, "FLAG_BLOCK 0x2");
        check(CharacterRange.FLAG_ASSIGNMENT == 0x0004, "FLAG_ASSIGNMENT 0x4");
        check(CharacterRange.FLAG_FLOW_CONTROLLER == 0x0008, "FLAG_FLOW_CONTROLLER 0x8");
        check(CharacterRange.FLAG_FLOW_TARGET == 0x0010, "FLAG_FLOW_TARGET 0x10");
        check(CharacterRange.FLAG_INVOKE == 0x0020, "FLAG_INVOKE 0x20");
        check(CharacterRange.FLAG_CREATE == 0x0040, "FLAG_CREATE 0x40");
        check(CharacterRange.FLAG_BRANCH_TRUE == 0x0080, "FLAG_BRANCH_TRUE 0x80");
        check(CharacterRange.FLAG_BRANCH_FALSE == 0x0100, "FLAG_BRANCH_FALSE 0x100");
        final Label[] l = new Label[2];
        ClassFile.of().build(ClassDesc.of("EmCHR2"), cb -> cb.withMethodBody("m", ConstantDescs.MTD_void, ClassFile.ACC_STATIC, cob -> {
            l[0] = cob.newLabel();
            l[1] = cob.newLabel();
            // 工厂 5 参精确回读（全部 9 个 flag 位组合）
            CharacterRange all = CharacterRange.of(l[0], l[1], 0, Integer.MAX_VALUE, 0x1FF);
            check(all.flags() == 0x1FF, "all flags 0x1FF");
            check(all.characterRangeStart() == 0 && all.characterRangeEnd() == Integer.MAX_VALUE, "range 0..MAX");
            check(all.startScope() == l[0] && all.endScope() == l[1], "scope identity");
            CharacterRange zero = CharacterRange.of(l[0], l[1], 0, 0, 0);
            check(zero.flags() == 0, "flags 0 legal");
            cob.return_();
        }));
        try { CharacterRange.of(null, null, 0, 0, 1); check(false, "of(null,...) must throw"); }
        catch (NullPointerException e) { check(true, "of(null,...) NPE"); }
        // 多条 range 共存
        byte[] b = ClassFile.of().build(ClassDesc.of("EmCHR3"), cb -> cb.withMethodBody("m", ConstantDescs.MTD_void, ClassFile.ACC_STATIC, cob -> {
            Label s = cob.newLabel(), e = cob.newLabel();
            cob.labelBinding(s).nop().labelBinding(e);
            cob.characterRange(s, e, 1, 2, CharacterRange.FLAG_BLOCK);
            cob.characterRange(s, e, 3, 4, CharacterRange.FLAG_INVOKE);
            cob.return_();
        }));
        int n = 0;
        for (CodeElement el : ClassFile.of().parse(b).methods().get(0).code().get().elementList()) if (el instanceof CharacterRange) n++;
        check(n == 2, "two CharacterRange pseudos coexist");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testEmitReadback();
        testFactoryNegative();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
