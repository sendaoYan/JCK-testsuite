# javax.crypto.spec.HKDFParameterSpec.Expand 设计案

## 1. API 功能
HKDF-Expand（RFC 5869 第二阶段）参数，`final class implements HKDFParameterSpec`，私有构造，经静态工厂 `HKDFParameterSpec.expandOnly(SecretKey prk, byte[] info, int length)` 获取：
- 校验：prk null → NullPointerException("prk must not be null")；length<=0 → IllegalArgumentException("length must be > 0")；info 可为 null（KDF 实现按零长处理）且克隆存储。
- 查询：`prk()`（原引用返回）；`info()`（副本或 null）；`length()`。
- KDF 侧约束（实现层）：prk 短于 HMAC 输出长度（SHA-256 为 32）时 deriveData 抛 InvalidAlgorithmParameterException；length 上限 255×HashLen。

## 2. Java 用户如何使用
先 Extract 得 PRK（`kdf.deriveKey("PRK", extractSpec)`）→ `HKDFParameterSpec.expandOnly(prk, info, 42)` → `kdf.deriveData(spec)` 复现完整 HKDF 输出；适用"PRK 预先算好、多次不同 info 扩展"的延迟扩展场景。

## 3. 开源代码实际用法调研
web 搜索不可用，采用 JDK 源码与官方测试（向量均已本机 JDK 27.0.1 实测核对）：
- `src/java.base/share/classes/javax/crypto/spec/HKDFParameterSpec.java` Expand 类（`this.info = (info == null) ? null : info.clone()`、`length must be > 0`）。
- `test/jdk/com/sun/crypto/provider/KDF/HKDFDelayedPRK.java` L67-68：`KDF.getInstance("HKDF-SHA256").deriveData(HKDFParameterSpec.expandOnly(prk, null, 32))` —— PRK 复用模式（本测试真实链路同款）。
- `test/jdk/com/sun/crypto/provider/KDF/HKDFKnownAnswerTests.java`：TC1 PRK=077709362c2e32df...（本测试展开断言的基准）。
- 实测补充：`expandOnly(new SecretKeySpec(new byte[16],"Generic"), info, 32)` → deriveData 抛 InvalidAlgorithmParameterException（SunJCE 提示不支持该输入）；length=255*32=8160 正常派生。

## 4. 测试用例设计 + 自动化实现
`ExpandTest.java` 白盒（8 check）：public final/extends Object/直接 implements HKDFParameterSpec/嵌套归属、prk/info/length 方法名、无公共构造器。黑盒（19 check）场景：
1. 保留旧断言：prk 32 字节、info 精确、length 32。
2. prk 同一引用；info 防御拷贝 + 每次新数组；length 1 与 255*32=8160；空 info 保留为空；null info → info() null。
3. instanceof 层级（HKDFParameterSpec + AlgorithmParameterSpec）。
4. 异常：prk null NPE、length 0 IAE、length 负 IAE。
5. 最强链路：RFC 5869 TC1 Extract 得精确 PRK（hex 断言）→ expandOnly → deriveData 精确 TC1 OKM → 与 extract-then-expand 合并调用逐字节相等；短 PRK（16 字节）被 KDF 以 InvalidAlgorithmParameterException 拒绝。
