/*
 * @test
 * @summary java.lang.classfile.constantpool.DynamicConstantPoolEntry conformance: white-box structure + black-box behavior.
 * @run main DynamicConstantPoolEntryTest
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

public class DynamicConstantPoolEntryTest {
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
        Class<?> c = Class.forName("java.lang.classfile.constantpool.DynamicConstantPoolEntry");
        check(c != null, "java.lang.classfile.constantpool.DynamicConstantPoolEntry loads");
        check(c.getName().equals("java.lang.classfile.constantpool.DynamicConstantPoolEntry"), "binary name java.lang.classfile.constantpool.DynamicConstantPoolEntry");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.constantpool.DynamicConstantPoolEntry is public");
        check(c.isInterface(), "java.lang.classfile.constantpool.DynamicConstantPoolEntry is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.constantpool.DynamicConstantPoolEntry has no superclass");
        check(declares(c, "bootstrap"), "declares bootstrap");
        check(declares(c, "bootstrapMethodIndex"), "declares bootstrapMethodIndex");
        check(declares(c, "nameAndType"), "declares nameAndType");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        ConstantPoolBuilder cpb = ConstantPoolBuilder.of();
        BootstrapMethodEntry bsm = cpb.bsmEntry(MethodHandleDesc.ofMethod(DirectMethodHandleDesc.Kind.STATIC, ClassDesc.of("java.lang.Integer"), "valueOf", MethodTypeDesc.of(ClassDesc.of("java.lang.Integer"), ConstantDescs.CD_int)), java.util.List.of());
        NameAndTypeEntry nat = cpb.nameAndTypeEntry("m", MethodTypeDesc.of(ConstantDescs.CD_void));
        DynamicConstantPoolEntry d = cpb.invokeDynamicEntry(bsm, nat);
        check(d.bootstrap() != null, "bootstrap");
        check(d.bootstrapMethodIndex() == bsm.bsmIndex(), "bootstrapMethodIndex");
        check(d.nameAndType().name().stringValue().equals("m"), "nameAndType");
        check(d.name().stringValue().equals("m") && d.type().stringValue().equals("()V"), "name+type defaults");

    }

    // ---- Part 3: AMHC 常量基础设施预初始化（JDK-27 实测：先完整初始化 ConstantDescs 可避开
    // ConstantDescs <-> PrimitiveClassDescImpl 类初始化环，见 DESIGN.md 易错细节） ----
    static void testConstantDescsPreinit() {
        check(ConstantDescs.CD_Object.descriptorString().equals("Ljava/lang/Object;"), "ConstantDescs.CD_Object preinit");
    }

    // ---- Part 4: DynamicConstantPoolEntry 契约（两具体实现/bsm+nat 面/双 tag/invokedynamic 发射闭环/池不变量） ----
    static void testDcpeImplementations() {
        ConstantPoolBuilder b = ConstantPoolBuilder.of();
        BootstrapMethodEntry bsm = b.bsmEntry(ConstantDescs.BSM_NULL_CONSTANT, List.of());
        ConstantDynamicEntry cd = b.constantDynamicEntry(bsm, b.nameAndTypeEntry("c", ClassDesc.of("java.lang.Object")));
        InvokeDynamicEntry idn = b.invokeDynamicEntry(bsm, b.nameAndTypeEntry("i", MethodTypeDesc.ofDescriptor("()V")));
        check(cd instanceof DynamicConstantPoolEntry, "ConstantDynamicEntry is DynamicConstantPoolEntry");
        check(idn instanceof DynamicConstantPoolEntry, "InvokeDynamicEntry is DynamicConstantPoolEntry");
        check(!(((Object) idn) instanceof LoadableConstantEntry), "indy is NOT loadable (only condy is)");
        check(cd instanceof LoadableConstantEntry, "condy is loadable");
        // 双 tag：DYNAMIC=17 / INVOKE_DYNAMIC=18
        check(cd.tag() == PoolEntry.TAG_DYNAMIC && cd.tag() == 17, "condy tag 17");
        check(idn.tag() == PoolEntry.TAG_INVOKE_DYNAMIC && idn.tag() == 18, "indy tag 18");
        check(cd.width() == 1 && idn.width() == 1, "both single-slot");
        // 共同面：bootstrap/bootstrapMethodIndex/nameAndType/name/type
        check(cd.bootstrap() == bsm && idn.bootstrap() == bsm, "bootstrap() identity");
        check(cd.bootstrapMethodIndex() == bsm.bsmIndex(), "condy bootstrapMethodIndex");
        check(cd.nameAndType().name().stringValue().equals("c") && cd.type().stringValue().equals("Ljava/lang/Object;"), "condy name/type");
        check(idn.nameAndType().name().stringValue().equals("i") && idn.type().stringValue().equals("()V"), "indy name/type");
        check(cd.name() == cd.nameAndType().name() && cd.type() == cd.nameAndType().type(), "name()/type() delegate to NAT");
        check(idn.name() == idn.nameAndType().name() && idn.type() == idn.nameAndType().type(), "indy name()/type() delegate");
        check(cd.constantPool() == b && idn.constantPool() == b, "pool face");
        check(cd.index() > 0 && idn.index() > 0 && cd.index() != idn.index(), "distinct indices");
    }

    static void testDcpeEmissionLoop() throws Exception {
        // invokedynamic 发射 -> 池条目 -> bootstrap -> 常量池 全段闭环
        DynamicCallSiteDesc dcs = DynamicCallSiteDesc.of(ConstantDescs.BSM_NULL_CONSTANT, "nv", MethodTypeDesc.ofDescriptor("()Ljava/lang/Object;"));
        byte[] bytes = ClassFile.of().build(ClassDesc.of("pkg.EmitDcp"), cb -> cb
                .withMethodBody("m", MethodTypeDesc.ofDescriptor("()Ljava/lang/Object;"), ClassFile.ACC_STATIC,
                        cob -> cob.invokedynamic(dcs).areturn()));
        ClassModel cm2 = ClassFile.of().parse(bytes);
        ConstantPool cp2 = cm2.constantPool();
        boolean saw = false;
        for (PoolEntry pe : cp2) if (pe instanceof DynamicConstantPoolEntry d) {
            saw = true;
            check(d.bootstrapMethodIndex() >= 0 && d.bootstrapMethodIndex() < cp2.bootstrapMethodCount(), "bsm index in range");
            check(d.bootstrap() == cp2.bootstrapMethodEntry(d.bootstrapMethodIndex()), "bootstrap() == pool.bootstrapMethodEntry(index)");
            check(d.bootstrap().constantPool() == cp2, "bsm bound to same pool");
            if (d instanceof InvokeDynamicEntry ie) {
                check(ie.name().stringValue().equals("nv"), "indy name roundtrip");
                check(ie.type().stringValue().equals("()Ljava/lang/Object;"), "indy type roundtrip");
                check(ie.typeSymbol().descriptorString().equals("()Ljava/lang/Object;"), "indy typeSymbol");
                check(ie.asSymbol().equals(dcs), "indy asSymbol equals source DynamicCallSiteDesc");
            }
        }
        check(saw, "emitted invokedynamic leaves DynamicConstantPoolEntry in pool");
        check(cp2.bootstrapMethodCount() == 1, "exactly one bootstrap method recorded");
        BootstrapMethodEntry be = cp2.bootstrapMethodEntry(0);
        check(be.bootstrapMethod().reference().owner().asInternalName().equals("java/lang/invoke/ConstantBootstraps"), "bsm owner");
        check(be.bootstrapMethod().reference().name().stringValue().equals("nullConstant"), "bsm method name");
        check(be.arguments().isEmpty(), "bsm no args");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testConstantDescsPreinit();
        testDcpeImplementations();
        testDcpeEmissionLoop();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
