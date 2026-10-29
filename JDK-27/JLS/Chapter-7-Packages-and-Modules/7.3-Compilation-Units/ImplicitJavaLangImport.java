/*
 * @test
 * @summary JLS 7.3: every compilation unit implicitly imports the public types
 *          of java.lang (as if "import java.lang.*;" were present), so names
 *          such as String, Object, Integer, and Math are available as simple
 *          names without any explicit import.
 * @run main ImplicitJavaLangImport
 */

public class ImplicitJavaLangImport {
    public static void main(String[] args) {
        String s = "abc";                 // java.lang.String
        Object o = s;                     // java.lang.Object
        Integer i = Integer.valueOf(3);   // java.lang.Integer
        double d = Math.sqrt(16.0);       // java.lang.Math

        if (s.length() != 3) throw new AssertionError("String");
        if (o != s) throw new AssertionError("Object");
        if (i.intValue() != 3) throw new AssertionError("Integer");
        if (d != 4.0) throw new AssertionError("Math");

        System.out.println("PASS");
    }
}
