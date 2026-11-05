/*
 * @test
 * @summary java.lang.classfile.constantpool.PoolEntry conformance: white-box structure + black-box behavior.
 * @run main PoolEntryTest
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

public class PoolEntryTest {
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

    static final long CONST_LONG = 0x123456789ABCDEFL;

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.classfile.constantpool.PoolEntry");
        check(c != null, "java.lang.classfile.constantpool.PoolEntry loads");
        check(c.getName().equals("java.lang.classfile.constantpool.PoolEntry"), "binary name java.lang.classfile.constantpool.PoolEntry");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.constantpool.PoolEntry is public");
        check(c.isInterface(), "java.lang.classfile.constantpool.PoolEntry is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.constantpool.PoolEntry has no superclass");
        check(declares(c, "constantPool"), "declares constantPool");
        check(declares(c, "tag"), "declares tag");
        check(declares(c, "index"), "declares index");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        ConstantPoolBuilder cpb = ConstantPoolBuilder.of();
        check(PoolEntry.TAG_UTF8 == 1 && PoolEntry.TAG_CLASS == 7 && PoolEntry.TAG_STRING == 8 && PoolEntry.TAG_INTEGER == 3 && PoolEntry.TAG_LONG == 5 && PoolEntry.TAG_DOUBLE == 6 && PoolEntry.TAG_FLOAT == 4, "TAG constants");
        PoolEntry u = cpb.utf8Entry("x");
        check(u.tag() == PoolEntry.TAG_UTF8, "utf8 tag");
        check(u.index() == 1, "index");
        check(u.width() == 1, "utf8 width 1");
        PoolEntry l = cpb.longEntry(5L);
        check(l.width() == 2, "long width 2");
        check(u.constantPool() != null, "constantPool backref");

    }

    // ---- Part 3: AMHC 常量基础设施预初始化（JDK-27 实测：先完整初始化 ConstantDescs 可避开
    // ConstantDescs <-> PrimitiveClassDescImpl 类初始化环，见 DESIGN.md 易错细节） ----
    static void testConstantDescsPreinit() {
        check(ConstantDescs.CD_Object.descriptorString().equals("Ljava/lang/Object;"), "ConstantDescs.CD_Object preinit");
    }

    // ---- Part 4: PoolEntry 契约（TAG 常量全表/width 表/index 布局/通用面/toString/真类池一致性） ----
    static void testTagConstantTable() {
        check(PoolEntry.TAG_UTF8 == 1, "TAG_UTF8 == 1");
        check(PoolEntry.TAG_INTEGER == 3, "TAG_INTEGER == 3");
        check(PoolEntry.TAG_FLOAT == 4, "TAG_FLOAT == 4");
        check(PoolEntry.TAG_LONG == 5, "TAG_LONG == 5");
        check(PoolEntry.TAG_DOUBLE == 6, "TAG_DOUBLE == 6");
        check(PoolEntry.TAG_CLASS == 7, "TAG_CLASS == 7");
        check(PoolEntry.TAG_STRING == 8, "TAG_STRING == 8");
        check(PoolEntry.TAG_FIELDREF == 9, "TAG_FIELDREF == 9");
        check(PoolEntry.TAG_METHODREF == 10, "TAG_METHODREF == 10");
        check(PoolEntry.TAG_INTERFACE_METHODREF == 11, "TAG_INTERFACE_METHODREF == 11");
        check(PoolEntry.TAG_NAME_AND_TYPE == 12, "TAG_NAME_AND_TYPE == 12");
        check(PoolEntry.TAG_METHOD_HANDLE == 15, "TAG_METHOD_HANDLE == 15");
        check(PoolEntry.TAG_METHOD_TYPE == 16, "TAG_METHOD_TYPE == 16");
        check(PoolEntry.TAG_DYNAMIC == 17, "TAG_DYNAMIC == 17");
        check(PoolEntry.TAG_INVOKE_DYNAMIC == 18, "TAG_INVOKE_DYNAMIC == 18");
        check(PoolEntry.TAG_MODULE == 19, "TAG_MODULE == 19");
        check(PoolEntry.TAG_PACKAGE == 20, "TAG_PACKAGE == 20");
        // 无 tag 0/2/13/14（历史保留：CONSTANT_Unicode/unused）
        java.util.Set<Integer> tags = new java.util.TreeSet<>();
        for (java.lang.reflect.Field f : PoolEntry.class.getFields())
            if (f.getName().startsWith("TAG_")) try { tags.add(f.getInt(null)); } catch (ReflectiveOperationException e) { }
        check(tags.equals(new java.util.TreeSet<>(java.util.Arrays.asList(1, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 15, 16, 17, 18, 19, 20))), "17 tag constants, no gaps except JVMS-reserved 2/13/14: " + tags);
    }

    static void testKindFaceTable() {
        ConstantPoolBuilder b = ConstantPoolBuilder.of();
        MethodRefEntry mre = b.methodRefEntry(ClassDesc.of("java.lang.Object"), "toString", MethodTypeDesc.ofDescriptor("()Ljava/lang/String;"));
        MethodHandleEntry mh = b.methodHandleEntry(6, mre);
        BootstrapMethodEntry bsm = b.bsmEntry(mh, List.of());
        PoolEntry[] single = {
                b.utf8Entry("u"), b.intEntry(1), b.floatEntry(1f),
                b.classEntry(ClassDesc.of("p.C")), b.stringEntry("s"),
                b.nameAndTypeEntry("n", ClassDesc.of("I")),
                b.fieldRefEntry(ClassDesc.of("p.C"), "f", ClassDesc.of("I")), mre,
                b.interfaceMethodRefEntry(ClassDesc.of("p.I"), "g", MethodTypeDesc.ofDescriptor("()V")),
                mh, b.methodTypeEntry(MethodTypeDesc.ofDescriptor("()V")),
                b.constantDynamicEntry(bsm, b.nameAndTypeEntry("c", ClassDesc.of("java.lang.Object"))),
                b.invokeDynamicEntry(bsm, b.nameAndTypeEntry("i", MethodTypeDesc.ofDescriptor("()V"))),
                b.moduleEntry(ModuleDesc.of("m")), b.packageEntry(PackageDesc.of("p")),
        };
        java.util.Set<Integer> indices = new java.util.HashSet<>();
        for (PoolEntry pe : single) {
            check(pe.width() == 1, "single-slot kind " + pe.tag());
            check(pe.index() >= 1, "positive index");
            check(indices.add(pe.index()), "single-slot indices unique: tag " + pe.tag());
            check(pe.constantPool() == b, "constantPool() is the owning builder");
        }
        check(b.longEntry(2L).width() == 2, "long double-slot");
        check(b.doubleEntry(2d).width() == 2, "double double-slot");
        // 双槽条目使下一 index 跳一位
        ConstantPoolBuilder b2 = ConstantPoolBuilder.of();
        LongEntry l = b2.longEntry(9L);
        check(b2.doubleEntry(9d).index() == l.index() + 2, "entry after long skips phantom");
        check(b2.utf8Entry("z").index() == l.index() + 4, "entry after double skips phantom");
    }

    static void testPoolEntryToStringAndRealPool() throws Exception {
        byte[] self = PoolEntryTest.class.getResourceAsStream("PoolEntryTest.class").readAllBytes();
        ClassModel cm = ClassFile.of().parse(self);
        ConstantPool cp = cm.constantPool();
        // toString 含 index 与内容（实测形如 "10 java/lang/Object.<init>:()V"）
        boolean toStringOk = false;
        for (PoolEntry pe : cp) if (pe.toString().contains(String.valueOf(pe.index()))) { toStringOk = true; break; }
        check(toStringOk, "toString mentions index");
        // 通用面：每一条目 tag 与 tag 常量集合吻合、entryByIndex 反查同一
        java.util.Set<Integer> validTags = new java.util.HashSet<>(java.util.Arrays.asList(1, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 15, 16, 17, 18, 19, 20));
        boolean allValid = true;
        for (PoolEntry pe : cp) {
            if (!validTags.contains(pe.tag())) allValid = false;
            if (cp.entryByIndex(pe.index()) != pe) allValid = false;
            if (pe.constantPool() != cp) allValid = false;
            if (pe.width() != ((pe.tag() == PoolEntry.TAG_LONG || pe.tag() == PoolEntry.TAG_DOUBLE) ? 2 : 1)) allValid = false;
        }
        check(allValid, "every real entry: valid tag + identity + pool + width law");
        // 池大小 = 最大 index + 1；双槽造成 size > 迭代数 + 1
        int iterCount = 0, maxIdx = 0;
        for (PoolEntry pe : cp) { iterCount++; if (pe.index() > maxIdx) maxIdx = pe.index(); }
        check(cp.size() == maxIdx + 1, "size() == maxIndex + 1");
        check(iterCount < cp.size() - 1 || cp.size() - iterCount == 1, "phantom slots explain size-iteration gap (CONST_LONG)");
        // 越界：ConstantPoolException（本接口面统一入口）
        try {
            cp.entryByIndex(cp.size());
            check(false, "oob must throw");
        } catch (ConstantPoolException e) {
            check(e.getMessage() != null, "oob message non-null");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testConstantDescsPreinit();
        testTagConstantTable();
        testKindFaceTable();
        testPoolEntryToStringAndRealPool();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
