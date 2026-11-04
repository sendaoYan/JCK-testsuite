/*
 * @test
 * @summary java.sql.JDBCType conformance: enum constants and members.
 * @run main JDBCTypeTest
 */


public class JDBCTypeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.sql.JDBCType");
        check(c.isEnum(), "is enum");
        check(!c.isInterface(), "not interface");
        check(c.getSuperclass() == java.lang.Enum.class, "enum superclass");
        Object[] ec = c.getEnumConstants();
        check(ec.length == 39, "enum constant count");
        check(((java.lang.Enum<?>) ec[0]).name().equals("BIT"), "constant BIT");
        check(((java.lang.Enum<?>) ec[1]).name().equals("TINYINT"), "constant TINYINT");
        check(((java.lang.Enum<?>) ec[2]).name().equals("SMALLINT"), "constant SMALLINT");
        check(((java.lang.Enum<?>) ec[3]).name().equals("INTEGER"), "constant INTEGER");
        check(((java.lang.Enum<?>) ec[4]).name().equals("BIGINT"), "constant BIGINT");
        check(((java.lang.Enum<?>) ec[5]).name().equals("FLOAT"), "constant FLOAT");
        check(((java.lang.Enum<?>) ec[6]).name().equals("REAL"), "constant REAL");
        check(((java.lang.Enum<?>) ec[7]).name().equals("DOUBLE"), "constant DOUBLE");
        check(((java.lang.Enum<?>) ec[8]).name().equals("NUMERIC"), "constant NUMERIC");
        check(((java.lang.Enum<?>) ec[9]).name().equals("DECIMAL"), "constant DECIMAL");
        check(((java.lang.Enum<?>) ec[10]).name().equals("CHAR"), "constant CHAR");
        check(((java.lang.Enum<?>) ec[11]).name().equals("VARCHAR"), "constant VARCHAR");
        check(((java.lang.Enum<?>) ec[12]).name().equals("LONGVARCHAR"), "constant LONGVARCHAR");
        check(((java.lang.Enum<?>) ec[13]).name().equals("DATE"), "constant DATE");
        check(((java.lang.Enum<?>) ec[14]).name().equals("TIME"), "constant TIME");
        check(((java.lang.Enum<?>) ec[15]).name().equals("TIMESTAMP"), "constant TIMESTAMP");
        check(((java.lang.Enum<?>) ec[16]).name().equals("BINARY"), "constant BINARY");
        check(((java.lang.Enum<?>) ec[17]).name().equals("VARBINARY"), "constant VARBINARY");
        check(((java.lang.Enum<?>) ec[18]).name().equals("LONGVARBINARY"), "constant LONGVARBINARY");
        check(((java.lang.Enum<?>) ec[19]).name().equals("NULL"), "constant NULL");
        check(((java.lang.Enum<?>) ec[20]).name().equals("OTHER"), "constant OTHER");
        check(((java.lang.Enum<?>) ec[21]).name().equals("JAVA_OBJECT"), "constant JAVA_OBJECT");
        check(((java.lang.Enum<?>) ec[22]).name().equals("DISTINCT"), "constant DISTINCT");
        check(((java.lang.Enum<?>) ec[23]).name().equals("STRUCT"), "constant STRUCT");
        check(((java.lang.Enum<?>) ec[24]).name().equals("ARRAY"), "constant ARRAY");
        check(((java.lang.Enum<?>) ec[25]).name().equals("BLOB"), "constant BLOB");
        check(((java.lang.Enum<?>) ec[26]).name().equals("CLOB"), "constant CLOB");
        check(((java.lang.Enum<?>) ec[27]).name().equals("REF"), "constant REF");
        check(((java.lang.Enum<?>) ec[28]).name().equals("DATALINK"), "constant DATALINK");
        check(((java.lang.Enum<?>) ec[29]).name().equals("BOOLEAN"), "constant BOOLEAN");
        check(((java.lang.Enum<?>) ec[30]).name().equals("ROWID"), "constant ROWID");
        check(((java.lang.Enum<?>) ec[31]).name().equals("NCHAR"), "constant NCHAR");
        check(((java.lang.Enum<?>) ec[32]).name().equals("NVARCHAR"), "constant NVARCHAR");
        check(((java.lang.Enum<?>) ec[33]).name().equals("LONGNVARCHAR"), "constant LONGNVARCHAR");
        check(((java.lang.Enum<?>) ec[34]).name().equals("NCLOB"), "constant NCLOB");
        check(((java.lang.Enum<?>) ec[35]).name().equals("SQLXML"), "constant SQLXML");
        check(((java.lang.Enum<?>) ec[36]).name().equals("REF_CURSOR"), "constant REF_CURSOR");
        check(((java.lang.Enum<?>) ec[37]).name().equals("TIME_WITH_TIMEZONE"), "constant TIME_WITH_TIMEZONE");
        check(((java.lang.Enum<?>) ec[38]).name().equals("TIMESTAMP_WITH_TIMEZONE"), "constant TIMESTAMP_WITH_TIMEZONE");
        java.util.Set<String> expIfs = new java.util.HashSet<String>();
        expIfs.add("java.sql.SQLType");
        java.util.Set<String> actIfs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) actIfs.add(i.getName());
        check(actIfs.equals(expIfs), "direct interfaces");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getName"), "has method getName");
        check(names.contains("getVendor"), "has method getVendor");
        check(names.contains("getVendorTypeNumber"), "has method getVendorTypeNumber");        System.out.println("PASS (" + passed + " assertions)");
    }
}
