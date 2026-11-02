/*
 * @test
 * @summary java.lang.classfile.MethodModel conformance: behavioral.
 * @run main MethodModelTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class MethodModelTest {
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
        MethodModel mm = cm.methods().get(0);
        check("m".equals(mm.methodName().stringValue()), "methodName");
        check("()I".equals(mm.methodType().stringValue()), "methodType");
        check(MethodTypeDesc.of(ConstantDescs.CD_int).equals(mm.methodTypeSymbol()), "methodTypeSymbol");
        check(mm.flags().has(AccessFlag.PUBLIC) && mm.flags().has(AccessFlag.STATIC), "flags");
        check(mm.parent().get() == cm, "parent");
        check(mm.code().isPresent(), "has code");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
