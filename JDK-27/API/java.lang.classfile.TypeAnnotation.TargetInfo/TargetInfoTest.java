/*
 * @test
 * @summary java.lang.classfile.TypeAnnotation.TargetInfo conformance: behavioral.
 * @run main TargetInfoTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class TargetInfoTest {
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
        check(TypeAnnotation.TargetInfo.ofField().targetType() == TypeAnnotation.TargetType.FIELD, "ofField");
        check(TypeAnnotation.TargetInfo.ofMethodReturn().targetType() == TypeAnnotation.TargetType.METHOD_RETURN, "ofMethodReturn");
        check(TypeAnnotation.TargetInfo.ofClassTypeParameter(2).typeParameterIndex() == 2, "ofClassTypeParameter");
        check(TypeAnnotation.TargetInfo.ofMethodFormalParameter(1).formalParameterIndex() == 1, "ofMethodFormalParameter");
        check(TypeAnnotation.TargetInfo.ofThrows(0).throwsTargetIndex() == 0, "ofThrows");
        check(TypeAnnotation.TargetInfo.ofExceptionParameter(3).exceptionTableIndex() == 3, "ofExceptionParameter");
        check(TypeAnnotation.TargetInfo.ofClassExtends(0).supertypeIndex() == 0, "ofSupertype");
        check(TypeAnnotation.TargetInfo.ofTypeParameterBound(TypeAnnotation.TargetType.CLASS_TYPE_PARAMETER_BOUND, 1, 2).typeParameterIndex() == 1, "ofTypeParameterBound");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
