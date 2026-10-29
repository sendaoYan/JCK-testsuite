/*
 * @test
 * @summary JLS 6.1: in a non-generic context a reference to a generic class C
 *          denotes the class C itself, not a parameterized type. A class literal
 *          (item 8) and a static member access via the raw class name (item 13)
 *          are such contexts and involve no unchecked warning.
 * @run main NonGenericContexts
 */

class Box<T> {
    T value;
    static String kind = "box";
}

public class NonGenericContexts {
    public static void main(String[] args) {
        // Class literal of a generic class is a non-generic context.
        if (!Box.class.getName().equals("Box")) throw new AssertionError("Box.class");

        // Static member access via the class name denotes the class, not a raw type.
        if (!Box.kind.equals("box")) throw new AssertionError("Box.kind");

        System.out.println("PASS");
    }
}
