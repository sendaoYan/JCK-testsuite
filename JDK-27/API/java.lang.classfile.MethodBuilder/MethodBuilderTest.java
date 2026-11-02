/*
 * @test
 * @summary java.lang.classfile.MethodBuilder conformance: behavioral.
 * @run main MethodBuilderTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class MethodBuilderTest {
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
        byte[] b = ClassFile.of().build(ClassDesc.of("pkg.T"), cb -> cb.withSuperclass(ClassDesc.of("java.lang.Object")).withMethod("m", MethodTypeDesc.of(ConstantDescs.CD_void), ClassFile.ACC_PUBLIC, mb -> mb.withFlags(AccessFlag.PUBLIC).withCode(cob -> cob.return_())));
        ClassModel cm = ClassFile.of().parse(b);
        check(cm.methods().size() == 1, "one method");
        check(cm.methods().get(0).code().isPresent(), "has code");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
