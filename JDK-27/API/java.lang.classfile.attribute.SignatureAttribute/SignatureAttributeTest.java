/*
 * @test
 * @summary java.lang.classfile.attribute.SignatureAttribute conformance: behavioral.
 * @run main SignatureAttributeTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class SignatureAttributeTest {
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
        SignatureAttribute a = SignatureAttribute.of(Signature.of(ConstantDescs.CD_int));
        check("I".equals(a.signature().stringValue()), "signature");
        check("I".equals(a.asTypeSignature().signatureString()), "asTypeSignature");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
