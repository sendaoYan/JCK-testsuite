/*
 * @test
 * @summary java.lang.classfile.constantpool.FloatEntry conformance: white-box structure + black-box behavior.
 * @run main FloatEntryTest
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

public class FloatEntryTest {
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

    static final float CONST_FLOAT = -2.75f;

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.classfile.constantpool.FloatEntry");
        check(c != null, "java.lang.classfile.constantpool.FloatEntry loads");
        check(c.getName().equals("java.lang.classfile.constantpool.FloatEntry"), "binary name java.lang.classfile.constantpool.FloatEntry");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.constantpool.FloatEntry is public");
        check(c.isInterface(), "java.lang.classfile.constantpool.FloatEntry is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.constantpool.FloatEntry has no superclass");
        check(declares(c, "floatValue"), "declares floatValue");
        check(declares(c, "typeKind"), "declares typeKind");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        ConstantPoolBuilder cpb = ConstantPoolBuilder.of();
        FloatEntry fe = cpb.floatEntry(1.5f);
        check(fe.floatValue() == 1.5f, "floatValue");
        check(fe.constantValue().equals(1.5f), "constantValue");
        check(fe.typeKind() == TypeKind.FLOAT, "typeKind FLOAT");

    }

    // ---- Part 3: AMHC 常量基础设施预初始化（JDK-27 实测：先完整初始化 ConstantDescs 可避开
    // ConstantDescs <-> PrimitiveClassDescImpl 类初始化环，见 DESIGN.md 易错细节） ----
    static void testConstantDescsPreinit() {
        check(ConstantDescs.CD_Object.descriptorString().equals("Ljava/lang/Object;"), "ConstantDescs.CD_Object preinit");
    }

    // ---- Part 4: FloatEntry 契约（值边界/去重含位模式/单槽/ldc 发射闭环/真类池） ----
    static void testFloatValuesAndFace() {
        ConstantPoolBuilder b = ConstantPoolBuilder.of();
        FloatEntry f = b.floatEntry(3.5f);
        check(f.floatValue() == 3.5f, "floatValue roundtrip");
        check(f.constantValue().equals(3.5f), "constantValue boxed Float");
        check(f.constantValue() instanceof Float, "constantValue type Float");
        check(f.typeKind() == TypeKind.FLOAT, "typeKind FLOAT");
        check(f.tag() == PoolEntry.TAG_FLOAT && f.tag() == 4, "tag TAG_FLOAT == 4");
        check(f.width() == 1, "single-slot entry");
        check(f.index() > 0 && f.constantPool() == b, "pool face");
        check(f instanceof ConstantValueEntry && f instanceof AnnotationConstantValueEntry
                && f instanceof LoadableConstantEntry && f instanceof PoolEntry, "full hierarchy chain");
        check(Float.isNaN(b.floatEntry(Float.NaN).floatValue()), "NaN roundtrip isNaN");
        check(b.floatEntry(Float.POSITIVE_INFINITY).floatValue() == Float.POSITIVE_INFINITY, "+Inf roundtrip");
        check(b.floatEntry(Float.MIN_VALUE).floatValue() == Float.MIN_VALUE, "MIN_VALUE roundtrip");
        check(b.floatEntry(Float.MAX_VALUE).floatValue() == Float.MAX_VALUE, "MAX_VALUE roundtrip");
        check(b.floatEntry(-0.0f).floatValue() == 0.0f, "-0.0f == 0.0f numerically");
    }

    static void testFloatDedupAndSlots() {
        ConstantPoolBuilder b = ConstantPoolBuilder.of();
        int s0 = b.size();
        FloatEntry f1 = b.floatEntry(1.25f);
        int s1 = b.size();
        check(s1 - s0 == 1, "float put grows size by 1");
        check(b.floatEntry(1.25f) == f1, "same-value float dedup identity");
        check(b.size() == s1, "dedup re-put does not grow pool");
        check(b.floatEntry(-0.0f) != b.floatEntry(0.0f), "-0.0f and +0.0f are distinct entries (bit semantics)");
        check(Float.floatToRawIntBits(b.floatEntry(-0.0f).floatValue()) == Float.floatToRawIntBits(-0.0f), "-0.0f bits roundtrip");
        check(b.floatEntry(Float.NaN) != b.floatEntry(Float.NaN), "two NaN puts are distinct entries (实测)");
        check(b.floatEntry(Float.MIN_NORMAL) == b.floatEntry(Float.MIN_NORMAL), "MIN_NORMAL dedup");
    }

    static void testFloatEmissionAndRealPool() throws Exception {
        byte[] bytes = ClassFile.of().build(ClassDesc.of("pkg.EmitF"), cb -> cb
                .withMethodBody("m", MethodTypeDesc.ofDescriptor("()V"), ClassFile.ACC_STATIC,
                        cob -> cob.ldc(2.25f).pop().return_()));
        ConstantPool cp2 = ClassFile.of().parse(bytes).constantPool();
        boolean saw = false;
        for (PoolEntry pe : cp2) if (pe instanceof FloatEntry fe && fe.floatValue() == 2.25f) saw = true;
        check(saw, "emitted ldc(float) leaves FloatEntry with value roundtrip");
        byte[] self = FloatEntryTest.class.getResourceAsStream("FloatEntryTest.class").readAllBytes();
        ClassModel cm = ClassFile.of().parse(self);
        ConstantPool cp = cm.constantPool();
        boolean sawConst = false;
        for (PoolEntry pe : cp) if (pe instanceof FloatEntry fe && fe.floatValue() == CONST_FLOAT) sawConst = true;
        check(sawConst, "real pool contains CONST_FLOAT FloatEntry");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testConstantDescsPreinit();
        testFloatValuesAndFace();
        testFloatDedupAndSlots();
        testFloatEmissionAndRealPool();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
