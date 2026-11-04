# javax.crypto.KEM.Encapsulator 设计案

## 1. API 功能
`KEM` 的嵌套 `public final` 封装器类（JDK 27），构造器私有，只能经 `KEM.newEncapsulator` 获得：
- `encapsulate()`：等价 `encapsulate(0, secretSize(), "Generic")`，每次调用产生新密钥+新密文。
- `encapsulate(int from, int to, String algorithm)`：取共享密钥 [from,to) 子段并指定算法名（如前 16 字节作 "AES"）；from<0、from>to、to>secretSize 抛 IndexOutOfBoundsException，algorithm null 抛 NPE。
- `secretSize()` / `encapsulationSize()`：可在封装前查询。
- `providerName()`：底层 provider 名。

## 2. Java 用户如何使用
`KEM.Encapsulator e = kem.newEncapsulator(receiverPub);` → 先 `e.secretSize()/encapsulationSize()` 预分配缓冲 → `e.encapsulate()` 或 `e.encapsulate(0, 16, "AES")` 得部分密钥 → `enc.encapsulation()` 发给接收方。每次调用必须重新封装（密文/密钥全新），接收方逐个配对解封。三个 newEncapsulator 重载（无参/SecureRandom/spec+random）产出同等可用的封装器。

## 3. 开源代码实际用法调研
web 搜索不可用，采用 JDK 源码与官方测试：
- `src/java.base/share/classes/javax/crypto/KEM.java` L188-296：`encapsulate()` 委派 `encapsulate(0, secretSize(), "Generic")`，javadoc 示例 "`encapsulate(0, 16, "AES")` uses the first 16 bytes of the shared secret as a 128-bit AES key"；throws 子句 IndexOutOfBoundsException/NullPointerException。
- `test/jdk/javax/crypto/KEM/KemTest.java` L98-148：断言 `encT.providerName()=="SunJCE"`、`encT.encapsulationSize()==enc.encapsulation().length`、`encT.secretSize()==enc.key().getEncoded().length`、两次 `encT.encapsulate()` 密文不同且都解封一致、`encT.encapsulate(0, secretSize(), "AES")`。
- `test/jdk/javax/crypto/KEM/KemInterop.java` L69-73：对同一 KEM 并发取多个 encapsulator，encapsulation 互不相同。

## 4. 测试用例设计 + 自动化实现
`EncapsulatorTest.java` 白盒（11 check）：public final、无公共构造器、5 个方法（providerName/encapsulate×2/secretSize/encapsulationSize）返回类型与参数数。黑盒（22 check）场景：
1. 保留旧 ML-KEM 断言（providerName 非空、secretSize 32、encapsulationSize>0、encapsulate 非 null）。
2. DHKEM X25519：providerName=="SunJCE"、32/32 尺寸、尺寸在封装前后不变。
3. `encapsulate()` 与显式 `(0, secretSize(), "Generic")` 等价（算法名 Generic、长度一致）。
4. `encapsulate(0, 16, "AES")`：算法 AES、16 字节密钥、密文仍为全长。
5. 两次封装密文/密钥均不同且经解封器各自配对成功。
6. SecureRandom 与 null-spec 两个 newEncapsulator 重载产出可 roundtrip 的封装器；不同封装产出不同密钥。
7. from>to、to>secretSize、from<0 → IndexOutOfBoundsException；algorithm null → NullPointerException。
