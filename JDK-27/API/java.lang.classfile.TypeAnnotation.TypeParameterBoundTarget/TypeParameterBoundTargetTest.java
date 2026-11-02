/*
 * @test
 * @summary java.lang.classfile.TypeAnnotation.TypeParameterBoundTarget conformance: behavioral.
 * @run main TypeParameterBoundTargetTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class TypeParameterBoundTargetTest {
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
        TypeAnnotation.TypeParameterBoundTarget t = TypeAnnotation.TargetInfo.ofClassTypeParameterBound(1, 2);
        check(t.typeParameterIndex() == 1 && t.boundIndex() == 2, "typeParameterBound");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
