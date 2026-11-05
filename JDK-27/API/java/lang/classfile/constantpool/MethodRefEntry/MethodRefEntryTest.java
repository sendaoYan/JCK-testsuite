/*
 * @test
 * @summary java.lang.classfile.constantpool.MethodRefEntry conformance: white-box structure + black-box behavior.
 * @run main MethodRefEntryTest
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

public class MethodRefEntryTest {
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
        Class<?> c = Class.forName("java.lang.classfile.constantpool.MethodRefEntry");
        check(c != null, "java.lang.classfile.constantpool.MethodRefEntry loads");
        check(c.getName().equals("java.lang.classfile.constantpool.MethodRefEntry"), "binary name java.lang.classfile.constantpool.MethodRefEntry");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.constantpool.MethodRefEntry is public");
        check(c.isInterface(), "java.lang.classfile.constantpool.MethodRefEntry is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.constantpool.MethodRefEntry has no superclass");
        check(declares(c, "typeSymbol"), "declares typeSymbol");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        ConstantPoolBuilder cpb = ConstantPoolBuilder.of();
        MethodRefEntry mre = cpb.methodRefEntry(ClassDesc.of("java.lang.Object"), "toString", MethodTypeDesc.of(ConstantDescs.CD_String));
        check(mre.owner().asInternalName().equals("java/lang/Object"), "owner");
        check(mre.name().stringValue().equals("toString"), "name");
        check(mre.typeSymbol().equals(MethodTypeDesc.of(ConstantDescs.CD_String)), "typeSymbol");
        check(mre.nameAndType().name().stringValue().equals("toString"), "nameAndType name");

    }

    // ---- Part 3: AMHC 常量基础设施预初始化（JDK-27 实测：先完整初始化 ConstantDescs 可避开
    // ConstantDescs <-> PrimitiveClassDescImpl 类初始化环，见 DESIGN.md 易错细节） ----
    static void testConstantDescsPreinit() {
        check(ConstantDescs.CD_Object.descriptorString().equals("Ljava/lang/Object;"), "ConstantDescs.CD_Object preinit");
    }

    // ---- Part 4: MethodRefEntry 契约（双工厂/去重/MemberRef 面/invokevirtual 发射闭环/真类池 <init>） ----
    static void testMrefPutDedup() {
        ConstantPoolBuilder b = ConstantPoolBuilder.of();
        MethodTypeDesc mtd = MethodTypeDesc.ofDescriptor("()Ljava/lang/String;");
        MethodRefEntry m1 = b.methodRefEntry(ClassDesc.of("java.lang.Object"), "toString", mtd);
        MethodRefEntry m2 = b.methodRefEntry(ClassDesc.of("java.lang.Object"), "toString", mtd);
        check(m1 == m2, "same-value methodRefEntry dedup identity");
        MethodRefEntry m3 = b.methodRefEntry(m1.owner(), m1.nameAndType());
        check(m3 == m1, "cross-overload dedup");
        MethodRefEntry m4 = b.methodRefEntry(ClassDesc.of("java.lang.Object"), "hashCode", MethodTypeDesc.ofDescriptor("()I"));
        check(m4 != m1 && m4.index() != m1.index(), "different member different entry");
        check(b.methodRefEntry(m1.owner(), m1.nameAndType()).index() == m1.index(), "dedup index stable");
    }

    static void testMrefFace() {
        ConstantPoolBuilder b = ConstantPoolBuilder.of();
        MethodRefEntry m = b.methodRefEntry(ClassDesc.of("java.lang.Object"), "toString", MethodTypeDesc.ofDescriptor("()Ljava/lang/String;"));
        check(m.owner().asInternalName().equals("java/lang/Object"), "owner");
        check(m.name().stringValue().equals("toString"), "name");
        check(m.type().stringValue().equals("()Ljava/lang/String;"), "type descriptor");
        check(m.nameAndType().name() == m.name() && m.nameAndType().type() == m.type(), "name()/type() delegate to NAT");
        check(m.typeSymbol().descriptorString().equals("()Ljava/lang/String;"), "typeSymbol MethodTypeDesc");
        check(m.typeSymbol().parameterCount() == 0, "typeSymbol parameterCount");
        check(m.tag() == PoolEntry.TAG_METHODREF && m.tag() == 10, "tag TAG_METHODREF == 10");
        check(m.width() == 1 && m.index() > 0 && m.constantPool() == b, "pool face");
        check(m instanceof MemberRefEntry, "is MemberRefEntry");
        check(!(((Object) m) instanceof InterfaceMethodRefEntry), "method ref is NOT InterfaceMethodRefEntry");
        check(!(((Object) m) instanceof LoadableConstantEntry), "not loadable");
    }

    static void testMrefEmissionAndRealPool() throws Exception {
        byte[] bytes = ClassFile.of().build(ClassDesc.of("pkg.EmitMr"), cb -> cb
                .withMethodBody("m", MethodTypeDesc.ofDescriptor("(Ljava/lang/Object;)V"), ClassFile.ACC_STATIC,
                        cob -> cob.aload(0).invokevirtual(ClassDesc.of("java.lang.Object"), "toString", MethodTypeDesc.ofDescriptor("()Ljava/lang/String;")).pop().return_()));
        ClassModel cm2 = ClassFile.of().parse(bytes);
        boolean saw = false;
        for (PoolEntry pe : cm2.constantPool()) if (pe instanceof MethodRefEntry mr
                && mr.owner().asInternalName().equals("java/lang/Object")
                && mr.name().stringValue().equals("toString")
                && mr.type().stringValue().equals("()Ljava/lang/String;")) saw = true;
        check(saw, "emitted invokevirtual leaves MethodRefEntry roundtrip");
        check(ClassFile.of().verify(cm2).isEmpty(), "emitted invokevirtual class verifies clean");
        // 真类池：隐式构造器 super() 一定引用 Object.<init>
        byte[] self = MethodRefEntryTest.class.getResourceAsStream("MethodRefEntryTest.class").readAllBytes();
        ClassModel cm = ClassFile.of().parse(self);
        ConstantPool cp = cm.constantPool();
        boolean sawInit = false; int mrefCount = 0;
        for (PoolEntry pe : cp) if (pe instanceof MethodRefEntry mr) {
            mrefCount++;
            if (mr.owner().asInternalName().equals("java/lang/Object") && mr.name().stringValue().equals("<init>")
                    && mr.type().stringValue().equals("()V")) sawInit = true;
        }
        check(mrefCount > 0, "real pool has MethodRefEntry entries");
        check(sawInit, "real pool references Object.<init> via implicit constructor");
    }

    static void testMrefNullNegative() {
        ConstantPoolBuilder b = ConstantPoolBuilder.of();
        try {
            b.methodRefEntry(null, "m", MethodTypeDesc.ofDescriptor("()V"));
            check(false, "methodRefEntry(null owner) must throw");
        } catch (NullPointerException e) { check(true, "methodRefEntry(null owner) NPE"); }
        try {
            b.methodRefEntry(null, null);
            check(false, "methodRefEntry(null,null) must throw");
        } catch (NullPointerException e) { check(true, "methodRefEntry(null,null) NPE"); }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testConstantDescsPreinit();
        testMrefPutDedup();
        testMrefFace();
        testMrefEmissionAndRealPool();
        testMrefNullNegative();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
