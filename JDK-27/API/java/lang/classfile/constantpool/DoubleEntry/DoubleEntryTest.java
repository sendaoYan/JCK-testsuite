/*
 * @test
 * @summary java.lang.classfile.constantpool.DoubleEntry conformance: white-box structure + black-box behavior.
 * @run main DoubleEntryTest
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

public class DoubleEntryTest {
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

    static final double CONST_DOUBLE = 1.25e300;

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.classfile.constantpool.DoubleEntry");
        check(c != null, "java.lang.classfile.constantpool.DoubleEntry loads");
        check(c.getName().equals("java.lang.classfile.constantpool.DoubleEntry"), "binary name java.lang.classfile.constantpool.DoubleEntry");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.constantpool.DoubleEntry is public");
        check(c.isInterface(), "java.lang.classfile.constantpool.DoubleEntry is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.constantpool.DoubleEntry has no superclass");
        check(declares(c, "doubleValue"), "declares doubleValue");
        check(declares(c, "typeKind"), "declares typeKind");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        ConstantPoolBuilder cpb = ConstantPoolBuilder.of();
        DoubleEntry de = cpb.doubleEntry(2.5);
        check(de.doubleValue() == 2.5, "doubleValue");
        check(de.constantValue().equals(2.5), "constantValue");
        check(de.typeKind() == TypeKind.DOUBLE, "typeKind DOUBLE");
        check(de.width() == 2, "width 2");

    }

    // ---- Part 3: AMHC 常量基础设施预初始化（JDK-27 实测：先完整初始化 ConstantDescs 可避开
    // ConstantDescs <-> PrimitiveClassDescImpl 类初始化环，见 DESIGN.md 易错细节） ----
    static void testConstantDescsPreinit() {
        check(ConstantDescs.CD_Object.descriptorString().equals("Ljava/lang/Object;"), "ConstantDescs.CD_Object preinit");
    }

    // ---- Part 4: DoubleEntry 契约（值边界/去重含位模式/双槽/ldc 发射闭环/真类池） ----
    static void testDoubleValuesAndFace() {
        ConstantPoolBuilder b = ConstantPoolBuilder.of();
        DoubleEntry d = b.doubleEntry(4.5d);
        check(d.doubleValue() == 4.5d, "doubleValue roundtrip");
        check(d.constantValue().equals(4.5d), "constantValue boxed Double");
        check(d.constantValue() instanceof Double, "constantValue type Double");
        check(d.typeKind() == TypeKind.DOUBLE, "typeKind DOUBLE");
        check(d.tag() == PoolEntry.TAG_DOUBLE && d.tag() == 6, "tag TAG_DOUBLE == 6");
        check(d.width() == 2, "double occupies two slots");
        check(d.index() > 0 && d.constantPool() == b, "pool face");
        check(d instanceof ConstantValueEntry && d instanceof AnnotationConstantValueEntry
                && d instanceof LoadableConstantEntry && d instanceof PoolEntry, "full hierarchy chain");
        check(b.doubleEntry(Double.NaN).doubleValue() != b.doubleEntry(Double.NaN).doubleValue(), "NaN doubleValue stays NaN (!=)");
        check(Double.isNaN(b.doubleEntry(Double.NaN).doubleValue()), "NaN roundtrip isNaN");
        check(b.doubleEntry(Double.POSITIVE_INFINITY).doubleValue() == Double.POSITIVE_INFINITY, "+Inf roundtrip");
        check(b.doubleEntry(Double.NEGATIVE_INFINITY).doubleValue() == Double.NEGATIVE_INFINITY, "-Inf roundtrip");
        check(b.doubleEntry(Double.MIN_VALUE).doubleValue() == Double.MIN_VALUE, "MIN_VALUE roundtrip");
        check(b.doubleEntry(Double.MAX_VALUE).doubleValue() == Double.MAX_VALUE, "MAX_VALUE roundtrip");
    }

    static void testDoubleDedupAndSlots() {
        ConstantPoolBuilder b = ConstantPoolBuilder.of();
        int s0 = b.size();
        DoubleEntry d1 = b.doubleEntry(1.5d);
        int s1 = b.size();
        check(s1 - s0 == 2, "double put grows size by 2 (double slot)");
        check(b.doubleEntry(1.5d) == d1, "same-value double dedup identity");
        check(b.size() == s1, "dedup re-put does not grow pool");
        // 位模式语义：-0.0 与 +0.0 是不同条目
        DoubleEntry nz = b.doubleEntry(-0.0d), pz = b.doubleEntry(0.0d);
        check(nz != pz && nz.index() != pz.index(), "-0.0 and +0.0 are distinct entries");
        check(Double.doubleToRawLongBits(nz.doubleValue()) == Double.doubleToRawLongBits(-0.0d), "-0.0 bits roundtrip");
        // NaN 字面量不去重（按值语义 NaN!=NaN，实测锁定）
        check(b.doubleEntry(Double.NaN) != b.doubleEntry(Double.NaN), "two NaN puts are distinct entries (实测)");
        // 双槽占位：紧跟 long/double 之后的 index 不可用
        ConstantPoolBuilder b2 = ConstantPoolBuilder.of();
        DoubleEntry first = b2.doubleEntry(9.5d);
        PoolEntry next = b2.utf8Entry("after");
        check(next.index() == first.index() + 2, "entry after double skips phantom slot");
        try {
            b2.entryByIndex(first.index() + 1);
            check(false, "phantom slot must throw");
        } catch (ConstantPoolException e) {
            check(e.getMessage() != null, "phantom slot ConstantPoolException");
        }
    }

    static void testDoubleEmissionAndRealPool() throws Exception {
        byte[] bytes = ClassFile.of().build(ClassDesc.of("pkg.EmitD"), cb -> cb
                .withMethodBody("m", MethodTypeDesc.ofDescriptor("()V"), ClassFile.ACC_STATIC,
                        cob -> cob.ldc(4.25d).pop2().return_()));
        ConstantPool cp2 = ClassFile.of().parse(bytes).constantPool();
        boolean saw = false;
        for (PoolEntry pe : cp2) if (pe instanceof DoubleEntry de && de.doubleValue() == 4.25d) {
            saw = true;
            try {
                cp2.entryByIndex(de.index() + 1);
                check(false, "parsed phantom slot must throw");
            } catch (ConstantPoolException e) { check(e.getMessage() != null, "parsed phantom slot CPE"); }
        }
        check(saw, "emitted ldc(double) leaves DoubleEntry with value roundtrip");
        byte[] self = DoubleEntryTest.class.getResourceAsStream("DoubleEntryTest.class").readAllBytes();
        ClassModel cm = ClassFile.of().parse(self);
        ConstantPool cp = cm.constantPool();
        boolean sawConst = false;
        for (PoolEntry pe : cp) if (pe instanceof DoubleEntry de && de.doubleValue() == CONST_DOUBLE) sawConst = true;
        check(sawConst, "real pool contains CONST_DOUBLE DoubleEntry");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testConstantDescsPreinit();
        testDoubleValuesAndFace();
        testDoubleDedupAndSlots();
        testDoubleEmissionAndRealPool();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
