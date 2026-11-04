# javax.crypto.KEM.Encapsulated 设计案

## 1. API 功能
`KEM` 的嵌套 `public final` 值类（JDK 27），公开构造器 `Encapsulated(SecretKey key, byte[] encapsulation, byte[] params)`，三个访问器：
- `key()`：共享密钥 `SecretKey`（不可 null）。
- `encapsulation()`：封装消息字节数组，**每次返回新拷贝**。
- `params()`：可选参数，未指定时为 null，**每次返回新拷贝**。
构造器对入参数组做防御性拷贝；key/encapsulation 为 null 抛 NPE。

## 2. Java 用户如何使用
发送方 `encapsulator.encapsulate()` 拿到本类实例后：`enc.key()` 直接交给 KDF 或 Cipher 用作对称密钥；`enc.encapsulation()` 序列化发给接收方；`enc.params()`（若非 null）随消息传递供接收方 `newDecapsulator(priv, spec)` 用。也可手工构造（如自定义 KEMSpi 返回固定结果）：`new KEM.Encapsulated(secretKey, ct, paramsOrNull)`。接收方以 `decapsulator.decapsulate(e.encapsulation())` 与 `e.key()` 字节比对验证。

## 3. 开源代码实际用法调研
web 搜索不可用，采用 JDK 源码与官方测试：
- `src/java.base/share/classes/javax/crypto/KEM.java` L121-176：实现即 `this.encapsulation = encapsulation.clone(); this.params = params == null ? null : params.clone();`，访问器 `return encapsulation.clone()`；javadoc 注明 "contents of the array are copied to protect against subsequent modification"、"A new copy of the byte array is returned"、key/encapsulation 为 null 抛 NullPointerException。
- `test/jdk/javax/crypto/KEM/KemTest.java` L101-137：`enc.key().getEncoded().length == nSecret`、`enc.encapsulation().length == nEnc`、同一 Encapsulated 多次取值内容一致、`decT.decapsulate(enc.encapsulation())` 与 `enc.key()` 一致。
- `test/jdk/javax/crypto/KEM/RSA_KEM.java`：自定义 KEMSpi 直接 `new KEM.Encapsulated(...)` 组装返回值（第三方 provider 的典型用法）。

## 4. 测试用例设计 + 自动化实现
`EncapsulatedTest.java` 白盒（9 check）：public final、嵌套于 javax.crypto.KEM、公开三元构造器签名、key/encapsulation/params 返回类型。黑盒（21 check）场景：
1. 保留旧断言：手工构造三访问器取值、key() 返回同一 SecretKey 实例。
2. 防御性拷贝：构造后改入参数组不影响访问器；改返回数组不影响后续取值；两次调用返回不同数组对象。
3. null params 合法且 params() 为 null；内容跨调用稳定。
4. null key / null encapsulation → NullPointerException。
5. 真实 DHKEM Encapsulated：key()==Generic 且长度==secretSize()、encapsulation 长度==encapsulationSize()、与解封器联动 `decapsulate(real.encapsulation())` 字节相等；外部篡改 `real.encapsulation()` 返回的数组不影响内部值。
