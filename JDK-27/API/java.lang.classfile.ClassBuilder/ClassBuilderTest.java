/*
 * @test
 * @summary java.lang.classfile.ClassBuilder conformance: behavioral.
 * @run main ClassBuilderTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class ClassBuilderTest {
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
        byte[] b = ClassFile.of().build(ClassDesc.of("pkg.T"), cb -> cb.withVersion(71, 0).withFlags(AccessFlag.PUBLIC).withSuperclass(ClassDesc.of("java.lang.Object")).withField("f", ConstantDescs.CD_int, fb -> fb.withFlags(AccessFlag.PRIVATE)).withMethod("m", MethodTypeDesc.of(ConstantDescs.CD_void), ClassFile.ACC_PUBLIC, mb -> mb.withCode(cob -> cob.return_())));
        ClassModel cm = ClassFile.of().parse(b);
        check(cm.thisClass().asInternalName().equals("pkg/T"), "thisClass");
        check(cm.fields().size() == 1 && cm.methods().size() == 1, "field+method");
        check(cm.majorVersion() == 71, "majorVersion");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
