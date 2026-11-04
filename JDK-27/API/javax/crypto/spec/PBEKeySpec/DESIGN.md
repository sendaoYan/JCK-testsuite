# javax.crypto.spec.PBEKeySpec 设计案

## 1. API 功能
口令型密钥材料规范（实现 `java.security.spec.KeySpec`，since 1.4）。javap 确认 JDK-27 共 3 个公共构造器、5 个 public final 方法（无 isDestroyed/Destoryable 方法）：
- 构造：`PBEKeySpec(char[] password)`（null/空 → 空 char[]，getSalt==null、iterationCount/keyLength==0）；`PBEKeySpec(char[], byte[] salt, int iterationCount)`；`PBEKeySpec(char[], byte[] salt, int iterationCount, int keyLength)`。password/salt 均克隆。
- 校验：salt null → NPE、salt 空 → IAE、iterationCount<=0 → IAE、keyLength<=0（4 参）→ IAE。
- 生命周期/查询：`clearPassword()`（幂等，清零后置 null）；`getPassword()`（每次新数组；已清 → IllegalStateException）；`getSalt()`（克隆或 null）；`getIterationCount()`；`getKeyLength()`。清口令后 salt/iters/keyLen 仍可读。

## 2. Java 用户如何使用
```java
PBEKeySpec spec = new PBEKeySpec(password, salt, 2048, 256);
SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
SecretKey key = f.generateSecret(spec);          // PBKDF2 派生，可复现
spec.clearPassword();                             // 用毕清零
// 派生 key 可直接作 AES key：
Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
c.init(ENCRYPT_MODE, new SecretKeySpec(key.getEncoded(), "AES"), new GCMParameterSpec(128, iv));
```
典型链路：构造 spec → 工厂派生（两次派生相等）→ 派生密钥驱动对称加密往返 → clearPassword → getPassword 抛 ISE；已清 spec 再送工厂 → InvalidKeySpecException（cause ISE，SecretKeyFactory.generateSecret 包装，见 jdk25u-dev SecretKeyFactory.java L348-353）。

## 3. 开源代码实际用法调研
- JDK 自带测试 `~/git/jdk25u-dev/test/jdk/javax/crypto/spec/PBEKeySpec/PBEKeySpecRacing.java`：clearPassword 与 getPassword 并发竞态（本设计不测竞态，只测顺序生命周期）。
- JDK 自带测试 `test/jdk/com/sun/crypto/provider/Cipher/PBE/PBES2Test.java`（L76-79）：`new PBEKeySpec("mypassword".toCharArray())` + SecretKeyFactory 生成 PBEKey。
- JDK 源码 `src/java.base/share/classes/javax/crypto/spec/PBEKeySpec.java`（L82-172）：三构造器克隆与校验分支；（L178-201）clearPassword/getPassword。
- Web 调研（DuckDuckGo 搜索）：`SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")` + `new PBEKeySpec(password, salt, 10000+, 256)` 是 Spring Security/Baeldung/Stack Overflow 上口令哈希的标准范式（随机 salt、上万次迭代、Base64/Hex 存储）。

## 4. 测试用例设计 + 自动化实现
`PBEKeySpecTest.java`：
- **白盒**：public/非 final、实现 KeySpec、3 个构造器签名、5 个方法均 public final。
- **黑盒**：
  1. 4 参构造器四 getter + clearPassword→ISE（保留原有 5 断言）；
  2. 防御拷贝：外部改 pw/salt 不影响；getPassword/getSalt 两次非同一引用内容相等；改返回值脱离内部；
  3. 仅口令构造器默认值（salt null、iters 0、keyLen 0）；null/空口令变空数组；3 参构造器 keyLen==0；
  4. 异常矩阵 7 项：null salt ×2 → NPE、空 salt ×2 → IAE、iterationCount=0 → IAE、keyLength=0 → IAE；
  5. 生命周期：清口令后 ISE（幂等两次），salt/iters/keyLen 仍可读；
  6. 真实链路：PBKDF2WithHmacSHA256 固定口令/salt/迭代/长度 → 两次派生相等 + KAT `fd3fe870...d2262a1`（32B）；不同迭代数派生不同密钥；已清 spec 送工厂 → InvalidKeySpecException(cause ISE)；派生密钥作 AES key 完成 GCM 往返。
