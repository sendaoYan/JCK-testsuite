/*
 * @test
 * @summary JLS 11.3: control is transferred to the nearest dynamically enclosing
 *          catch clause that can handle the exception.
 * @run main NearestEnclosingCatch
 */

public class NearestEnclosingCatch {
    public static void main(String[] args) {
        String result = "";
        try {
            try {
                throw new IllegalArgumentException("inner");
            } catch (IllegalArgumentException e) {
                result = "inner-caught";
            }
        } catch (RuntimeException e) {
            result = "outer-caught";
        }
        if (!"inner-caught".equals(result))
            throw new AssertionError("nearest enclosing catch not used");
        System.out.println("PASS");
    }
}
