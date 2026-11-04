# javax.crypto.spec.HKDFParameterSpec.ExtractThenExpand 设计案

## 1. API 功能
HKDF 单趟 Extract+Expand（RFC 5869 合并）参数，`final class implements HKDFParameterSpec`，私有构造，经 `Builder.thenExpand(byte[] info, int length)` 获取：
- 查询：`ikms()`/`salts()`（不可修改 List，镜像 builder 状态）；`info()`（副本或 null）；`length()`。
- 校验：length<=0 → IllegalArgumentException；info 可为 null（实现按零长处理）且克隆存储。
- 语义：与"extractOnly 得 PRK 再 expandOnly"完全等价；分段 add 按序拼接。

## 2. Java 用户如何使用
`HKDFParameterSpec spec = HKDFParameterSpec.ofExtract().addIKM(ikm).addSalt(salt).thenExpand(info, 42);` → `KDF.getInstance("HKDF-SHA256").deriveData(spec)` / `deriveKey("AES", spec)` —— 最常用的一趟式派生。

## 3. 开源代码实际用法调研
web 搜索不可用，采用 JDK 源码与官方测试（向量均已本机 JDK 27.0.1 实测核对）：
- `src/java.base/share/classes/javax/crypto/spec/HKDFParameterSpec.java` ExtractThenExpand 类（内部组合 Extract + Expand(null prk)，info/length 委托 Expand 的克隆与校验）与接口 javadoc 第三段 snippet。
- `test/jdk/com/sun/crypto/provider/KDF/HKDFKnownAnswerTests.java` L76-104：RFC 5869 TC1（L=42）与 TC2（80 字节 IKM/salt/info、L=82）向量。
- `test/jdk/com/sun/crypto/provider/KDF/HKDFExhaustiveTest.java`：合并模式与分离模式全组合等价（本测试的 combined==separated 断言同源）。
- 实测补充：TC2 OKM=b11e398d...（82 字节）精确；分段 add（IKM 11+11、salt 5+8）与全量 add 结果逐字节相等；`deriveKey("AES", spec)` 得 32 字节 AES 密钥。

## 4. 测试用例设计 + 自动化实现
`ExtractThenExpandTest.java` 白盒（8 check）：public final/extends Object/直接 implements HKDFParameterSpec/嵌套归属、ikms/salts/info/length 方法名、无公共构造器。黑盒（24 check）场景：
1. 保留旧断言：ikms/salts size、info、length。
2. 多 add 保序（ikms/salts 逐一 getEncoded）；两列表不可修改。
3. info 防御拷贝 + 每次新数组；null info 保留 null；length 上限 255*32。
4. instanceof 层级。
5. 异常：length 0/负 → IAE。
6. 最强链路：RFC 5869 TC1（L=42）与 TC2（L=82）OKM 精确 hex + 长度；合并 == 分离（extractOnly PRK + expandOnly）；分段 add 等价；deriveKey("AES") 32 字节。
