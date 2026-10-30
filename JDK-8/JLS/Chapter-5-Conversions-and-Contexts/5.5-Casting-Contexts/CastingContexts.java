/*
 * @test
 * @summary JLS 5.5 (Java SE 8): casting may narrow reference types at compile time
 *          and throws ClassCastException at run time when the cast is impossible.
 * @run main CastingContexts
 */

public class CastingContexts {
    public static void main(String[] args) {
        Object o = "str";
        String s = (String) o;
        if (!s.equals("str")) throw new AssertionError("reference cast");

        boolean caught = false;
        try {
            Object n = new Integer(5);
            String bad = (String) n;
        } catch (ClassCastException expected) {
            caught = true;
        }
        if (!caught) throw new AssertionError("ClassCastException expected");

        System.out.println("PASS");
    }
}
