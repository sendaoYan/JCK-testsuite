/*
 * @test
 * @summary java.lang.classfile.attribute.NestMembersAttribute conformance: behavioral.
 * @run main NestMembersAttributeTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class NestMembersAttributeTest {
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
        NestMembersAttribute a = NestMembersAttribute.ofSymbols(ClassDesc.of("pkg.A"), ClassDesc.of("pkg.B"));
        check(a.nestMembers().size() == 2, "nestMembers");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
