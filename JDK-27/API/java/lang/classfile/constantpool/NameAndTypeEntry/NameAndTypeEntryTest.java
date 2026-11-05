/*
 * @test
 * @summary java.lang.classfile.constantpool.NameAndTypeEntry conformance: white-box structure + black-box behavior.
 * @run main NameAndTypeEntryTest
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

public class NameAndTypeEntryTest {
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
        Class<?> c = Class.forName("java.lang.classfile.constantpool.NameAndTypeEntry");
        check(c != null, "java.lang.classfile.constantpool.NameAndTypeEntry loads");
        check(c.getName().equals("java.lang.classfile.constantpool.NameAndTypeEntry"), "binary name java.lang.classfile.constantpool.NameAndTypeEntry");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.constantpool.NameAndTypeEntry is public");
        check(c.isInterface(), "java.lang.classfile.constantpool.NameAndTypeEntry is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.constantpool.NameAndTypeEntry has no superclass");
        check(declares(c, "name"), "declares name");
        check(declares(c, "type"), "declares type");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        ConstantPoolBuilder cpb = ConstantPoolBuilder.of();
        NameAndTypeEntry nat = cpb.nameAndTypeEntry("foo", MethodTypeDesc.of(ConstantDescs.CD_int, ConstantDescs.CD_String));
        check(nat.name().stringValue().equals("foo"), "name");
        check(nat.type().stringValue().equals("(Ljava/lang/String;)I"), "type descriptor");

    }

    // ---- Part 3: AMHC 常量基础设施预初始化（JDK-27 实测：先完整初始化 ConstantDescs 可避开
    // ConstantDescs <-> PrimitiveClassDescImpl 类初始化环，见 DESIGN.md 易错细节） ----
    static void testConstantDescsPreinit() {
        check(ConstantDescs.CD_Object.descriptorString().equals("Ljava/lang/Object;"), "ConstantDescs.CD_Object preinit");
    }

    // ---- Part 4: NameAndTypeEntry 契约（三工厂/去重/身份法/发射闭环/真类池） ----
    static void testNatPutDedup() {
        ConstantPoolBuilder b = ConstantPoolBuilder.of();
        NameAndTypeEntry n1 = b.nameAndTypeEntry("m", MethodTypeDesc.ofDescriptor("()V"));
        NameAndTypeEntry n2 = b.nameAndTypeEntry("m", MethodTypeDesc.ofDescriptor("()V"));
        check(n1 == n2, "same-value NAT dedup identity");
        check(n1.index() == n2.index(), "dedup same index");
        NameAndTypeEntry n3 = b.nameAndTypeEntry(b.utf8Entry("m"), b.utf8Entry("()V"));
        check(n3 == n1, "(String,MethodTypeDesc) overload dedups with (Utf8,Utf8) overload (实测)");
        NameAndTypeEntry n4 = b.nameAndTypeEntry("m", ClassDesc.of("I"));
        check(n4 != n1, "field-type NAT differs from method-type NAT");
        // 实测：ClassDesc 重载与 Utf8 重载内容相同却不去重（分派路径不同）
        NameAndTypeEntry n5 = b.nameAndTypeEntry("f", ClassDesc.of("I"));
        NameAndTypeEntry n6 = b.nameAndTypeEntry(b.utf8Entry("f"), b.utf8Entry("I"));
        check(n5 != n6, "ClassDesc overload does NOT dedup with Utf8 overload (实测)");
        check(n5.name().stringValue().equals("f") && n6.type().stringValue().equals("I"), "both overloads yield same name/type text");
        check(b.nameAndTypeEntry("f2", ClassDesc.of("I")) != n4, "different name different entry");
    }

    static void testNatFace() {
        ConstantPoolBuilder b = ConstantPoolBuilder.of();
        NameAndTypeEntry n = b.nameAndTypeEntry("m", MethodTypeDesc.ofDescriptor("(I)V"));
        check(n.name().stringValue().equals("m"), "name");
        check(n.type().stringValue().equals("(I)V"), "type");
        check(n.name() == b.utf8Entry("m"), "name is the shared utf8 entry");
        check(n.type() == b.utf8Entry("(I)V"), "type is the shared utf8 entry");
        check(n.tag() == PoolEntry.TAG_NAME_AND_TYPE && n.tag() == 12, "tag TAG_NAME_AND_TYPE == 12");
        check(n.width() == 1 && n.index() > 0 && n.constantPool() == b, "pool face");
        check(n instanceof PoolEntry && !(((Object) n) instanceof LoadableConstantEntry), "hierarchy: PoolEntry only");
        NameAndTypeEntry nf = b.nameAndTypeEntry("f", ClassDesc.ofDescriptor("Ljava/lang/String;"));
        check(nf.type().stringValue().equals("Ljava/lang/String;"), "field-type NAT type descriptor");
    }

    static void testNatEmissionAndRealPool() throws Exception {
        byte[] bytes = ClassFile.of().build(ClassDesc.of("pkg.EmitN"), cb -> cb
                .withMethodBody("m", MethodTypeDesc.ofDescriptor("()V"), ClassFile.ACC_STATIC,
                        cob -> cob.getstatic(ClassDesc.of("java.lang.System"), "out", ClassDesc.of("java.io.PrintStream")).pop().return_()));
        ConstantPool cp2 = ClassFile.of().parse(bytes).constantPool();
        boolean saw = false;
        for (PoolEntry pe : cp2) if (pe instanceof FieldRefEntry fr && fr.name().stringValue().equals("out")) {
            check(fr.nameAndType().name().stringValue().equals("out"), "NAT name via member ref");
            check(fr.nameAndType().type().stringValue().equals("Ljava/io/PrintStream;"), "NAT type via member ref");
            saw = true;
        }
        check(saw, "emitted getstatic exposes NAT roundtrip");
        byte[] self = NameAndTypeEntryTest.class.getResourceAsStream("NameAndTypeEntryTest.class").readAllBytes();
        ClassModel cm = ClassFile.of().parse(self);
        ConstantPool cp = cm.constantPool();
        int natCount = 0; boolean sawInitNat = false;
        for (PoolEntry pe : cp) if (pe instanceof NameAndTypeEntry nt) {
            natCount++;
            if (nt.name().stringValue().equals("<init>") && nt.type().stringValue().equals("()V")) sawInitNat = true;
        }
        check(natCount > 0, "real pool has NameAndTypeEntry entries");
        check(sawInitNat, "real pool NAT for Object.<init>");
    }

    static void testNatNullNegative() {
        ConstantPoolBuilder b = ConstantPoolBuilder.of();
        try {
            b.nameAndTypeEntry((String) null, ClassDesc.of("I"));
            check(false, "nameAndTypeEntry(null name) must throw");
        } catch (NullPointerException e) { check(true, "nameAndTypeEntry(null name) NPE"); }
        try {
            b.nameAndTypeEntry((Utf8Entry) null, (Utf8Entry) null);
            check(false, "nameAndTypeEntry(null,null) must throw");
        } catch (NullPointerException e) { check(true, "nameAndTypeEntry(null,null) NPE"); }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testConstantDescsPreinit();
        testNatPutDedup();
        testNatFace();
        testNatEmissionAndRealPool();
        testNatNullNegative();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
