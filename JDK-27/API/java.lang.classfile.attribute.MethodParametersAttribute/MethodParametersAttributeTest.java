/*
 * @test
 * @summary java.lang.classfile.attribute.MethodParametersAttribute conformance: behavioral.
 * @run main MethodParametersAttributeTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class MethodParametersAttributeTest {
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
        MethodParameterInfo i = MethodParameterInfo.ofParameter(Optional.of("p"), 0);
        MethodParametersAttribute a = MethodParametersAttribute.of(i);
        check(a.parameters().size() == 1, "parameters");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
