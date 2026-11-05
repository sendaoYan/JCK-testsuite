/*
 * @test
 * @summary java.lang.classfile.constantpool.IntegerEntry conformance: white-box structure + black-box behavior.
 * @run main IntegerEntryTest
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

public class IntegerEntryTest {
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

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.classfile.constantpool.IntegerEntry");
        check(c != null, "java.lang.classfile.constantpool.IntegerEntry loads");
        check(c.getName().equals("java.lang.classfile.constantpool.IntegerEntry"), "binary name java.lang.classfile.constantpool.IntegerEntry");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.constantpool.IntegerEntry is public");
        check(c.isInterface(), "java.lang.classfile.constantpool.IntegerEntry is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.constantpool.IntegerEntry has no superclass");
        check(declares(c, "intValue"), "declares intValue");
        check(declares(c, "typeKind"), "declares typeKind");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        ConstantPoolBuilder cpb = ConstantPoolBuilder.of();
        IntegerEntry ie = cpb.intEntry(123456);
        check(ie.intValue() == 123456, "intValue");
        check(ie.constantValue().equals(123456), "constantValue");
        check(ie.typeKind() == TypeKind.INT, "typeKind INT");

    }

    // ---- Part 3: AMHC 常量基础设施预初始化（JDK-27 实测：先完整初始化 ConstantDescs 可避开
    // ConstantDescs <-> PrimitiveClassDescImpl 类初始化环，见 DESIGN.md 易错细节） ----
    static void testConstantDescsPreinit() {
        check(ConstantDescs.CD_Object.descriptorString().equals("Ljava/lang/Object;"), "ConstantDescs.CD_Object preinit");
    }

    // ---- Part 4: IntegerEntry 契约（值边界/去重/单槽/ldc 发射闭环/真类池） ----
    static void testIntValuesAndFace() {
        ConstantPoolBuilder b = ConstantPoolBuilder.of();
        IntegerEntry i = b.intEntry(31337);
        check(i.intValue() == 31337, "intValue roundtrip");
        check(i.constantValue().equals(31337), "constantValue boxed Integer");
        check(i.constantValue() instanceof Integer, "constantValue type Integer");
        check(i.typeKind() == TypeKind.INT, "typeKind INT");
        check(i.tag() == PoolEntry.TAG_INTEGER && i.tag() == 3, "tag TAG_INTEGER == 3");
        check(i.width() == 1, "single-slot entry");
        check(i.index() > 0 && i.constantPool() == b, "pool face");
        check(i instanceof ConstantValueEntry && i instanceof AnnotationConstantValueEntry
                && i instanceof LoadableConstantEntry && i instanceof PoolEntry, "full hierarchy chain");
        check(b.intEntry(Integer.MIN_VALUE).intValue() == Integer.MIN_VALUE, "MIN_VALUE roundtrip");
        check(b.intEntry(Integer.MAX_VALUE).intValue() == Integer.MAX_VALUE, "MAX_VALUE roundtrip");
        check(b.intEntry(0).intValue() == 0, "zero roundtrip");
        check(b.intEntry(-1).intValue() == -1, "negative roundtrip");
    }

    static void testIntDedupAndSize() {
        ConstantPoolBuilder b = ConstantPoolBuilder.of();
        int s0 = b.size();
        IntegerEntry i1 = b.intEntry(77);
        int s1 = b.size();
        check(s1 - s0 == 1, "int put grows size by 1");
        check(b.intEntry(77) == i1, "same-value dedup identity");
        check(b.intEntry(77).index() == i1.index(), "dedup same index");
        check(b.size() == s1, "dedup re-put does not grow pool");
        check(b.intEntry(78) != i1 && b.intEntry(78).index() != i1.index(), "different value different entry");
        check(b.intEntry(Integer.MIN_VALUE) == b.intEntry(Integer.MIN_VALUE), "MIN_VALUE dedup (long-style bit identity)");
    }

    static void testIntEmissionAndRealPool() throws Exception {
        byte[] bytes = ClassFile.of().build(ClassDesc.of("pkg.EmitI"), cb -> cb
                .withMethodBody("m", MethodTypeDesc.ofDescriptor("()V"), ClassFile.ACC_STATIC,
                        cob -> cob.ldc(987654).pop().return_()));
        ConstantPool cp2 = ClassFile.of().parse(bytes).constantPool();
        boolean saw = false;
        for (PoolEntry pe : cp2) if (pe instanceof IntegerEntry ie && ie.intValue() == 987654) saw = true;
        check(saw, "emitted ldc(int) leaves IntegerEntry with value roundtrip");
        // 经 ConstantValue 属性的闭环（javac 产物）
        byte[] self = IntegerEntryTest.class.getResourceAsStream("IntegerEntryTest.class").readAllBytes();
        ClassModel cm = ClassFile.of().parse(self);
        ConstantPool cp = cm.constantPool();
        boolean sawConst = false, sawViaAttr = false;
        for (PoolEntry pe : cp) if (pe instanceof IntegerEntry ie && ie.intValue() == CONST_INT) sawConst = true;
        for (FieldModel fm : cm.fields()) if (fm.fieldName().equalsString("CONST_INT")) {
            ConstantValueAttribute cva = fm.findAttribute(Attributes.constantValue()).orElse(null);
            if (cva != null && cva.constant() instanceof IntegerEntry ie && ie.intValue() == CONST_INT) sawViaAttr = true;
        }
        check(sawConst, "real pool contains CONST_INT IntegerEntry");
        check(sawViaAttr, "ConstantValue attribute exposes same IntegerEntry");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testConstantDescsPreinit();
        testIntValuesAndFace();
        testIntDedupAndSize();
        testIntEmissionAndRealPool();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
