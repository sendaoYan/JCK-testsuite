/*
 * @test
 * @summary java.lang.classfile.Signature.TypeParam conformance: behavioral.
 * @run main TypeParamTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class TypeParamTest {
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
        Signature.TypeParam tp = Signature.TypeParam.of("T", Signature.ClassTypeSig.of(ClassDesc.of("java.lang.Object")));
        check("T".equals(tp.identifier()), "identifier");
        check("Ljava/lang/Object;".equals(tp.classBound().get().signatureString()), "classBound");
        check(tp.interfaceBounds().isEmpty(), "no interface bounds");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
