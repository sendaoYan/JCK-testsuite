/*
 * @test
 * @summary JVMS 4.2/4.3 (Java SE 8): the internal form of a name replaces '.' with
 *          '/', while the binary name of a class uses '.', a nested class uses '$',
 *          and an array type is named by its descriptor ("[I", "[Ljava.lang.String;").
 * @run main NamesAndDescriptorsTest
 */

public class NamesAndDescriptorsTest {
    class Inner {}

    public static void main(String[] args) throws Exception {
        // Binary name of a top-level class uses '.' separators.
        if (!"java.lang.String".equals(String.class.getName()))
            throw new AssertionError("binary name: " + String.class.getName());

        // The internal form uses '/' and is visible in the resource path.
        String url = String.class.getResource("String.class").toString();
        if (!url.contains("java/lang/String.class"))
            throw new AssertionError("internal form should use '/': " + url);

        // A nested class uses '$' in its binary name.
        if (!"NamesAndDescriptorsTest$Inner".equals(Inner.class.getName()))
            throw new AssertionError("nested name: " + Inner.class.getName());

        // Array type names are field descriptors.
        if (!"[Ljava.lang.String;".equals(new String[0].getClass().getName()))
            throw new AssertionError("object array descriptor");
        if (!"[I".equals(new int[0].getClass().getName()))
            throw new AssertionError("int array descriptor");

        // Primitive type names are the single-letter descriptor characters.
        if (!"int".equals(int.class.getName())) throw new AssertionError("int name");
        if (!"long".equals(long.class.getName())) throw new AssertionError("long name");

        System.out.println("PASS");
    }
}
