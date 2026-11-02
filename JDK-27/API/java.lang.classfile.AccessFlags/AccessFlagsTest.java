/*
 * @test
 * @summary java.lang.classfile.AccessFlags conformance: behavioral.
 * @run main AccessFlagsTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

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

    public static void main(String[] args) throws Exception {
        byte[] b = ClassFile.of().build(ClassDesc.of("pkg.Sample"), cb -> cb.withSuperclass(ClassDesc.of("java.lang.Object")).withField("x", ConstantDescs.CD_int, ClassFile.ACC_PRIVATE).withMethodBody("m", MethodTypeDesc.of(ConstantDescs.CD_int), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC, cob -> cob.iconst_0().ireturn()));
        ClassModel cm = ClassFile.of().parse(b);
        AccessFlags af = cm.flags();
        check(af.location() == AccessFlag.Location.CLASS, "location CLASS");
        check(af.has(AccessFlag.PUBLIC), "has PUBLIC");
        check(af.has(AccessFlag.SUPER), "has SUPER");
        check(!af.has(AccessFlag.FINAL), "no FINAL");
        check(af.flagsMask() == (ClassFile.ACC_PUBLIC | ClassFile.ACC_SUPER), "flagsMask PUBLIC|SUPER");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
