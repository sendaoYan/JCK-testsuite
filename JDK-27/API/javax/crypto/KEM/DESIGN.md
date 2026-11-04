# javax.crypto.KEM 设计案

## 1. API 功能
JDK 21 引入的密钥封装机制（KEM）引擎类，JDK 27 中为 `public final`，无公共构造器，只能经工厂创建：
- `getInstance(String alg)` / `getInstance(String, Provider)` / `getInstance(String, String provider)`：按算法（DHKEM、ML-KEM 等）创建。
- `getAlgorithm()`：返回算法名。
- `newEncapsulator(PublicKey[, SecureRandom][, AlgorithmParameterSpec, SecureRandom])` ×3 重载：发送侧封装器。
- `newDecapsulator(PrivateKey[, AlgorithmParameterSpec])` ×2 重载：接收侧解封装器。
- KEM 对象不可变，可并发复用；provider 选择可延迟到 newEncapsulator/newDecapsulator（按 key 类型）。

## 2. Java 用户如何使用
接收方 `KeyPairGenerator` 生成密钥对并公布公钥 → 发送方 `KEM.getInstance("DHKEM")` → `newEncapsulator(publicKey)` → `encapsulate()` 得 `Encapsulated`（key()+encapsulation()+params()）→ 把封装消息发给接收方 → 接收方 `newDecapsulator(privateKey)` → `decapsulate(encapsulation)` 得相同共享密钥（`Arrays.equals` 校验）。典型链路：`getInstance → newEncapsulator → encapsulate → newDecapsulator → decapsulate`；异常链路：未知算法 `NoSuchAlgorithmException`、未知 provider `NoSuchProviderException`、null key `InvalidKeyException("input key is null")`。

## 3. 开源代码实际用法调研
web 搜索不可用，采用 JDK 源码与官方测试（路径为本机 jdk25u-dev 检出，与 JDK 27 行为一致）：
- `src/java.base/share/classes/javax/crypto/KEM.java`（类 javadoc 完整示例）：`KEM senderKEM = KEM.getInstance("ABC"); KEM.Encapsulator e = senderKEM.newEncapsulator(receiverPublicKey, senderSpec, null); KEM.Encapsulated enc = e.encapsulate();`；接收方 `receiverKEM.newDecapsulator(kp.getPrivate(), receiverSpec).decapsulate(ciphertext)`。
- `test/jdk/javax/crypto/KEM/KemTest.java` L58-147：`KEM.getInstance("DHKEM", "SunJCE")`，EC secp256r1 密钥对上断言 `encT.secretSize()==decT.secretSize()`、两次封装密文不同、`decT.decapsulate(new byte[len])` 抛 `DecapsulateException`、`encapsulate(0, secretSize, "AES")` 部分密钥。
- `test/jdk/javax/crypto/KEM/KemInterop.java` L68-99：同上链路并断言 `providerName()=="SunJCE"`。
- `test/jdk/javax/crypto/KEM/GenLargeNumberOfKeys.java`：大量 ML-KEM 密钥对的封装/解封回归。
- 实测补充（本机 JDK 27.0.1 探针）：X25519 DHKEM secretSize=encapsulationSize=32，P-256 为 32/65，ML-KEM（FIPS 203 final，默认 768）ct=1088；X25519 密文翻转首字节不报错（无点校验），全零/错误长度必抛 DecapsulateException。

## 4. 测试用例设计 + 自动化实现
`KEMTest.java` 白盒（17 check）：public/final/具体类、无公共构造器、3+3+2 重载数、三个嵌套类型 public final。黑盒（30 check）场景：
1. 保留旧 ML-KEM 场景（getAlgorithm/尺寸非空/roundtrip）。
2. DHKEM X25519：算法名、Generic/RAW 32B 密钥、32B 密文、params null、配对解封一致、同密文重复解封一致。
3. 第二次封装产出新密文/新密钥且各自配对解封、两组密钥不同。
4. 全零 32B、31B、33B 密文 → DecapsulateException。
5. EC P-256：secretSize 32 / encapsulationSize 65 / roundtrip。
6. provider 三重载一致，encapsulator/decapsulator providerName=="SunJCE"，同一 KEM 复用二次 roundtrip。
7. 未知算法/未知 provider/null 公私钥异常类型精确。
