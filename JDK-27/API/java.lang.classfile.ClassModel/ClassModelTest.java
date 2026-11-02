/*
 * @test
 * @summary java.lang.classfile.ClassModel conformance: behavioral.
 * @run main ClassModelTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class ClassModelTest {
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
        check("pkg/Sample".equals(cm.thisClass().asInternalName()), "thisClass");
        check("java/lang/Object".equals(cm.superclass().get().asInternalName()), "superclass");
        check(cm.majorVersion() >= 61, "majorVersion");
        check(cm.fields().size() == 1, "one field");
        check(cm.methods().size() == 1, "one method");
        check(!cm.isModuleInfo(), "not module-info");
        check(cm.constantPool().size() > 0, "constant pool non-empty");
        check(cm.elementList().size() > 0, "elementList non-empty");
        check(cm.toDebugString() != null && cm.toDebugString().length() > 0, "toDebugString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
