/*
 * @test
 * @summary javax.transaction.xa.XAResource conformance: interface shape, supertypes and constants
 * @run main XAResourceTest
 */

import java.lang.reflect.Modifier;

public class XAResourceTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.transaction.xa.XAResource");
        check(c != null, "loads");
        check(c.isInterface(), "is interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        Class<?>[] ifs = c.getInterfaces();
        check(ifs.length == 0, "0 direct superinterface(s)");
        check(c.getDeclaredMethods().length == 10, "declared method count 10");
        java.lang.reflect.Field f_TMENDRSCAN = c.getField("TMENDRSCAN");
        check(f_TMENDRSCAN.getInt(null) == 8388608, "TMENDRSCAN == 8388608");
        java.lang.reflect.Field f_TMFAIL = c.getField("TMFAIL");
        check(f_TMFAIL.getInt(null) == 536870912, "TMFAIL == 536870912");
        java.lang.reflect.Field f_TMJOIN = c.getField("TMJOIN");
        check(f_TMJOIN.getInt(null) == 2097152, "TMJOIN == 2097152");
        java.lang.reflect.Field f_TMNOFLAGS = c.getField("TMNOFLAGS");
        check(f_TMNOFLAGS.getInt(null) == 0, "TMNOFLAGS == 0");
        java.lang.reflect.Field f_TMONEPHASE = c.getField("TMONEPHASE");
        check(f_TMONEPHASE.getInt(null) == 1073741824, "TMONEPHASE == 1073741824");
        java.lang.reflect.Field f_TMRESUME = c.getField("TMRESUME");
        check(f_TMRESUME.getInt(null) == 134217728, "TMRESUME == 134217728");
        java.lang.reflect.Field f_TMSTARTRSCAN = c.getField("TMSTARTRSCAN");
        check(f_TMSTARTRSCAN.getInt(null) == 16777216, "TMSTARTRSCAN == 16777216");
        java.lang.reflect.Field f_TMSUCCESS = c.getField("TMSUCCESS");
        check(f_TMSUCCESS.getInt(null) == 67108864, "TMSUCCESS == 67108864");
        java.lang.reflect.Field f_TMSUSPEND = c.getField("TMSUSPEND");
        check(f_TMSUSPEND.getInt(null) == 33554432, "TMSUSPEND == 33554432");
        java.lang.reflect.Field f_XA_RDONLY = c.getField("XA_RDONLY");
        check(f_XA_RDONLY.getInt(null) == 3, "XA_RDONLY == 3");
        java.lang.reflect.Field f_XA_OK = c.getField("XA_OK");
        check(f_XA_OK.getInt(null) == 0, "XA_OK == 0");        System.out.println("PASS (" + passed + " assertions)");
    }
}
