/*
 * @test
 * @summary java.lang.classfile.TypeAnnotation.TypePathComponent conformance: behavioral.
 * @run main TypePathComponentTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class TypePathComponentTest {
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
        TypeAnnotation.TypePathComponent tpc = TypeAnnotation.TypePathComponent.of(TypeAnnotation.TypePathComponent.Kind.ARRAY, 0);
        check(tpc.typePathKind() == TypeAnnotation.TypePathComponent.Kind.ARRAY, "typePathKind");
        check(tpc.typeArgumentIndex() == 0, "typeArgumentIndex");
        check(TypeAnnotation.TypePathComponent.ARRAY != null && TypeAnnotation.TypePathComponent.INNER_TYPE != null && TypeAnnotation.TypePathComponent.WILDCARD != null, "predefined constants");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
