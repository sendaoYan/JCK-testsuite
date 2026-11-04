/*
 * @test
 * @summary JLS 7.3 (Java SE 8): every compilation unit implicitly imports the
 *          public type names declared in java.lang (as if "import java.lang.*;"
 *          appeared), so String/Object/Integer/Math are usable as simple names.
 * @run main ImplicitJavaLangImport
 */

public class ImplicitJavaLangImport {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        String s = "abc";                 // java.lang.String
        Object o = s;                     // java.lang.Object
        Integer i = Integer.valueOf(3);   // java.lang.Integer
        double d = Math.sqrt(16.0);       // java.lang.Math

        check(s.length() == 3, "String");
        check(o == s, "Object identity");
        check(i.intValue() == 3, "Integer");
        check(d == 4.0, "Math.sqrt");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
