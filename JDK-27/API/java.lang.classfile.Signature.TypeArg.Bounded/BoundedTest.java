/*
 * @test
 * @summary java.lang.classfile.Signature.TypeArg.Bounded conformance: behavioral.
 * @run main BoundedTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class BoundedTest {
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
        Signature.TypeArg.Bounded b = Signature.TypeArg.bounded(Signature.TypeArg.Bounded.WildcardIndicator.EXTENDS, Signature.ClassTypeSig.of(ClassDesc.of("java.lang.Number")));
        check(b.wildcardIndicator() == Signature.TypeArg.Bounded.WildcardIndicator.EXTENDS, "wildcardIndicator");
        check("Ljava/lang/Number;".equals(b.boundType().signatureString()), "boundType");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
