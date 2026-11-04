/*
 * @test
 * @summary sun.security.util.ArrayUtil conformance: type shape + behavioral (headless-safe).
 * @run main ArrayUtilTest
 */

public class ArrayUtilTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.util.ArrayUtil", false, ArrayUtilTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "isInterface");
        check(!c.isEnum(), "isEnum");
        check(!c.isAnnotation(), "isAnnotation");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        byte[] arr = new byte[]{1,2,3,4};
        sun.security.util.ArrayUtil.reverse(arr);
        check(arr[0]==4 && arr[1]==3 && arr[2]==2 && arr[3]==1, "ArrayUtil reverse");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
