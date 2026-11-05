/*
 * @test
 * @summary java.lang.classfile.constantpool.FieldRefEntry conformance: white-box structure + black-box behavior.
 * @run main FieldRefEntryTest
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

public class FieldRefEntryTest {
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
        Class<?> c = Class.forName("java.lang.classfile.constantpool.FieldRefEntry");
        check(c != null, "java.lang.classfile.constantpool.FieldRefEntry loads");
        check(c.getName().equals("java.lang.classfile.constantpool.FieldRefEntry"), "binary name java.lang.classfile.constantpool.FieldRefEntry");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.constantpool.FieldRefEntry is public");
        check(c.isInterface(), "java.lang.classfile.constantpool.FieldRefEntry is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.constantpool.FieldRefEntry has no superclass");
        check(declares(c, "typeSymbol"), "declares typeSymbol");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        ConstantPoolBuilder cpb = ConstantPoolBuilder.of();
        FieldRefEntry fre = cpb.fieldRefEntry(ClassDesc.of("java.lang.System"), "out", ClassDesc.of("java.io.PrintStream"));
        check(fre.owner().asInternalName().equals("java/lang/System"), "owner");
        check(fre.name().stringValue().equals("out"), "name");
        check(fre.type().stringValue().equals("Ljava/io/PrintStream;"), "type");
        check(fre.typeSymbol().equals(ClassDesc.of("java.io.PrintStream")), "typeSymbol");

    }

    // ---- Part 3: AMHC 常量基础设施预初始化（JDK-27 实测：先完整初始化 ConstantDescs 可避开
    // ConstantDescs <-> PrimitiveClassDescImpl 类初始化环，见 DESIGN.md 易错细节） ----
    static void testConstantDescsPreinit() {
        check(ConstantDescs.CD_Object.descriptorString().equals("Ljava/lang/Object;"), "ConstantDescs.CD_Object preinit");
    }

    // ---- Part 4: FieldRefEntry 契约（双工厂/去重/MemberRef 面/typeSymbol/getstatic 发射闭环/真类池） ----
    static void testFieldRefPutDedup() {
        ConstantPoolBuilder b = ConstantPoolBuilder.of();
        FieldRefEntry f1 = b.fieldRefEntry(ClassDesc.of("java.lang.System"), "out", ClassDesc.of("java.io.PrintStream"));
        FieldRefEntry f2 = b.fieldRefEntry(ClassDesc.of("java.lang.System"), "out", ClassDesc.of("java.io.PrintStream"));
        check(f1 == f2, "same-value fieldRefEntry dedup identity");
        check(f1.index() == f2.index(), "dedup same index");
        FieldRefEntry f3 = b.fieldRefEntry(f1.owner(), f1.nameAndType());
        check(f3 == f1, "cross-overload dedup: (ClassEntry,NAT) == (ClassDesc,String,ClassDesc)");
        FieldRefEntry f4 = b.fieldRefEntry(ClassDesc.of("java.lang.System"), "err", ClassDesc.of("java.io.PrintStream"));
        check(f4 != f1 && f4.index() != f1.index(), "different name different entry");
    }

    static void testFieldRefFace() {
        ConstantPoolBuilder b = ConstantPoolBuilder.of();
        FieldRefEntry f = b.fieldRefEntry(ClassDesc.of("java.lang.System"), "out", ClassDesc.of("java.io.PrintStream"));
        check(f.owner().asInternalName().equals("java/lang/System"), "owner");
        check(f.name().stringValue().equals("out"), "name");
        check(f.type().stringValue().equals("Ljava/io/PrintStream;"), "type descriptor");
        check(f.nameAndType().name() == f.name() && f.nameAndType().type() == f.type(), "name()/type() delegate to NAT (identity)");
        check(f.typeSymbol().descriptorString().equals("Ljava/io/PrintStream;"), "typeSymbol ClassDesc");
        check(f.tag() == PoolEntry.TAG_FIELDREF && f.tag() == 9, "tag TAG_FIELDREF == 9");
        check(f.width() == 1 && f.index() > 0 && f.constantPool() == b, "pool face");
        check(f instanceof MemberRefEntry && f instanceof PoolEntry, "hierarchy MemberRef+PoolEntry");
        check(!(((Object) ((Object) f)) instanceof MethodRefEntry) && !(((Object) ((Object) f)) instanceof InterfaceMethodRefEntry), "field ref is not a method ref");
        check(!(((Object) f) instanceof LoadableConstantEntry), "field ref is not loadable");
    }

    static void testFieldRefEmissionAndRealPool() throws Exception {
        byte[] bytes = ClassFile.of().build(ClassDesc.of("pkg.EmitFr"), cb -> cb
                .withMethodBody("m", MethodTypeDesc.ofDescriptor("()V"), ClassFile.ACC_STATIC,
                        cob -> cob.getstatic(ClassDesc.of("java.lang.System"), "out", ClassDesc.of("java.io.PrintStream")).pop().return_()));
        ConstantPool cp2 = ClassFile.of().parse(bytes).constantPool();
        boolean saw = false;
        for (PoolEntry pe : cp2) if (pe instanceof FieldRefEntry fr
                && fr.owner().asInternalName().equals("java/lang/System")
                && fr.name().stringValue().equals("out")
                && fr.type().stringValue().equals("Ljava/io/PrintStream;")) saw = true;
        check(saw, "emitted getstatic leaves FieldRefEntry with full triple roundtrip");
        byte[] self = FieldRefEntryTest.class.getResourceAsStream("FieldRefEntryTest.class").readAllBytes();
        ClassModel cm = ClassFile.of().parse(self);
        ConstantPool cp = cm.constantPool();
        boolean sawOut = false; int fieldRefCount = 0;
        for (PoolEntry pe : cp) if (pe instanceof FieldRefEntry fr) {
            fieldRefCount++;
            if (fr.owner().asInternalName().equals("java/lang/System") && fr.name().stringValue().equals("out")) sawOut = true;
        }
        check(fieldRefCount > 0, "real pool has FieldRefEntry entries");
        check(sawOut, "real pool references System.out (main uses println)");
    }

    static void testFieldRefNullNegative() {
        ConstantPoolBuilder b = ConstantPoolBuilder.of();
        try {
            b.fieldRefEntry((ClassDesc) null, "f", ClassDesc.of("I"));
            check(false, "fieldRefEntry(null owner) must throw");
        } catch (NullPointerException e) { check(true, "fieldRefEntry(null owner) NPE"); }
        try {
            b.fieldRefEntry(null, null);
            check(false, "fieldRefEntry(null,null) must throw");
        } catch (NullPointerException e) { check(true, "fieldRefEntry(null,null) NPE"); }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testConstantDescsPreinit();
        testFieldRefPutDedup();
        testFieldRefFace();
        testFieldRefEmissionAndRealPool();
        testFieldRefNullNegative();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
