/*
 * @test
 * @summary JLS 4.3: there are four kinds of reference types - class types,
 *          interface types, type variables, and array types.
 * @run main ReferenceTypesAndValues
 */

import java.io.Serializable;

interface Named {
    String name();
}

class Point {
    final int x, y;
    Point(int x, int y) { this.x = x; this.y = y; }
}

public class ReferenceTypesAndValues {
    // T is a type variable.
    static <T> T identity(T value) { return value; }

    public static void main(String[] args) {
        // Class type.
        Point p = new Point(1, 2);

        // Interface type.
        Named n = () -> "origin";

        // Type variable: used as a type inside the generic method.
        String s = identity("hello");

        // Array types: primitive component and class component.
        int[] ints = new int[3];
        Point[] points = new Point[2];

        // A class or interface type may have type arguments (parameterized type).
        java.util.List<String> list = new java.util.ArrayList<>();

        if (p.x != 1 || p.y != 2) throw new AssertionError("class type");
        if (!n.name().equals("origin")) throw new AssertionError("interface type");
        if (!s.equals("hello")) throw new AssertionError("type variable");
        if (ints.length != 3 || points.length != 2) throw new AssertionError("array type");
        if (!list.isEmpty()) throw new AssertionError("parameterized type");
        // An array type whose component type is a class type is also a reference type.
        if (!(points instanceof Object)) throw new AssertionError("arrays are references");

        System.out.println("PASS");
    }
}
