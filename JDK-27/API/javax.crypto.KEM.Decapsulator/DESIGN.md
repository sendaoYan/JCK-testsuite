# javax.crypto.KEM.Decapsulator 设计案

## 1. API 功能
`KEM` 的嵌套 `public final` 解封装器类（JDK 27），构造器私有，只能经 `KEM.newDecapsulator` 获得：
- `decapsulate(byte[] encapsulation)`：等价 `decapsulate(encapsulation, 0, secretSize(), "Generic")`；封装消息长度必须等于 encapsulationSize() 否则抛 `DecapsulateException`；null 抛 NPE。
- `decapsulate(byte[], int from, int to, String algorithm)`：取恢复密钥 [from,to) 子段；from<0、from>to、to>secretSize 抛 IndexOutOfBoundsException，algorithm null 抛 NPE。
- `secretSize()` / `encapsulationSize()`：与对应 Encapsulator 一致。
- `providerName()`：底层 provider 名。

## 2. Java 用户如何使用
接收方 `KEM.Decapsulator d = kem.newDecapsulator(myPriv);` → 收到密文后 `d.decapsulate(ct)` 得共享密钥（与发送方 `enc.key()` 字节相同）→ 需要对称密钥时用 `d.decapsulate(ct, secretSize()-16, secretSize(), "AES")` 取尾部 16 字节。同一密文重复解封结果确定；篡改/错误长度密文抛 DecapsulateException。

## 3. 开源代码实际用法调研
web 搜索不可用，采用 JDK 源码与官方测试：
- `src/java.base/share/classes/javax/crypto/KEM.java` L317-433：`decapsulate(byte[])` 委派 `decapsulate(encapsulation, 0, secretSize(), "Generic")`；javadoc "The size must be equal to the value returned by encapsulationSize(), or a DecapsulateException will be thrown"、示例 "`decapsulate(encapsulation, secretSize()-16, secretSize(), "AES")`"。
- `test/jdk/javax/crypto/KEM/KemTest.java` L121-147：`decT.decapsulate(enc.encapsulation())` 与 `enc.key()` 一致、同一密文重复解封一致、`decT.decapsulate(new byte[enc.encapsulation().length])` 抛 DecapsulateException、`decT1.decapsulate(enc3.encapsulation(), 0, secretSize(), "AES")` 与 `enc3.key()` 一致。
- 实测补充（本机 JDK 27.0.1 探针）：X25519 全零 32B 密文抛 DecapsulateException("Cannot decapsulate")；P-256 合法密文翻转 1 字节即抛（EC 点校验）；X25519 翻转不抛（无点校验），故测试只对 EC 断篡改异常。
- `test/jdk/javax/crypto/KEM/KemInterop.java` L84-99：`decT.decapsulate(enc.encapsulation()).getEncoded().length == secretSize()`。

## 4. 测试用例设计 + 自动化实现
`DecapsulatorTest.java` 白盒（12 check）：public final、无公共构造器、5 方法签名、4 参重载声明 DecapsulateException。黑盒（21 check）场景：
1. 保留旧 ML-KEM 断言（providerName/secretSize 32/encapsulationSize>0/decapsulate 非 null）。
2. DHKEM X25519：providerName=="SunJCE"、32/32、与封装器尺寸一致。
3. 配对解封字节相等；同密文重复解封一致；1 参 == 4 参 `(0, secretSize(), "Generic")`。
4. 子段重载：前 16 字节 AES、后 16 字节 AES，均与全量密钥对应切片相等。
5. 全零/31B/33B 密文 → DecapsulateException；EC P-256 翻转 1 字节 → DecapsulateException（EC 有点校验）。
6. null 密文、from<0、from>to、to>secretSize、algorithm null 的异常类型精确。
