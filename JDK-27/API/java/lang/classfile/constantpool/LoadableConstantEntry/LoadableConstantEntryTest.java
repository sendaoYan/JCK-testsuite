/*
 * @test
 * @summary java.lang.classfile.constantpool.LoadableConstantEntry conformance: white-box structure + black-box behavior.
 * @run main LoadableConstantEntryTest
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

public class LoadableConstantEntryTest {
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
        Class<?> c = Class.forName("java.lang.classfile.constantpool.LoadableConstantEntry");
        check(c != null, "java.lang.classfile.constantpool.LoadableConstantEntry loads");
        check(c.getName().equals("java.lang.classfile.constantpool.LoadableConstantEntry"), "binary name java.lang.classfile.constantpool.LoadableConstantEntry");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.constantpool.LoadableConstantEntry is public");
        check(c.isInterface(), "java.lang.classfile.constantpool.LoadableConstantEntry is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.constantpool.LoadableConstantEntry has no superclass");
        check(declares(c, "constantValue"), "declares constantValue");
        check(declares(c, "typeKind"), "declares typeKind");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        ConstantPoolBuilder cpb = ConstantPoolBuilder.of();
        LoadableConstantEntry e1 = cpb.stringEntry("s");
        check(e1.constantValue().equals("s"), "string constantValue");
        check(e1.typeKind() == TypeKind.REFERENCE, "string typeKind REFERENCE");
        LoadableConstantEntry e2 = cpb.classEntry(ClassDesc.of("java.lang.String"));
        check(e2.typeKind() == TypeKind.REFERENCE, "class typeKind REFERENCE");
        LoadableConstantEntry e3 = cpb.intEntry(7);
        check(e3.constantValue().equals(7) && e3.typeKind() == TypeKind.INT, "int entry");

    }

    // ---- Part 3: AMHC 常量基础设施预初始化（JDK-27 实测：先完整初始化 ConstantDescs 可避开
    // ConstantDescs <-> PrimitiveClassDescImpl 类初始化环，见 DESIGN.md 易错细节） ----
    static void testConstantDescsPreinit() {
        check(ConstantDescs.CD_Object.descriptorString().equals("Ljava/lang/Object;"), "ConstantDescs.CD_Object preinit");
    }

    // ---- Part 4: LoadableConstantEntry 契约（17 类 instanceof 矩阵/constantValue 多态/typeKind 表/分派工厂/ldc 发射） ----
    static void testLceHierarchyMatrix() {
        ConstantPoolBuilder b = ConstantPoolBuilder.of();
        MethodRefEntry mre = b.methodRefEntry(ClassDesc.of("java.lang.Object"), "toString", MethodTypeDesc.ofDescriptor("()Ljava/lang/String;"));
        Object[] loadable = {
                b.intEntry(1), b.longEntry(2L), b.floatEntry(3f), b.doubleEntry(4d),
                b.stringEntry("s"), b.classEntry(ClassDesc.of("p.C")),
                b.methodHandleEntry(6, mre), b.methodTypeEntry(MethodTypeDesc.ofDescriptor("()V")),
        };
        for (Object o : loadable) check(o instanceof LoadableConstantEntry, "loadable kind: " + o.getClass().getSimpleName());
        BootstrapMethodEntry bsm = b.bsmEntry(b.methodHandleEntry(6, mre), List.of());
        check(b.constantDynamicEntry(bsm, b.nameAndTypeEntry("c", ClassDesc.of("java.lang.Object"))) instanceof LoadableConstantEntry, "condy is loadable");
        Object[] notLoadable = {
                b.utf8Entry("u"), b.nameAndTypeEntry("n", ClassDesc.of("I")),
                b.fieldRefEntry(ClassDesc.of("p.C"), "f", ClassDesc.of("I")),
                mre,
                b.interfaceMethodRefEntry(ClassDesc.of("p.I"), "g", MethodTypeDesc.ofDescriptor("()V")),
                b.moduleEntry(ModuleDesc.of("m")), b.packageEntry(PackageDesc.of("p")),
                b.invokeDynamicEntry(bsm, b.nameAndTypeEntry("i", MethodTypeDesc.ofDescriptor("()V"))),
        };
        for (Object o : notLoadable) check(!(o instanceof LoadableConstantEntry), "not loadable: " + o.getClass().getSimpleName());
        // 实测矩阵：Loadable 中只有 5 种 ConstantValue；Utf8 不 Loadable
        for (Object o : loadable) check(o instanceof PoolEntry, "loadable implies PoolEntry");
    }

    static void testLceConstantValuePolymorphism() {
        ConstantPoolBuilder b = ConstantPoolBuilder.of();
        check(b.intEntry(5).constantValue().equals(5), "IntegerEntry -> Integer");
        check(b.longEntry(6L).constantValue().equals(6L), "LongEntry -> Long");
        check(b.floatEntry(7f).constantValue().equals(7f), "FloatEntry -> Float");
        check(b.doubleEntry(8d).constantValue().equals(8d), "DoubleEntry -> Double");
        check(b.stringEntry("x").constantValue().equals("x"), "StringEntry -> String");
        check(b.classEntry(ClassDesc.of("java.lang.String")).constantValue() instanceof ClassDesc, "ClassEntry -> ClassDesc");
        check(b.classEntry(ClassDesc.of("java.lang.String")).constantValue().equals(ClassDesc.of("java.lang.String")), "ClassDesc value equality");
        MethodHandleEntry mh = b.methodHandleEntry(6, b.methodRefEntry(ClassDesc.of("java.lang.Object"), "toString", MethodTypeDesc.ofDescriptor("()Ljava/lang/String;")));
        check(mh.constantValue() instanceof DirectMethodHandleDesc, "MethodHandleEntry -> DirectMethodHandleDesc");
        check(mh.constantValue().equals(mh.asSymbol()), "DirectMethodHandleDesc equals asSymbol");
        MethodTypeEntry mt = b.methodTypeEntry(MethodTypeDesc.ofDescriptor("(I)V"));
        check(mt.constantValue() instanceof MethodTypeDesc, "MethodTypeEntry -> MethodTypeDesc");
        check(mt.constantValue().equals(MethodTypeDesc.ofDescriptor("(I)V")), "MethodTypeDesc value equality");
    }

    static void testLceTypeKindTable() {
        ConstantPoolBuilder b = ConstantPoolBuilder.of();
        MethodHandleEntry mh = b.methodHandleEntry(6, b.methodRefEntry(ClassDesc.of("java.lang.Object"), "toString", MethodTypeDesc.ofDescriptor("()Ljava/lang/String;")));
        check(b.intEntry(1).typeKind() == TypeKind.INT, "int typeKind INT");
        check(b.longEntry(1L).typeKind() == TypeKind.LONG, "long typeKind LONG");
        check(b.floatEntry(1f).typeKind() == TypeKind.FLOAT, "float typeKind FLOAT");
        check(b.doubleEntry(1d).typeKind() == TypeKind.DOUBLE, "double typeKind DOUBLE");
        check(b.stringEntry("s").typeKind() == TypeKind.REFERENCE, "string typeKind REFERENCE");
        check(b.classEntry(ClassDesc.of("p.C")).typeKind() == TypeKind.REFERENCE, "class typeKind REFERENCE");
        check(mh.typeKind() == TypeKind.REFERENCE, "methodhandle typeKind REFERENCE");
        check(b.methodTypeEntry(MethodTypeDesc.ofDescriptor("()V")).typeKind() == TypeKind.REFERENCE, "methodtype typeKind REFERENCE");
    }

    static void testLceDispatchAndEmission() {
        ConstantPoolBuilder b = ConstantPoolBuilder.of();
        check(b.loadableConstantEntry(31) == b.intEntry(31), "loadableConstantEntry(Integer) identity");
        check(b.loadableConstantEntry("ls") == b.stringEntry("ls"), "loadableConstantEntry(String) identity");
        check(b.loadableConstantEntry(32L) == b.longEntry(32L), "loadableConstantEntry(Long) identity");
        check(b.loadableConstantEntry(ClassDesc.of("q.D")) == b.classEntry(ClassDesc.of("q.D")), "loadableConstantEntry(ClassDesc) identity");
        try {
            b.loadableConstantEntry(null);
            check(false, "loadableConstantEntry(null) must throw");
        } catch (NullPointerException e) { check(true, "loadableConstantEntry(null) NPE"); }
        // ldc(LoadableConstantEntry) 发射闭环
        StringEntry se = b.stringEntry("ldcFace");
        byte[] bytes = ClassFile.of().build(ClassDesc.of("pkg.EmitLce"), cb -> cb
                .withMethodBody("m", MethodTypeDesc.ofDescriptor("()V"), ClassFile.ACC_STATIC,
                        cob -> cob.ldc(se).pop().return_()));
        boolean saw = false;
        for (PoolEntry pe : ClassFile.of().parse(bytes).constantPool())
            if (pe instanceof StringEntry s2 && s2.stringValue().equals("ldcFace")) saw = true;
        check(saw, "ldc(LoadableConstantEntry) emission roundtrip");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testConstantDescsPreinit();
        testLceHierarchyMatrix();
        testLceConstantValuePolymorphism();
        testLceTypeKindTable();
        testLceDispatchAndEmission();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
