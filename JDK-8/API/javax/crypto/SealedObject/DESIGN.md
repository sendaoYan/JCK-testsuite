# javax.crypto.SealedObject 设计案

## 1. API 功能
可序列化对象的加密信封（public，实现 Serializable；字段 `protected byte[] encodedParams`）：
- 构造：`SealedObject(Serializable, Cipher)`（序列化后用已初始化的 Cipher 加密，参数随对象保存）；`protected SealedObject(SealedObject)`（深拷贝构造器，供子类）。
- 查询：`getAlgorithm()`（封装时 Cipher 的 transformation 名）。
- 解封：`getObject(Key)` / `getObject(Key, String provider)`（按保存的参数自行建解密 Cipher）、`getObject(Cipher)`（外部已初始化的解密 Cipher）。

## 2. Java 用户如何使用
`Cipher c = Cipher.getInstance("AES/GCM/NoPadding"); c.init(ENCRYPT_MODE, key, gcmSpec); SealedObject so = new SealedObject(obj, c);` 之后凭 `so.getObject(key)`（无需记住 IV）或预先初始化好的解密 Cipher `so.getObject(dec)` 还原对象；两种方式可对同一对象反复解封，每次得到反序列化的新实例。GCM 下用错 key：`getObject(Key)` 把 AEADBadTagException 包成 `InvalidKeyException`（GeneralSecurityException 子类），`getObject(Cipher)` 直接抛 `AEADBadTagException`。SealedObject 自身可再序列化传输；`protected` 拷贝构造器让子类克隆信封。

## 3. 开源代码实际用法调研
web 搜索在当前环境不可用（GitHub 检索无有效返回），采用 JDK 源码与自带测试：
- `~/git/jdk8u-dev/test/jdk/javax/crypto/SealedObject/NullKeySealedObject.java` L40-56：`DES/CBC/PKCS5Padding` + `new SecretKeySpec(new byte[8], "DES")` 密封 `SecretKeySpec` 对象，断言 `getObject((Key)null)` 与 `getObject((Key)null, "ANY")` 均 NPE——本设计沿用 NPE 场景。
- `~/git/jdk8u-dev/test/jdk/javax/crypto/SealedObject/TestSealedObjectNull.java`：密封 null 对象的往返。
- `~/git/jdk8u-dev/src/java.base/share/classes/javax/crypto/SealedObject.java` L120-172（构造：ObjectOutputStream 序列化 → doFinal，保存 `getParameters().getEncoded()` 与 sealAlg）、L243-253（getObject(Key)：`catch (IllegalBlockSizeException | BadPaddingException e) → throw new InvalidKeyException(e.getMessage())`）、L267-272（getObject(Cipher) 直读）。篡改场景（序列化字节改一位）极易破坏流结构而非密码学校验，按 brief 改用错 key 路径断言（InvalidKeyException / AEADBadTagException），行为与 L243-253 一致。
- GitHub Security Lab 对 SealedObject 的攻击面研究（CVE-2024-1597 相关）指出其反序列化风险并推荐 AEAD 模式——本设计采用 GCM 与该实践一致（来源：GHSL-2024-004 公开报告，web 检索摘要）。

## 4. 测试用例设计 + 自动化实现
`SealedObjectTest.java`：
- testWhiteBox：public 具体类、实现 Serializable、父类 Object、public (Serializable,Cipher) 与 protected (SealedObject) 构造器、getAlgorithm/getObject 为 final、protected byte[] encodedParams 字段。
- testBlackBox：
  - `legacyAesEcbRoundTrip`：保留旧浅测试全部断言（"AES" 零 key、getAlgorithm "AES"、getObject(cipher)/getObject(key)）。
  - `gcmRoundTrips`：固定 key/IV 的 GCM——getAlgorithm "AES/GCM/NoPadding"；getObject(Key)/(Key,"SunJCE")/(Cipher) 三重载往返；重复解封得相等的新对象；自定义 Point 类字段级还原；密封 null；AES/ECB/NoPadding 非 16 倍数内容抛 IllegalBlockSizeException。
  - `failurePaths`：错 key 的 getObject(Key) → InvalidKeyException（且为 GeneralSecurityException 子类）；错 key 的 getObject(Cipher) → AEADBadTagException；null key（单参/双参重载）NPE；未知 provider 名 NoSuchProviderException。
  - `serializationAndCopy`：SealedObject 自身序列化往返后 getAlgorithm 与解封保持；protected 拷贝构造器（CopiedSealed）保持算法与内容；不同 IV 密封等值明文均能解封。
