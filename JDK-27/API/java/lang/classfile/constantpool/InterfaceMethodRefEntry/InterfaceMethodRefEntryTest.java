/*
 * @test
 * @summary java.lang.classfile.constantpool.InterfaceMethodRefEntry conformance: white-box structure + black-box behavior.
 * @run main InterfaceMethodRefEntryTest
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

public class InterfaceMethodRefEntryTest {
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
        Class<?> c = Class.forName("java.lang.classfile.constantpool.InterfaceMethodRefEntry");
        check(c != null, "java.lang.classfile.constantpool.InterfaceMethodRefEntry loads");
        check(c.getName().equals("java.lang.classfile.constantpool.InterfaceMethodRefEntry"), "binary name java.lang.classfile.constantpool.InterfaceMethodRefEntry");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.constantpool.InterfaceMethodRefEntry is public");
        check(c.isInterface(), "java.lang.classfile.constantpool.InterfaceMethodRefEntry is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.constantpool.InterfaceMethodRefEntry has no superclass");
        check(declares(c, "typeSymbol"), "declares typeSymbol");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        ConstantPoolBuilder cpb = ConstantPoolBuilder.of();
        InterfaceMethodRefEntry imre = cpb.interfaceMethodRefEntry(ClassDesc.of("java.lang.Runnable"), "run", MethodTypeDesc.of(ConstantDescs.CD_void));
        check(imre.owner().asInternalName().equals("java/lang/Runnable"), "owner");
        check(imre.name().stringValue().equals("run"), "name");
        check(imre.typeSymbol().equals(MethodTypeDesc.of(ConstantDescs.CD_void)), "typeSymbol");

    }

    // ---- Part 3: AMHC 常量基础设施预初始化（JDK-27 实测：先完整初始化 ConstantDescs 可避开
    // ConstantDescs <-> PrimitiveClassDescImpl 类初始化环，见 DESIGN.md 易错细节） ----
    static void testConstantDescsPreinit() {
        check(ConstantDescs.CD_Object.descriptorString().equals("Ljava/lang/Object;"), "ConstantDescs.CD_Object preinit");
    }

    // ---- Part 4: InterfaceMethodRefEntry 契约（双工厂/去重/MemberRef 面/invokeinterface 发射闭环/真类池） ----
    static void testImrPutDedup() {
        ConstantPoolBuilder b = ConstantPoolBuilder.of();
        InterfaceMethodRefEntry i1 = b.interfaceMethodRefEntry(ClassDesc.of("java.lang.Runnable"), "run", MethodTypeDesc.ofDescriptor("()V"));
        InterfaceMethodRefEntry i2 = b.interfaceMethodRefEntry(ClassDesc.of("java.lang.Runnable"), "run", MethodTypeDesc.ofDescriptor("()V"));
        check(i1 == i2, "same-value interfaceMethodRefEntry dedup identity");
        InterfaceMethodRefEntry i3 = b.interfaceMethodRefEntry(i1.owner(), i1.nameAndType());
        check(i3 == i1, "cross-overload dedup");
        InterfaceMethodRefEntry i4 = b.interfaceMethodRefEntry(ClassDesc.of("java.lang.Runnable"), "toString", MethodTypeDesc.ofDescriptor("()Ljava/lang/String;"));
        check(i4 != i1 && i4.index() != i1.index(), "different member different entry");
    }

    static void testImrFace() {
        ConstantPoolBuilder b = ConstantPoolBuilder.of();
        InterfaceMethodRefEntry i = b.interfaceMethodRefEntry(ClassDesc.of("java.lang.Runnable"), "run", MethodTypeDesc.ofDescriptor("()V"));
        check(i.owner().asInternalName().equals("java/lang/Runnable"), "owner");
        check(i.name().stringValue().equals("run"), "name");
        check(i.type().stringValue().equals("()V"), "type descriptor");
        check(i.nameAndType().name() == i.name() && i.nameAndType().type() == i.type(), "name()/type() delegate to NAT");
        check(i.typeSymbol().descriptorString().equals("()V"), "typeSymbol MethodTypeDesc");
        check(i.tag() == PoolEntry.TAG_INTERFACE_METHODREF && i.tag() == 11, "tag TAG_INTERFACE_METHODREF == 11");
        check(i.width() == 1 && i.index() > 0 && i.constantPool() == b, "pool face");
        check(i instanceof MemberRefEntry, "is MemberRefEntry");
        check(!(((Object) i) instanceof MethodRefEntry), "interface method ref is NOT MethodRefEntry");
        check(!(((Object) i) instanceof LoadableConstantEntry), "not loadable");
    }

    static void testImrEmissionAndRealPool() throws Exception {
        byte[] bytes = ClassFile.of().build(ClassDesc.of("pkg.EmitIr"), cb -> cb
                .withInterfaceSymbols(ClassDesc.of("java.lang.Runnable"))
                .withMethodBody("m", MethodTypeDesc.ofDescriptor("(Ljava/lang/Runnable;)V"), ClassFile.ACC_STATIC,
                        cob -> cob.aload(0).invokeinterface(ClassDesc.of("java.lang.Runnable"), "run", MethodTypeDesc.ofDescriptor("()V")).return_()));
        ClassModel cm2 = ClassFile.of().parse(bytes);
        boolean saw = false;
        for (PoolEntry pe : cm2.constantPool()) if (pe instanceof InterfaceMethodRefEntry im
                && im.owner().asInternalName().equals("java/lang/Runnable")
                && im.name().stringValue().equals("run")
                && im.type().stringValue().equals("()V")) saw = true;
        check(saw, "emitted invokeinterface leaves InterfaceMethodRefEntry roundtrip");
        check(ClassFile.of().verify(cm2).isEmpty(), "emitted invokeinterface class verifies clean");
        // 真类池：LAMBDA_R.run() 经 invokeinterface
        byte[] self = InterfaceMethodRefEntryTest.class.getResourceAsStream("InterfaceMethodRefEntryTest.class").readAllBytes();
        ClassModel cm = ClassFile.of().parse(self);
        ConstantPool cp = cm.constantPool();
        boolean sawRun = false; int imrCount = 0;
        for (PoolEntry pe : cp) if (pe instanceof InterfaceMethodRefEntry im) {
            imrCount++;
            if (im.owner().asInternalName().equals("java/lang/Runnable") && im.name().stringValue().equals("run")) sawRun = true;
        }
        check(imrCount > 0, "real pool has InterfaceMethodRefEntry entries");
        check(sawRun, "real pool references Runnable.run via touchLambda");
    }

    static void testImrNullNegative() {
        ConstantPoolBuilder b = ConstantPoolBuilder.of();
        try {
            b.interfaceMethodRefEntry(null, "g", MethodTypeDesc.ofDescriptor("()V"));
            check(false, "interfaceMethodRefEntry(null owner) must throw");
        } catch (NullPointerException e) { check(true, "interfaceMethodRefEntry(null owner) NPE"); }
        try {
            b.interfaceMethodRefEntry(null, null);
            check(false, "interfaceMethodRefEntry(null,null) must throw");
        } catch (NullPointerException e) { check(true, "interfaceMethodRefEntry(null,null) NPE"); }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testConstantDescsPreinit();
        testImrPutDedup();
        testImrFace();
        testImrEmissionAndRealPool();
        testImrNullNegative();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
