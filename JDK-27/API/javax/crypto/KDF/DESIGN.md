# javax.crypto.KDF 设计案

## 1. API 功能
JDK 24+ 的密钥派生函数（KDF）引擎类，JDK 27 中 `public final`、无公共构造器：
- `getInstance(String alg[, KDFParameters][, Provider|String provider])` ×6 重载（alg、alg+params、alg+provider、alg+params+provider）；Temurin 27 支持 "HKDF-SHA256"/"HKDF-SHA384"/"HKDF-SHA512"。
- `getAlgorithm()` / `getProviderName()` / `getParameters()`（未配置 KDFParameters 时 null）。
- `deriveKey(String alg, AlgorithmParameterSpec spec)`：返回指定算法名的 SecretKey；alg 空 → NoSuchAlgorithmException，null → NPE。
- `deriveData(AlgorithmParameterSpec spec)`：返回原始 OKM 字节；spec null → NPE。
- 延迟 provider 选择：首次 derive 时按 spec 选 provider。
- 派生参数用 `javax.crypto.spec.HKDFParameterSpec.Builder`：`ofExtract().addIKM(...).addSalt(...).thenExpand(info, len)`（合并）、`.extractOnly()`（仅提取 PRK）、静态 `HKDFParameterSpec.expandOnly(prk, info, len)`（仅扩展）。

## 2. Java 用户如何使用
`KDF kdf = KDF.getInstance("HKDF-SHA256");` → 构造 spec：`HKDFParameterSpec spec = HKDFParameterSpec.ofExtract().addIKM(ikm).addSalt(salt).thenExpand(info, 42);` → `kdf.deriveKey("AES", spec)` 得 AES 密钥或 `kdf.deriveData(spec)` 得原始字节 → 分离场景先 `extractOnly()` 得 PRK 再 `expandOnly(prk, info, len)`（两者组合与合并调用结果逐字节相等）。

## 3. 开源代码实际用法调研
web 搜索不可用，采用 JDK 源码与官方测试（向量均已本机 JDK 27.0.1 实测核对）：
- `src/java.base/share/classes/javax/crypto/KDF.java` L55-66 javadoc 示例：`KDF kdfHkdf = KDF.getInstance("HKDF-SHA256"); ... SecretKey sKey = kdfHkdf.deriveKey("AES", derivationSpec);`。
- `test/jdk/com/sun/crypto/provider/KDF/HKDFKnownAnswerTests.java` L76-200：RFC 5869 TC1（IKM=0x0b×22、salt=0x00..0x0c、info=0xf0..0xf9、L=42，OKM=3cb25f25...，PRK=07770936...）三种模式 extractOnly/expandOnly/thenExpand 全断言。
- `test/jdk/com/sun/crypto/provider/KDF/HKDFDelayedPRK.java` L67-68：`KDF.getInstance("HKDF-SHA256").deriveData(HKDFParameterSpec.expandOnly(prk, null, 32))` —— PRK 复用模式。
- `test/jdk/com/sun/crypto/provider/KDF/HKDFSaltIKMTest.java` L50-81：`addIKM(byte[]|SecretKey)`/`addSalt(...)` 等价性与确定性。
- 实测补充：thenExpand(info, 0) → IllegalArgumentException；L=255*32+1 → InvalidAlgorithmParameterException（HKDF 上限 255×HashLen）；deriveKey("AES", 42B spec) 得 42 字节 "AES" 密钥（不校验算法位长）；getProviderName()=="SunJCE"。

## 4. 测试用例设计 + 自动化实现
`KDFTest.java` 白盒（12 check）：public final/无公共构造器/6 方法名/6 个 getInstance 重载/deriveKey throws NoSuchAlgorithmException/deriveData 返回 byte[]。黑盒（28 check）场景：
1. 保留旧断言（算法名/provider 非空/params null/32B AES deriveKey/deriveData 长度）+ deriveKey 编码 == deriveData。
2. RFC 5869 TC1 合并调用 OKM 精确 hex + 确定性重复。
3. extractOnly 得精确 PRK；expandOnly(prk) 复现同一 OKM；分离 == 合并。
4. deriveKey("AES", TC1) 与 deriveData 逐字节相等且算法名 AES。
5. RFC 5869 TC2（80B IKM/salt/info，L=82）与 TC3（空 salt+空 info，L=42）精确 OKM。
6. 4 种 getInstance 重载算法名一致、null KDFParameters 保持 getParameters null、全部产出相同 OKM、providerName SunJCE。
7. 异常：未知算法/未知 provider/空 alg/ null alg/ null spec/ L=0 / L=8161 的异常类型精确。
