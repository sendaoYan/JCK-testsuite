/*
 * @test
 * @summary java.lang.classfile.Signature.TypeArg conformance: behavioral.
 * @run main TypeArgTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class TypeArgTest {
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
        Signature.TypeArg.Bounded b = Signature.TypeArg.extendsOf(Signature.ClassTypeSig.of(ClassDesc.of("java.lang.Number")));
        check(b.wildcardIndicator() == Signature.TypeArg.Bounded.WildcardIndicator.EXTENDS, "extendsOf");
        Signature.TypeArg.Unbounded u = Signature.TypeArg.unbounded();
        check(u != null, "unbounded");
        check(Signature.TypeArg.superOf(Signature.ClassTypeSig.of(ClassDesc.of("java.lang.Object"))).wildcardIndicator() == Signature.TypeArg.Bounded.WildcardIndicator.SUPER, "superOf");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
