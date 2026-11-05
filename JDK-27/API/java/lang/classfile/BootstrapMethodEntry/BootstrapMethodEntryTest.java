/*
 * @test
 * @summary java.lang.classfile.BootstrapMethodEntry conformance: white-box structure + black-box behavior.
 * @run main BootstrapMethodEntryTest
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

public class BootstrapMethodEntryTest {
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
        Class<?> c = Class.forName("java.lang.classfile.BootstrapMethodEntry");
        check(c != null, "java.lang.classfile.BootstrapMethodEntry loads");
        check(c.getName().equals("java.lang.classfile.BootstrapMethodEntry"), "binary name java.lang.classfile.BootstrapMethodEntry");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.BootstrapMethodEntry is public");
        check(c.isInterface(), "java.lang.classfile.BootstrapMethodEntry is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.BootstrapMethodEntry has no superclass");
        check(declares(c, "constantPool"), "declares constantPool");
        check(declares(c, "bsmIndex"), "declares bsmIndex");
        check(declares(c, "bootstrapMethod"), "declares bootstrapMethod");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        ConstantPoolBuilder cpb = ConstantPoolBuilder.of();
        MethodHandleEntry bsm = cpb.methodHandleEntry(MethodHandleDesc.ofMethod(DirectMethodHandleDesc.Kind.STATIC, ClassDesc.of("java.lang.Integer"), "valueOf", MethodTypeDesc.of(ClassDesc.of("java.lang.Integer"), ConstantDescs.CD_int)));
        BootstrapMethodEntry e = cpb.bsmEntry(MethodHandleDesc.ofMethod(DirectMethodHandleDesc.Kind.STATIC, ClassDesc.of("java.lang.Integer"), "valueOf", MethodTypeDesc.of(ClassDesc.of("java.lang.Integer"), ConstantDescs.CD_int)), java.util.List.of());
        check(e.bootstrapMethod().asSymbol().equals(bsm.asSymbol()), "bootstrapMethod");
        check(e.arguments().isEmpty(), "no args");
        check(e.constantPool() != null, "constantPool");
        check(e.bsmIndex() >= 0, "bsmIndex");

    }

    // ---- 测试类自带的 invokedynamic 产生器（lambda + 字符串拼接，供真类解析） ----
    static Runnable lambdaHelper = () -> { };
    static String concatHelper(String a, String b) { return a + "/" + b; }

    // ---- Part 3: lambda 真类解析 → BootstrapMethodsAttribute 条目全等回读（实测锁定） ----
    static void testLambdaRealClass() throws Exception {
        ClassFile cf = ClassFile.of();
        byte[] self = BootstrapMethodEntryTest.class.getResourceAsStream("BootstrapMethodEntryTest.class").readAllBytes();
        ClassModel cm = cf.parse(self);
        Optional<BootstrapMethodsAttribute> bma = cm.findAttribute(Attributes.bootstrapMethods());
        check(bma.isPresent(), "self class has BootstrapMethods attribute");
        check(bma.get().bootstrapMethodsSize() == bma.get().bootstrapMethods().size(), "bootstrapMethodsSize == list size");
        // 找 LambdaMetafactory 条目（lambdaHelper 字段初始化器）
        BootstrapMethodEntry lam = null;
        for (BootstrapMethodEntry e : bma.get().bootstrapMethods()) {
            if (e.bootstrapMethod().reference().owner().asInternalName().equals("java/lang/invoke/LambdaMetafactory")) { lam = e; break; }
        }
        check(lam != null, "LambdaMetafactory BSM entry present");
        check(lam.bootstrapMethod().reference().name().stringValue().equals("metafactory"), "bsm name metafactory");
        check(lam.bootstrapMethod().kind() == 6, "bsm handle kind 6 (REF_invokeStatic)");
        check(lam.arguments().size() == 3, "lambda bsm 3 static args (实测)");
        check(lam.arguments().get(0) instanceof MethodTypeEntry, "arg0 MethodTypeEntry (samMethodType)");
        check(lam.arguments().get(1) instanceof MethodHandleEntry, "arg1 MethodHandleEntry (implMethod)");
        check(lam.arguments().get(2) instanceof MethodTypeEntry, "arg2 MethodTypeEntry (instantiatedMethodType)");
        check(lam.arguments().get(1) instanceof LoadableConstantEntry, "args are LoadableConstantEntry");
        check(lam.bsmIndex() >= 0, "bsmIndex >= 0");
        check(lam.constantPool() == cm.constantPool(), "entry constantPool == model pool");
        // 找 StringConcatFactory 条目（concatHelper 的 + 拼接 invokedynamic）
        boolean sawConcat = false;
        for (BootstrapMethodEntry e : bma.get().bootstrapMethods()) {
            if (e.bootstrapMethod().reference().owner().asInternalName().equals("java/lang/invoke/StringConcatFactory")) { sawConcat = true; break; }
        }
        check(sawConcat, "StringConcatFactory BSM entry present (javac + 拼接)");
        // InvokeDynamicInstruction 与池条目联动
        boolean sawIndy = false;
        for (MethodModel m : cm.methods()) {
            if (m.code().isEmpty()) continue;
            for (CodeElement el : m.code().get().elementList()) {
                if (el instanceof InvokeDynamicInstruction ii) {
                    sawIndy = true;
                    check(ii.invokedynamic() != null, "invokedynamic() entry non-null");
                    check(ii.bootstrapMethod().methodName().equals("metafactory") || ii.bootstrapMethod().methodName().equals("makeConcatWithConstants"),
                            "indy bsm name: " + ii.bootstrapMethod().methodName());
                    check(ii.bootstrapMethod().owner().descriptorString().equals("Ljava/lang/invoke/LambdaMetafactory;")
                            || ii.bootstrapMethod().owner().descriptorString().equals("Ljava/lang/invoke/StringConcatFactory;"), "indy bsm owner desc");
                }
            }
        }
        check(sawIndy, "InvokeDynamicInstruction present in self");
    }

    // ---- Part 4: bsmEntry 构造面（ConstantPoolBuilder，实测去重/参数回读） ----
    static void testBsmEntryBuilder() {
        ConstantPoolBuilder cpb = ConstantPoolBuilder.of();
        DirectMethodHandleDesc d = MethodHandleDesc.ofMethod(DirectMethodHandleDesc.Kind.STATIC,
                ClassDesc.of("java.lang.Integer"), "valueOf", MethodTypeDesc.ofDescriptor("(I)Ljava/lang/Integer;"));
        BootstrapMethodEntry e1 = cpb.bsmEntry(d, List.of());
        BootstrapMethodEntry e2 = cpb.bsmEntry(d, List.of());
        check(e1 == e2, "bsmEntry dedup: same desc+args → same entry (==)");
        check(e1.bsmIndex() == e2.bsmIndex(), "dedup same bsmIndex");
        check(e1.bootstrapMethod().asSymbol().equals(d), "bootstrapMethod().asSymbol roundtrip");
        check(e1.arguments().isEmpty(), "no args empty list");
        // 带 ConstantDesc 参数（String/Integer 自动转池条目）
        BootstrapMethodEntry e3 = cpb.bsmEntry(d, List.of("a", "b"));
        check(e3 != e1, "different args → different entry");
        check(e3.arguments().size() == 2, "2 args");
        check(((StringEntry) e3.arguments().get(0)).stringValue().equals("a"), "arg0 string");
        check(((StringEntry) e3.arguments().get(1)).stringValue().equals("b"), "arg1 string");
        check(e3.arguments().get(0).constantPool() == cpb, "arg bound to same pool");
        // MethodHandleEntry 版本重载
        MethodHandleEntry mh = cpb.methodHandleEntry(d);
        BootstrapMethodEntry e4 = cpb.bsmEntry(mh, List.of());
        check(e4 == e1 || e4.bootstrapMethod() == mh, "bsmEntry(MethodHandleEntry, List) overload");
        check(e4.bootstrapMethod().kind() == 6, "handle kind via MethodHandleEntry");
        // bootstrapMethod() 引用面
        check(e1.bootstrapMethod().reference().owner().asInternalName().equals("java/lang/Integer"), "reference owner");
        check(e1.bootstrapMethod().reference().name().stringValue().equals("valueOf"), "reference name");
        check(e1.bootstrapMethod().reference() instanceof MethodRefEntry, "reference is MethodRefEntry");
        // PoolEntry 通用面
        check(e1.constantPool() == cpb, "entry pool identity");
    }

    // ---- Part 5: build 注入 invokedynamic → BootstrapMethodsAttribute 自动生成 + bsmIndex 联动（实测） ----
    static void testInvokeDynamicInjection() throws Exception {
        ClassFile cf = ClassFile.of();
        DirectMethodHandleDesc bsm = MethodHandleDesc.ofMethod(DirectMethodHandleDesc.Kind.STATIC,
                ClassDesc.of("java.lang.invoke.LambdaMetafactory"), "metafactory",
                MethodTypeDesc.ofDescriptor("(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;"));
        byte[] b = cf.build(ClassDesc.of("BmeT"), cb -> cb
                .withMethodBody("m", MethodTypeDesc.ofDescriptor("()Ljava/lang/Runnable;"), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC,
                        c -> c.invokedynamic(DynamicCallSiteDesc.of(bsm, "run", MethodTypeDesc.ofDescriptor("()Ljava/lang/Runnable;"),
                                MethodTypeDesc.ofDescriptor("()V"),
                                MethodHandleDesc.ofMethod(DirectMethodHandleDesc.Kind.STATIC, ClassDesc.of("BmeT"), "lam$", MethodTypeDesc.ofDescriptor("()V")),
                                MethodTypeDesc.ofDescriptor("()V"))).areturn()));
        ClassModel cm = cf.parse(b);
        check(cf.verify(cm).isEmpty(), "indy injection verify clean");
        // BootstrapMethodsAttribute 由 build 自动生成（无需手动 with）
        Optional<BootstrapMethodsAttribute> bma = cm.findAttribute(Attributes.bootstrapMethods());
        check(bma.isPresent(), "auto-generated BootstrapMethods attribute");
        check(bma.get().bootstrapMethods().size() == 1, "1 bsm entry");
        BootstrapMethodEntry ent = bma.get().bootstrapMethods().get(0);
        check(ent.bootstrapMethod().reference().owner().asInternalName().equals("java/lang/invoke/LambdaMetafactory"), "auto entry owner");
        check(ent.arguments().size() == 3, "auto entry 3 args");
        check(ent.bsmIndex() == 0, "auto entry bsmIndex 0");
        // 指令 ↔ 属性条目联动：InvokeDynamicEntry.bootstrapMethodIndex() == 条目 bsmIndex()（实测）
        for (CodeElement e : cm.methods().get(0).code().get().elementList()) {
            if (e instanceof InvokeDynamicInstruction ii) {
                check(ii.name().stringValue().equals("run"), "indy name run");
                check(ii.typeSymbol().descriptorString().equals("()Ljava/lang/Runnable;"), "indy typeSymbol");
                check(ii.bootstrapArgs().size() == 3, "indy bootstrapArgs 3");
                check(ii.invokedynamic().bootstrapMethodIndex() == ent.bsmIndex(), "indy bsmIndex linkage == attr entry bsmIndex");
                check(ii.invokedynamic().bootstrap().bootstrapMethod().reference().name().stringValue().equals("metafactory"), "InvokeDynamicEntry.bootstrap() chain");
                check(ii.invokedynamic().nameAndType().name().stringValue().equals("run"), "nameAndType name");
                check(ii.opcode() == Opcode.INVOKEDYNAMIC, "INVOKEDYNAMIC opcode");
            }
        }
        // build 回调内 cb.constantPool().bsmEntry(...) 可构造（池通道）
        final int[] idx = new int[1];
        cf.build(ClassDesc.of("BmeU"), cb -> {
            BootstrapMethodEntry en = cb.constantPool().bsmEntry(
                    MethodHandleDesc.ofMethod(DirectMethodHandleDesc.Kind.STATIC, ClassDesc.of("java.lang.Integer"), "valueOf", MethodTypeDesc.ofDescriptor("(I)Ljava/lang/Integer;")),
                    List.of());
            idx[0] = en.bsmIndex();
        });
        check(idx[0] >= 0, "bsmEntry via cb.constantPool() usable: " + idx[0]);
        // BootstrapMethodsAttribute 无公开 of 工厂（实测）—— 只能自动生成或解析
        boolean hasOf = false;
        for (java.lang.reflect.Method m : BootstrapMethodsAttribute.class.getMethods()) if (m.getName().equals("of")) hasOf = true;
        check(!hasOf, "BootstrapMethodsAttribute has no public of() factory");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testLambdaRealClass();
        testBsmEntryBuilder();
        testInvokeDynamicInjection();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
