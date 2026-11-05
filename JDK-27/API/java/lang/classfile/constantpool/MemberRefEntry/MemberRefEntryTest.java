/*
 * @test
 * @summary java.lang.classfile.constantpool.MemberRefEntry conformance: white-box structure + black-box behavior.
 * @run main MemberRefEntryTest
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

public class MemberRefEntryTest {
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
        Class<?> c = Class.forName("java.lang.classfile.constantpool.MemberRefEntry");
        check(c != null, "java.lang.classfile.constantpool.MemberRefEntry loads");
        check(c.getName().equals("java.lang.classfile.constantpool.MemberRefEntry"), "binary name java.lang.classfile.constantpool.MemberRefEntry");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.constantpool.MemberRefEntry is public");
        check(c.isInterface(), "java.lang.classfile.constantpool.MemberRefEntry is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.constantpool.MemberRefEntry has no superclass");
        check(declares(c, "owner"), "declares owner");
        check(declares(c, "nameAndType"), "declares nameAndType");
        check(declares(c, "name"), "declares name");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        ConstantPoolBuilder cpb = ConstantPoolBuilder.of();
        MemberRefEntry mre = cpb.methodRefEntry(ClassDesc.of("java.lang.Integer"), "valueOf", MethodTypeDesc.of(ClassDesc.of("java.lang.Integer"), ConstantDescs.CD_int));
        check(mre.owner().asInternalName().equals("java/lang/Integer"), "owner");
        check(mre.nameAndType().name().stringValue().equals("valueOf"), "nameAndType name");
        check(mre.name().stringValue().equals("valueOf"), "name default");
        check(mre.type().stringValue().equals("(I)Ljava/lang/Integer;"), "type default");

    }

    // ---- Part 3: AMHC 常量基础设施预初始化（JDK-27 实测：先完整初始化 ConstantDescs 可避开
    // ConstantDescs <-> PrimitiveClassDescImpl 类初始化环，见 DESIGN.md 易错细节） ----
    static void testConstantDescsPreinit() {
        check(ConstantDescs.CD_Object.descriptorString().equals("Ljava/lang/Object;"), "ConstantDescs.CD_Object preinit");
    }

    // ---- Part 4: MemberRefEntry 契约（三具体实现/身份法/tag 表/发射闭环/真类池） ----
    static void testMemberRefImplementations() {
        ConstantPoolBuilder b = ConstantPoolBuilder.of();
        ClassEntry owner = b.classEntry(ClassDesc.of("p.Owner"));
        NameAndTypeEntry nat = b.nameAndTypeEntry("mem", MethodTypeDesc.ofDescriptor("(I)V"));
        MethodRefEntry mr = b.methodRefEntry(owner, nat);
        FieldRefEntry fr = b.fieldRefEntry(b.classEntry(ClassDesc.of("p.Owner")), b.nameAndTypeEntry("mem", ClassDesc.of("I")));
        InterfaceMethodRefEntry ir = b.interfaceMethodRefEntry(owner, nat);
        check(mr instanceof MemberRefEntry && fr instanceof MemberRefEntry && ir instanceof MemberRefEntry, "all three kinds are MemberRefEntry");
        check(mr.owner() == owner, "owner() identity is the shared ClassEntry");
        check(mr.nameAndType() == nat, "nameAndType() identity is the shared NAT");
        check(mr.name() == nat.name() && mr.type() == nat.type(), "name()/type() delegate to NAT (identity law)");
        check(fr.name() == fr.nameAndType().name() && fr.type() == fr.nameAndType().type(), "fieldref delegation law");
        check(ir.name() == nat.name() && ir.type() == nat.type(), "imethodref delegation law");
        check(mr.tag() == PoolEntry.TAG_METHODREF && mr.tag() == 10, "methodref tag");
        check(fr.tag() == PoolEntry.TAG_FIELDREF && fr.tag() == 9, "fieldref tag");
        check(ir.tag() == PoolEntry.TAG_INTERFACE_METHODREF && ir.tag() == 11, "imethodref tag");
        check(mr.width() == 1 && fr.width() == 1 && ir.width() == 1, "all single-slot");
        check(mr.constantPool() == b, "pool face");
        check(!(((Object) mr) instanceof LoadableConstantEntry), "member refs are not loadable");
    }

    static void testMemberRefEmissionAndRealPool() throws Exception {
        byte[] bytes = ClassFile.of().build(ClassDesc.of("pkg.EmitMr2"), cb -> cb
                .withMethodBody("m", MethodTypeDesc.ofDescriptor("(Ljava/lang/Object;)V"), ClassFile.ACC_STATIC,
                        cob -> cob.aload(0).invokevirtual(ClassDesc.of("java.lang.Object"), "hashCode", MethodTypeDesc.ofDescriptor("()I")).pop().return_()));
        boolean saw = false;
        for (PoolEntry pe : ClassFile.of().parse(bytes).constantPool())
            if (pe instanceof MemberRefEntry m && m.owner().asInternalName().equals("java/lang/Object")
                    && m.name().stringValue().equals("hashCode") && m.type().stringValue().equals("()I")) saw = true;
        check(saw, "emitted invokevirtual exposes MemberRefEntry triple");
        byte[] self = MemberRefEntryTest.class.getResourceAsStream("MemberRefEntryTest.class").readAllBytes();
        ClassModel cm = ClassFile.of().parse(self);
        ConstantPool cp = cm.constantPool();
        int n = 0; boolean sawInit = false;
        for (PoolEntry pe : cp) if (pe instanceof MemberRefEntry m) {
            n++;
            check(m.owner() != null && m.nameAndType() != null, "member ref parts non-null");
            check(m.name() != null && m.type() != null, "name/type non-null");
            if (m instanceof MethodRefEntry && m.name().stringValue().equals("<init>")
                    && m.owner().asInternalName().equals("java/lang/Object")) sawInit = true;
        }
        check(n > 0, "real pool has MemberRefEntry entries");
        check(sawInit, "real pool Object.<init> member ref");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testConstantDescsPreinit();
        testMemberRefImplementations();
        testMemberRefEmissionAndRealPool();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
