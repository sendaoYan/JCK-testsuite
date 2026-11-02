/*
 * @test
 * @summary java.lang.classfile.FieldModel conformance: behavioral.
 * @run main FieldModelTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class FieldModelTest {
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
        FieldModel fm = cm.fields().get(0);
        check("x".equals(fm.fieldName().stringValue()), "fieldName");
        check("I".equals(fm.fieldType().stringValue()), "fieldType");
        check(ConstantDescs.CD_int.equals(fm.fieldTypeSymbol()), "fieldTypeSymbol");
        check(fm.flags().has(AccessFlag.PRIVATE), "private flag");
        check(fm.parent().get() == cm, "parent");
        check(fm.attributes() != null, "attributes");
        check(fm.elementList().size() > 0, "elementList");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
