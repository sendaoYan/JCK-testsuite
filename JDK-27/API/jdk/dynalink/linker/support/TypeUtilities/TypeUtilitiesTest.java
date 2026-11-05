/*
 * @test
 * @summary jdk.dynalink.linker.support.TypeUtilities conformance: white-box structure + black-box behavior.
 * @run main TypeUtilitiesTest
 */

import jdk.dynalink.*;
import jdk.dynalink.linker.*;
import jdk.dynalink.linker.support.*;
import jdk.dynalink.beans.*;
import jdk.dynalink.support.*;
import java.lang.invoke.*;
import java.lang.reflect.*;
import java.util.*;

public class TypeUtilitiesTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.dynalink.linker.support.TypeUtilities");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(hasMethod(c, "isSubtype"), "isSubtype");
        check(hasMethod(c, "getWrapperType"), "getWrapperType");
        check(hasMethod(c, "getPrimitiveTypeByName"), "getPrimitiveTypeByName");
        check(hasMethod(c, "isWrapperType"), "isWrapperType");
        check(hasMethod(c, "getPrimitiveType"), "getPrimitiveType");
        check(hasMethod(c, "isMethodInvocationConvertible"), "isMethodInvocationConvertible");

    }

    static void testBlackBox() throws Exception {
        check(TypeUtilities.isSubtype(String.class, Object.class), "isSubtype");
        check(TypeUtilities.getWrapperType(int.class) == Integer.class, "wrapper type");
        check(TypeUtilities.getPrimitiveTypeByName("int") == int.class, "primitive by name");
        check(TypeUtilities.isWrapperType(Integer.class), "isWrapperType");
        check(TypeUtilities.getPrimitiveType(Integer.class) == int.class, "primitive type");
        check(TypeUtilities.isMethodInvocationConvertible(int.class, Integer.class), "method invocation convertible");
        check(TypeUtilities.isConvertibleWithoutLoss(int.class, long.class), "convertible without loss");

    }


    static void testConversionMatrix() {
        // method-invocation conversion (JLS 5.3): widening + boxing, never narrowing
        check(TypeUtilities.isMethodInvocationConvertible(int.class, long.class), "int -> long");
        check(TypeUtilities.isMethodInvocationConvertible(int.class, double.class), "int -> double");
        check(TypeUtilities.isMethodInvocationConvertible(int.class, Integer.class), "int -> Integer");
        check(TypeUtilities.isMethodInvocationConvertible(Integer.class, int.class), "Integer -> int");
        check(TypeUtilities.isMethodInvocationConvertible(int.class, Object.class), "int -> Object");
        check(!TypeUtilities.isMethodInvocationConvertible(long.class, int.class), "long !-> int");
        check(!TypeUtilities.isMethodInvocationConvertible(String.class, int.class), "String !-> int");
        check(TypeUtilities.isMethodInvocationConvertible(String.class, CharSequence.class), "String -> CharSequence");
        // lossless conversion adds unboxing-widening chains like int -> long
        check(TypeUtilities.isConvertibleWithoutLoss(int.class, long.class), "lossless int -> long");
        check(!TypeUtilities.isConvertibleWithoutLoss(long.class, int.class), "lossy long -> int");
        check(TypeUtilities.isConvertibleWithoutLoss(Integer.class, Object.class), "lossless widening");
        check(!TypeUtilities.isConvertibleWithoutLoss(Object.class, String.class), "downcast is lossy");
        // subtype relation: reflexive, transitive through wrappers is NOT assumed
        check(TypeUtilities.isSubtype(Integer.class, Number.class), "Integer <: Number");
        check(!TypeUtilities.isSubtype(Number.class, Integer.class), "Number not<: Integer");
        check(TypeUtilities.isSubtype(int.class, int.class), "reflexive on primitives");
        check(TypeUtilities.isSubtype(int.class, long.class), "primitive widening counts as subtyping");
        check(TypeUtilities.isSubtype(long.class, double.class), "long <: double");
        check(!TypeUtilities.isSubtype(int.class, Integer.class), "primitive and wrapper are unrelated");
    }

    static void testPrimitiveMapping() {
        for (Class<?> p : List.of(boolean.class, byte.class, char.class, short.class, int.class, long.class, float.class, double.class, void.class)) {
            check(TypeUtilities.getPrimitiveTypeByName(p.getName()) == p, "name -> primitive: " + p.getName());
            check(TypeUtilities.isWrapperType(TypeUtilities.getWrapperType(p)),
                  "wrapper for " + p.getName() + ": " + TypeUtilities.getWrapperType(p));
            check(TypeUtilities.getPrimitiveType(TypeUtilities.getWrapperType(p)) == p, "wrapper -> primitive round trip");
        }
        check(TypeUtilities.getPrimitiveTypeByName("nope") == null, "unknown name -> null");
        check(TypeUtilities.getPrimitiveType(Void.class) == void.class, "Void maps back to void");
        check(TypeUtilities.getPrimitiveType(String.class) == null, "non-wrapper -> null");
        check(TypeUtilities.isWrapperType(Integer.class) && TypeUtilities.isWrapperType(Character.class), "wrapper types");
        check(!TypeUtilities.isWrapperType(int.class) && !TypeUtilities.isWrapperType(String.class), "non-wrappers");
        check(TypeUtilities.getWrapperType(Integer.class) == null, "non-primitive wrapper lookup -> null");
    }

    public static void main(String[] args) throws Throwable {
        testWhiteBox();
        testBlackBox();
        testConversionMatrix();
        testPrimitiveMapping();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
