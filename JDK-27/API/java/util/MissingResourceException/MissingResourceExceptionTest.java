/*
 * @test
 * @summary java.util.MissingResourceException conformance: white-box structure + black-box behavior.
 * @run main MissingResourceExceptionTest
 */

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class MissingResourceExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expectThrows(Runnable r, Class<? extends Throwable> type, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected " + type.getSimpleName());
        } catch (Throwable t) {
            if (!type.isInstance(t))
                throw new AssertionError(msg + ": expected " + type.getSimpleName()
                        + " but got " + t.getClass().getSimpleName(), t);
            passed++;
        }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.MissingResourceException");
        check(c.getSuperclass() == RuntimeException.class, "extends RuntimeException");
        c.getDeclaredConstructor(String.class, String.class, String.class);
        check(c.getDeclaredMethod("getClassName").getReturnType() == String.class,
                "getClassName String");
        check(c.getDeclaredMethod("getKey").getReturnType() == String.class, "getKey String");
    }

    static void testBlackBox() {
        MissingResourceException e = new MissingResourceException("msg", "com.foo.Bundle", "k1");
        check("com.foo.Bundle".equals(e.getClassName()), "getClassName");
        check("k1".equals(e.getKey()), "getKey");
        check("msg".equals(e.getMessage()), "getMessage");

        expectThrows(() -> ResourceBundle.getBundle("no.such.b9x", Locale.ROOT),
                MissingResourceException.class, "missing bundle");
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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testMissingResourceContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
