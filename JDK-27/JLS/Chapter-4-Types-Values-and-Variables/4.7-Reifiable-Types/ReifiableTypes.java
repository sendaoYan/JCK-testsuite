/*
 * @test
 * @summary JLS 4.7: a type is reifiable if and only if it is a non-generic
 *          class/interface type, a parameterized type with all unbounded
 *          wildcards, a raw type, a primitive type, an array whose element
 *          type is reifiable, or a nested type of reifiable types. Only
 *          reifiable types may follow instanceof or appear in array creation.
 * @run main ReifiableTypes
 */

import java.util.List;
import java.util.ArrayList;

public class ReifiableTypes {
    public static void main(String[] args) {
        // Reifiable: primitive type.
        Object o = Integer.valueOf(1);
        if (!(o instanceof Integer)) throw new AssertionError("primitive type is reifiable");

        // Reifiable: non-generic class type.
        o = new String();
        if (!(o instanceof String)) throw new AssertionError("non-generic class is reifiable");

        // Reifiable: parameterized type with all unbounded wildcards (List<?>).
        o = new ArrayList<String>();
        if (!(o instanceof List<?>)) throw new AssertionError("List<?> is reifiable");

        // Reifiable: array type whose element type is reifiable.
        o = new String[0];
        if (!(o instanceof String[])) throw new AssertionError("String[] is reifiable");

        // Reifiable: raw type.
        o = new ArrayList();
        if (!(o instanceof List)) throw new AssertionError("raw List is reifiable");

        System.out.println("PASS");
    }
}
