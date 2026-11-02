/*
 * @test
 * @summary java.lang.classfile.ClassSignature conformance: behavioral.
 * @run main ClassSignatureTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class ClassSignatureTest {
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
        ClassSignature s = ClassSignature.parseFrom("Ljava/lang/String;");
        check("Ljava/lang/String;".equals(s.signatureString()), "parseFrom");
        check("java/lang/String".equals(s.superclassSignature().className()), "superclassSignature");
        check(s.superinterfaceSignatures().isEmpty(), "no superinterfaces");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
