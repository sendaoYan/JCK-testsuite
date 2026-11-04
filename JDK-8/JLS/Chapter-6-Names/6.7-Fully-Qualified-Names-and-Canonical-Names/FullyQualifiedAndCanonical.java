/*
 * @test
 * @summary JLS 6.7 (Java SE 8): the fully qualified name of a primitive type is
 *          its keyword; of a named package is its name; of a class/interface is
 *          its package name plus simple name; a member type is qualified by its
 *          enclosing type. A canonical name is undefined for local and
 *          anonymous classes.
 * @run main FullyQualifiedAndCanonical
 */

public class FullyQualifiedAndCanonical {
    static int passed = 0;
    static void check(boolean c, String w) { if (c) passed++; else throw new AssertionError(w); }

    static class Inner {
    }

    public static void main(String[] args) {
        // primitive type fully qualified name
        check(int.class.getName().equals("int"), "primitive type fully qualified name");

        // named package fully qualified name
        check(String.class.getPackage().getName().equals("java.lang"),
              "named package fully qualified name");

        // top-level class fully qualified name
        check(String.class.getName().equals("java.lang.String"),
              "top-level class fully qualified name");

        // member class: binary name uses '$', canonical name uses '.'
        check(Inner.class.getName().endsWith("$Inner"),
              "member class binary name uses '$'");
        check(Inner.class.getCanonicalName().endsWith(".Inner"),
              "member class canonical name uses '.'");

        // local and anonymous classes have no canonical name
        class Local {
        }
        check(Local.class.getCanonicalName() == null, "local class has no canonical name");

        Runnable r = new Runnable() { public void run() { } };
        check(r.getClass().getCanonicalName() == null, "anonymous class has no canonical name");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
