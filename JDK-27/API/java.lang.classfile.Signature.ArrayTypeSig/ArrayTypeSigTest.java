/*
 * @test
 * @summary java.lang.classfile.Signature.ArrayTypeSig conformance: behavioral.
 * @run main ArrayTypeSigTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class ArrayTypeSigTest {
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
        Signature.ArrayTypeSig s = Signature.ArrayTypeSig.of(Signature.of(ConstantDescs.CD_int));
        check("[I".equals(s.signatureString()), "of(int)");
        check("I".equals(s.componentSignature().signatureString()), "componentSignature");
        check("[[I".equals(Signature.ArrayTypeSig.of(2, Signature.of(ConstantDescs.CD_int)).signatureString()), "of(2,int)");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
