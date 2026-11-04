# javax.crypto.spec.HKDFParameterSpec.Builder 设计案

## 1. API 功能
HKDF Extract / ExtractThenExpand 参数的构建器（`final class`，私有构造，经 `HKDFParameterSpec.ofExtract()` 获取）：
- 累加：`addIKM(byte[])`/`addIKM(SecretKey)`、`addSalt(byte[])`/`addSalt(SecretKey)`，均返回 this 可链式调用；null 参数 → NPE；byte[] 版转成 `SecretKeySpec(…, "Generic")`（内部克隆）并丢弃零长度数组；SecretKey 版保持引用。
- 构建：`extractOnly()` 产出不可变 Extract；`thenExpand(byte[] info, int length)` 产出不可变 ExtractThenExpand（info 可为 null、克隆存储；length<=0 → IllegalArgumentException）。构建产物是 builder 状态的快照，后续 add 不影响已构建对象。

## 2. Java 用户如何使用
`HKDFParameterSpec.ofExtract().addIKM(ikm).addSalt(salt).extractOnly()` 或 `.thenExpand(info, 42)` → 交给 `KDF.getInstance("HKDF-SHA256")`；分段 IKM/salt（TLS 1.3 labeled HKDF 场景）多次 add 按序拼接。

## 3. 开源代码实际用法调研
web 搜索不可用，采用 JDK 源码与官方测试（向量均已本机 JDK 27.0.1 实测核对）：
- `src/java.base/share/classes/javax/crypto/spec/HKDFParameterSpec.java` Builder 类（addIKM `Objects.requireNonNull(ikm, "ikm must not be null")`、零长数组 `if (ikm.length != 0) … else return this`、`List.copyOf` 快照）。
- `test/jdk/com/sun/crypto/provider/KDF/HKDFSaltIKMTest.java` L50-81：`addIKM(byte[]|SecretKey)`/`addSalt(...)` 的等价性与顺序拼接语义（本测试的 split-adds 场景同源）。
- `test/jdk/com/sun/crypto/provider/KDF/HKDFKnownAnswerTests.java` L76-87：RFC 5869 TC1 三模式全断言。
- 实测补充：`thenExpand(new byte[]{0}, 0)` → IllegalArgumentException("length must be > 0")；分段 add 结果与单次 add 全量结果逐字节相等。

## 4. 测试用例设计 + 自动化实现
`BuilderTest.java` 白盒（8 check）：public final/extends Object/嵌套于 HKDFParameterSpec、addIKM/addSalt/extractOnly/thenExpand 方法名、无公共构造器、addIKM 与 addSalt 各 (byte[])/(SecretKey) 两重载。黑盒（30 check）场景：
1. 保留旧断言：链式 add 后 ikms/salts size、thenExpand length、空 builder extractOnly 空列表。
2. 四个 add 均返回 this；多次 add 保序（byte[] 项转 SecretKeySpec 后 getEncoded 逐一精确）。
3. SecretKey 身份保持；byte[] 防御拷贝（add 后改源数组不影响）。
4. 零长度 addIKM/addSalt 被丢弃。
5. 构建快照不可变：extractOnly 后继续 add 不影响已构建对象。
6. 异常：4 个 add null → NPE；thenExpand length 0/负 → IAE；info null 被接受且 info() 返回 null。
7. 最强链路：builder → KDF("HKDF-SHA256")：extractOnly → 精确 RFC 5869 TC1 PRK；thenExpand → 精确 TC1 OKM；分段 IKM(11+11)/salt(5+8) 拼接与全量结果逐字节相等。
