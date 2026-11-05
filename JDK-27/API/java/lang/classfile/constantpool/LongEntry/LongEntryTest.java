/*
 * @test
 * @summary java.lang.classfile.constantpool.LongEntry conformance: white-box structure + black-box behavior.
 * @run main LongEntryTest
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

public class LongEntryTest {
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
        Class<?> c = Class.forName("java.lang.classfile.constantpool.LongEntry");
        check(c != null, "java.lang.classfile.constantpool.LongEntry loads");
        check(c.getName().equals("java.lang.classfile.constantpool.LongEntry"), "binary name java.lang.classfile.constantpool.LongEntry");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.constantpool.LongEntry is public");
        check(c.isInterface(), "java.lang.classfile.constantpool.LongEntry is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.constantpool.LongEntry has no superclass");
        check(declares(c, "longValue"), "declares longValue");
        check(declares(c, "typeKind"), "declares typeKind");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        ConstantPoolBuilder cpb = ConstantPoolBuilder.of();
        LongEntry le = cpb.longEntry(Long.MAX_VALUE);
        check(le.longValue() == Long.MAX_VALUE, "longValue");
        check(le.constantValue().equals(Long.MAX_VALUE), "constantValue");
        check(le.typeKind() == TypeKind.LONG, "typeKind LONG");
        check(le.width() == 2, "width 2");

    }

    // ---- Part 3: AMHC 常量基础设施预初始化（JDK-27 实测：先完整初始化 ConstantDescs 可避开
    // ConstantDescs <-> PrimitiveClassDescImpl 类初始化环，见 DESIGN.md 易错细节） ----
    static void testConstantDescsPreinit() {
        check(ConstantDescs.CD_Object.descriptorString().equals("Ljava/lang/Object;"), "ConstantDescs.CD_Object preinit");
    }

    // ---- Part 4: LongEntry 契约（值边界/去重/双槽/ldc 发射闭环/真类池） ----
    static void testLongValuesAndFace() {
        ConstantPoolBuilder b = ConstantPoolBuilder.of();
        LongEntry l = b.longEntry(4294967296L);
        check(l.longValue() == 4294967296L, "longValue roundtrip");
        check(l.constantValue().equals(4294967296L), "constantValue boxed Long");
        check(l.constantValue() instanceof Long, "constantValue type Long");
        check(l.typeKind() == TypeKind.LONG, "typeKind LONG");
        check(l.tag() == PoolEntry.TAG_LONG && l.tag() == 5, "tag TAG_LONG == 5");
        check(l.width() == 2, "long occupies two slots");
        check(l.index() > 0 && l.constantPool() == b, "pool face");
        check(l instanceof ConstantValueEntry && l instanceof AnnotationConstantValueEntry
                && l instanceof LoadableConstantEntry && l instanceof PoolEntry, "full hierarchy chain");
        check(b.longEntry(Long.MIN_VALUE).longValue() == Long.MIN_VALUE, "MIN_VALUE roundtrip");
        check(b.longEntry(Long.MAX_VALUE).longValue() == Long.MAX_VALUE, "MAX_VALUE roundtrip");
        check(b.longEntry(0L).longValue() == 0L, "zero roundtrip");
        check(b.longEntry(-1L).longValue() == -1L, "negative roundtrip");
    }

    static void testLongDedupAndSlots() {
        ConstantPoolBuilder b = ConstantPoolBuilder.of();
        int s0 = b.size();
        LongEntry l1 = b.longEntry(111111111111L);
        int s1 = b.size();
        check(s1 - s0 == 2, "long put grows size by 2 (double slot)");
        check(b.longEntry(111111111111L) == l1, "same-value dedup identity");
        check(b.size() == s1, "dedup re-put does not grow pool");
        check(b.longEntry(222222222222L) != l1, "different value different entry");
        check(b.longEntry(Long.MIN_VALUE) == b.longEntry(Long.MIN_VALUE), "MIN_VALUE dedup identity");
        // 双槽占位
        ConstantPoolBuilder b2 = ConstantPoolBuilder.of();
        LongEntry first = b2.longEntry(1L);
        PoolEntry next = b2.utf8Entry("after");
        check(next.index() == first.index() + 2, "entry after long skips phantom slot");
        try {
            b2.entryByIndex(first.index() + 1);
            check(false, "phantom slot must throw");
        } catch (ConstantPoolException e) {
            check(e.getMessage() != null, "phantom slot ConstantPoolException");
        }
    }

    static void testLongEmissionAndRealPool() throws Exception {
        byte[] bytes = ClassFile.of().build(ClassDesc.of("pkg.EmitL"), cb -> cb
                .withMethodBody("m", MethodTypeDesc.ofDescriptor("()V"), ClassFile.ACC_STATIC,
                        cob -> cob.ldc(4294967296L).pop2().return_()));
        ConstantPool cp2 = ClassFile.of().parse(bytes).constantPool();
        boolean saw = false;
        for (PoolEntry pe : cp2) if (pe instanceof LongEntry le && le.longValue() == 4294967296L) {
            saw = true;
            check(le.width() == 2, "parsed long width 2");
        }
        check(saw, "emitted ldc(long) leaves LongEntry with value roundtrip");
        byte[] self = LongEntryTest.class.getResourceAsStream("LongEntryTest.class").readAllBytes();
        ClassModel cm = ClassFile.of().parse(self);
        ConstantPool cp = cm.constantPool();
        boolean sawConst = false;
        for (PoolEntry pe : cp) if (pe instanceof LongEntry le && le.longValue() == CONST_LONG) sawConst = true;
        check(sawConst, "real pool contains CONST_LONG LongEntry");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testConstantDescsPreinit();
        testLongValuesAndFace();
        testLongDedupAndSlots();
        testLongEmissionAndRealPool();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
