/*
 * @test
 * @summary java.lang.classfile.AccessFlags conformance: white-box structure + black-box behavior.
 * @run main AccessFlagsTest
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

public class AccessFlagsTest {
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
        Class<?> c = Class.forName("java.lang.classfile.AccessFlags");
        check(c != null, "java.lang.classfile.AccessFlags loads");
        check(c.getName().equals("java.lang.classfile.AccessFlags"), "binary name java.lang.classfile.AccessFlags");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.AccessFlags is public");
        check(c.isInterface(), "java.lang.classfile.AccessFlags is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.AccessFlags has no superclass");
        check(declares(c, "flagsMask"), "declares flagsMask");
        check(declares(c, "flags"), "declares flags");
        check(declares(c, "has"), "declares has");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        byte[] b = ClassFile.of().build(ClassDesc.of("pkg.Sample"), cb -> cb.withSuperclass(ClassDesc.of("java.lang.Object")).withField("x", ConstantDescs.CD_int, ClassFile.ACC_PRIVATE).withMethodBody("m", MethodTypeDesc.of(ConstantDescs.CD_int), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC, cob -> cob.iconst_0().ireturn()));
        ClassModel cm = ClassFile.of().parse(b);
        AccessFlags af = cm.flags();
        check(af.location() == AccessFlag.Location.CLASS, "location CLASS");
        check(af.has(AccessFlag.PUBLIC), "has PUBLIC");
        check(af.has(AccessFlag.SUPER), "has SUPER");
        check(!af.has(AccessFlag.FINAL), "no FINAL");
        check(af.flagsMask() == (ClassFile.ACC_PUBLIC | ClassFile.ACC_SUPER), "flagsMask PUBLIC|SUPER");
        check(af.flags().contains(AccessFlag.PUBLIC) && af.flags().contains(AccessFlag.SUPER), "flags set");
        AccessFlags mf = cm.methods().get(0).flags();
        check(mf.location() == AccessFlag.Location.METHOD, "method location METHOD");
        check(mf.has(AccessFlag.PUBLIC) && mf.has(AccessFlag.STATIC), "method PUBLIC|STATIC");
        check(mf.flagsMask() == (ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC), "method flagsMask");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
