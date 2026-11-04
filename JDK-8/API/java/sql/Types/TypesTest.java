/*
 * @test
 * @summary java.sql.Types API conformance: exact XOPEN/SQL:2003/JDBC-defined
 *          values for every public constant (including the negative XOPEN codes,
 *          e.g. BIT=-7, BIGINT=-5), plus reflection checks that every field is a
 *          public static final int and that the JDK-8 constant set is complete.
 * @run main TypesTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

public class TypesTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Map<String, Integer> expected = new HashMap<String, Integer>();
        expected.put("BIT", -7);
        expected.put("TINYINT", -6);
        expected.put("SMALLINT", 5);
        expected.put("INTEGER", 4);
        expected.put("BIGINT", -5);
        expected.put("FLOAT", 6);
        expected.put("REAL", 7);
        expected.put("DOUBLE", 8);
        expected.put("NUMERIC", 2);
        expected.put("DECIMAL", 3);
        expected.put("CHAR", 1);
        expected.put("VARCHAR", 12);
        expected.put("LONGVARCHAR", -1);
        expected.put("DATE", 91);
        expected.put("TIME", 92);
        expected.put("TIMESTAMP", 93);
        expected.put("BINARY", -2);
        expected.put("VARBINARY", -3);
        expected.put("LONGVARBINARY", -4);
        expected.put("NULL", 0);
        expected.put("OTHER", 1111);
        expected.put("JAVA_OBJECT", 2000);
        expected.put("DISTINCT", 2001);
        expected.put("STRUCT", 2002);
        expected.put("ARRAY", 2003);
        expected.put("BLOB", 2004);
        expected.put("CLOB", 2005);
        expected.put("REF", 2006);
        expected.put("DATALINK", 70);
        expected.put("BOOLEAN", 16);
        expected.put("ROWID", -8);
        expected.put("NCHAR", -15);
        expected.put("NVARCHAR", -9);
        expected.put("LONGNVARCHAR", -16);
        expected.put("NCLOB", 2011);
        expected.put("SQLXML", 2009);
        expected.put("REF_CURSOR", 2012);
        expected.put("TIME_WITH_TIMEZONE", 2013);
        expected.put("TIMESTAMP_WITH_TIMEZONE", 2014);

        Field[] fields = java.sql.Types.class.getFields();
        Map<String, Field> byName = new HashMap<String, Field>();
        for (Field f : fields) {
            byName.put(f.getName(), f);
            int mods = f.getModifiers();
            check(Modifier.isPublic(mods), f.getName() + " public");
            check(Modifier.isStatic(mods), f.getName() + " static");
            check(Modifier.isFinal(mods), f.getName() + " final");
            check(f.getType() == int.class, f.getName() + " is int");
        }

        check(fields.length == expected.size(),
                "exact constant count " + fields.length + " == " + expected.size());

        for (Map.Entry<String, Integer> e : expected.entrySet()) {
            Field f = byName.get(e.getKey());
            check(f != null, "has constant " + e.getKey());
            if (f != null) {
                check(f.getInt(null) == e.getValue().intValue(),
                        e.getKey() + " == " + e.getValue() + " (got " + f.getInt(null) + ")");
            }
        }
        System.out.println("PASS (" + passed + " assertions)");
    }
}
