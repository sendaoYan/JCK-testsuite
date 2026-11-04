# javax.crypto.Mac 设计案

## 1. API 功能

MAC（消息认证码）引擎包装类，`Cloneable`。按方法分组：

- **工厂**：`getInstance(String)` / `getInstance(String, String providerName)` / `getInstance(String, Provider)`——按转换名（如 `HmacSHA256`）取引擎实例。
- **查询**：`getAlgorithm()`（规范化算法名）、`getProvider()`、`getMacLength()`（字节长度，等于内层散列输出长度）。
- **初始化**：`init(Key)` / `init(Key, AlgorithmParameterSpec)`——HMAC 家族只需密钥，重复 init 会重置状态。
- **数据摄入**：`update(byte)` / `update(byte[])` / `update(byte[], int, int)` / `update(ByteBuffer)`（可堆/直接/只读缓冲）。
- **输出**：`doFinal()` / `doFinal(byte[], int)`（写入指定偏移，缓冲不足抛 `ShortBufferException`）/ `doFinal(byte[])`（一次喂入并完成）；`doFinal` 完成后自动重置到 init 后状态。
- **状态**：`reset()`（丢弃已缓冲数据，回到 init 后状态）。
- **克隆**：`clone()`——依赖底层 SPI 的克隆能力，SunJCE HMAC 支持克隆。

## 2. Java 用户如何使用

典型链路：`Mac.getInstance("HmacSHA256")` → `init(new SecretKeySpec(keyBytes, "HmacSHA256"))` → 多次 `update(...)` 分块喂入 → `doFinal()` 得到 32 字节 tag；或直接 `mac.doFinal(data)` 一步完成。验证时对同一数据重算并 `MessageDigest.isEqual` 比较。需要并行计算时 `clone()` 出副本各自 `doFinal`。典型错误链路：未 `init` 就 `update/doFinal` 抛 `IllegalStateException`；给 HMAC 传非对称密钥抛 `InvalidKeyException`。

## 3. 开源代码实际用法调研

（本轮 WebSearch 对 Mac 直查无返回，以下用 JDK 自带测试与源码，均标注路径。）

- `~/git/jdk8u-dev/test/jdk/com/sun/crypto/provider/Mac/MacKAT.java` L286/291/296：RFC 4231 已知答案，`t("HmacSHA256", STR_HI_THERE, "b0344c61...cff7", BKEY_20)`（key=0x0b×20）、`t("HmacSHA256", STR_WHAT_DO_YA_WANT, "5bdcc146...c3843", s("Jefe"))`、`t("HmacSHA256", DDDATA_50, "773ea91e...565fe", AAKEY_20)`——本测试黑盒前 3 条直接采用这 3 个向量。
- `~/git/jdk8u-dev/test/jdk/javax/crypto/Mac/ByteBuffers.java`（`@run main ByteBuffers HmacSha256`）：`mac.init(key)` 后 `doFinal(t)` 与 `update(ByteBuffer)`（backing-array slice、direct、asReadOnlyBuffer 三种）结果一致性——对应本测试的 ByteBuffer 等价段。
- `~/git/jdk8u-dev/test/jdk/com/sun/crypto/provider/Mac/MacClone.java` L72-112：`(Mac)mac.clone()` 后两者各自 `doFinal()`，断言 tag 相同——对应本测试 clone 独立性段。
- `~/git/jdk8u-dev/test/jdk/javax/crypto/Mac/TestGetInstance.java`：`getInstance(alg)` / `getInstance(alg, "SunJCE")` / `getInstance(alg, provider)` 三变体及 `getProvider()` 一致性——对应本测试 getInstance 段。
- 行为权威源码：`~/git/jdk8u-dev/src/java.base/share/classes/javax/crypto/Mac.java` L94/487/581（`initialized` 标志，未初始化时各 update/doFinal 抛 `IllegalStateException("MAC not initialized")`）。

## 4. 测试用例设计 + 自动化实现

`MacTest.java`，`testWhiteBox()`（结构，17 check）：public 具体类、extends Object、implements Cloneable、protected 构造器 `(MacSpi, Provider, String)`、全部公共方法名存在、`getInstance` 为 static final、`init` 等为 final。

`testBlackBox()`（行为，46 check）场景：

1. RFC 4231 TC1/TC2/TC3 精确 hex（key 0x0b×20、"Jefe"、0xaa×20+0xdd×50）。
2. `getMacLength`：HmacSHA256=32、SHA384=48、SHA512=64（JDK-8 无 HmacSHA3-*，SHA-3 系列为 JDK-9+）。
3. 分块 update（空 update+单字节+区间+整体）与一次性 `doFinal(data)` 等价。
4. `update(ByteBuffer)`：heap/direct/readOnly 三种视图等价。
5. reset 语义：`reset()` 丢弃已缓冲数据；`doFinal` 后自动重置，同 key 同数据重算相同；`doFinal(byte[])` 同。
6. `doFinal(out, off)` 支持偏移写入；31 字节短缓冲抛 `ShortBufferException`。
7. clone：克隆后 getAlgorithm/getMacLength 保持；原件先 doFinal 不影响克隆，tag 相同。
8. 未 init：4 种 update、3 种 doFinal 全部 `IllegalStateException`；getAlgorithm/getProvider/getMacLength/reset 不抛。
9. `init(RSA 公钥)` 抛 `InvalidKeyException`。
10. getInstance 三变体 + `getProvider()` 为 SunJCE；bogus 算法→`NoSuchAlgorithmException`、bogus provider→`NoSuchProviderException`、null 算法→`NullPointerException`、null Provider→`IllegalArgumentException`。
11. 自定义 `MacSpi` 经 protected 构造器组装成 `Mac`：init/update/doFinal/reset 分派计数正确；`clone()` 同类副本。
