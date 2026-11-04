import java.math.BigInteger;
import java.util.Locale;
import java.util.TimeZone;

public class BigIntegerProbe {
    interface Fn { void run() throws Exception; }
    static void emit(String k, Object v) { System.out.println(k + "=" + v); }
    static void ex(String k, Fn f) {
        try { f.run(); }
        catch (Throwable t) { System.out.println(k + "=exception=" + t.getClass().getName()); }
    }
    public static void main(String[] a) {
        Locale.setDefault(Locale.US);
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
        try {
            BigInteger big = new BigInteger("123456789012345678901234567890");
            emit("add", big.add(BigInteger.ONE).toString());
            emit("multiply", new BigInteger("999999999999").multiply(new BigInteger("999999999999")).toString());
            emit("mod.neg", new BigInteger("-7").mod(new BigInteger("3")).toString());
            emit("remainder.neg", new BigInteger("-7").remainder(new BigInteger("3")).toString());
            emit("gcd", new BigInteger("48").gcd(new BigInteger("18")).toString());
            emit("modInverse", new BigInteger("3").modInverse(new BigInteger("11")).toString());
            ex("modInverse.nonCoprime", () -> emit("modInverse.nonCoprime", new BigInteger("2").modInverse(new BigInteger("4"))));
            emit("bitLength.0", new BigInteger("0").bitLength());
            emit("bitLength.neg1", new BigInteger("-1").bitLength());
            emit("bitLength.big", new BigInteger("256").bitLength());
            emit("toString.radix36", new BigInteger("255").toString(36));
            emit("toString.neg", new BigInteger("-255").toString());
            ex("ctor.radix37", () -> emit("ctor.radix37", new BigInteger("1", 37)));
            ex("ctor.empty", () -> emit("ctor.empty", new BigInteger("")));
            ex("ctor.null", () -> emit("ctor.null", new BigInteger((String) null)));
            emit("valueOf.cache", BigInteger.valueOf(16) == BigInteger.valueOf(16));
            emit("isProbablePrime.97", new BigInteger("97").isProbablePrime(10));
            emit("isProbablePrime.100", new BigInteger("100").isProbablePrime(10));
            emit("shiftLeft", BigInteger.ONE.shiftLeft(100).toString());
            emit("shiftRight", BigInteger.ONE.shiftLeft(100).shiftRight(90).toString());
            emit("pow", new BigInteger("3").pow(20).toString());
            emit("and", new BigInteger("0xFF").and(new BigInteger("0x0F")).toString());
            emit("xor", new BigInteger("0xFF").xor(new BigInteger("0x0F")).toString());
        } catch (Throwable t) { System.out.println("fatal=exception=" + t.getClass().getName()); }
    }
}
