# javax.crypto.spec.HKDFParameterSpec.Extract 设计案

## 1. API 功能
HKDF-Extract（RFC 5869 第一阶段）参数，`final class implements HKDFParameterSpec`，私有构造，经 `HKDFParameterSpec.ofExtract()...extractOnly()` 获取：
- 查询：`ikms()` 返回不可修改 `List<SecretKey>`（按 add 顺序，byte[] 项已转 SecretKeySpec，空 builder → 空表）；`salts()` 同构。
- 语义：实现方应将列表各项拼接为单一 IKM/salt 参与计算；无 salt 等价空 salt（实测）。

## 2. Java 用户如何使用
`HKDFParameterSpec.Extract spec = HKDFParameterSpec.ofExtract().addIKM(ikm).addSalt(salt).extractOnly();` → `kdf.deriveKey("PRK", spec)` 得 PRK（32 字节，算法名 "PRK"）或 `kdf.deriveData(spec)` 得 PRK 原始字节。

## 3. 开源代码实际用法调研
web 搜索不可用，采用 JDK 源码与官方测试（向量均已本机 JDK 27.0.1 实测核对）：
- `src/java.base/share/classes/javax/crypto/spec/HKDFParameterSpec.java` Extract 类（`List.copyOf` 不可变列表、javadoc "unmodifiable List ... in the order they were added"、"Empty arrays are discarded"）。
- `test/jdk/com/sun/crypto/provider/KDF/HKDFKnownAnswerTests.java` L76-87：extractOnly 断言 PRK=077709362c2e32df0ddc3f0dc47bba6390b6c73bb50f9c3122ec844ad7c2b3e5。
- `test/jdk/com/sun/crypto/provider/KDF/HKDFSaltIKMTest.java`：salt/IKM 以 SecretKey 与 byte[] 两种形式给出的等价性；无 salt 与空 salt 等价（本测试实测同款断言）。
- 实测补充：`deriveData(Extract)` 返回 32 字节且与 `deriveKey("PRK", …)` 编码相等；RFC 5869 TC3（空 salt + 无 info）OKM=8da4e775... 精确。

## 4. 测试用例设计 + 自动化实现
`ExtractTest.java` 白盒（8 check）：public final/extends Object/直接 implements HKDFParameterSpec/嵌套归属、ikms/salts 方法名、无公共构造器。黑盒（19 check）场景：
1. 保留旧断言：单 IKM/单 salt size。
2. 空 builder → 两空表；多次 add 保序 + byte[] 项转 SecretKeySpec（getEncoded 精确 + 算法名 "Generic"）；SecretKey 身份保持（ikms/salts 同一引用）。
3. 返回列表不可修改（add → UnsupportedOperationException，两列表各一例）。
4. instanceof 层级。
5. 最强链路：TC1 spec → deriveKey("PRK") 精确 PRK hex + 长度 32；deriveData(spec) 与 deriveKey 编码相等；SecretKey 形式 salt 与 byte[] salt 等价；无 salt == 空 salt；RFC 5869 TC3（IKM=0x0b×22、无 salt、无 info）→ extract+expand 精确 OKM hex。
