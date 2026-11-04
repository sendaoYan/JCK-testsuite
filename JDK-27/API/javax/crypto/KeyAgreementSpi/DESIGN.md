# javax.crypto.KeyAgreementSpi 设计案

## 1. API 功能
密钥协商 SPI 抽象基类（public abstract，public 无参构造器），定义 6 个 protected abstract 引擎方法（javap 实测）：
- `engineInit(Key, SecureRandom)` / `engineInit(Key, AlgorithmParameterSpec, SecureRandom)`（throws InvalidKeyException/InvalidAlgorithmParameterException）
- `engineDoPhase(Key, boolean)`（throws InvalidKeyException, IllegalStateException；返回中间 Key，可 null）
- `engineGenerateSecret()`（byte[]，IllegalStateException）
- `engineGenerateSecret(byte[], int)`（写入并返回长度，ShortBufferException）
- `engineGenerateSecret(String)`（返回 SecretKey，NoSuchAlgorithmException/InvalidKeyException）

## 2. Java 用户如何使用
密码学提供者作者继承本类并实现全部 6 个引擎方法，再通过 `KeyAgreement` 的 protected 构造器 `KeyAgreement(Spi, Provider, String)` 组装出引擎 façade；终端用户经由 `KeyAgreement.getInstance(...)` 触达。测试视角两条路：直接在子类里加 public 桥接方法调用 protected engine*（验证契约本身）；或用 protected 构造器包装自定义 Spi，经 `KeyAgreement` 公共 API 验证委托关系（init 的 4 个重载分别路由到两个 engineInit、doPhase 转发 key/lastPhase 并回传结果、generateSecret 三个重载原样透传字节与异常）。

## 3. 开源代码实际用法调研
web 搜索在当前环境不可用，采用 JDK 源码与自带测试：
- `~/git/jdk25u-dev/src/java.base/share/classes/sun/security/ec/XDHKeyAgreement.java`、`ECDHKeyAgreement.java`：SunEC 对本 SPI 的标准实现（init 校验密钥类型抛 InvalidKeyException、engineGenerateSecret 未就绪抛 IllegalStateException("Not initialized correctly")、容量检查先行抛 ShortBufferException）。
- `~/git/jdk25u-dev/src/java.base/share/classes/javax/crypto/KeyAgreement.java` L127-138：包装类持有 spi 时 `init(Key,SecureRandom)` → `spi.engineInit(key, random)`、`init(Key,spec,random)` → `spi.engineInit(key, params, random)` 的直接委托；doPhase/generateSecret 同样先判 spi 非空再转发（L560+）。本设计的委托断言即依据此实现。
- `~/git/jdk25u-dev/test/jdk/sun/security/ec/ECDHKeyAgreementParamValidation.java`：对 SPI 实现做参数校验行为测试的样例。

## 4. 测试用例设计 + 自动化实现
`KeyAgreementSpiTest.java`：
- testWhiteBox：public abstract、父类 Object、public 无参构造器、6 个引擎方法按精确签名均为 abstract 且 protected、反射 newInstance 抛 InstantiationException。
- testBlackBox：
  - 包装委托：`WrapperKA extends KeyAgreement`（protected 构造器）+ `RecordingSpi` 记录参数；getAlgorithm/getProvider 属性来自构造器；init(Key) 转发 key 且注入非 null 默认 SecureRandom；init(Key,SecureRandom) 保留 random 身份；init(Key,spec,random) 转发 spec；engine 抛出的 InvalidKeyException/IllegalStateException/ShortBufferException/NoSuchAlgorithmException 均原样穿透；doPhase 转发 key 与 lastPhase（true/false 两轮）并回传哨兵 Key；generateSecret() 字节、generateSecret(buf,off) 偏移写入与长度、generateSecret(String) 算法名与字节。
  - 直接契约：RecordingSpi 的桥接方法直调 engine*（记录参数、默认 doPhase 返回 null、短缓冲 ShortBufferException）。
