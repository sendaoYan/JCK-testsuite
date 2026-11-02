/*
 * @test
 * @summary java.lang.classfile.Signature.ClassTypeSig conformance: behavioral.
 * @run main ClassTypeSigTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class ClassTypeSigTest {
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
        Signature.ClassTypeSig s = Signature.ClassTypeSig.of(ClassDesc.of("java.lang.String"));
        check("Ljava/lang/String;".equals(s.signatureString()), "of(String)");
        check("java/lang/String".equals(s.className()), "className");
        check(ClassDesc.of("java.lang.String").equals(s.classDesc()), "classDesc");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
