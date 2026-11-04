# java.security.spec.ECGenParameterSpec — 设计案

## 1. API 功能
以标准名请求 EC 域参数生成：`ECGenParameterSpec(stdName)` + `getName()`；implements AlgorithmParameterSpec（JDK-8 直接实现，JDK-9+ extends NamedParameterSpec）。

## 2. 使用方式
`KeyPairGenerator.getInstance("EC").initialize(new ECGenParameterSpec("secp256r1"))`。

## 3. 参考来源
- javadoc（stdName null→NPE）。
- SEC/OID 标准曲线名（secp256r1/secp384r1/secp521r1/secp256k1/sect283r1）。

## 4. 测试用例设计 + 易错规范细节
- 构造矩阵（实测）：null→NPE；**空串合法**（getName()==""）；任意名（"BrainPool999"）不校验、原样保留、大小写不规范化。
- getName() 稳定且返回同一实例；标准名逐个往返。
- **无 equals/hashCode 覆写**：等值内容两实例 equals==false（identity，实测）。
- JDK-8 形态断言：superclass==Object（JDK-9+ 变为 NamedParameterSpec——版本差异记录点）。
- 可放入 AlgorithmParameterSpec 集合；非 Serializable。
