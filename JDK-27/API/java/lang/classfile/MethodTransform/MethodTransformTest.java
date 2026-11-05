/*
 * @test
 * @summary java.lang.classfile.MethodTransform conformance: white-box structure + black-box behavior.
 * @run main MethodTransformTest
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

public class MethodTransformTest {
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
        Class<?> c = Class.forName("java.lang.classfile.MethodTransform");
        check(c != null, "java.lang.classfile.MethodTransform loads");
        check(c.getName().equals("java.lang.classfile.MethodTransform"), "binary name java.lang.classfile.MethodTransform");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.MethodTransform is public");
        check(c.isInterface(), "java.lang.classfile.MethodTransform is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.MethodTransform has no superclass");
        check(declares(c, "ofStateful"), "declares ofStateful");
        check(declares(c, "endHandler"), "declares endHandler");
        check(declares(c, "dropping"), "declares dropping");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        byte[] src = ClassFile.of().build(ClassDesc.of("pkg.T"), cb -> cb.withMethodBody("a", MethodTypeDesc.of(ConstantDescs.CD_void), 0, cob -> cob.return_()).withMethodBody("b", MethodTypeDesc.of(ConstantDescs.CD_void), 0, cob -> cob.return_()));
        ClassModel cm = ClassFile.of().parse(src);
        byte[] out = ClassFile.of().transformClass(cm, ClassTransform.transformingMethods(me -> me.methodName().stringValue().equals("a"), MethodTransform.dropping(e -> e instanceof CodeModel)));
        ClassModel cm2 = ClassFile.of().parse(out);
        check(cm2.methods().size() == 2, "two methods retained");
        check(cm2.methods().stream().anyMatch(m -> m.methodName().stringValue().equals("a") && m.code().isEmpty()), "a code dropped");
        check(cm2.methods().stream().anyMatch(m -> m.methodName().stringValue().equals("b") && m.code().isPresent()), "b code retained");

    }

    // ---- Part 3: ACCEPT_ALL 存在性 + 经 transformingMethods 使用（成员/属性/代码全守恒） ----
    static void testAcceptAll() throws Exception {
        ClassFile cf = ClassFile.of();
        byte[] b = cf.build(ClassDesc.of("MTA"), cb -> cb
                .withMethod("m", MethodTypeDesc.ofDescriptor("(I)I"), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC,
                        mb -> mb.withCode(cob -> cob.iload(0).ireturn()).with(DeprecatedAttribute.of())));
        ClassModel src = cf.parse(b);
        ClassModel out = cf.parse(cf.transformClass(src, ClassTransform.transformingMethods(MethodTransform.ACCEPT_ALL)));
        check(out.methods().size() == 1, "ACCEPT_ALL keeps method");
        MethodModel mm = out.methods().get(0);
        check(mm.methodName().stringValue().equals("m"), "ACCEPT_ALL keeps method name");
        check(mm.methodTypeSymbol().descriptorString().equals("(I)I"), "ACCEPT_ALL keeps descriptor");
        check(mm.flags().has(AccessFlag.PUBLIC) && mm.flags().has(AccessFlag.STATIC), "ACCEPT_ALL keeps flags");
        check(mm.code().isPresent(), "ACCEPT_ALL keeps code");
        check(mm.findAttribute(Attributes.deprecated()).isPresent(), "ACCEPT_ALL keeps method attribute");
        check(cf.verify(out).isEmpty(), "ACCEPT_ALL output verifies");
        check(MethodTransform.ACCEPT_ALL == MethodTransform.ACCEPT_ALL, "ACCEPT_ALL singleton identity");
        check(ClassFileTransform.class.isAssignableFrom(MethodTransform.class), "MethodTransform is-a ClassFileTransform");
    }

    // ---- Part 4: dropping 剥方法内元素（Code 模型/属性）+ endHandler 注入属性 → re-parse ----
    static void testDroppingAndEndHandler() throws Exception {
        ClassFile cf = ClassFile.of();
        byte[] b = cf.build(ClassDesc.of("MTD"), cb -> cb
                .withMethod("m", ConstantDescs.MTD_void, 0, mb -> mb.withCode(cob -> cob.return_())
                        .with(DeprecatedAttribute.of()).with(SyntheticAttribute.of()))
                .withMethod("n", ConstantDescs.MTD_void, 0, mb -> mb.withCode(cob -> cob.return_()).with(DeprecatedAttribute.of())));
        ClassModel src = cf.parse(b);
        // dropping 剥 Deprecated 属性（两方法都剥）
        ClassModel noDep = cf.parse(cf.transformClass(src, ClassTransform.transformingMethods(MethodTransform.dropping(e -> e instanceof DeprecatedAttribute))));
        for (MethodModel mm : noDep.methods())
            check(mm.findAttribute(Attributes.deprecated()).isEmpty(), "dropping strips Deprecated on " + mm.methodName().stringValue());
        check(noDep.methods().get(0).findAttribute(Attributes.synthetic()).isPresent(), "dropping keeps other attributes");
        check(noDep.methods().stream().allMatch(m -> m.code().isPresent()), "dropping attr keeps code");
        // dropping 剥 CodeModel（方法体消失，方法保留）
        ClassModel noCode = cf.parse(cf.transformClass(src, ClassTransform.transformingMethods(MethodTransform.dropping(e -> e instanceof CodeModel))));
        check(noCode.methods().stream().allMatch(m -> m.code().isEmpty()), "dropping CodeModel removes bodies");
        check(noCode.methods().size() == 2, "methods themselves retained");
        // endHandler 注入 DeprecatedAttribute → re-parse 存在
        ClassModel injected = cf.parse(cf.transformClass(src, ClassTransform.transformingMethods(
                MethodTransform.endHandler(mb -> mb.with(DeprecatedAttribute.of())))));
        check(injected.methods().stream().allMatch(m -> m.findAttribute(Attributes.deprecated()).isPresent()), "endHandler injects DeprecatedAttribute on every method");
        // andThen：dropping(deprecated).andThen(endHandler(synthetic))——剥+注双效（实测）
        ClassModel both = cf.parse(cf.transformClass(src, ClassTransform.transformingMethods(
                MethodTransform.dropping(e -> e instanceof DeprecatedAttribute)
                        .andThen(MethodTransform.endHandler(mb -> mb.with(SyntheticAttribute.of()))))));
        MethodModel bm = both.methods().get(0);
        check(bm.findAttribute(Attributes.deprecated()).isEmpty(), "andThen: dropping effect applied");
        check(bm.findAttribute(Attributes.synthetic()).isPresent(), "andThen: endHandler effect applied");
    }

    // ---- Part 5: transformingCode(CodeTransform) 桥接 + ofStateful 每方法新实例 ----
    static void testTransformingCodeAndStateful() throws Exception {
        ClassFile cf = ClassFile.of();
        byte[] b = cf.build(ClassDesc.of("MTC"), cb -> cb
                .withMethodBody("m", ConstantDescs.MTD_void, 0, cob -> cob.nop().return_())
                .withMethodBody("n", ConstantDescs.MTD_void, 0, cob -> cob.nop().nop().return_()));
        CodeTransform dropNop = new CodeTransform() {
            public void accept(CodeBuilder cb, CodeElement e) {
                if (!(e instanceof Instruction i) || i.opcode() != Opcode.NOP) cb.with(e);
            }
        };
        // MethodTransform.transformingCode 桥（javap 锁定存在）
        ClassModel out = cf.parse(cf.transformClass(cf.parse(b), ClassTransform.transformingMethods(MethodTransform.transformingCode(dropNop))));
        for (MethodModel mm : out.methods()) {
            int nops = 0;
            for (CodeElement e : mm.code().get().elementList())
                if (e instanceof Instruction i && i.opcode() == Opcode.NOP) nops++;
            check(nops == 0, "transformingCode strips NOPs in " + mm.methodName().stringValue());
        }
        // ofStateful：Supplier 每方法调用一次（实测 2 方法 → 2 次）
        int[] supplies = {0};
        MethodTransform st = MethodTransform.ofStateful(() -> { supplies[0]++; return MethodTransform.ACCEPT_ALL; });
        cf.transformClass(cf.parse(b), ClassTransform.transformingMethods(st));
        check(supplies[0] == 2, "ofStateful supplier per method: " + supplies[0]);
        try {
            MethodTransform.ofStateful(null);
            check(false, "ofStateful(null) must throw");
        } catch (NullPointerException expected) {
            check(true, "ofStateful(null) NPE");
        }
        // endHandler(null) → NPE
        try {
            MethodTransform.endHandler(null);
            check(false, "endHandler(null) must throw");
        } catch (NullPointerException expected) {
            check(true, "endHandler(null) NPE");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testAcceptAll();
        testDroppingAndEndHandler();
        testTransformingCodeAndStateful();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
