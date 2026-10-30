/*
 * @test
 * @summary JLS 10.7/10.8/10.9 (Java SE 8): arrays have a public length member,
 *          inherit Object members such as clone(), have a Class object, and an
 *          array of char is not a String.
 * @run main ArrayMembersTest
 */

public class ArrayMembersTest {
    public static void main(String[] args) {
        int[] a = {10, 20, 30};
        if (a.length != 3) throw new AssertionError("public length member");

        int[] b = a.clone();
        if (b == a || b.length != 3 || b[0] != 10) throw new AssertionError("clone");

        Class<?> c = a.getClass();
        if (c != int[].class) throw new AssertionError("array class object");
        if (!c.getName().equals("[I")) throw new AssertionError("array class name");

        char[] chars = {'a', 'b'};
        String str = "ab";
        if (chars.equals(str)) throw new AssertionError("char[] is not a String");
        if (!String.valueOf(chars).equals(str)) throw new AssertionError("String.valueOf(char[])");
        if (!(chars instanceof Object)) throw new AssertionError("array is an Object");

        System.out.println("PASS");
    }
}
