/*
 * @test
 * @summary java.lang.classfile.constantpool.ConstantPoolBuilder conformance: white-box structure + black-box behavior.
 * @run main ConstantPoolBuilderTest
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

public class ConstantPoolBuilderTest {
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
        Class<?> c = Class.forName("java.lang.classfile.constantpool.ConstantPoolBuilder");
        check(c != null, "java.lang.classfile.constantpool.ConstantPoolBuilder loads");
        check(c.getName().equals("java.lang.classfile.constantpool.ConstantPoolBuilder"), "binary name java.lang.classfile.constantpool.ConstantPoolBuilder");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.constantpool.ConstantPoolBuilder is public");
        check(c.isInterface(), "java.lang.classfile.constantpool.ConstantPoolBuilder is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.constantpool.ConstantPoolBuilder has no superclass");
        check(declares(c, "of"), "declares of");
        check(declares(c, "canWriteDirect"), "declares canWriteDirect");
        check(declares(c, "utf8Entry"), "declares utf8Entry");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        ConstantPoolBuilder cpb = ConstantPoolBuilder.of();
        Utf8Entry u = cpb.utf8Entry("hello");
        ClassEntry cl = cpb.classEntry(ClassDesc.of("java.lang.String"));
        IntegerEntry ie = cpb.intEntry(42);
        LongEntry le = cpb.longEntry(1L << 40);
        StringEntry se = cpb.stringEntry("hi");
        NameAndTypeEntry nat = cpb.nameAndTypeEntry("m", MethodTypeDesc.of(ConstantDescs.CD_void));
        MethodRefEntry mre = cpb.methodRefEntry(ClassDesc.of("java.lang.Object"), "toString", MethodTypeDesc.of(ConstantDescs.CD_String));
        check(u.index() == 1, "utf8 index 1");
        check(cl.asInternalName().equals("java/lang/String"), "class internal");
        check(ie.intValue() == 42, "int value");
        check(le.longValue() == (1L << 40), "long value");
        check(se.stringValue().equals("hi"), "string value");
        check(nat.name().stringValue().equals("m") && nat.type().stringValue().equals("()V"), "nameAndType");
        check(mre.name().stringValue().equals("toString"), "methodRef name");
        check(cpb.size() > 10, "size grows");
        check(cpb.canWriteDirect(ConstantPoolBuilder.of()) == false, "canWriteDirect false for fresh");

    }

    // ---- Part 3: AMHC 常量基础设施预初始化（JDK-27 实测：先完整初始化 ConstantDescs 可避开
    // ConstantDescs <-> PrimitiveClassDescImpl 类初始化环，见 DESIGN.md 易错细节） ----
    static void testConstantDescsPreinit() {
        check(ConstantDescs.CD_Object.descriptorString().equals("Ljava/lang/Object;"), "ConstantDescs.CD_Object preinit");
    }

    // ---- Part 4: ConstantPoolBuilder 契约（fresh size=1/精确增量/三 utf8 重载/拷贝/canWriteDirect/分派工厂/null 矩阵） ----
    static void testFreshSizeAndIncrements() {
        ConstantPoolBuilder b = ConstantPoolBuilder.of();
        check(b.size() == 1, "fresh builder size == 1 (slot 0 reserved)");
        int s = b.size();
        b.utf8Entry("a");
        check(b.size() == s + 1, "utf8 put +1");
        b.intEntry(1);
        check(b.size() == s + 2, "int put +1");
        b.longEntry(1L);
        check(b.size() == s + 4, "long put +2 (double slot)");
        b.floatEntry(1f);
        check(b.size() == s + 5, "float put +1");
        b.doubleEntry(1d);
        check(b.size() == s + 7, "double put +2");
        b.stringEntry("a");
        check(b.size() == s + 8, "string put +1 (utf8 deduped)");
        int before = b.size();
        b.intEntry(1);
        b.longEntry(1L);
        b.floatEntry(1f);
        b.doubleEntry(1d);
        b.stringEntry("a");
        b.utf8Entry("a");
        check(b.size() == before, "duplicate puts grow nothing");
        // first index is 1
        ConstantPoolBuilder b2 = ConstantPoolBuilder.of();
        check(b2.utf8Entry("first").index() == 1, "first entry gets index 1");
        check(b2.entryByIndex(1).tag() == PoolEntry.TAG_UTF8, "entryByIndex(1) on builder works");
    }

    static void testUtf8Overloads() {
        ConstantPoolBuilder b = ConstantPoolBuilder.of();
        Utf8Entry u1 = b.utf8Entry("Ljava/lang/String;");
        check(b.utf8Entry(ClassDesc.of("java.lang.String")) == u1, "utf8Entry(ClassDesc) dedups with String overload");
        Utf8Entry u2 = b.utf8Entry("(I)V");
        check(b.utf8Entry(MethodTypeDesc.ofDescriptor("(I)V")) == u2, "utf8Entry(MethodTypeDesc) dedups with String overload");
        check(b.utf8Entry(ClassDesc.of("java.lang.String")).stringValue().equals("Ljava/lang/String;"), "ClassDesc overload yields descriptor");
        check(b.utf8Entry(MethodTypeDesc.ofDescriptor("(I)V")).stringValue().equals("(I)V"), "MethodTypeDesc overload yields descriptor");
    }

    static void testCopyFromModel() throws Exception {
        byte[] self = ConstantPoolBuilderTest.class.getResourceAsStream("ConstantPoolBuilderTest.class").readAllBytes();
        ClassModel cm = ClassFile.of().parse(self);
        ConstantPool cp = cm.constantPool();
        ConstantPoolBuilder copy = ConstantPoolBuilder.of(cm);
        check(copy.size() == cp.size(), "copy has same size as source pool");
        boolean same = true;
        Iterator<PoolEntry> itSrc = cp.iterator(), itCpy = copy.iterator();
        int checked = 0;
        while (itSrc.hasNext() && itCpy.hasNext()) {
            PoolEntry a = itSrc.next(), c = itCpy.next();
            if (a.index() != c.index() || a.tag() != c.tag() || !a.toString().equals(c.toString())) { same = false; break; }
            checked++;
        }
        check(same && checked > 0, "copy preserves index/tag/value sequence (" + checked + " entries)");
        check(!itSrc.hasNext() && !itCpy.hasNext(), "copy iteration covers every entry");
        check(copy.canWriteDirect(cp), "copy canWriteDirect(source pool) == true");
        check(copy.canWriteDirect(copy), "copy canWriteDirect(itself) == true");
        // 拷贝可继续扩容（探测值运行期计算，避免与自身类池常量撞车去重）
        int before = copy.size();
        copy.intEntry(before * 1000003 + 7);
        check(copy.size() == before + 1, "copy still accepts new puts (+1 exactly)");
    }

    static void testCanWriteDirect() throws Exception {
        byte[] self = ConstantPoolBuilderTest.class.getResourceAsStream("ConstantPoolBuilderTest.class").readAllBytes();
        ClassModel cm = ClassFile.of().parse(self);
        ConstantPool cp = cm.constantPool();
        ConstantPoolBuilder fresh = ConstantPoolBuilder.of();
        check(fresh.canWriteDirect(fresh), "builder canWriteDirect(itself) == true");
        check(!fresh.canWriteDirect(cp), "fresh builder cannot write directly to a parsed pool");
        ConstantPoolBuilder other = ConstantPoolBuilder.of();
        check(!fresh.canWriteDirect(other), "unrelated builders cannot write directly");
        try {
            fresh.canWriteDirect(null);
            check(false, "canWriteDirect(null) must throw");
        } catch (NullPointerException e) { check(true, "canWriteDirect(null) NPE"); }
        // 再解析一个新池也不可直接写
        byte[] bytes = ClassFile.of().build(ClassDesc.of("pkg.OtherPool"), cb -> { });
        check(!fresh.canWriteDirect(ClassFile.of().parse(bytes).constantPool()), "different parsed pool not writable");
    }

    static void testDispatchFactories() {
        ConstantPoolBuilder b = ConstantPoolBuilder.of();
        check(b.constantValueEntry(7) == b.intEntry(7), "constantValueEntry(Integer) dispatches to intEntry (identity)");
        check(b.constantValueEntry("s") == b.stringEntry("s"), "constantValueEntry(String) dispatches to stringEntry");
        check(b.constantValueEntry(8L) == b.longEntry(8L), "constantValueEntry(Long) dispatches to longEntry");
        check(b.constantValueEntry(9.5f) == b.floatEntry(9.5f), "constantValueEntry(Float) dispatches to floatEntry");
        check(b.constantValueEntry(10.5d) == b.doubleEntry(10.5d), "constantValueEntry(Double) dispatches to doubleEntry");
        try {
            b.constantValueEntry(ClassDesc.of("java.lang.String"));
            check(false, "constantValueEntry(ClassDesc) must throw");
        } catch (IllegalArgumentException e) { check(e.getMessage() != null, "constantValueEntry(ClassDesc) IAE"); }
        try {
            b.constantValueEntry(MethodTypeDesc.ofDescriptor("()V"));
            check(false, "constantValueEntry(MethodTypeDesc) must throw");
        } catch (IllegalArgumentException e) { check(true, "constantValueEntry(MethodTypeDesc) IAE"); }
        check(b.loadableConstantEntry(MethodTypeDesc.ofDescriptor("()V")) == b.methodTypeEntry(MethodTypeDesc.ofDescriptor("()V")), "loadableConstantEntry(MethodTypeDesc) dispatches to methodTypeEntry");
        check(b.loadableConstantEntry(ClassDesc.of("q.D")) instanceof ClassEntry, "loadableConstantEntry(ClassDesc) dispatches to classEntry");
        check(b.loadableConstantEntry(2.5f) instanceof FloatEntry, "loadableConstantEntry(Float) dispatches to floatEntry");
        check(b.loadableConstantEntry(ConstantDescs.BSM_NULL_CONSTANT) instanceof MethodHandleEntry, "loadableConstantEntry(MH desc) dispatches to methodHandleEntry");
        // bsmEntry 双工厂
        BootstrapMethodEntry bsm = b.bsmEntry(ConstantDescs.BSM_NULL_CONSTANT, List.of(1));
        check(bsm.bootstrapMethod().reference().name().stringValue().equals("nullConstant"), "bsmEntry(desc,args) method");
        check(bsm.arguments().size() == 1, "bsmEntry(desc,args) arg count");
        check(b.bsmEntry(bsm.bootstrapMethod(), bsm.arguments()) == bsm, "bsmEntry dedup identity");
    }

    static void testNullNegativeMatrix() {
        ConstantPoolBuilder b = ConstantPoolBuilder.of();
        RunnableProbe[] probes = {
            new RunnableProbe(() -> b.utf8Entry((String) null)),
            new RunnableProbe(() -> b.utf8Entry((ClassDesc) null)),
            new RunnableProbe(() -> b.utf8Entry((MethodTypeDesc) null)),
            new RunnableProbe(() -> b.classEntry((ClassDesc) null)),
            new RunnableProbe(() -> b.classEntry((Utf8Entry) null)),
            new RunnableProbe(() -> b.stringEntry((String) null)),
            new RunnableProbe(() -> b.stringEntry((Utf8Entry) null)),
            new RunnableProbe(() -> b.nameAndTypeEntry((String) null, ClassDesc.of("I"))),
            new RunnableProbe(() -> b.methodRefEntry(null, "m", MethodTypeDesc.ofDescriptor("()V"))),
            new RunnableProbe(() -> b.fieldRefEntry(null, "f", ClassDesc.of("I"))),
            new RunnableProbe(() -> b.interfaceMethodRefEntry(null, "g", MethodTypeDesc.ofDescriptor("()V"))),
            new RunnableProbe(() -> b.methodHandleEntry(6, null)),
            new RunnableProbe(() -> b.methodHandleEntry((DirectMethodHandleDesc) null)),
            new RunnableProbe(() -> b.methodTypeEntry((MethodTypeDesc) null)),
            new RunnableProbe(() -> b.moduleEntry((ModuleDesc) null)),
            new RunnableProbe(() -> b.packageEntry((PackageDesc) null)),
            new RunnableProbe(() -> b.constantValueEntry(null)),
            new RunnableProbe(() -> b.loadableConstantEntry(null)),
            new RunnableProbe(() -> ConstantPoolBuilder.of(null)),
        };
        for (RunnableProbe p : probes) {
            try {
                p.r.run();
                check(false, "null put must throw");
            } catch (NullPointerException e) {
                check(true, "null put NPE");
            }
        }
    }

    static class RunnableProbe {
        final Runnable r;
        RunnableProbe(Runnable r) { this.r = r; }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testConstantDescsPreinit();
        testFreshSizeAndIncrements();
        testUtf8Overloads();
        testCopyFromModel();
        testCanWriteDirect();
        testDispatchFactories();
        testNullNegativeMatrix();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
