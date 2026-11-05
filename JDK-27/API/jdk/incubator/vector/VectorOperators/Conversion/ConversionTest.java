/*
 * @test
 * @summary jdk.incubator.vector.VectorOperators.Conversion conformance: conversion operator interface.
 * @modules jdk.incubator.vector
 * @run main ConversionTest
 */

import java.lang.reflect.*;

public class ConversionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testDeepen() {
        jdk.incubator.vector.VectorOperators.Conversion<Integer, Double> i2d = jdk.incubator.vector.VectorOperators.I2D;
        check(i2d.domainType() == int.class && i2d.rangeType() == double.class, "I2D domain/range");
        check(i2d.arity() == 1 && !i2d.isBoolean(), "conversion arity 1");
        check(i2d.name().equals("I2D") && i2d.operatorName().equals("int-C-double"), "I2D name + cast token int-C-double");
        check(jdk.incubator.vector.VectorOperators.REINTERPRET_F2I.domainType() == float.class
                && jdk.incubator.vector.VectorOperators.REINTERPRET_F2I.rangeType() == int.class, "REINTERPRET_F2I");
        check(jdk.incubator.vector.VectorOperators.ZERO_EXTEND_B2I.domainType() == byte.class
                && jdk.incubator.vector.VectorOperators.ZERO_EXTEND_B2I.rangeType() == int.class, "ZERO_EXTEND_B2I");
        check(jdk.incubator.vector.VectorOperators.Conversion.ofCast(int.class, double.class) == i2d, "ofCast returns I2D");
        check(jdk.incubator.vector.VectorOperators.Conversion.ofCast(double.class, double.class)
                .name().equals("COPY_D2D"), "ofCast same type yields COPY_D2D");
        check(jdk.incubator.vector.VectorOperators.Conversion.ofReinterpret(float.class, int.class)
                == jdk.incubator.vector.VectorOperators.REINTERPRET_F2I, "ofReinterpret F2I");
        check(jdk.incubator.vector.VectorOperators.Conversion.ofReinterpret(float.class, double.class)
                .name().equals("REINTERPRET_F2D"), "ofReinterpret widen reinterpret");
        boolean uoe = false;
        try { jdk.incubator.vector.VectorOperators.Conversion.ofCast(int.class, boolean.class); }
        catch (UnsupportedOperationException e) {
            uoe = e.getMessage().equals("Bad vector element type: boolean (should be a primitive type such as byte.class with a known bit-size)");
        }
        check(uoe, "ofCast boolean UOE exact message");
        boolean npe = false;
        try { jdk.incubator.vector.VectorOperators.Conversion.ofCast(null, double.class); } catch (NullPointerException e) { npe = true; }
        check(npe, "ofCast null NPE");
        // behavior: value cast vs bit reinterpret
        check(((jdk.incubator.vector.DoubleVector) jdk.incubator.vector.IntVector.broadcast(
                jdk.incubator.vector.IntVector.SPECIES_128, 7).convert(i2d, 0)).lane(0) == 7.0, "I2D behavior");
        check(((jdk.incubator.vector.IntVector) jdk.incubator.vector.FloatVector.broadcast(
                jdk.incubator.vector.FloatVector.SPECIES_128, 1.0f)
                .convert(jdk.incubator.vector.VectorOperators.REINTERPRET_F2I, 0)).lane(0) == 0x3F800000, "REINTERPRET bits");
        check(((jdk.incubator.vector.IntVector) jdk.incubator.vector.ByteVector.broadcast(
                jdk.incubator.vector.ByteVector.SPECIES_128, (byte) -1)
                .convert(jdk.incubator.vector.VectorOperators.ZERO_EXTEND_B2I, 0)).lane(0) == 255, "ZERO_EXTEND behavior");
    }

    public static void main(String[] a) throws Exception {
        testDeepen();

        Class<?> c = Class.forName("jdk.incubator.vector.VectorOperators$Conversion");
        check(c.isInterface(), "is interface");
        check(Modifier.isPublic(c.getModifiers()), "is public");
        check(jdk.incubator.vector.VectorOperators.Operator.class.isAssignableFrom(c), "extends Operator");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
