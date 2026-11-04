/*
 * @test
 * @summary java.util.MissingResourceException API conformance: getClassName/getKey.
 * @run main MissingResourceExceptionTest
 */

import java.util.MissingResourceException;

public class MissingResourceExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testMissingResourceException() {
        MissingResourceException e = new MissingResourceException("m", "ClassName", "key");
        check(e instanceof RuntimeException, "extends RuntimeException");
        check("ClassName".equals(e.getClassName()), "getClassName");
        check("key".equals(e.getKey()), "getKey");
    }


    // ===================== 契约深化：层次/构造器矩阵/getter 保持 =====================
    static void testMissingResourceContracts() {
        check(MissingResourceException.class.getSuperclass() == RuntimeException.class,
              "extends RuntimeException");
        MissingResourceException e = new MissingResourceException("msg", "cls", "key");
        check("msg".equals(e.getMessage()) && "cls".equals(e.getClassName())
              && "key".equals(e.getKey()), "ctor stores message/class/key");
        try {
            java.lang.reflect.Constructor<MissingResourceException> fourArg =
                  MissingResourceException.class.getDeclaredConstructor(
                      String.class, String.class, String.class, Throwable.class);
            check(fourArg != null, "4-arg ctor present (package-private, Java 7+)");
            try {
                fourArg.setAccessible(true);
                MissingResourceException withCause =
                      fourArg.newInstance("msg", "cls", "key", new IllegalStateException("c"));
                check(withCause.getCause() instanceof IllegalStateException,
                      "4-arg ctor accepts cause when accessible");
                check("cls".equals(withCause.getClassName()) && "key".equals(withCause.getKey()),
                      "4-arg ctor preserves getters");
            } catch (RuntimeException re) {
                // 模块系统未 opens java.util（JDK-9+ 封装）：构造器存在性已验证
                check(re.getClass().getSimpleName().equals("InaccessibleObjectException"),
                      "package-private ctor encapsulated by module system on modern JDKs");
            }
        } catch (NoSuchMethodException nsme) {
            check(false, "4-arg ctor expected present");
        } catch (ReflectiveOperationException re) {
            check(false, "4-arg ctor invocation failed: " + re);
        }
        MissingResourceException nulls = new MissingResourceException(null, null, null);
        check(nulls.getMessage() == null && nulls.getClassName() == null
              && nulls.getKey() == null, "null message/class/key preserved");
    }

    public static void main(String[] args) {
        testMissingResourceException();
        testMissingResourceContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
