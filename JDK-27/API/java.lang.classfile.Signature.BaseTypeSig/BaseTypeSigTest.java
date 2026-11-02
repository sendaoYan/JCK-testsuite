/*
 * @test
 * @summary java.lang.classfile.Signature.BaseTypeSig conformance: behavioral.
 * @run main BaseTypeSigTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class BaseTypeSigTest {
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
        Signature.BaseTypeSig s = Signature.BaseTypeSig.of((char) 73);
        check(73 == s.baseType() && "I".equals(s.signatureString()), "of(I)");
        check("J".equals(Signature.BaseTypeSig.of(ConstantDescs.CD_long).signatureString()), "of(long)");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
