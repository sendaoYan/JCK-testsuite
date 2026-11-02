/*
 * @test
 * @summary java.lang.classfile.FieldBuilder conformance: behavioral.
 * @run main FieldBuilderTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class FieldBuilderTest {
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
        byte[] b = ClassFile.of().build(ClassDesc.of("pkg.T"), cb -> cb.withSuperclass(ClassDesc.of("java.lang.Object")).withField("f", ConstantDescs.CD_int, fb -> fb.withFlags(AccessFlag.PRIVATE)));
        ClassModel cm = ClassFile.of().parse(b);
        check(cm.fields().size() == 1 && cm.fields().get(0).flags().has(AccessFlag.PRIVATE), "field flags");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
