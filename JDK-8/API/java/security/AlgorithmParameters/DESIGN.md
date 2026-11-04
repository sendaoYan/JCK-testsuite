# AlgorithmParameters — 设计案

## 1. API 功能
算法参数编码/解码。getInstance 工厂 + init(spec/bytes)/getEncoded/getParameterSpec/getAlgorithm/getProvider。

## 2. 使用方式
`AlgorithmParameters.getInstance("AES")` → `init(new IvParameterSpec(new byte[16]))` → `getParameterSpec(IvParameterSpec.class)`。

## 3. 参考来源
- javadoc `java.security.AlgorithmParameters`。
- OpenJDK `test/jdk/java/security/AlgorithmParameters/`。
- JCK runtime-8b `api/java_security/AlgorithmParameters/`。

## 4. 测试用例设计 + 易错规范细节
- 工厂矩阵：bogus→NSAE；null→NPE；null Provider/String→IAE；bogus provider→NoSuchProviderException。
- init(IvParameterSpec) → getEncoded 非空、getEncoded("ASN.1") 等长、getParameterSpec 往返。
- 异常：getParameterSpec 错误类→InvalidParameterSpecException；getParameterSpec(null)→NPE；init(null spec)→InvalidParameterSpecException；init(坏字节)→IOException（JDK-8 IOException / JDK-27 EOFException 子类）。

## 5. 契约深化补充（本批）
- 未初始化态机：getParameterSpec → InvalidParameterSpecException、getEncoded → IOException、toString → null（实测）、getAlgorithm 可用。
- init 只允许一次：二次 init → InvalidParameterSpecException already initialized（关键契约）。
- AES IV：DER 18 字节精确、字节级 round-trip、getEncoded 防御拷贝、未知 format 名回退主编码（实测）。DSA：p/q/g 值级 round-trip、编码确定性、空/垃圾 DER → IOException、init(bytes,ASN.1) 变体。
