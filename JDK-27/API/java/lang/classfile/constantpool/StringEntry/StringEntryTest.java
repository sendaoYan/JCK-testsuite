/*
 * @test
 * @summary java.lang.classfile.constantpool.StringEntry conformance: white-box structure + black-box behavior.
 * @run main StringEntryTest
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

public class StringEntryTest {
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

    static final String CONST_STRING = "constStringValue7";

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.classfile.constantpool.StringEntry");
        check(c != null, "java.lang.classfile.constantpool.StringEntry loads");
        check(c.getName().equals("java.lang.classfile.constantpool.StringEntry"), "binary name java.lang.classfile.constantpool.StringEntry");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.constantpool.StringEntry is public");
        check(c.isInterface(), "java.lang.classfile.constantpool.StringEntry is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.constantpool.StringEntry has no superclass");
        check(declares(c, "utf8"), "declares utf8");
        check(declares(c, "stringValue"), "declares stringValue");
        check(declares(c, "equalsString"), "declares equalsString");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        ConstantPoolBuilder cpb = ConstantPoolBuilder.of();
        StringEntry se = cpb.stringEntry("abc");
        check(se.stringValue().equals("abc"), "stringValue");
        check(se.equalsString("abc") && !se.equalsString("z"), "equalsString");
        check(se.utf8().stringValue().equals("abc"), "utf8");
        check(se.constantValue().equals("abc"), "constantValue");

    }

    // ---- Part 3: AMHC 常量基础设施预初始化（JDK-27 实测：先完整初始化 ConstantDescs 可避开
    // ConstantDescs <-> PrimitiveClassDescImpl 类初始化环，见 DESIGN.md 易错细节） ----
    static void testConstantDescsPreinit() {
        check(ConstantDescs.CD_Object.descriptorString().equals("Ljava/lang/Object;"), "ConstantDescs.CD_Object preinit");
    }

    // ---- Part 4: StringEntry 契约（双工厂/跨重载去重/utf8 身份/ldc 发射闭环/真类池） ----
    static void testStringPutDedup() {
        ConstantPoolBuilder b = ConstantPoolBuilder.of();
        Utf8Entry u = b.utf8Entry("sv");
        StringEntry s1 = b.stringEntry("sv");
        StringEntry s2 = b.stringEntry(u);
        check(s1 == s2, "cross-overload dedup: stringEntry(String) == stringEntry(Utf8Entry)");
        check(s1.utf8() == u, "utf8() is the shared utf8 entry (identity)");
        check(b.stringEntry("sv") == s1, "same-value dedup identity");
        check(b.stringEntry("sv").index() == s1.index(), "dedup same index");
        check(b.stringEntry("other") != s1 && b.stringEntry("other").index() != s1.index(), "different value different entry");
        check(!s1.equalsString("other") && s1.equalsString("sv"), "equalsString pos/neg");
    }

    static void testStringFace() {
        ConstantPoolBuilder b = ConstantPoolBuilder.of();
        StringEntry s = b.stringEntry("sv");
        check(s.stringValue().equals("sv"), "stringValue");
        check(s.constantValue().equals("sv"), "constantValue is the string");
        check(s.constantValue() instanceof String, "constantValue type String");
        check(s.tag() == PoolEntry.TAG_STRING && s.tag() == 8, "tag TAG_STRING == 8");
        check(s.width() == 1 && s.index() > 0 && s.constantPool() == b, "pool face");
        check(s instanceof ConstantValueEntry && s instanceof LoadableConstantEntry, "hierarchy ConstantValue+Loadable");
        check(!(((Object) s) instanceof AnnotationConstantValueEntry), "StringEntry is NOT AnnotationConstantValueEntry");
        check(s.utf8().tag() == PoolEntry.TAG_UTF8, "referenced utf8 has TAG_UTF8");
        check(s.typeKind() == TypeKind.REFERENCE, "typeKind REFERENCE");
        // toString 形如 '8 "sv"'（tag + 引号内容，实测锁定）
        check(s.toString().contains("sv"), "toString contains value");
    }

    static void testStringEmissionAndRealPool() throws Exception {
        byte[] bytes = ClassFile.of().build(ClassDesc.of("pkg.EmitS"), cb -> cb
                .withMethodBody("m", MethodTypeDesc.ofDescriptor("()V"), ClassFile.ACC_STATIC,
                        cob -> cob.ldc("ldcStringValue").pop().return_()));
        ConstantPool cp2 = ClassFile.of().parse(bytes).constantPool();
        boolean saw = false;
        for (PoolEntry pe : cp2) if (pe instanceof StringEntry se && se.stringValue().equals("ldcStringValue")
                && se.utf8().stringValue().equals("ldcStringValue")) saw = true;
        check(saw, "emitted ldc(String) leaves StringEntry + utf8 roundtrip");
        byte[] self = StringEntryTest.class.getResourceAsStream("StringEntryTest.class").readAllBytes();
        ClassModel cm = ClassFile.of().parse(self);
        ConstantPool cp = cm.constantPool();
        boolean sawConst = false; int strCount = 0;
        for (PoolEntry pe : cp) if (pe instanceof StringEntry se) {
            strCount++;
            if (se.stringValue().equals(CONST_STRING)) sawConst = true;
        }
        check(strCount > 0, "real pool has StringEntry entries");
        check(sawConst, "real pool contains CONST_STRING StringEntry");
        boolean sawViaAttr = false;
        for (FieldModel fm : cm.fields()) if (fm.fieldName().equalsString("CONST_STRING")) {
            ConstantValueAttribute cva = fm.findAttribute(Attributes.constantValue()).orElse(null);
            if (cva != null && cva.constant() instanceof StringEntry se && se.stringValue().equals(CONST_STRING)) sawViaAttr = true;
        }
        check(sawViaAttr, "ConstantValue attribute exposes same StringEntry");
    }

    static void testStringNullNegative() {
        ConstantPoolBuilder b = ConstantPoolBuilder.of();
        try {
            b.stringEntry((String) null);
            check(false, "stringEntry(null String) must throw");
        } catch (NullPointerException e) { check(true, "stringEntry(null String) NPE"); }
        try {
            b.stringEntry((Utf8Entry) null);
            check(false, "stringEntry(null Utf8Entry) must throw");
        } catch (NullPointerException e) { check(true, "stringEntry(null Utf8Entry) NPE"); }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testConstantDescsPreinit();
        testStringPutDedup();
        testStringFace();
        testStringEmissionAndRealPool();
        testStringNullNegative();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
