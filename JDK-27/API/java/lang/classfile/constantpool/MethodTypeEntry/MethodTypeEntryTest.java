/*
 * @test
 * @summary java.lang.classfile.constantpool.MethodTypeEntry conformance: white-box structure + black-box behavior.
 * @run main MethodTypeEntryTest
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

public class MethodTypeEntryTest {
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
        Class<?> c = Class.forName("java.lang.classfile.constantpool.MethodTypeEntry");
        check(c != null, "java.lang.classfile.constantpool.MethodTypeEntry loads");
        check(c.getName().equals("java.lang.classfile.constantpool.MethodTypeEntry"), "binary name java.lang.classfile.constantpool.MethodTypeEntry");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.constantpool.MethodTypeEntry is public");
        check(c.isInterface(), "java.lang.classfile.constantpool.MethodTypeEntry is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.constantpool.MethodTypeEntry has no superclass");
        check(declares(c, "constantValue"), "declares constantValue");
        check(declares(c, "descriptor"), "declares descriptor");
        check(declares(c, "asSymbol"), "declares asSymbol");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        ConstantPoolBuilder cpb = ConstantPoolBuilder.of();
        MethodTypeEntry mte = cpb.methodTypeEntry(MethodTypeDesc.of(ConstantDescs.CD_int, ConstantDescs.CD_int));
        check(mte.descriptor().stringValue().equals("(I)I"), "descriptor");
        check(mte.asSymbol().equals(MethodTypeDesc.of(ConstantDescs.CD_int, ConstantDescs.CD_int)), "asSymbol");
        check(mte.matches(MethodTypeDesc.of(ConstantDescs.CD_int, ConstantDescs.CD_int)), "matches");
        check(mte.constantValue().equals(MethodTypeDesc.of(ConstantDescs.CD_int, ConstantDescs.CD_int)), "constantValue");

    }

    // ---- Part 3: AMHC 常量基础设施预初始化（JDK-27 实测：先完整初始化 ConstantDescs 可避开
    // ConstantDescs <-> PrimitiveClassDescImpl 类初始化环，见 DESIGN.md 易错细节） ----
    static void testConstantDescsPreinit() {
        check(ConstantDescs.CD_Object.descriptorString().equals("Ljava/lang/Object;"), "ConstantDescs.CD_Object preinit");
    }

    // ---- Part 4: MethodTypeEntry 契约（双工厂/跨重载去重/matches/ldc 发射闭环/真类池） ----
    static void testMtPutDedup() {
        ConstantPoolBuilder b = ConstantPoolBuilder.of();
        MethodTypeDesc mtd = MethodTypeDesc.ofDescriptor("(I)V");
        MethodTypeEntry m1 = b.methodTypeEntry(mtd);
        MethodTypeEntry m2 = b.methodTypeEntry(mtd);
        check(m1 == m2, "same-value methodTypeEntry dedup identity");
        MethodTypeEntry m3 = b.methodTypeEntry(b.utf8Entry("(I)V"));
        check(m3 == m1, "cross-overload dedup: (MethodTypeDesc) == (Utf8Entry)");
        check(b.methodTypeEntry(MethodTypeDesc.ofDescriptor("()V")) != m1, "different descriptor different entry");
        check(b.methodTypeEntry(MethodTypeDesc.ofDescriptor("()V")).index() != m1.index(), "different descriptor different index");
    }

    static void testMtFace() {
        ConstantPoolBuilder b = ConstantPoolBuilder.of();
        MethodTypeEntry m = b.methodTypeEntry(MethodTypeDesc.ofDescriptor("(I)V"));
        check(m.descriptor().stringValue().equals("(I)V"), "descriptor utf8");
        check(m.descriptor() == b.utf8Entry("(I)V"), "descriptor is the shared utf8 entry");
        check(m.asSymbol().descriptorString().equals("(I)V"), "asSymbol descriptorString");
        check(m.asSymbol().parameterCount() == 1, "asSymbol parameterCount");
        check(m.matches(MethodTypeDesc.ofDescriptor("(I)V")), "matches positive");
        check(!m.matches(MethodTypeDesc.ofDescriptor("()V")), "matches negative");
        check(m.constantValue() instanceof MethodTypeDesc, "constantValue is MethodTypeDesc");
        check(m.constantValue().equals(m.asSymbol()), "constantValue equals asSymbol");
        check(m.tag() == PoolEntry.TAG_METHOD_TYPE && m.tag() == 16, "tag TAG_METHOD_TYPE == 16");
        check(m.width() == 1 && m.index() > 0 && m.constantPool() == b, "pool face");
        check(m instanceof LoadableConstantEntry && m instanceof PoolEntry, "hierarchy Loadable+PoolEntry");
        check(!(((Object) m) instanceof ConstantValueEntry), "MethodTypeEntry is NOT ConstantValueEntry");
        check(m.typeKind() == TypeKind.REFERENCE, "typeKind REFERENCE");
    }

    static void testMtEmissionAndRealPool() throws Exception {
        ConstantPoolBuilder b = ConstantPoolBuilder.of();
        MethodTypeEntry m = b.methodTypeEntry(MethodTypeDesc.ofDescriptor("(Ljava/lang/Object;)V"));
        byte[] bytes = ClassFile.of().build(ClassDesc.of("pkg.EmitMt"), cb -> cb
                .withMethodBody("m", MethodTypeDesc.ofDescriptor("()V"), ClassFile.ACC_STATIC,
                        cob -> cob.ldc(m).pop().return_()));
        ConstantPool cp2 = ClassFile.of().parse(bytes).constantPool();
        boolean saw = false;
        for (PoolEntry pe : cp2) if (pe instanceof MethodTypeEntry mt && mt.descriptor().stringValue().equals("(Ljava/lang/Object;)V")
                && mt.asSymbol().descriptorString().equals("(Ljava/lang/Object;)V")) saw = true;
        check(saw, "emitted ldc(MethodType) leaves MethodTypeEntry roundtrip");
        byte[] self = MethodTypeEntryTest.class.getResourceAsStream("MethodTypeEntryTest.class").readAllBytes();
        ClassModel cm = ClassFile.of().parse(self);
        ConstantPool cp = cm.constantPool();
        int mtCount = 0; boolean sawLambdaArg = false;
        for (PoolEntry pe : cp) if (pe instanceof MethodTypeEntry mt) {
            mtCount++;
            if (mt.descriptor().stringValue().equals("()V")) sawLambdaArg = true;
        }
        check(mtCount > 0, "real pool has MethodTypeEntry entries (lambda bootstrap args)");
        check(sawLambdaArg, "real pool MethodTypeEntry ()V (lambda functional signature)");
    }

    static void testMtNullNegative() {
        ConstantPoolBuilder b = ConstantPoolBuilder.of();
        try {
            b.methodTypeEntry((MethodTypeDesc) null);
            check(false, "methodTypeEntry(null MethodTypeDesc) must throw");
        } catch (NullPointerException e) { check(true, "methodTypeEntry(null MethodTypeDesc) NPE"); }
        try {
            b.methodTypeEntry((Utf8Entry) null);
            check(false, "methodTypeEntry(null Utf8Entry) must throw");
        } catch (NullPointerException e) { check(true, "methodTypeEntry(null Utf8Entry) NPE"); }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testConstantDescsPreinit();
        testMtPutDedup();
        testMtFace();
        testMtEmissionAndRealPool();
        testMtNullNegative();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
