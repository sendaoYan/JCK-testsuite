/*
 * @test
 * @summary java.lang.classfile.constantpool.ConstantValueEntry conformance: white-box structure + black-box behavior.
 * @run main ConstantValueEntryTest
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

public class ConstantValueEntryTest {
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

    static final int CONST_INT = 31337;
    static final long CONST_LONG = 0x123456789ABCDEFL;
    static final float CONST_FLOAT = -2.75f;
    static final double CONST_DOUBLE = 1.25e300;
    static final String CONST_STRING = "constStringValue7";

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.classfile.constantpool.ConstantValueEntry");
        check(c != null, "java.lang.classfile.constantpool.ConstantValueEntry loads");
        check(c.getName().equals("java.lang.classfile.constantpool.ConstantValueEntry"), "binary name java.lang.classfile.constantpool.ConstantValueEntry");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.constantpool.ConstantValueEntry is public");
        check(c.isInterface(), "java.lang.classfile.constantpool.ConstantValueEntry is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.constantpool.ConstantValueEntry has no superclass");
        check(declares(c, "constantValue"), "declares constantValue");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        ConstantPoolBuilder cpb = ConstantPoolBuilder.of();
        ConstantValueEntry cve = cpb.intEntry(9);
        check(cve.constantValue().equals(9), "int constantValue");
        ConstantValueEntry cve2 = cpb.stringEntry("x");
        check(cve2.constantValue().equals("x"), "string constantValue");

    }

    // ---- Part 3: AMHC 常量基础设施预初始化（JDK-27 实测：先完整初始化 ConstantDescs 可避开
    // ConstantDescs <-> PrimitiveClassDescImpl 类初始化环，见 DESIGN.md 易错细节） ----
    static void testConstantDescsPreinit() {
        check(ConstantDescs.CD_Object.descriptorString().equals("Ljava/lang/Object;"), "ConstantDescs.CD_Object preinit");
    }

    // ---- Part 4: ConstantValueEntry 契约（实现集/constantValue 全类型回读/分派工厂/IAE/真类池 ConstantValue 属性） ----
    static void testCvHierarchy() {
        ConstantPoolBuilder b = ConstantPoolBuilder.of();
        check(b.intEntry(1) instanceof ConstantValueEntry, "IntegerEntry is ConstantValueEntry");
        check(b.longEntry(1L) instanceof ConstantValueEntry, "LongEntry is ConstantValueEntry");
        check(b.floatEntry(1f) instanceof ConstantValueEntry, "FloatEntry is ConstantValueEntry");
        check(b.doubleEntry(1d) instanceof ConstantValueEntry, "DoubleEntry is ConstantValueEntry");
        check(b.stringEntry("s") instanceof ConstantValueEntry, "StringEntry is ConstantValueEntry");
        check(b.classEntry(ClassDesc.of("p.C")) instanceof LoadableConstantEntry
                && !(((Object) b.classEntry(ClassDesc.of("p.C"))) instanceof ConstantValueEntry), "ClassEntry is Loadable but NOT ConstantValue");
        MethodHandleEntry mh = b.methodHandleEntry(6, b.methodRefEntry(ClassDesc.of("java.lang.Object"), "toString", MethodTypeDesc.ofDescriptor("()Ljava/lang/String;")));
        check(!(((Object) mh) instanceof ConstantValueEntry), "MethodHandleEntry is NOT ConstantValueEntry");
        check(!(((Object) b.methodTypeEntry(MethodTypeDesc.ofDescriptor("()V"))) instanceof ConstantValueEntry), "MethodTypeEntry is NOT ConstantValueEntry");
        // constantValue 全类型精确回读
        check(b.intEntry(11).constantValue().equals(11), "int constantValue");
        check(b.longEntry(12L).constantValue().equals(12L), "long constantValue");
        check(b.floatEntry(13f).constantValue().equals(13f), "float constantValue");
        check(b.doubleEntry(14d).constantValue().equals(14d), "double constantValue");
        check(b.stringEntry("cv").constantValue().equals("cv"), "string constantValue");
        for (PoolEntry pe : new PoolEntry[]{b.intEntry(11), b.longEntry(12L), b.floatEntry(13f), b.doubleEntry(14d), b.stringEntry("cv")})
            check(pe instanceof LoadableConstantEntry, "ConstantValueEntry implies LoadableConstantEntry");
    }

    static void testCvDispatchFactories() {
        ConstantPoolBuilder b = ConstantPoolBuilder.of();
        ConstantValueEntry vi = b.constantValueEntry(21);
        check(vi instanceof IntegerEntry && vi == b.intEntry(21), "constantValueEntry(Integer) -> IntegerEntry identity");
        ConstantValueEntry vs = b.constantValueEntry("cs");
        check(vs instanceof StringEntry && vs == b.stringEntry("cs"), "constantValueEntry(String) -> StringEntry identity");
        ConstantValueEntry vl = b.constantValueEntry(22L);
        check(vl instanceof LongEntry && vl == b.longEntry(22L), "constantValueEntry(Long) -> LongEntry identity");
        ConstantValueEntry vf = b.constantValueEntry(23f);
        check(vf instanceof FloatEntry && vf == b.floatEntry(23f), "constantValueEntry(Float) -> FloatEntry identity");
        ConstantValueEntry vd = b.constantValueEntry(24d);
        check(vd instanceof DoubleEntry && vd == b.doubleEntry(24d), "constantValueEntry(Double) -> DoubleEntry identity");
        try {
            b.constantValueEntry(ClassDesc.of("p.C"));
            check(false, "constantValueEntry(ClassDesc) must throw");
        } catch (IllegalArgumentException e) {
            check(e.getMessage() != null && e.getMessage().contains("Illegal type"), "IAE Illegal type: " + e.getMessage());
        }
        try {
            b.constantValueEntry(null);
            check(false, "constantValueEntry(null) must throw");
        } catch (NullPointerException e) { check(true, "constantValueEntry(null) NPE"); }
    }

    static void testCvRealPoolAttributes() throws Exception {
        byte[] self = ConstantValueEntryTest.class.getResourceAsStream("ConstantValueEntryTest.class").readAllBytes();
        ClassModel cm = ClassFile.of().parse(self);
        ConstantPool cp = cm.constantPool();
        String[] names = {"CONST_INT", "CONST_LONG", "CONST_FLOAT", "CONST_DOUBLE", "CONST_STRING"};
        Object[] values = {CONST_INT, CONST_LONG, CONST_FLOAT, CONST_DOUBLE, CONST_STRING};
        boolean[] seen = new boolean[5];
        for (FieldModel fm : cm.fields()) {
            ConstantValueAttribute cva = fm.findAttribute(Attributes.constantValue()).orElse(null);
            if (cva == null) continue;
            ConstantValueEntry cv = cva.constant();
            check(cv instanceof ConstantValueEntry, "attribute constant is ConstantValueEntry");
            for (int i = 0; i < names.length; i++) if (fm.fieldName().equalsString(names[i])) {
                check(cv.constantValue().equals(values[i]), names[i] + " value via ConstantValue attribute");
                seen[i] = true;
            }
        }
        for (int i = 0; i < seen.length; i++) check(seen[i], names[i] + " field has ConstantValue entry");
        int cvCount = 0;
        for (PoolEntry pe : cp) if (pe instanceof ConstantValueEntry) cvCount++;
        check(cvCount >= 5, "real pool holds at least the five constant value entries");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testConstantDescsPreinit();
        testCvHierarchy();
        testCvDispatchFactories();
        testCvRealPoolAttributes();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
