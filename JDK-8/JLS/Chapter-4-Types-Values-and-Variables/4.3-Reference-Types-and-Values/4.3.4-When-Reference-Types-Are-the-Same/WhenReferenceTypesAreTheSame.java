/*
 * @test
 * @summary JLS 4.3.4 (Java SE 8): two reference types are the same compile-time
 *          type if they have the same binary name and, recursively, the same
 *          type arguments. At run time, two reference types are the same if
 *          they are the same class/interface (same loader and binary name) or
 *          array types whose components are the same run-time type.
 * @run main WhenReferenceTypesAreTheSame
 */

public class WhenReferenceTypesAreTheSame {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        // Same compile-time type via identical binary name.
        String a = "a";
        String b = "b";
        check(a.getClass() == b.getClass(), "same binary name -> same run-time class");

        // Arrays are the same run-time type iff components are the same type.
        check(new String[0].getClass() == new String[0].getClass(),
              "String[] == String[] run-time type");
        check(new int[0].getClass() == int[].class, "int[] == int[].class");
        Class<?> intArr = int[].class, longArr = long[].class;
        check(intArr != longArr, "int[] and long[] are distinct run-time types");
        Class<?> strArr = String[].class, objArr = Object[].class;
        check(strArr != objArr, "String[] and Object[] are distinct run-time types");
        Class<?> strArr2 = String[][].class;
        check(strArr != strArr2, "String[] and String[][] are distinct");

        // A class type and its array type are distinct.
        Class<?> strCls = String.class;
        check(strCls != strArr, "String and String[] are distinct run-time types");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
