/*
 * @test
 * @summary java.lang.classfile.attribute.StackMapTableAttribute conformance: white-box structure + black-box behavior.
 * @run main StackMapTableAttributeTest
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

public class StackMapTableAttributeTest {
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
        Class<?> c = Class.forName("java.lang.classfile.attribute.StackMapTableAttribute");
        check(c != null, "java.lang.classfile.attribute.StackMapTableAttribute loads");
        check(c.getName().equals("java.lang.classfile.attribute.StackMapTableAttribute"), "binary name java.lang.classfile.attribute.StackMapTableAttribute");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.attribute.StackMapTableAttribute is public");
        check(c.isInterface(), "java.lang.classfile.attribute.StackMapTableAttribute is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.attribute.StackMapTableAttribute has no superclass");
        check(declares(c, "entries"), "declares entries");
        check(declares(c, "of"), "declares of");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        byte[] b = ClassFile.of().build(ClassDesc.of("pkg.T"), cb -> cb.withMethodBody("m", MethodTypeDesc.of(ConstantDescs.CD_int), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC, cob -> {
            Label l = cob.newLabel();
            cob.iconst_0().ifeq(l).iconst_1().ireturn().labelBinding(l).iconst_2().ireturn();
        }));
        StackMapTableAttribute a = ClassFile.of().parse(b).methods().get(0).code().get().findAttribute(Attributes.stackMapTable()).get();
        check(a.entries().size() == 1, "size");
        check(a.entries().get(0).frameType() == 6, "frameType SAME");

    }


    // ---- real-product member（javac 分支/循环 → StackMapTable）----
    static class SMTReal {
        static int branchy(int x) {
            int r = 0;
            if (x > 3) r = x * 2; else r = -x;
            for (int i = 0; i < x; i++) r += i;
            return r;
        }
    }

    static byte[] buildBranchy() {
        return ClassFile.of().build(ClassDesc.of("DynSMT"), cb -> cb.withMethodBody("m",
                MethodTypeDesc.ofDescriptor("(I)I"), ClassFile.ACC_STATIC,
                cob -> { Label l = cob.newLabel(); cob.iconst_0().iload(0).if_icmpge(l); cob.iconst_1().ireturn(); cob.labelBinding(l).iconst_m1().ireturn(); }));
    }

    // ---- Part 3: build 自动生成→re-parse entries 访问器 ----
    static void testBuildGenerated() throws Exception {
        ClassModel m = ClassFile.of().parse(buildBranchy());
        CodeAttribute ca = (CodeAttribute) m.methods().get(0).code().get();
        var smt = ca.findAttribute(Attributes.stackMapTable());
        check(smt.isPresent(), "branch build emits StackMapTable");
        check(!smt.get().entries().isEmpty(), "entries non-empty");
        StackMapFrameInfo f = smt.get().entries().get(0);
        check(f.frameType() >= 0 && f.frameType() <= 255, "frameType u1 range");
        check(f.locals().size() == 1, "static (I)I one local at branch");
        check(f.locals().get(0).tag() == StackMapFrameInfo.VerificationTypeInfo.ITEM_INTEGER, "int arg as ITEM_INTEGER");
        check(f.stack().isEmpty(), "empty stack at branch target");
        int bci = ca.labelToBci(f.target());
        check(bci >= 0 && bci < ca.codeLength(), "target bci in code range");
        check(smt.get().attributeName().stringValue().equals("StackMapTable")
                && smt.get().attributeName().stringValue().equals(Attributes.NAME_STACK_MAP_TABLE), "NAME_STACK_MAP_TABLE");
        check(smt.get().attributeMapper() == Attributes.stackMapTable(), "parsed mapper identity");
        check(smt.get().attributeMapper().stability() == AttributeMapper.AttributeStability.LABELS, "stability LABELS");
        check(!smt.get().attributeMapper().allowMultiple(), "allowMultiple false");
        check(ClassFile.of().verify(m).isEmpty(), "verify clean");
    }

    // ---- Part 4: javac 真产物 ----
    static void testRealJavacProduct() throws Exception {
        byte[] rb = StackMapTableAttributeTest.class.getResourceAsStream("StackMapTableAttributeTest$SMTReal.class").readAllBytes();
        ClassModel m = ClassFile.of().parse(rb);
        for (MethodModel mm : m.methods()) {
            if (!mm.methodName().stringValue().equals("branchy") || mm.code().isEmpty()) continue;
            CodeAttribute ca = (CodeAttribute) mm.code().get();
            var smt = ca.findAttribute(Attributes.stackMapTable()).get();
            check(smt.entries().size() >= 3, "if/else + loop frames: " + smt.entries().size());
            for (StackMapFrameInfo f : smt.entries()) {
                check(f.frameType() >= 0 && f.frameType() <= 255, "javac frameType u1");
                check(f.target() != null, "frame target label");
            }
        }
    }

    // ---- Part 5: of(List) 工厂访问器（无法经 with 注入方法体——实测 IAE）----
    static void testOfFactoryAndInjectNegative() throws Exception {
        ClassModel m = ClassFile.of().parse(buildBranchy());
        CodeAttribute ca = (CodeAttribute) m.methods().get(0).code().get();
        StackMapFrameInfo parsed = ca.findAttribute(Attributes.stackMapTable()).get().entries().get(0);
        var objVti = StackMapFrameInfo.ObjectVerificationTypeInfo.of(ClassDesc.of("java.lang.String"));
        StackMapFrameInfo frame = StackMapFrameInfo.of(parsed.target(), List.of(objVti), List.of());
        StackMapTableAttribute smt = StackMapTableAttribute.of(List.of(frame));
        check(smt.entries().size() == 1, "of(List) size");
        check(smt.entries().get(0).locals().get(0).tag() == StackMapFrameInfo.VerificationTypeInfo.ITEM_OBJECT, "of frame locals readback");
        try { StackMapTableAttribute.of(null); check(false, "of(null) must throw"); }
        catch (NullPointerException e) { check(true, "of(null) NPE"); }
        // 外来/未绑定 label 的 SMT 不能经 MethodBuilder.with 注入（SMT 是 CodeElement 不是 MethodElement）
        try {
            ClassFile.of().build(ClassDesc.of("DynSMT2"), cb -> cb.withMethod("m", MethodTypeDesc.ofDescriptor("()V"),
                    ClassFile.ACC_STATIC, mb -> mb.withCode(cob -> { cob.return_(); cob.with(smt); })));
            check(false, "inject foreign SMT must throw");
        } catch (IllegalArgumentException e) {
            check(e.getMessage() != null, "foreign SMT inject IAE: " + e.getMessage());
        }
    }

    // ---- Part 6: DROP_STACK_MAPS 交叉（build 侧省略 + verify 报缺）----
    static void testDropStackMapsCross() throws Exception {
        ClassFile drop = ClassFile.of().withOptions(ClassFile.StackMapsOption.DROP_STACK_MAPS);
        byte[] noSm = drop.build(ClassDesc.of("DynSMT3"), cb -> cb.withMethodBody("m",
                MethodTypeDesc.ofDescriptor("(I)I"), ClassFile.ACC_STATIC,
                cob -> { Label l = cob.newLabel(); cob.iconst_0().iload(0).if_icmpge(l); cob.iconst_1().ireturn(); cob.labelBinding(l).iconst_m1().ireturn(); }));
        check(!ClassFile.of().parse(noSm).methods().get(0).code().get().findAttribute(Attributes.stackMapTable()).isPresent(),
                "DROP_STACK_MAPS build omits attribute");
        var errs = ClassFile.of().verify(noSm);
        check(!errs.isEmpty(), "verify flags missing stack maps at v71");
        // parse 侧保留模型属性
        check(drop.parse(buildBranchy()).methods().get(0).code().get().findAttribute(Attributes.stackMapTable()).isPresent(),
                "DROP_STACK_MAPS parse keeps model attribute");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testBuildGenerated();
        testRealJavacProduct();
        testOfFactoryAndInjectNegative();
        testDropStackMapsCross();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
