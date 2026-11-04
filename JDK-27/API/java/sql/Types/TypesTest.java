/*
 * @test
 * @summary java.sql.Types conformance: white-box structure + black-box behavior.
 * @run main TypesTest
 */
public class TypesTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    interface Throwing { void run() throws Throwable; }
    static void checkThrows(Class<? extends Throwable> t, String label, Throwing r) {
        try { r.run(); } catch (Throwable x) { check(t.isInstance(x), label + " throws " + t.getSimpleName()); return; }
        throw new AssertionError(label + ": expected " + t.getSimpleName());
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.sql.Types");
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(!c.isAnnotation(), "not annotation");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "no direct superinterface");
        check(c.getDeclaredMethods().length == 0, "declared method count 0");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        int[][] exp = {
            {-7, java.sql.Types.BIT}, {-6, java.sql.Types.TINYINT}, {5, java.sql.Types.SMALLINT},
            {4, java.sql.Types.INTEGER}, {-5, java.sql.Types.BIGINT}, {6, java.sql.Types.FLOAT},
            {7, java.sql.Types.REAL}, {8, java.sql.Types.DOUBLE}, {2, java.sql.Types.NUMERIC},
            {3, java.sql.Types.DECIMAL}, {1, java.sql.Types.CHAR}, {12, java.sql.Types.VARCHAR},
            {-1, java.sql.Types.LONGVARCHAR}, {91, java.sql.Types.DATE}, {92, java.sql.Types.TIME},
            {93, java.sql.Types.TIMESTAMP}, {-2, java.sql.Types.BINARY}, {-3, java.sql.Types.VARBINARY},
            {-4, java.sql.Types.LONGVARBINARY}, {0, java.sql.Types.NULL}, {1111, java.sql.Types.OTHER},
            {2000, java.sql.Types.JAVA_OBJECT}, {2001, java.sql.Types.DISTINCT}, {2002, java.sql.Types.STRUCT},
            {2003, java.sql.Types.ARRAY}, {2004, java.sql.Types.BLOB}, {2005, java.sql.Types.CLOB},
            {2006, java.sql.Types.REF}, {70, java.sql.Types.DATALINK}, {16, java.sql.Types.BOOLEAN},
            {-8, java.sql.Types.ROWID}, {-15, java.sql.Types.NCHAR}, {-9, java.sql.Types.NVARCHAR},
            {-16, java.sql.Types.LONGNVARCHAR}, {2011, java.sql.Types.NCLOB}, {2009, java.sql.Types.SQLXML},
            {2012, java.sql.Types.REF_CURSOR}, {2013, java.sql.Types.TIME_WITH_TIMEZONE},
            {2014, java.sql.Types.TIMESTAMP_WITH_TIMEZONE}, {2015, java.sql.Types.DECFLOAT},
            {2016, java.sql.Types.JSON}
        };
        check(exp.length == 41, "41 constant values checked");
        for (int[] e : exp) check(e[1] == e[0], "Types constant " + e[1]);
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
