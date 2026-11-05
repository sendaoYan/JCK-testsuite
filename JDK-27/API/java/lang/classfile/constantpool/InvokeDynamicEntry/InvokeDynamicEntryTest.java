/*
 * @test
 * @summary java.lang.classfile.constantpool.InvokeDynamicEntry conformance: white-box structure + black-box behavior.
 * @run main InvokeDynamicEntryTest
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

public class InvokeDynamicEntryTest {
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

    static final java.lang.Runnable LAMBDA_R = () -> {};
    private static void touchLambda() { LAMBDA_R.run(); }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.classfile.constantpool.InvokeDynamicEntry");
        check(c != null, "java.lang.classfile.constantpool.InvokeDynamicEntry loads");
        check(c.getName().equals("java.lang.classfile.constantpool.InvokeDynamicEntry"), "binary name java.lang.classfile.constantpool.InvokeDynamicEntry");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.constantpool.InvokeDynamicEntry is public");
        check(c.isInterface(), "java.lang.classfile.constantpool.InvokeDynamicEntry is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.constantpool.InvokeDynamicEntry has no superclass");
        check(declares(c, "typeSymbol"), "declares typeSymbol");
        check(declares(c, "asSymbol"), "declares asSymbol");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        ConstantPoolBuilder cpb = ConstantPoolBuilder.of();
        BootstrapMethodEntry bsm = cpb.bsmEntry(MethodHandleDesc.ofMethod(DirectMethodHandleDesc.Kind.STATIC, ClassDesc.of("java.lang.Integer"), "valueOf", MethodTypeDesc.of(ClassDesc.of("java.lang.Integer"), ConstantDescs.CD_int)), java.util.List.of());
        NameAndTypeEntry nat = cpb.nameAndTypeEntry("m", MethodTypeDesc.of(ConstantDescs.CD_void));
        InvokeDynamicEntry ide = cpb.invokeDynamicEntry(bsm, nat);
        check(ide.bootstrap() != null, "bootstrap");
        check(ide.bootstrapMethodIndex() == bsm.bsmIndex(), "bootstrapMethodIndex");
        check(ide.nameAndType().name().stringValue().equals("m"), "nameAndType name");
        check(ide.name().stringValue().equals("m"), "name default");
        check(ide.type().stringValue().equals("()V"), "type default");
        check(ide.typeSymbol().equals(MethodTypeDesc.of(ConstantDescs.CD_void)), "typeSymbol");

    }

    // ---- Part 3: AMHC 常量基础设施预初始化（JDK-27 实测：先完整初始化 ConstantDescs 可避开
    // ConstantDescs <-> PrimitiveClassDescImpl 类初始化环，见 DESIGN.md 易错细节） ----
    static void testConstantDescsPreinit() {
        check(ConstantDescs.CD_Object.descriptorString().equals("Ljava/lang/Object;"), "ConstantDescs.CD_Object preinit");
    }

    // ---- Part 4: InvokeDynamicEntry 契约（双工厂/去重/符号面/invokedynamic 发射闭环/真 lambda 池） ----
    static void testIndyPutDedup() {
        ConstantPoolBuilder b = ConstantPoolBuilder.of();
        BootstrapMethodEntry bsm = b.bsmEntry(ConstantDescs.BSM_NULL_CONSTANT, List.of());
        NameAndTypeEntry nat = b.nameAndTypeEntry("im", MethodTypeDesc.ofDescriptor("()Ljava/lang/Object;"));
        InvokeDynamicEntry i1 = b.invokeDynamicEntry(bsm, nat);
        InvokeDynamicEntry i2 = b.invokeDynamicEntry(bsm, nat);
        check(i1 == i2, "same bsm+nat indy dedup identity");
        check(b.invokeDynamicEntry(bsm, b.nameAndTypeEntry("in", MethodTypeDesc.ofDescriptor("()Ljava/lang/Object;"))) != i1, "different nat different entry");
        DynamicCallSiteDesc dcs = DynamicCallSiteDesc.of(ConstantDescs.BSM_NULL_CONSTANT, "im", MethodTypeDesc.ofDescriptor("()Ljava/lang/Object;"));
        check(b.invokeDynamicEntry(dcs) == i1, "invokeDynamicEntry(DynamicCallSiteDesc) dedups with equal (bsm,nat)");
    }

    static void testIndyFace() {
        ConstantPoolBuilder b = ConstantPoolBuilder.of();
        BootstrapMethodEntry bsm = b.bsmEntry(ConstantDescs.BSM_NULL_CONSTANT, List.of());
        NameAndTypeEntry nat = b.nameAndTypeEntry("im", MethodTypeDesc.ofDescriptor("()Ljava/lang/Object;"));
        InvokeDynamicEntry i = b.invokeDynamicEntry(bsm, nat);
        check(i.bootstrap() == bsm, "bootstrap identity");
        check(i.bootstrapMethodIndex() == bsm.bsmIndex(), "bootstrapMethodIndex");
        check(i.nameAndType() == nat, "nameAndType identity");
        check(i.name().stringValue().equals("im"), "name");
        check(i.type().stringValue().equals("()Ljava/lang/Object;"), "type");
        check(i.typeSymbol().descriptorString().equals("()Ljava/lang/Object;"), "typeSymbol");
        check(i.typeSymbol().parameterCount() == 0, "typeSymbol parameterCount");
        check(i.asSymbol() instanceof DynamicCallSiteDesc, "asSymbol is DynamicCallSiteDesc");
        check(i.asSymbol().invocationName().equals("im"), "asSymbol invocationName");
        check(i.asSymbol().invocationType().descriptorString().equals("()Ljava/lang/Object;"), "asSymbol invocationType");
        check(i.tag() == PoolEntry.TAG_INVOKE_DYNAMIC && i.tag() == 18, "tag TAG_INVOKE_DYNAMIC == 18");
        check(i.width() == 1 && i.index() > 0 && i.constantPool() == b, "pool face");
        check(i instanceof DynamicConstantPoolEntry, "hierarchy DynamicConstantPoolEntry");
        check(!(((Object) i) instanceof LoadableConstantEntry), "indy is NOT loadable");
        check(!(((Object) i) instanceof ConstantDynamicEntry), "indy is NOT ConstantDynamicEntry");
    }

    static void testIndyEmissionAndRealPool() throws Exception {
        // 发射闭环：DynamicCallSiteDesc -> invokedynamic 指令 -> 池条目 -> 全访问器回读
        DynamicCallSiteDesc dcs = DynamicCallSiteDesc.of(ConstantDescs.BSM_NULL_CONSTANT, "ev", MethodTypeDesc.ofDescriptor("()Ljava/lang/Object;"));
        byte[] bytes = ClassFile.of().build(ClassDesc.of("pkg.EmitIdy"), cb -> cb
                .withMethodBody("m", MethodTypeDesc.ofDescriptor("()Ljava/lang/Object;"), ClassFile.ACC_STATIC,
                        cob -> cob.invokedynamic(dcs).areturn()));
        ConstantPool cp2 = ClassFile.of().parse(bytes).constantPool();
        boolean saw = false;
        for (PoolEntry pe : cp2) if (pe instanceof InvokeDynamicEntry ie && ie.name().stringValue().equals("ev")) {
            saw = true;
            check(ie.type().stringValue().equals("()Ljava/lang/Object;"), "emitted indy type");
            check(ie.bootstrapMethodIndex() >= 0 && ie.bootstrapMethodIndex() < cp2.bootstrapMethodCount(), "emitted bsm index valid");
            check(ie.bootstrap() == cp2.bootstrapMethodEntry(ie.bootstrapMethodIndex()), "bootstrap identity in parsed pool");
            check(ie.asSymbol().equals(dcs), "asSymbol roundtrip equals source");
        }
        check(saw, "emitted invokedynamic leaves InvokeDynamicEntry");
        // 真 lambda 池：javac 产物 LambdaMetafactory
        byte[] self = InvokeDynamicEntryTest.class.getResourceAsStream("InvokeDynamicEntryTest.class").readAllBytes();
        ClassModel cm = ClassFile.of().parse(self);
        ConstantPool cp = cm.constantPool();
        boolean sawLmf = false;
        for (PoolEntry pe : cp) if (pe instanceof InvokeDynamicEntry ie) {
            check(ie.bootstrapMethodIndex() >= 0 && ie.bootstrapMethodIndex() < cp.bootstrapMethodCount(), "real indy bsm index valid");
            if (ie.bootstrap().bootstrapMethod().reference().owner().asInternalName().equals("java/lang/invoke/LambdaMetafactory")) {
                sawLmf = true;
                check(ie.typeSymbol() != null, "real indy typeSymbol non-null");
                check(ie.name().stringValue() != null && !ie.name().stringValue().isEmpty(), "real indy name non-empty");
            }
        }
        check(sawLmf, "real pool contains LambdaMetafactory InvokeDynamicEntry (LAMBDA_R)");
    }

    static void testIndyNullNegative() {
        ConstantPoolBuilder b = ConstantPoolBuilder.of();
        try {
            b.invokeDynamicEntry((BootstrapMethodEntry) null, null);
            check(false, "invokeDynamicEntry(null bsm) must throw");
        } catch (NullPointerException e) { check(true, "invokeDynamicEntry(null bsm) NPE"); }
        try {
            b.invokeDynamicEntry((DynamicCallSiteDesc) null);
            check(false, "invokeDynamicEntry(null desc) must throw");
        } catch (NullPointerException e) { check(true, "invokeDynamicEntry(null desc) NPE"); }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testConstantDescsPreinit();
        testIndyPutDedup();
        testIndyFace();
        testIndyEmissionAndRealPool();
        testIndyNullNegative();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
