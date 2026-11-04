/*
 * @test
 * @summary JLS 8.1.4: a class whose declaration lacks an extends clause has the
 *          direct superclass type Object; Object itself has no superclass.
 * @run main ImplicitObjectSuperclass
 */
public class ImplicitObjectSuperclass {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    public static void main(String[] args) {
        check(ImplicitObjectSuperclass.class.getSuperclass() == Object.class, "implicit direct superclass is Object");
        check(Object.class.getSuperclass() == null, "Object has no direct superclass type");
        check(String.class.getSuperclass() == Object.class, "String extends Object");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
