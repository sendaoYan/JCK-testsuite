/*
 * @test
 * @summary JLS 4.12.1: a variable of a primitive type always holds a primitive
 *          value of that exact primitive type.
 * @run main VariablesOfPrimitiveType
 */

public class VariablesOfPrimitiveType {
    public static void main(String[] args) {
        byte b = 1;
        short s = 2;
        int i = 3;
        long l = 4L;
        char c = 'c';
        float f = 1.5f;
        double d = 2.5;
        boolean z = true;

        // A primitive variable holds a value of exactly that primitive type.
        if (b != (byte) 1) throw new AssertionError("byte");
        if (s != (short) 2) throw new AssertionError("short");
        if (i != 3) throw new AssertionError("int");
        if (l != 4L) throw new AssertionError("long");
        if (c != 'c') throw new AssertionError("char");
        if (f != 1.5f) throw new AssertionError("float");
        if (d != 2.5) throw new AssertionError("double");
        if (!z) throw new AssertionError("boolean");

        // Primitive variables never hold references.
        // (Type safety prevents assigning an object to a primitive variable.)

        System.out.println("PASS");
    }
}
