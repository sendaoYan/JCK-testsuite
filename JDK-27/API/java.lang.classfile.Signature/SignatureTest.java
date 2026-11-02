/*
 * @test
 * @summary java.lang.classfile.Signature conformance: behavioral.
 * @run main SignatureTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class SignatureTest {
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
        Signature s = Signature.of(ConstantDescs.CD_int);
        check("I".equals(s.signatureString()), "of(int)");
        check("Ljava/lang/String;".equals(Signature.parseFrom("Ljava/lang/String;").signatureString()), "parseFrom");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
