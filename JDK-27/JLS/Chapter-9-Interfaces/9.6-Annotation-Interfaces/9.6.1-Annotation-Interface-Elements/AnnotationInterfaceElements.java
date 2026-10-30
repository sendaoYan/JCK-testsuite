/*
 * @test
 * @summary JLS 9.6.1: an annotation interface element's return type must be a
 *          primitive, String, Class, an enum type, an annotation interface type,
 *          or an array of one of these.
 * @run main AnnotationInterfaceElements
 */

public class AnnotationInterfaceElements {
    enum Level { BAD, INDIFFERENT, GOOD }

    @interface Name {
        String first();
        String last();
    }

    @interface Meta {
        int count();        // primitive
        String label();     // String
        Class<?> type();    // Class
        Level level();      // enum type
        Name name();        // annotation interface type
        String[] tags();    // array type
    }

    public static void main(String[] args) {
        if (Meta.class.getDeclaredMethods().length != 6)
            throw new AssertionError("element count: " + Meta.class.getDeclaredMethods().length);
        System.out.println("PASS");
    }
}
