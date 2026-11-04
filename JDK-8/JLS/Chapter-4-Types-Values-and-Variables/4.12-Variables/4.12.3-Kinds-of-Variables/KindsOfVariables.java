/*
 * @test
 * @summary JLS 4.12.3 (Java SE 8): there are eight kinds of variables: class
 *          variables, instance variables, array components, method parameters,
 *          constructor parameters, lambda parameters, exception parameters, and
 *          local variables. Each kind is created and initialized as specified.
 * @run main KindsOfVariables
 */

import java.io.IOException;

public class KindsOfVariables {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static int classVar = 1;            // class variable
    int instanceVar = 2;                // instance variable

    // constructor parameter
    KindsOfVariables(int ctorParam) {
        check(ctorParam == 3, "constructor parameter is initialized with the argument");
    }

    // method parameter
    int methodParam(int p) {
        return p;
    }

    public static void main(String[] args) {
        KindsOfVariables k = new KindsOfVariables(3);
        check(classVar == 1, "class variable holds its value");
        check(k.instanceVar == 2, "instance variable holds its value");
        check(k.methodParam(4) == 4, "method parameter is initialized per invocation");

        // array component
        int[] arr = new int[1];
        arr[0] = 5;
        check(arr[0] == 5, "array component is an assignable storage location");

        // lambda parameter
        Runnable r = () -> { int local = 6; check(local == 6, "local variable inside lambda"); };
        // lambda parameter demonstration via a functional interface
        Adder add = (a, b) -> a + b;
        check(add.add(2, 3) == 5, "lambda parameters are initialized per invocation");
        r.run();

        // exception parameter
        try {
            throw new IOException("x");
        } catch (IOException ex) {
            check("x".equals(ex.getMessage()), "exception parameter is initialized with the caught object");
        }

        // local variable
        int localVar = 7;
        check(localVar == 7, "local variable holds its value");

        System.out.println("PASS (" + passed + " assertions)");
    }

    interface Adder { int add(int a, int b); }
}
