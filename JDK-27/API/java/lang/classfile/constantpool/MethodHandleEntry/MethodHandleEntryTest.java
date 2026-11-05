/*
 * @test
 * @summary java.lang.classfile.constantpool.MethodHandleEntry conformance: white-box structure + black-box behavior.
 * @run main MethodHandleEntryTest
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

public class MethodHandleEntryTest {
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
        Class<?> c = Class.forName("java.lang.classfile.constantpool.MethodHandleEntry");
        check(c != null, "java.lang.classfile.constantpool.MethodHandleEntry loads");
        check(c.getName().equals("java.lang.classfile.constantpool.MethodHandleEntry"), "binary name java.lang.classfile.constantpool.MethodHandleEntry");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.constantpool.MethodHandleEntry is public");
        check(c.isInterface(), "java.lang.classfile.constantpool.MethodHandleEntry is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.constantpool.MethodHandleEntry has no superclass");
        check(declares(c, "constantValue"), "declares constantValue");
        check(declares(c, "kind"), "declares kind");
        check(declares(c, "reference"), "declares reference");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        ConstantPoolBuilder cpb = ConstantPoolBuilder.of();
        DirectMethodHandleDesc mh = MethodHandleDesc.ofMethod(DirectMethodHandleDesc.Kind.STATIC, ClassDesc.of("java.lang.Integer"), "valueOf", MethodTypeDesc.of(ClassDesc.of("java.lang.Integer"), ConstantDescs.CD_int));
        MethodHandleEntry mhe = cpb.methodHandleEntry(mh);
        check(mhe.kind() == DirectMethodHandleDesc.Kind.STATIC.refKind, "kind STATIC refKind");
        check(mhe.reference().name().stringValue().equals("valueOf"), "reference name");
        check(mhe.asSymbol().equals(mh), "asSymbol");
        check(mhe.constantValue().equals(mh), "constantValue");

    }

    // ---- Part 3: AMHC 常量基础设施预初始化（JDK-27 实测：先完整初始化 ConstantDescs 可避开
    // ConstantDescs <-> PrimitiveClassDescImpl 类初始化环，见 DESIGN.md 易错细节） ----
    static void testConstantDescsPreinit() {
        check(ConstantDescs.CD_Object.descriptorString().equals("Ljava/lang/Object;"), "ConstantDescs.CD_Object preinit");
    }

    // ---- Part 4: MethodHandleEntry 契约（kind 值表/双工厂/去重/符号面/kind 边界/ldc 发射/真 lambda 池） ----
    static void testMhKindTable() {
        // kind() 就是 JVMS 5.4.3.5 的 reference kind；与 DirectMethodHandleDesc.Kind.refKind 一致
        check(DirectMethodHandleDesc.Kind.GETTER.refKind == 1, "GETTER = getField = 1");
        check(DirectMethodHandleDesc.Kind.STATIC_GETTER.refKind == 2, "STATIC_GETTER = getStatic = 2");
        check(DirectMethodHandleDesc.Kind.SETTER.refKind == 3, "SETTER = putField = 3");
        check(DirectMethodHandleDesc.Kind.STATIC_SETTER.refKind == 4, "STATIC_SETTER = putStatic = 4");
        check(DirectMethodHandleDesc.Kind.VIRTUAL.refKind == 5, "VIRTUAL = invokevirtual = 5");
        check(DirectMethodHandleDesc.Kind.STATIC.refKind == 6, "STATIC = invokestatic = 6");
        check(DirectMethodHandleDesc.Kind.SPECIAL.refKind == 7, "SPECIAL = invokespecial = 7");
        check(DirectMethodHandleDesc.Kind.CONSTRUCTOR.refKind == 8, "CONSTRUCTOR = new = 8");
        check(DirectMethodHandleDesc.Kind.INTERFACE_VIRTUAL.refKind == 9, "INTERFACE_VIRTUAL = invokeinterface = 9");
        DirectMethodHandleDesc.Kind[] kinds = DirectMethodHandleDesc.Kind.values();
        check(kinds.length == 11, "11 Kind values (JDK-27: STATIC/INTERFACE_STATIC/VIRTUAL/INTERFACE_VIRTUAL/SPECIAL/INTERFACE_SPECIAL/CONSTRUCTOR/GETTER/SETTER/STATIC_GETTER/STATIC_SETTER)");
    }

    static void testMhPutDedup() {
        ConstantPoolBuilder b = ConstantPoolBuilder.of();
        MethodRefEntry mre = b.methodRefEntry(ClassDesc.of("java.lang.Object"), "toString", MethodTypeDesc.ofDescriptor("()Ljava/lang/String;"));
        MethodHandleEntry m1 = b.methodHandleEntry(5, mre);
        MethodHandleEntry m2 = b.methodHandleEntry(5, mre);
        check(m1 == m2, "same kind+ref dedup identity");
        check(b.methodHandleEntry(6, mre) != m1, "different kind different entry");
        DirectMethodHandleDesc d = MethodHandleDesc.ofMethod(DirectMethodHandleDesc.Kind.VIRTUAL, ClassDesc.of("java.lang.Object"), "toString", MethodTypeDesc.ofDescriptor("()Ljava/lang/String;"));
        check(b.methodHandleEntry(d) == m1, "methodHandleEntry(DirectMethodHandleDesc) dedups with equal (kind,ref)");
    }

    static void testMhFace() {
        ConstantPoolBuilder b = ConstantPoolBuilder.of();
        MethodRefEntry mre = b.methodRefEntry(ClassDesc.of("java.lang.Object"), "toString", MethodTypeDesc.ofDescriptor("()Ljava/lang/String;"));
        MethodHandleEntry m = b.methodHandleEntry(5, mre);
        check(m.kind() == 5, "kind() roundtrip");
        check(m.reference() == mre, "reference() identity");
        DirectMethodHandleDesc sym = m.asSymbol();
        check(sym.refKind() == 5, "asSymbol refKind");
        check(sym.kind() == DirectMethodHandleDesc.Kind.VIRTUAL, "asSymbol kind VIRTUAL");
        check(sym.owner().descriptorString().equals("Ljava/lang/Object;"), "asSymbol owner");
        check(sym.methodName().equals("toString"), "asSymbol methodName");
        check(sym.lookupDescriptor().equals("()Ljava/lang/String;"), "asSymbol lookupDescriptor");
        check(m.constantValue() instanceof DirectMethodHandleDesc, "constantValue is DirectMethodHandleDesc");
        check(m.constantValue().equals(sym), "constantValue equals asSymbol");
        check(m.typeKind() == TypeKind.REFERENCE, "typeKind REFERENCE");
        check(m.tag() == PoolEntry.TAG_METHOD_HANDLE && m.tag() == 15, "tag TAG_METHOD_HANDLE == 15");
        check(m.width() == 1 && m.index() > 0 && m.constantPool() == b, "pool face");
        check(m instanceof LoadableConstantEntry && m instanceof PoolEntry, "hierarchy");
        check(!(((Object) m) instanceof ConstantValueEntry), "not ConstantValueEntry");
    }

    static void testMhKindBounds() {
        ConstantPoolBuilder b = ConstantPoolBuilder.of();
        MethodRefEntry mre = b.methodRefEntry(ClassDesc.of("java.lang.Object"), "toString", MethodTypeDesc.ofDescriptor("()Ljava/lang/String;"));
        try {
            b.methodHandleEntry(-1, mre);
            check(false, "kind -1 must throw");
        } catch (IllegalArgumentException e) {
            check(e.getMessage() != null, "kind -1 IAE message: " + e.getMessage());
        }
        try {
            b.methodHandleEntry(256, mre);
            check(false, "kind 256 must throw");
        } catch (IllegalArgumentException e) { check(true, "kind 256 IAE (u1 range)"); }
        MethodHandleEntry k0 = b.methodHandleEntry(0, mre);
        check(k0.kind() == 0, "kind 0 stored as-is (u1 range accepted)");
        try {
            k0.asSymbol();
            check(false, "asSymbol on kind 0 must throw");
        } catch (IllegalArgumentException e) { check(true, "asSymbol(kind=0) IAE (no symbolic kind)"); }
        check(b.methodHandleEntry(99, mre).kind() == 99, "kind 99 stored as-is (u1 range)");
        try {
            b.methodHandleEntry(6, null);
            check(false, "methodHandleEntry(6,null) must throw");
        } catch (NullPointerException e) { check(true, "methodHandleEntry(6,null) NPE"); }
    }

    static void testMhEmissionAndRealPool() throws Exception {
        ConstantPoolBuilder b = ConstantPoolBuilder.of();
        DirectMethodHandleDesc d = MethodHandleDesc.ofMethod(DirectMethodHandleDesc.Kind.STATIC, ClassDesc.of("java.lang.Math"), "abs", MethodTypeDesc.ofDescriptor("(I)I"));
        MethodHandleEntry m = b.methodHandleEntry(d);
        byte[] bytes = ClassFile.of().build(ClassDesc.of("pkg.EmitMh"), cb -> cb
                .withMethodBody("mm", MethodTypeDesc.ofDescriptor("()V"), ClassFile.ACC_STATIC,
                        cob -> cob.ldc(m).pop().return_()));
        boolean saw = false;
        for (PoolEntry pe : ClassFile.of().parse(bytes).constantPool())
            if (pe instanceof MethodHandleEntry h && h.kind() == 6
                    && h.reference().owner().asInternalName().equals("java/lang/Math")
                    && h.reference().name().stringValue().equals("abs")) saw = true;
        check(saw, "emitted ldc(MethodHandle) leaves MethodHandleEntry roundtrip");
        // 真 lambda 池：LambdaMetafactory.metafactory 是 STATIC kind=6
        byte[] self = MethodHandleEntryTest.class.getResourceAsStream("MethodHandleEntryTest.class").readAllBytes();
        ClassModel cm = ClassFile.of().parse(self);
        ConstantPool cp = cm.constantPool();
        boolean sawLmf = false; int mhCount = 0;
        for (PoolEntry pe : cp) if (pe instanceof MethodHandleEntry h) {
            mhCount++;
            if (h.reference().owner().asInternalName().equals("java/lang/invoke/LambdaMetafactory")
                    && h.reference().name().stringValue().equals("metafactory")
                    && h.kind() == 6) sawLmf = true;
        }
        check(mhCount > 0, "real pool has MethodHandleEntry entries");
        check(sawLmf, "real pool LambdaMetafactory.metafactory MethodHandleEntry kind=6");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testConstantDescsPreinit();
        testMhKindTable();
        testMhPutDedup();
        testMhFace();
        testMhKindBounds();
        testMhEmissionAndRealPool();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
