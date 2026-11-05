/*
 * @test
 * @summary java.lang.classfile.constantpool.AnnotationConstantValueEntry conformance: white-box structure + black-box behavior.
 * @run main AnnotationConstantValueEntryTest
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

public class AnnotationConstantValueEntryTest {
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
    static final java.lang.Runnable LAMBDA_R = () -> {};
    private static void touchLambda() { LAMBDA_R.run(); }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.classfile.constantpool.AnnotationConstantValueEntry");
        check(c != null, "java.lang.classfile.constantpool.AnnotationConstantValueEntry loads");
        check(c.getName().equals("java.lang.classfile.constantpool.AnnotationConstantValueEntry"), "binary name java.lang.classfile.constantpool.AnnotationConstantValueEntry");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.constantpool.AnnotationConstantValueEntry is public");
        check(c.isInterface(), "java.lang.classfile.constantpool.AnnotationConstantValueEntry is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.constantpool.AnnotationConstantValueEntry has no superclass");
        check(declares(c, "constantValue"), "declares constantValue");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        ConstantPoolBuilder cpb = ConstantPoolBuilder.of();
        AnnotationConstantValueEntry e1 = cpb.utf8Entry("abc");
        check(e1.constantValue().equals("abc"), "utf8 constantValue");
        AnnotationConstantValueEntry e2 = cpb.intEntry(5);
        check(e2.constantValue().equals(5), "int constantValue");
        AnnotationConstantValueEntry e3 = cpb.doubleEntry(1.0);
        check(e3.constantValue().equals(1.0), "double constantValue");

    }

    // ---- Part 3: AMHC 常量基础设施预初始化（JDK-27 实测：先完整初始化 ConstantDescs 可避开
    // ConstantDescs <-> PrimitiveClassDescImpl 类初始化环，见 DESIGN.md 易错细节） ----
    static void testConstantDescsPreinit() {
        check(ConstantDescs.CD_Object.descriptorString().equals("Ljava/lang/Object;"), "ConstantDescs.CD_Object preinit");
    }

    // ---- Part 4: 注解常量值条目契约（层次面 + constantValue 分派 + 真类池实证） ----
    static void testAcveHierarchyAndValues() {
        ConstantPoolBuilder b = ConstantPoolBuilder.of();
        check(b.utf8Entry("uv") instanceof AnnotationConstantValueEntry, "Utf8Entry is AnnotationConstantValueEntry");
        check(b.intEntry(1) instanceof AnnotationConstantValueEntry, "IntegerEntry is AnnotationConstantValueEntry");
        check(b.longEntry(2L) instanceof AnnotationConstantValueEntry, "LongEntry is AnnotationConstantValueEntry");
        check(b.floatEntry(3f) instanceof AnnotationConstantValueEntry, "FloatEntry is AnnotationConstantValueEntry");
        check(b.doubleEntry(4d) instanceof AnnotationConstantValueEntry, "DoubleEntry is AnnotationConstantValueEntry");
        check(!(((Object) b.stringEntry("s")) instanceof AnnotationConstantValueEntry), "StringEntry is NOT AnnotationConstantValueEntry");
        check(!(((Object) b.classEntry(ClassDesc.of("p.C"))) instanceof AnnotationConstantValueEntry), "ClassEntry is NOT AnnotationConstantValueEntry");
        check(!(((Object) b.nameAndTypeEntry("n", ClassDesc.of("I"))) instanceof AnnotationConstantValueEntry), "NameAndTypeEntry is NOT AnnotationConstantValueEntry");
        check(!(((Object) b.methodRefEntry(ClassDesc.of("p.C"), "m", MethodTypeDesc.ofDescriptor("()V"))) instanceof AnnotationConstantValueEntry), "MemberRef is NOT AnnotationConstantValueEntry");
        check(b.utf8Entry("uv").constantValue().equals("uv"), "utf8 ACVE constantValue is its text");
        check(b.utf8Entry("uv").constantValue() instanceof String, "utf8 ACVE constantValue type String");
        check(b.intEntry(41).constantValue().equals(41), "int ACVE constantValue boxed Integer");
        check(b.intEntry(41).constantValue() instanceof Integer, "int ACVE constantValue type Integer");
        check(b.longEntry(42L).constantValue().equals(42L), "long ACVE constantValue boxed Long");
        check(b.floatEntry(2.5f).constantValue().equals(2.5f), "float ACVE constantValue boxed Float");
        check(b.doubleEntry(3.5d).constantValue().equals(3.5d), "double ACVE constantValue boxed Double");
        // 通用面：tag/width/constantPool 与 PoolEntry 契约
        PoolEntry u = b.utf8Entry("uv");
        check(u.tag() == PoolEntry.TAG_UTF8 && u.width() == 1 && u.index() > 0 && u.constantPool() == b, "utf8 pool face");
        check(b.intEntry(41).tag() == PoolEntry.TAG_INTEGER && b.intEntry(41).width() == 1, "int pool face");
        // 去重契约
        check(b.utf8Entry("uv") == u, "utf8 dedup same instance");
        check(b.intEntry(41) == b.intEntry(41), "int dedup same instance");
        check(b.intEntry(41).index() == b.intEntry(41).index(), "int dedup same index");
    }

    static void testAcveRealPool() throws Exception {
        byte[] self = AnnotationConstantValueEntryTest.class.getResourceAsStream("AnnotationConstantValueEntryTest.class").readAllBytes();
        ClassModel cm = ClassFile.of().parse(self);
        ConstantPool cp = cm.constantPool();
        int n = 0; boolean sawInt = false, sawUtf8 = false, sawDouble = false;
        for (PoolEntry pe : cp) if (pe instanceof AnnotationConstantValueEntry a) {
            n++;
            if (a instanceof IntegerEntry ie && ie.intValue() == CONST_INT) sawInt = true;
            if (a instanceof Utf8Entry) sawUtf8 = true;
            if (a instanceof DoubleEntry de && de.doubleValue() == CONST_DOUBLE) sawDouble = true;
        }
        check(n > 0, "real pool contains AnnotationConstantValueEntry entries");
        check(sawUtf8, "real pool ACVE includes Utf8 entries");
        check(sawInt, "real pool ACVE includes CONST_INT IntegerEntry");
        check(sawDouble, "real pool ACVE includes CONST_DOUBLE DoubleEntry");
        // 经 ConstantValue 属性（static final 字段）拿到的条目走 ACVE 面
        boolean viaAttr = false;
        for (FieldModel fm : cm.fields()) if (fm.fieldName().equalsString("CONST_INT")) {
            ConstantValueAttribute cva = fm.findAttribute(Attributes.constantValue()).orElse(null);
            if (cva != null && cva.constant() instanceof AnnotationConstantValueEntry a
                    && a.constantValue().equals(CONST_INT)) viaAttr = true;
        }
        check(viaAttr, "ConstantValue attribute entry readable through ACVE face");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testConstantDescsPreinit();
        testAcveHierarchyAndValues();
        testAcveRealPool();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
