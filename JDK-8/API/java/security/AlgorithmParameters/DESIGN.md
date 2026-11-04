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
