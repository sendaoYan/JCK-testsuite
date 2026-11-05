/*
 * @test
 * @summary java.lang.classfile.MethodBuilder conformance: white-box structure + black-box behavior.
 * @run main MethodBuilderTest
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

public class MethodBuilderTest {
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
        Class<?> c = Class.forName("java.lang.classfile.MethodBuilder");
        check(c != null, "java.lang.classfile.MethodBuilder loads");
        check(c.getName().equals("java.lang.classfile.MethodBuilder"), "binary name java.lang.classfile.MethodBuilder");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.MethodBuilder is public");
        check(c.isInterface(), "java.lang.classfile.MethodBuilder is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.MethodBuilder has no superclass");
        check(declares(c, "withFlags"), "declares withFlags");
        check(declares(c, "withCode"), "declares withCode");
        check(declares(c, "transformCode"), "declares transformCode");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        byte[] b = ClassFile.of().build(ClassDesc.of("pkg.T"), cb -> cb.withMethod("m", MethodTypeDesc.of(ConstantDescs.CD_int, ConstantDescs.CD_int), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC, mb -> mb.withCode(cob -> cob.iload(0).iconst_1().iadd().ireturn()).withFlags(AccessFlag.PUBLIC, AccessFlag.STATIC)));
        ClassModel cm = ClassFile.of().parse(b);
        MethodModel m = cm.methods().get(0);
        check(m.methodName().stringValue().equals("m"), "name");
        check(m.methodTypeSymbol().equals(MethodTypeDesc.of(ConstantDescs.CD_int, ConstantDescs.CD_int)), "type symbol");
        check(m.flags().has(AccessFlag.PUBLIC) && m.flags().has(AccessFlag.STATIC), "flags");
        check(m.code().isPresent(), "code present");
        check(m.code().get().elementList().size() >= 3, "code instructions");

    }

    // ---- Part 3: 属性注入全矩阵 → re-parse 回读（实测锁定） ----
    static void testAttributeMatrix() throws Exception {
        ClassFile cf = ClassFile.of();
        byte[] b = cf.build(ClassDesc.of("Mbt1.T"), cb -> cb.withMethod("m", MethodTypeDesc.ofDescriptor("(II)I"),
                ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC | ClassFile.ACC_SYNCHRONIZED | ClassFile.ACC_VARARGS, mb -> mb
                        .with(ExceptionsAttribute.ofSymbols(ClassDesc.of("java.io.IOException"), ClassDesc.of("java.lang.IllegalStateException")))
                        .with(DeprecatedAttribute.of())
                        .with(SignatureAttribute.of(MethodSignature.parseFrom("(II)I")))
                        .with(MethodParametersAttribute.of(
                                MethodParameterInfo.ofParameter(Optional.of("a"), ClassFile.ACC_FINAL),
                                MethodParameterInfo.ofParameter(Optional.empty(), 0)))
                        .withCode(c -> c.iload(0).iload(1).iadd().ireturn())));
        ClassModel cm = cf.parse(b);
        MethodModel m = cm.methods().get(0);
        check(m.flags().flagsMask() == 169, "flags mask 169 (PUBLIC|STATIC|SYNCHRONIZED|VARARGS)");
        check(m.flags().flags().containsAll(List.of(AccessFlag.PUBLIC, AccessFlag.STATIC, AccessFlag.SYNCHRONIZED, AccessFlag.VARARGS)), "flags set contents");
        ExceptionsAttribute ex = m.findAttribute(Attributes.exceptions()).orElseThrow();
        check(ex.exceptions().size() == 2, "exceptions count");
        check(ex.exceptions().get(0).asInternalName().equals("java/io/IOException"), "exception 0 order");
        check(ex.exceptions().get(1).asInternalName().equals("java/lang/IllegalStateException"), "exception 1 order");
        check(m.findAttribute(Attributes.deprecated()).isPresent(), "deprecated present");
        check(m.findAttribute(Attributes.signature()).get().signature().stringValue().equals("(II)I"), "signature roundtrip");
        MethodParametersAttribute mp = m.findAttribute(Attributes.methodParameters()).orElseThrow();
        check(mp.parameters().size() == 2, "method parameters count");
        check(mp.parameters().get(0).name().get().stringValue().equals("a"), "param 0 name");
        check(mp.parameters().get(0).flagsMask() == ClassFile.ACC_FINAL, "param 0 ACC_FINAL=16");
        check(mp.parameters().get(1).name().isEmpty(), "param 1 unnamed Optional.empty");
        check(mp.parameters().get(1).flagsMask() == 0, "param 1 flags 0");
        check(m.code().isPresent(), "code present");
        check(cf.verify(cm).isEmpty(), "attribute matrix verify clean");
        // withCode 内含 labelBinding/分支也随属性共存
        byte[] b2 = cf.build(ClassDesc.of("Mbt2.U"), cb -> cb.withMethodBody("sgn", MethodTypeDesc.ofDescriptor("(I)I"), ClassFile.ACC_STATIC, c -> {
            Label neg = c.newLabel();
            c.iload(0).iflt(neg).iconst_1().ireturn().labelBinding(neg).iconst_m1().ireturn();
        }));
        MethodModel m2 = cf.parse(b2).methods().get(0);
        check(m2.code().get().findAttribute(Attributes.stackMapTable()).isPresent(), "branch method gets StackMapTable");
        check(cf.verify(cf.parse(b2)).isEmpty(), "branch verify clean");
    }

    // ---- Part 4: 无 code 的抽象/原生方法 + flags 表 ----
    static void testAbstractNative() throws Exception {
        ClassFile cf = ClassFile.of();
        byte[] b = cf.build(ClassDesc.of("Mbt3.T"), cb -> cb
                .withMethod("ab", MethodTypeDesc.ofDescriptor("()I"), ClassFile.ACC_PUBLIC | ClassFile.ACC_ABSTRACT, mb -> { })
                .withMethod("nat", MethodTypeDesc.ofDescriptor("()J"), ClassFile.ACC_PUBLIC | ClassFile.ACC_NATIVE, mb -> { }));
        ClassModel cm = cf.parse(b);
        MethodModel ab = cm.methods().get(0), nat = cm.methods().get(1);
        check(!ab.code().isPresent(), "abstract method has no Code (build legal)");
        check(ab.flags().flagsMask() == (ClassFile.ACC_PUBLIC | ClassFile.ACC_ABSTRACT), "abstract mask 1025");
        check(ab.flags().has(AccessFlag.ABSTRACT) && ab.flags().has(AccessFlag.PUBLIC), "abstract flag bits");
        check(!nat.code().isPresent(), "native method has no Code (build legal)");
        check(nat.flags().flagsMask() == (ClassFile.ACC_PUBLIC | ClassFile.ACC_NATIVE), "native mask 257");
        check(nat.flags().has(AccessFlag.NATIVE), "native flag bit");
        check(cf.verify(cm).isEmpty(), "abstract+native class verify clean");
        check(ab.flags().toString().equals("AccessFlags[flags=1025]"), "AccessFlags toString: " + ab.flags());
        check(nat.flags().toString().equals("AccessFlags[flags=257]"), "native toString: " + nat.flags());
        check(ab.methodTypeSymbol().descriptorString().equals("()I") && nat.methodTypeSymbol().descriptorString().equals("()J"), "methodTypeSymbol");
        // 少用 flag 位回读：BRIDGE|SYNTHETIC|STRICT（withFlags(AccessFlag...) 重载）
        byte[] b2 = cf.build(ClassDesc.of("Mbt4.U"), cb -> cb
                .withMethod("br", MethodTypeDesc.ofDescriptor("()V"), 0, mb -> mb.withFlags(AccessFlag.BRIDGE, AccessFlag.SYNTHETIC).withCode(c -> c.return_()))
                .withMethod("st", MethodTypeDesc.ofDescriptor("()V"), 0, mb -> mb.withFlags(ClassFile.ACC_STRICT).withCode(c -> c.return_())));
        ClassModel cm2 = cf.parse(b2);
        check(cm2.methods().get(0).flags().has(AccessFlag.BRIDGE) && cm2.methods().get(0).flags().has(AccessFlag.SYNTHETIC), "BRIDGE|SYNTHETIC roundtrip");
        check(cm2.methods().get(0).flags().flagsMask() == (ClassFile.ACC_BRIDGE | ClassFile.ACC_SYNTHETIC), "bridge mask 0x1040");
        check(cm2.methods().get(1).flags().flagsMask() == ClassFile.ACC_STRICT, "ACC_STRICT mask roundtrip");
        // 实测锁定：v71 中 ACC_STRICT(0x800) 已废弃，flags() 映射抛 IAE "Unmatched bit position 0x800"
        try {
            cm2.methods().get(1).flags().flags();
            check(false, "STRICT flags() must throw at v71");
        } catch (IllegalArgumentException e) {
            check(e.getMessage() != null && e.getMessage().contains("0x800"), "STRICT flags() IAE msg: " + e.getMessage());
        }
        // ACC_ABSTRACT + withCode：build 放行、verify 拒绝（实测）
        byte[] b3 = cf.build(ClassDesc.of("Mbt5.V"), cb -> cb
                .withMethod("bad", MethodTypeDesc.ofDescriptor("()V"), ClassFile.ACC_ABSTRACT, mb -> mb.withCode(c -> c.return_())));
        ClassModel cm3 = cf.parse(b3);
        check(cm3.methods().get(0).code().isPresent(), "abstract+code build does not throw");
        check(!cf.verify(cm3).isEmpty(), "abstract+code flagged by verify");
    }

    // ---- Part 5: 负例 + 双 withCode 语义（实测锁定） ----
    static void testNegativeMatrix() {
        ClassFile cf = ClassFile.of();
        try {
            cf.build(ClassDesc.of("MbtN1"), cb -> cb.withMethod("m", MethodTypeDesc.ofDescriptor("()V"), ClassFile.ACC_STATIC, mb -> mb.withCode(null)));
            check(false, "withCode(null) must throw");
        } catch (NullPointerException e) {
            check(e.getMessage() != null && e.getMessage().contains("\"handler\" is null"), "withCode(null) NPE msg: " + e.getMessage());
        }
        try {
            cf.build(ClassDesc.of("MbtN2"), cb -> cb.withMethodBody("m", MethodTypeDesc.ofDescriptor("()I"), ClassFile.ACC_STATIC, c -> { }));
            check(false, "empty body must throw");
        } catch (IllegalArgumentException e) {
            check(e.getMessage() != null && e.getMessage().contains("Code length 0"), "empty body IAE msg: " + e.getMessage());
        }
        // 双 withCode：后者覆盖前者（实测 re-parse 单 Code，指令为第二组）
        byte[] b = cf.build(ClassDesc.of("MbtN3"), cb -> cb.withMethod("m", MethodTypeDesc.ofDescriptor("()I"), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC, mb -> mb
                .withCode(c -> c.iconst_1().ireturn())
                .withCode(c -> c.iconst_2().ireturn())));
        ClassModel cm = cf.parse(b);
        int codeElems = 0;
        for (MethodElement e : cm.methods().get(0)) if (e instanceof CodeModel) codeElems++;
        check(codeElems == 1, "double withCode keeps single Code element: " + codeElems);
        List<String> ops = new ArrayList<>();
        for (CodeElement e : cm.methods().get(0).code().get().elementList())
            if (e instanceof Instruction i) ops.add(i.opcode().name());
        check(ops.equals(List.of("ICONST_2", "IRETURN")), "second withCode wins: " + ops);
        check(cf.verify(cm).isEmpty(), "double withCode verify clean");
    }

    // ---- Part 6: transformCode 通道 + Utf8Entry 重载 ----
    static void testTransformChannel() throws Exception {
        ClassFile cf = ClassFile.of();
        byte[] src = cf.build(ClassDesc.of("MbtT1"), cb -> cb.withMethodBody("m", MethodTypeDesc.ofDescriptor("()I"), ClassFile.ACC_STATIC, c -> c.nop().iconst_5().ireturn()));
        ClassModel sm = cf.parse(src);
        MethodModel mm = sm.methods().get(0);
        // mb.transformCode(CodeModel, CodeTransform)：剥 NOP
        byte[] out = cf.build(ClassDesc.of("MbtT2"), cb -> cb.withMethod("m", mm.methodTypeSymbol(), ClassFile.ACC_STATIC,
                mb -> mb.transformCode(mm.code().get(), new CodeTransform() {
                    public void accept(CodeBuilder cb, CodeElement e) {
                        if (!(e instanceof Instruction i) || i.opcode() != Opcode.NOP) cb.with(e);
                    }
                })));
        List<String> ops = new ArrayList<>();
        for (CodeElement e : cf.parse(out).methods().get(0).code().get().elementList())
            if (e instanceof Instruction i) ops.add(i.opcode().name());
        check(ops.equals(List.of("ICONST_5", "IRETURN")), "transformCode strips NOP: " + ops);
        // transformCode + ACCEPT_ALL 保真
        byte[] out2 = cf.build(ClassDesc.of("MbtT3"), cb -> cb.withMethod("m", mm.methodTypeSymbol(), ClassFile.ACC_STATIC,
                mb -> mb.transformCode(mm.code().get(), CodeTransform.ACCEPT_ALL)));
        check(cf.parse(out2).methods().get(0).code().get().elementList().size()
                == mm.code().get().elementList().size(), "transformCode ACCEPT_ALL element count preserved");
        // MethodTransform 回调收到的 builder 有 constantPool（ClassFileBuilder 面）
        final boolean[] ok = new boolean[1];
        cf.transformClass(sm, new ClassTransform() {
            public void accept(ClassBuilder b, ClassElement e) {
                if (e instanceof MethodModel m2) b.transformMethod(m2, new MethodTransform() {
                    public void accept(MethodBuilder mb, MethodElement me) { ok[0] = mb.constantPool() != null; mb.with(me); }
                });
                else b.with(e);
            }
        });
        check(ok[0], "MethodTransform accept builder constantPool non-null");
        // withMethodBody(Utf8Entry,Utf8Entry,int,Consumer) 重载
        byte[] b3 = cf.build(ClassDesc.of("MbtT4"), cb -> {
            ConstantPoolBuilder cpb = cb.constantPool();
            cb.withMethodBody(cpb.utf8Entry("em"), cpb.utf8Entry("()I"), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC, c -> c.iconst_4().ireturn());
        });
        MethodModel m3 = cf.parse(b3).methods().get(0);
        check(m3.methodName().stringValue().equals("em") && m3.methodType().stringValue().equals("()I"), "Utf8Entry overload roundtrip");
        check(m3.flags().has(AccessFlag.PUBLIC) && m3.flags().has(AccessFlag.STATIC), "Utf8Entry overload flags");
    }

    // ---- Part 7: defineClass 运行闭环 ----
    static int dynSeq;
    static void testDefineClassLoop() throws Exception {
        ClassFile cf = ClassFile.of();
        String name = "DynMB" + (++dynSeq);
        byte[] b = cf.build(ClassDesc.of(name), cb -> cb
                .withMethodBody("mul", MethodTypeDesc.ofDescriptor("(II)I"), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC,
                        c -> c.iload(0).iload(1).imul().ireturn())
                .withMethodBody("abs", MethodTypeDesc.ofDescriptor("(I)I"), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC, c -> {
                    Label neg = c.newLabel();
                    c.iload(0).iflt(neg).iload(0).ireturn().labelBinding(neg).iload(0).ineg().ireturn();
                }));
        ClassModel rm = cf.parse(b);
        check(rm.thisClass().asInternalName().equals(name), "dyn thisClass roundtrip");
        check(cf.verify(rm).isEmpty(), "dyn verify clean");
        Class<?> dc = MethodHandles.lookup().defineClass(b);
        check(dc.getMethod("mul", int.class, int.class).invoke(null, 6, 7).equals(42), "DynMB mul(6,7)==42");
        check(dc.getMethod("abs", int.class).invoke(null, -9).equals(9), "DynMB abs(-9)==9");
        check(dc.getMethod("abs", int.class).invoke(null, 5).equals(5), "DynMB abs(5)==5");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testAttributeMatrix();
        testAbstractNative();
        testNegativeMatrix();
        testTransformChannel();
        testDefineClassLoop();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
