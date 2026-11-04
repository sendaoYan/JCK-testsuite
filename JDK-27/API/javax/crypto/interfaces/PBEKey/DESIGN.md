# javax.crypto.interfaces.PBEKey — 测试设计

## 1. API 功能
基于口令的加密（PBE）密钥接口（JDK 1.4 起），继承 `javax.crypto.SecretKey`（从而继承 Key 的 getAlgorithm/getFormat/getEncoded 与 Destroyable 的 destroy/isDestroyed）。

- `char[] getPassword()`：返回口令的拷贝（调用方负责清零）。
- `byte[] getSalt()`：返回盐的拷贝；未指定时可返回 null。
- `int getIterationCount()`：迭代次数；未指定时为 0。
- 字段：`long serialVersionUID = -1430015993304333921L`（JDK 24 起 @Deprecated，仍在；JDK-27 javap 确认三个方法全部存在且未废弃）。

## 2. Java 用户如何使用
实例来自 `SecretKeyFactory`（本 JDK 上通用 PBKDF2 变体为 `"PBKDF2WithHmacSHA256"`，要求 PBEKeySpec 显式给 keyLength，单位是位）：

```java
char[] pass = "topsecret12".toCharArray();
byte[] salt = ...16 字节...;
SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
PBEKey key = (PBEKey) skf.generateSecret(new PBEKeySpec(pass, salt, 10000, 256));
key.getIterationCount();   // 10000
key.getSalt();             // == salt（拷贝）
key.getPassword();         // == pass（拷贝）
key.getEncoded();          // 32B PBKDF2 派生密钥，getFormat()=="RAW"
KeySpec spec = skf.getKeySpec(key, PBEKeySpec.class);   // 反向导出口令/盐/迭代数
Mac mac = Mac.getInstance("HmacSHA256"); mac.init(key); // 作为 SecretKey 使用
```

组合链路：`PBEKeySpec 固定输入 → 工厂产 key → 三访问器对账 → 反向 getKeySpec → 相同输入再造一把（equals/编码一致）→ clearPassword 后旧 key 不受影响 → Mac 消费`。

## 3. 开源代码实际用法调研
Web 搜索在本环境不可用（WebSearch 无结果返回），按规则改用 JDK 权威源码与自带测试（路径均在 `~/git/jdk25u-dev`）：

- `src/java.base/share/classes/javax/crypto/interfaces/PBEKey.java`：接口定义；javadoc 明确"应返回口令/盐的拷贝，清零责任在调用方"——本测试防御性拷贝断言的直接依据。
- `src/java.base/share/classes/com/sun/crypto/provider/PBKDF2KeyImpl.java`（L58 类声明 `implements javax.crypto.interfaces.PBEKey`；L207-244）：`getEncoded()` 返回派生密钥 clone、`getFormat()=="RAW"`、`getAlgorithm()=="PBKDF2With"+prf`、`getPassword()/getSalt()` 均 clone、equals 比较 algorithm/format/编码——本测试全部行为断言的权威来源。
- `test/jdk/javax/crypto/SecretKeyFactory/PBKDF2TranslateTest.java`（L101、L175-186）：官方用例自建 `class MyPBKDF2SecretKey implements PBEKey`，经 `skf.translateKey` 转换并比对 getEncoded/equals——证明"PBEKey 三访问器 + getEncoded 一致性"是该接口的验收方式。
- 本机 JDK-27 实测补充（写入用例时区分两种工厂产物）：
  - `PBKDF2WithHmacSHA256` + 四参 PBEKeySpec → `PBKDF2KeyImpl`，instanceof PBEKey 为真；
  - `PBEWithHmacSHA256AndAES_128` + 三参 PBEKeySpec → `com.sun.crypto.provider.PBEKey`（内部类，仅实现 SecretKey），instanceof javax.crypto.interfaces.PBEKey 为假；
  - PBEKeySpec keyLength 单位是位（32 → 4B 派生密钥），测试用 256 → 32B。

## 4. 测试用例设计 + 自动化实现
`PBEKeyTest.java`，白盒 12 + 黑盒 29 断言。

白盒（testWhiteBox，保留原断言并补充）：
- public/interface/无父类、extends javax.crypto.SecretKey、方法 getPassword/getSalt/getIterationCount 存在（原有）；
- 三个方法返回类型精确为 char[]/byte[]/int，且接口声明方法恰为这三个；
- serialVersionUID 为 public static final long 且值 = -1430015993304333921L（经反射读取，避开源码级 deprecation 警告）。

黑盒（testBlackBox，场景；口令/盐/迭代数/keyLength 全部固定）：
1. 类型家族：工厂产物 instanceof PBEKey 与 SecretKey。
2. 参数访问器：getIterationCount==10000；getSalt 长度 16 且内容等于固定盐；getPassword 等于固定口令。
3. 防御性拷贝：篡改返回的 salt[0]/password[0] 后再次读取不受影响。
4. Key 元数据：getFormat=="RAW"、getAlgorithm=="PBKDF2WithHmacSHA256"、getEncoded 32B 且稳定。
5. 确定性与 equals：相同输入二次工厂产物编码相等、双向 equals、hashCode 相等；不同迭代数/不同口令 → 不等且编码不同。
6. 反向转换：skf.getKeySpec(key, PBEKeySpec) 回读口令/盐/迭代数一致。
7. Destroyable（继承自 SecretKey）：isDestroyed()==false；destroy() 抛 DestroyFailedException 且状态不变。
8. clearPassword 解耦：spec 清口令后，已产出的 key 三访问器/编码不变；对已清 spec 再次 generateSecret 抛 InvalidKeySpecException（cause 为 IllegalStateException）。
9. 组合消费：HmacSHA256 Mac 以该 key 初始化，两次/两把等价 key 的 MAC 确定相等。
10. 边界与对照：PBEKeySpec 迭代数 0 抛 IllegalArgumentException；PBEWithHmacSHA256AndAES_128 工厂产物不是本接口实例（SunJCE 实测对照）。
11. 序列化：经 KeyRep 反序列化仍是 SecretKey，编码与算法保持（Key 级契约）。

确定性：全部输入硬编码；无墙钟/随机/平台默认（charset 显式 UTF-8）。
