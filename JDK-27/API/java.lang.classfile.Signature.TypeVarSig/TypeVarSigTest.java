/*
 * @test
 * @summary java.lang.classfile.Signature.TypeVarSig conformance: behavioral.
 * @run main TypeVarSigTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class TypeVarSigTest {
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
        Signature.TypeVarSig s = Signature.TypeVarSig.of("T");
        check("TT;".equals(s.signatureString()), "of(T)");
        check("T".equals(s.identifier()), "identifier");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
