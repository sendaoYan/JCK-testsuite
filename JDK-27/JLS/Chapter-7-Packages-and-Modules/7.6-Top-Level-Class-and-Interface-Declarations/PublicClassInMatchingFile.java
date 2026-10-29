/*
 * @test
 * @summary JLS 7.6: a public top level class is declared in a file whose name
 *          matches the class name, and grants access from other packages.
 * @run main PublicClassInMatchingFile
 */

public class PublicClassInMatchingFile {
    public static void main(String[] args) {
        if (PublicClassInMatchingFile.class.getModifiers() == 0)
            throw new AssertionError("not public");

        System.out.println("PASS");
    }
}
