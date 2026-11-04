# javax.crypto.SecretKey 设计案

## 1. API 功能

对称（秘密）密钥的标记式接口，`public interface SecretKey extends Key, javax.security.auth.Destroyable`：

- **不声明任何方法**（javap 实测 `getDeclaredMethods()` 为空）；`getAlgorithm/getFormat/getEncoded` 继承自 `java.security.Key`，`destroy/isDestroyed` 继承自 `Destroyable`。
- **唯一声明成员**：`public static final long serialVersionUID = -4795878709595146952L`（接口序列化兼容标识，javap/反射实测）。
- 经 `Key` 传递继承 `java.io.Serializable`（非直接父接口）。

## 2. Java 用户如何使用

用户几乎总是以 `SecretKey` 作为类型出现在 API 边界：`Cipher.init(opmode, SecretKey)`、`Mac.init(SecretKey)`、`KeyGenerator.generateKey()` 返回它、`SecretKeyFactory.generateSecret/translateKey` 收发它。得到实例的三条主路：`KeyGenerator.generateKey()`、`SecretKeyFactory.generateSecret(spec)`、直接 `new SecretKeySpec(bytes, algorithm)`。敏感 key 用完可 `destroy()`（默认实现抛 `DestroyFailedException`，覆写实现才会真清除）。

## 3. 开源代码实际用法调研

- Baeldung《Java Symmetric Encryption》/ AES 指南（[baeldung.com/java-aes-encryption-decryption](https://www.baeldung.com/java-aes-encryption-decryption)）：`SecretKey` 由 `KeyGenerator.generateKey()` 产生后传入 `Cipher.init`——本测试"Cipher 往返经接口引用"段同构。
- OWASP Cryptographic Storage Cheat Sheet（[cheatsheetseries.owasp.org](https://cheatsheetseries.owasp.org/cheatsheets/Cryptographic_Storage_Cheat_Sheet.html)）：派生密钥材料经 `SecretKeySpec`→`SecretKey` 使用，并建议用后清零/销毁——对应 destroy 段。
- `~/git/jdk8u-dev/src/java.base/share/classes/javax/crypto/SecretKey.java`：接口全文仅 serialVersionUID（含 javadoc "The name ... returned by getAlgorithm is a String" 等约定）。
- `~/git/jdk8u-dev/src/java.base/share/classes/javax/crypto/spec/SecretKeySpec.java`：`implements KeySpec, SecretKey`——`SecretKeySpec` 即最常见的 SecretKey 实现，且未覆写 `destroy()`（继承 Destroyable 默认实现，抛 `DestroyFailedException`，实测确认）。
- `~/git/jdk8u-dev/src/java.base/share/classes/java/security/Key.java`：`getEncoded`（外部可传输格式，对称密钥 RAW）与 `getFormat`（"RAW"）约定。
- `~/git/jdk8u-dev/test/jdk/javax/crypto/KeyGenerator/CompareKeys.java`：以 `SecretKey` 引用做 `getEncoded` 等价比较与重建（`copy(alg, k)`），证明 RAW 编码往返是官方用法。

## 4. 测试用例设计 + 自动化实现

`SecretKeyTest.java`，`testWhiteBox()`（结构，13 check）：public interface、无父类、恰 2 个直接父接口（`java.security.Key`、`javax.security.auth.Destroyable`）、`getDeclaredMethods()` 为空（自身无新方法）、唯一声明字段 `serialVersionUID` 为 public static final long 且值 `-4795878709595146952L`、传递实现 `Serializable`、`getEncoded` 可经接口调用（继承自 Key）。

`testBlackBox()`（行为，24 check，接口类按 ≥5 要求）场景：

1. `SecretKeySpec` 以接口类型引用：instanceof SecretKey/Key/KeySpec/Serializable；`getAlgorithm/getFormat/getEncoded` 三访问器经接口分派正确。
2. 三条产出路径都得到 SecretKey：`KeyGenerator.generateKey()`（encoded 非空）、`SecretKeyFactory.generateSecret(PBEKeySpec)`（PBKDF2 16 字节）。
3. Destroyable 默认行为（未覆写实现的 SecretKeySpec）：`isDestroyed()==false`；`destroy()` 抛 `DestroyFailedException` 且状态不变。
4. 接口引用驱动 `Cipher AES/GCM` 往返（12 字节固定 IV、128-bit tag），密文长度 = 明文+16。
5. 序列化经接口写出、以 `SecretKey` 读回：材料/算法/format 保持且 `equals` 原对象。
6. 等价性：同字节同算法两个 SecretKey 相等且 hashCode 相同；同字节不同算法不等。
