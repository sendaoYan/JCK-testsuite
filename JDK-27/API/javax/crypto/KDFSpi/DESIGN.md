# javax.crypto.KDFSpi 设计案

## 1. API 功能
JDK 24+ 的 KDF 服务提供者基类，JDK 27 javap：`public abstract class KDFSpi`：
- 唯一构造器 `protected KDFSpi(KDFParameters)`（声明抛 InvalidAlgorithmParameterException），供子类配置算法级参数。
- 3 个 `protected abstract` 方法：`engineGetParameters()` → KDFParameters；`engineDeriveKey(String, AlgorithmParameterSpec)` → SecretKey（throws InvalidAlgorithmParameterException, NoSuchAlgorithmException）；`engineDeriveData(AlgorithmParameterSpec)` → byte[]（throws InvalidAlgorithmParameterException）。
- implNote 契约：可提取密钥的 `engineDeriveKey(...).getEncoded()` 应与 `engineDeriveData(...)` 内容一致。
- `KDF.getInstance(alg, params)` 经 `Service.newInstance(params)` 调用本类构造器完成配置。

## 2. Java 用户如何使用
自定义 KDF provider：`class MyKdfSpi extends KDFSpi { MyKdfSpi(KDFParameters p) { super(p); } ... 三方法实现 ... }` → 注册 Provider → 用户经 `KDF.getInstance` 使用。测试视角：写返回固定材料的子类（可把 protected 方法放宽为 public 以便外部断言），断言配置经构造器往返、三方法多态分派、调用计数、构造器拒绝非法配置的异常路径；再以内置 HKDF（也是 KDFSpi 实现）验证契约。

## 3. 开源代码实际用法调研
web 搜索不可用，采用 JDK 源码与官方测试：
- `src/java.base/share/classes/javax/crypto/KDFSpi.java`：构造器与三抽象方法声明、deriveKey 的 @implNote（getEncoded == deriveData 内容）。
- `src/java.base/share/classes/javax/crypto/KDF.java` L527-600：`theOne.spi().engineDeriveKey(alg, derivationSpec)` / `engineDeriveData(derivationSpec)` —— KDF 公共 API 的唯一委派点；`Service.newInstance(kdfParameters)` 把配置传给 SPI 构造器。
- `src/java.base/share/classes/com/sun/crypto/provider/HKDFKeyDerivation.java`：SunJCE 的 KDFSpi 真实实现（构造器对非 null KDFParameters 抛 IAPE，engineDeriveKey 按 alg 名包 SecretKeySpec，engineDeriveData 返回原始 OKM）。
- `test/jdk/com/sun/crypto/provider/KDF/HKDFBasicFunctionsTest.java` / `HKDFExhaustiveTest.java`：经 KDF 公共 API 间接驱动该 SPI 的全量行为。

## 4. 测试用例设计 + 自动化实现
`KDFSpiTest.java` 白盒（12 check）：保留旧 4 项（public/abstract class/无公共构造器/3 方法名）+ 唯一构造器 protected 且参数为 KDFParameters + 三方法 protected abstract 精确签名 + engineDeriveKey 声明 NoSuchAlgorithmException。黑盒（12 check）场景：
1. 配置往返：Fixed7Spi(cfg).engineGetParameters()==cfg；null 配置回读 null。
2. engineDeriveKey("AES")：算法名透传、7B 固定字节；空 alg → NoSuchAlgorithmException（子类自抛自断）。
3. implNote 契约：engineDeriveData == engineDeriveKey 的 getEncoded。
4. 第二实现 Fixed5Spi：独立固定值、无共享状态、三方法各被调用精确次数（1/2/1 计数断言）。
5. RejectingSpi：非 null 配置时构造器抛 InvalidAlgorithmParameterException("unsupported parameters")（消息精确），null 配置接受。
6. 内置 HKDF 同为 KDFSpi：同 spec 两次 deriveData 确定（16B）。
