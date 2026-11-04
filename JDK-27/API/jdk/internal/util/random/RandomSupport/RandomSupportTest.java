/*
 * @test
 * @summary jdk.internal.util.random.RandomSupport conformance: internal class reflection.
 * @modules java.base/jdk.internal.util.random:+open
 * @run main RandomSupportTest
 */
public class RandomSupportTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasM(java.lang.reflect.Method[] ms, String n) { for (java.lang.reflect.Method m : ms) if (m.getName().equals(n)) return true; return false; }
    static boolean hasF(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.getName().equals(n)) return true; return false; }
    static boolean hasI(Class<?>[] is, String n) { for (Class<?> i : is) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnum(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.util.random.RandomSupport", false, RandomSupportTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.util.random.RandomSupport"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.lang.reflect.Method[] ms = c.getDeclaredMethods();
        check(ms.length >= 29, "declared methods >= 29");
        check(hasM(ms, "boundedNextDouble"), "method boundedNextDouble");
        check(hasM(ms, "boundedNextFloat"), "method boundedNextFloat");
        check(hasM(ms, "boundedNextInt"), "method boundedNextInt");
        check(hasM(ms, "boundedNextLong"), "method boundedNextLong");
        check(hasM(ms, "checkBound"), "method checkBound");
        check(hasM(ms, "checkRange"), "method checkRange");
        check(hasM(ms, "checkStreamSize"), "method checkStreamSize");
        check(hasM(ms, "computeNextExponential"), "method computeNextExponential");
        check(hasM(ms, "computeNextExponentialSoftCapped"), "method computeNextExponentialSoftCapped");
        check(hasM(ms, "computeNextGaussian"), "method computeNextGaussian");
        check(hasM(ms, "convertSeedBytesToInts"), "method convertSeedBytesToInts");
        check(hasM(ms, "convertSeedBytesToLongs"), "method convertSeedBytesToLongs");
        check(hasM(ms, "initialSeed"), "method initialSeed");
        check(hasM(ms, "mixLea32"), "method mixLea32");
        check(hasM(ms, "mixLea64"), "method mixLea64");
        java.lang.reflect.Field[] fs = c.getDeclaredFields();
        check(fs.length >= 11, "declared fields >= 11");
        check(hasF(fs, "BAD_BOUND"), "field BAD_BOUND");
        check(hasF(fs, "BAD_DISTANCE"), "field BAD_DISTANCE");
        check(hasF(fs, "BAD_FLOATING_BOUND"), "field BAD_FLOATING_BOUND");
        check(hasF(fs, "BAD_RANGE"), "field BAD_RANGE");
        check(hasF(fs, "BAD_SIZE"), "field BAD_SIZE");
        check(hasF(fs, "GOLDEN_RATIO_32"), "field GOLDEN_RATIO_32");
        check(hasF(fs, "GOLDEN_RATIO_64"), "field GOLDEN_RATIO_64");
        check(hasF(fs, "SILVER_RATIO_32"), "field SILVER_RATIO_32");
        check(hasF(fs, "SILVER_RATIO_64"), "field SILVER_RATIO_64");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
