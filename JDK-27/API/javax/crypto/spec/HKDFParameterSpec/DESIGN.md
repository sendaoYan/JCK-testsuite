# javax.crypto.spec.HKDFParameterSpec 设计案

## 1. API 功能
HKDF（RFC 5869）派生参数的顶层接口（JDK 24 定稿，`since 25`），`public interface extends java.security.spec.AlgorithmParameterSpec`：
- 静态工厂：`ofExtract()` 返回新 Builder；`expandOnly(SecretKey prk, byte[] info, int length)` 直接构造 Expand（prk null → NPE、length<=0 → IAE）。
- 嵌套类型：`final class Builder`、`final class Extract`、`final class Expand`、`final class ExtractThenExpand`，后三者均 implements 本接口（各自有独立测试文件）。
- 接口本身定义两个静态方法，无实例方法。

## 2. Java 用户如何使用
`HKDFParameterSpec spec = HKDFParameterSpec.ofExtract().addIKM(ikm).addSalt(salt).thenExpand(info, 42);` → `KDF.getInstance("HKDF-SHA256").deriveData(spec)` 得 OKM，或 `deriveKey("AES", spec)` 得 SecretKey；仅提取阶段用 `.extractOnly()`，仅扩展阶段用 `expandOnly(prk, info, len)`。

## 3. 开源代码实际用法调研
web 搜索不可用，采用 JDK 源码与官方测试（向量均已本机 JDK 27.0.1 实测核对）：
- `src/java.base/share/classes/javax/crypto/spec/HKDFParameterSpec.java` L77-84 类 javadoc snippet：`HKDFParameterSpec.ofExtract().addIKM(label).addIKM(ikm).addSalt(salt).extractOnly()` 等 3 种用法示例。
- `test/jdk/com/sun/crypto/provider/KDF/HKDFKnownAnswerTests.java` L76-95：RFC 5869 TC1（IKM=0x0b×22、salt=0x00..0x0c、info=0xf0..0xf9、L=42 → OKM=3cb25f25...、PRK=07770936...）。
- `src/java.base/share/classes/javax/crypto/KDF.java` L55-66 javadoc：`KDF.getInstance("HKDF-SHA256") + deriveKey("AES", spec)` 主链路。
- 实测补充：`deriveData(extractOnly-spec)` 返回 32 字节 PRK，与 `deriveKey("PRK", spec)` 相等；expandOnly 与 thenExpand 组合结果逐字节相等。

## 4. 测试用例设计 + 自动化实现
`HKDFParameterSpecTest.java` 白盒（11 check）：public interface/无父类/extends AlgorithmParameterSpec、ofExtract/expandOnly 方法名、Builder 与 3 个嵌套 final 类的形态（public final、各自直接 implements HKDFParameterSpec、Extract 私有构造）。黑盒（14 check）场景：
1. 三个实现类实例的 instanceof 层级（HKDFParameterSpec + AlgorithmParameterSpec）。
2. ofExtract() 每次返回独立 Builder，互不串扰。
3. 最强链路：接口静态工厂构造的 ExtractThenExpand → KDF("HKDF-SHA256").deriveData → RFC 5869 TC1 OKM 精确 hex + 长度 42；Extract → deriveKey("PRK") → 精确 PRK hex。
4. 一致性：expandOnly(该 PRK) 复现同一 OKM；重复 derive 确定性。
5. deriveKey("AES", ete)：算法名 AES、编码 42 字节。
