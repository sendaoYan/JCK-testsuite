# java.security.spec.ECParameterSpec — 设计案

## 1. API 功能
EC 域参数集合：`ECParameterSpec(curve, generator, order, cofactor)` + 四 getter；implements AlgorithmParameterSpec。

## 2. 使用方式
KeyPairGenerator/KeyFactory/Signature 的 EC 参数（ECC 公私钥规格携带）。

## 3. 参考来源
- javadoc（curve/generator/order null→NPE；cofactor 非正→IAE）。
- NIST P-256 标准参数（曲线/基点/阶 256bit/余因子 1）。

## 4. 测试用例设计 + 易错规范细节
- 构造矩阵（实测）：null curve→NPE "curve is null"、null g→NPE "g is null"、null n→NPE "n is null"；h=0/-1→IAE "h is not positive"；Integer.MAX_VALUE 合法。
- **无 equals/hashCode 覆写**：等值内容两实例 equals==false（identity 语义，实测记录）。
- NIST P-256 全参数往返：curve equals、order bitLength==256、field size 256。
- 非 Serializable。
