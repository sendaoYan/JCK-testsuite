/*
 * @test
 * @summary java.lang.classfile.constantpool.Utf8Entry conformance: white-box structure + black-box behavior.
 * @run main Utf8EntryTest
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

public class Utf8EntryTest {
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
        Class<?> c = Class.forName("java.lang.classfile.constantpool.Utf8Entry");
        check(c != null, "java.lang.classfile.constantpool.Utf8Entry loads");
        check(c.getName().equals("java.lang.classfile.constantpool.Utf8Entry"), "binary name java.lang.classfile.constantpool.Utf8Entry");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.constantpool.Utf8Entry is public");
        check(c.isInterface(), "java.lang.classfile.constantpool.Utf8Entry is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.constantpool.Utf8Entry has no superclass");
        check(declares(c, "stringValue"), "declares stringValue");
        check(declares(c, "equalsString"), "declares equalsString");
        check(declares(c, "isFieldType"), "declares isFieldType");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        ConstantPoolBuilder cpb = ConstantPoolBuilder.of();
        Utf8Entry u = cpb.utf8Entry("hello");
        check(u.stringValue().equals("hello"), "stringValue");
        check(u.equalsString("hello") && !u.equalsString("bye"), "equalsString");
        check(u.length() == 5, "length 5");
        check(u.charAt(0) == 'h', "charAt");
        check(u.toString().equals("hello"), "toString");
        check(u.isFieldType(ConstantDescs.CD_int) == false, "isFieldType false for hello");
        Utf8Entry ui = cpb.utf8Entry(ClassDesc.of("java.lang.String"));
        check(ui.stringValue().equals("Ljava/lang/String;"), "utf8 from ClassDesc descriptor");

    }

    // ---- Part 3: AMHC 常量基础设施预初始化（JDK-27 实测：先完整初始化 ConstantDescs 可避开
    // ConstantDescs <-> PrimitiveClassDescImpl 类初始化环，见 DESIGN.md 易错细节） ----
    static void testConstantDescsPreinit() {
        check(ConstantDescs.CD_Object.descriptorString().equals("Ljava/lang/Object;"), "ConstantDescs.CD_Object preinit");
    }

    // ---- Part 4: Utf8Entry 契约（三工厂重载/跨重载去重/CharSequence 全面/isField-isMethodType/equals 值语义/发射闭环） ----
    static void testUtf8OverloadsAndDedup() {
        ConstantPoolBuilder b = ConstantPoolBuilder.of();
        Utf8Entry u1 = b.utf8Entry("dup");
        Utf8Entry u2 = b.utf8Entry(new String("dup"));
        check(u1 == u2, "dedup is value-based across distinct String instances");
        check(b.utf8Entry("dup").index() == u1.index(), "dedup same index");
        Utf8Entry u3 = b.utf8Entry(ClassDesc.of("java.lang.String"));
        check(u3.stringValue().equals("Ljava/lang/String;"), "utf8Entry(ClassDesc) yields field descriptor");
        check(u3 == b.utf8Entry("Ljava/lang/String;"), "utf8Entry(ClassDesc) dedups with equal text");
        Utf8Entry u4 = b.utf8Entry(MethodTypeDesc.ofDescriptor("(I)V"));
        check(u4.stringValue().equals("(I)V"), "utf8Entry(MethodTypeDesc) yields method descriptor");
        check(u4 == b.utf8Entry("(I)V"), "utf8Entry(MethodTypeDesc) dedups with equal text");
        check(b.utf8Entry("other") != u1 && b.utf8Entry("other").index() != u1.index(), "different text different entry");
    }

    static void testUtf8CharSequenceFace() {
        ConstantPoolBuilder b = ConstantPoolBuilder.of();
        Utf8Entry u = b.utf8Entry("Ljava/lang/String;");
        check(u.length() == 18, "CharSequence length");
        check(u.charAt(0) == 'L' && u.charAt(17) == ';', "charAt bounds");
        check(u.subSequence(0, 1).toString().equals("L"), "subSequence");
        check(u.toString().equals("Ljava/lang/String;"), "toString equals stringValue");
        // 值语义 equals/hashCode（跨池，实测锁定）
        ConstantPoolBuilder b2 = ConstantPoolBuilder.of();
        Utf8Entry other = b2.utf8Entry("Ljava/lang/String;");
        check(u.equals(other), "equals is value-based across pools");
        check(u.hashCode() == other.hashCode(), "hashCode consistent with equals");
        check(!u.equals(b2.utf8Entry("different")), "equals negative");
        check(u.stringValue().equals(u.toString()), "stringValue == toString content");
        // 非 ASCII（modified UTF-8 编码）round-trip
        Utf8Entry n = b.utf8Entry("中文é");
        check(n.stringValue().equals("中文é"), "non-ASCII roundtrip");
        check(n.length() == 3, "non-ASCII length in UTF-16 units");
        check(n.equalsString("中文é"), "equalsString non-ASCII");
        // tag/width/层次
        check(u.tag() == PoolEntry.TAG_UTF8 && u.tag() == 1, "tag TAG_UTF8 == 1");
        check(u.width() == 1 && u.index() > 0 && u.constantPool() == b, "pool face");
        check(u instanceof AnnotationConstantValueEntry, "Utf8Entry is AnnotationConstantValueEntry");
        check(!(((Object) u) instanceof LoadableConstantEntry), "Utf8Entry is NOT LoadableConstantEntry");
        check(u.constantValue().equals("Ljava/lang/String;"), "ACVE constantValue is text");
    }

    static void testUtf8TypeMatchers() {
        ConstantPoolBuilder b = ConstantPoolBuilder.of();
        Utf8Entry ft = b.utf8Entry("Ljava/lang/String;");
        check(ft.isFieldType(ClassDesc.of("java.lang.String")), "isFieldType positive");
        check(!ft.isFieldType(ClassDesc.of("java.lang.Integer")), "isFieldType negative (different type)");
        check(!ft.isMethodType(MethodTypeDesc.ofDescriptor("()V")), "field descriptor is not method type");
        Utf8Entry mt = b.utf8Entry("()I");
        check(mt.isMethodType(MethodTypeDesc.ofDescriptor("()I")), "isMethodType positive");
        check(!mt.isMethodType(MethodTypeDesc.ofDescriptor("(I)I")), "isMethodType negative");
        check(!mt.isFieldType(ClassDesc.ofDescriptor("I")), "method descriptor is not field type");
        Utf8Entry plain = b.utf8Entry("plain");
        check(!plain.isFieldType(ClassDesc.of("java.lang.String")) && !plain.isMethodType(MethodTypeDesc.ofDescriptor("()V")), "plain text matches neither");
    }

    static void testUtf8EmissionAndRealPool() throws Exception {
        byte[] bytes = ClassFile.of().build(ClassDesc.of("pkg.EmitU"), cb -> cb
                .withMethodBody("m", MethodTypeDesc.ofDescriptor("()V"), ClassFile.ACC_STATIC,
                        cob -> cob.getstatic(ClassDesc.of("java.lang.System"), "out", ClassDesc.of("java.io.PrintStream")).pop().return_()));
        ConstantPool cp2 = ClassFile.of().parse(bytes).constantPool();
        boolean sawName = false, sawType = false;
        for (PoolEntry pe : cp2) if (pe instanceof FieldRefEntry fr && fr.name().stringValue().equals("out")) {
            check(fr.name().tag() == PoolEntry.TAG_UTF8, "field name is Utf8Entry");
            check(fr.type().stringValue().equals("Ljava/io/PrintStream;"), "field type utf8 roundtrip");
            sawName = true;
        }
        for (PoolEntry pe : cp2) if (pe instanceof Utf8Entry ue && ue.equalsString("Ljava/io/PrintStream;")) sawType = true;
        check(sawName, "emitted getstatic utf8 name roundtrip");
        check(sawType, "emitted getstatic utf8 descriptor in pool");
        byte[] self = Utf8EntryTest.class.getResourceAsStream("Utf8EntryTest.class").readAllBytes();
        ClassModel cm = ClassFile.of().parse(self);
        ConstantPool cp = cm.constantPool();
        int utf8Count = 0; boolean sawObject = false;
        for (PoolEntry pe : cp) if (pe instanceof Utf8Entry ue) {
            utf8Count++;
            if (ue.equalsString("java/lang/Object")) sawObject = true;
        }
        check(utf8Count > 0, "real pool has Utf8Entry entries");
        check(sawObject, "real pool utf8 contains java/lang/Object");
        // 越界/空负例
        try {
            cp.entryByIndex(0);
            check(false, "index 0 must throw");
        } catch (ConstantPoolException e) {
            check(e.getMessage() != null, "index 0 CPE message non-null");
        }
    }

    static void testUtf8NullNegative() {
        ConstantPoolBuilder b = ConstantPoolBuilder.of();
        try {
            b.utf8Entry((String) null);
            check(false, "utf8Entry(null String) must throw");
        } catch (NullPointerException e) { check(true, "utf8Entry(null String) NPE"); }
        try {
            b.utf8Entry((ClassDesc) null);
            check(false, "utf8Entry(null ClassDesc) must throw");
        } catch (NullPointerException e) { check(true, "utf8Entry(null ClassDesc) NPE"); }
        try {
            b.utf8Entry((MethodTypeDesc) null);
            check(false, "utf8Entry(null MethodTypeDesc) must throw");
        } catch (NullPointerException e) { check(true, "utf8Entry(null MethodTypeDesc) NPE"); }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testConstantDescsPreinit();
        testUtf8OverloadsAndDedup();
        testUtf8CharSequenceFace();
        testUtf8TypeMatchers();
        testUtf8EmissionAndRealPool();
        testUtf8NullNegative();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
