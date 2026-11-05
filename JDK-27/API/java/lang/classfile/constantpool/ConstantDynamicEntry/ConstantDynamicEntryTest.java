/*
 * @test
 * @summary java.lang.classfile.constantpool.ConstantDynamicEntry conformance: white-box structure + black-box behavior.
 * @run main ConstantDynamicEntryTest
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

public class ConstantDynamicEntryTest {
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
        Class<?> c = Class.forName("java.lang.classfile.constantpool.ConstantDynamicEntry");
        check(c != null, "java.lang.classfile.constantpool.ConstantDynamicEntry loads");
        check(c.getName().equals("java.lang.classfile.constantpool.ConstantDynamicEntry"), "binary name java.lang.classfile.constantpool.ConstantDynamicEntry");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.constantpool.ConstantDynamicEntry is public");
        check(c.isInterface(), "java.lang.classfile.constantpool.ConstantDynamicEntry is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.constantpool.ConstantDynamicEntry has no superclass");
        check(declares(c, "typeSymbol"), "declares typeSymbol");
        check(declares(c, "constantValue"), "declares constantValue");
        check(declares(c, "asSymbol"), "declares asSymbol");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        ConstantPoolBuilder cpb = ConstantPoolBuilder.of();
        BootstrapMethodEntry bsm = cpb.bsmEntry(MethodHandleDesc.ofMethod(DirectMethodHandleDesc.Kind.STATIC, ClassDesc.of("java.lang.Integer"), "valueOf", MethodTypeDesc.of(ClassDesc.of("java.lang.Integer"), ConstantDescs.CD_int)), java.util.List.of());
        NameAndTypeEntry nat = cpb.nameAndTypeEntry("x", ConstantDescs.CD_int);
        ConstantDynamicEntry cde = cpb.constantDynamicEntry(bsm, nat);
        check(cde.bootstrap() != null, "bootstrap");
        check(cde.bootstrapMethodIndex() == bsm.bsmIndex(), "bootstrapMethodIndex");
        check(cde.name().stringValue().equals("x"), "name");
        check(cde.type().stringValue().equals("I"), "type");
        check(cde.typeSymbol().equals(ConstantDescs.CD_int), "typeSymbol");
        check(cde.typeKind() == TypeKind.INT, "typeKind INT");

    }

    // ---- Part 3: AMHC 常量基础设施预初始化（JDK-27 实测：先完整初始化 ConstantDescs 可避开
    // ConstantDescs <-> PrimitiveClassDescImpl 类初始化环，见 DESIGN.md 易错细节） ----
    static void testConstantDescsPreinit() {
        check(ConstantDescs.CD_Object.descriptorString().equals("Ljava/lang/Object;"), "ConstantDescs.CD_Object preinit");
    }

    // ---- Part 4: ConstantDynamicEntry 契约（bsm/nat 回读、去重、双工厂、符号面、ldc 发射闭环） ----
    static void testCondyPutAndFace() {
        ConstantPoolBuilder b = ConstantPoolBuilder.of();
        MethodHandleEntry mh = b.methodHandleEntry(6, b.methodRefEntry(ClassDesc.of("java.lang.Integer"), "valueOf", MethodTypeDesc.ofDescriptor("(I)Ljava/lang/Integer;")));
        NameAndTypeEntry nat = b.nameAndTypeEntry("cv", ClassDesc.of("java.lang.Integer"));
        BootstrapMethodEntry bsm = b.bsmEntry(mh, List.of(b.intEntry(7)));
        ConstantDynamicEntry cd = b.constantDynamicEntry(bsm, nat);
        check(cd.name().stringValue().equals("cv"), "name from NAT");
        check(cd.type().stringValue().equals("Ljava/lang/Integer;"), "type from NAT (field descriptor)");
        check(cd.nameAndType() == nat, "nameAndType identity");
        check(cd.bootstrap() == bsm, "bootstrap identity");
        check(cd.bootstrapMethodIndex() == bsm.bsmIndex(), "bootstrapMethodIndex matches bsm entry");
        check(bsm.arguments().size() == 1 && bsm.arguments().get(0) instanceof IntegerEntry ie && ie.intValue() == 7, "bsm argument roundtrip");
        check(cd.tag() == PoolEntry.TAG_DYNAMIC && cd.tag() == 17, "tag TAG_DYNAMIC == 17");
        check(cd.width() == 1 && cd.index() > 0 && cd.constantPool() == b, "pool face");
        check(cd instanceof DynamicConstantPoolEntry && cd instanceof LoadableConstantEntry, "hierarchy Dynamic+Loadable");
        check(!(((Object) cd) instanceof ConstantValueEntry), "condy is NOT ConstantValueEntry");
        check(cd.typeSymbol().descriptorString().equals("Ljava/lang/Integer;"), "typeSymbol is field type");
        check(cd.constantValue() instanceof DynamicConstantDesc, "constantValue is DynamicConstantDesc");
        check(cd.asSymbol().equals(cd.constantValue()), "asSymbol equals constantValue");
        check(cd.typeKind() == TypeKind.REFERENCE, "typeKind from type symbol");
        // 去重：同 bsm+nat 同实例；不同 nat 不同 index
        check(b.constantDynamicEntry(bsm, nat) == cd, "same bsm+nat dedup identity");
        ConstantDynamicEntry cd2 = b.constantDynamicEntry(bsm, b.nameAndTypeEntry("cw", ClassDesc.of("java.lang.Integer")));
        check(cd2 != cd && cd2.index() != cd.index(), "different NAT different entry");
    }

    static void testCondyDescOverload() {
        ConstantPoolBuilder b = ConstantPoolBuilder.of();
        DynamicConstantDesc<?> dcd = DynamicConstantDesc.ofNamed(ConstantDescs.BSM_NULL_CONSTANT, "dn", ClassDesc.of("java.lang.Object"));
        ConstantDynamicEntry cd = b.constantDynamicEntry(dcd);
        check(cd.name().stringValue().equals("dn"), "constantDynamicEntry(DynamicConstantDesc) name");
        check(cd.type().stringValue().equals("Ljava/lang/Object;"), "constantDynamicEntry(DynamicConstantDesc) type");
        check(cd.asSymbol().equals(dcd), "asSymbol equals source DynamicConstantDesc");
        // bsm 参数经 bsmEntry(DirectMethodHandleDesc, List<ConstantDesc>) 工厂回读
        BootstrapMethodEntry bsm = b.bsmEntry(ConstantDescs.BSM_NULL_CONSTANT, List.of(1, "a"));
        check(bsm.bootstrapMethod().reference().name().stringValue().equals("nullConstant"), "bsmEntry(desc) method name");
        check(bsm.arguments().size() == 2, "bsmEntry(desc) arg count");
        check(bsm.arguments().get(0) instanceof IntegerEntry ie1 && ie1.intValue() == 1, "bsm arg0 int");
        check(bsm.arguments().get(1) instanceof StringEntry se && se.stringValue().equals("a"), "bsm arg1 string");
    }

    static void testCondyEmissionLoop() throws Exception {
        ConstantPoolBuilder b = ConstantPoolBuilder.of();
        BootstrapMethodEntry bsm = b.bsmEntry(ConstantDescs.BSM_NULL_CONSTANT, List.of());
        ConstantDynamicEntry cd = b.constantDynamicEntry(bsm, b.nameAndTypeEntry("nv", ClassDesc.of("java.lang.Object")));
        byte[] bytes = ClassFile.of().build(ClassDesc.of("pkg.EmitCd"), cb -> cb
                .withMethodBody("m", MethodTypeDesc.ofDescriptor("()V"), ClassFile.ACC_STATIC,
                        cob -> cob.ldc(cd).pop().return_()));
        ConstantPool cp2 = ClassFile.of().parse(bytes).constantPool();
        boolean saw = false;
        for (PoolEntry pe : cp2) if (pe instanceof ConstantDynamicEntry c) {
            saw = true;
            check(c.name().stringValue().equals("nv"), "emitted condy name roundtrip");
            check(c.type().stringValue().equals("Ljava/lang/Object;"), "emitted condy type roundtrip");
            check(c.bootstrapMethodIndex() >= 0 && c.bootstrapMethodIndex() < cp2.bootstrapMethodCount(), "emitted condy bsm index in range");
            check(c.bootstrap() == cp2.bootstrapMethodEntry(c.bootstrapMethodIndex()), "bootstrap == pool.bootstrapMethodEntry(index)");
            check(c.bootstrap().bootstrapMethod().reference().owner().asInternalName().equals("java/lang/invoke/ConstantBootstraps"), "bsm owner ConstantBootstraps");
        }
        check(saw, "emitted ldc(condy) leaves ConstantDynamicEntry in pool");
    }

    static void testCondyNullNegative() {
        ConstantPoolBuilder b = ConstantPoolBuilder.of();
        try {
            b.constantDynamicEntry((DynamicConstantDesc<?>) null);
            check(false, "constantDynamicEntry(null desc) must throw");
        } catch (NullPointerException e) { check(true, "constantDynamicEntry(null desc) NPE"); }
        try {
            b.constantDynamicEntry(null, null);
            check(false, "constantDynamicEntry(null bsm) must throw");
        } catch (NullPointerException e) { check(true, "constantDynamicEntry(null bsm) NPE"); }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testConstantDescsPreinit();
        testCondyPutAndFace();
        testCondyDescOverload();
        testCondyEmissionLoop();
        testCondyNullNegative();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
