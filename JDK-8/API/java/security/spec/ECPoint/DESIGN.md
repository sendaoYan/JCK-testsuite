# java.security.spec.ECPoint — 设计案

## 1. API 功能
仿射坐标点 `(x,y)` + 静态常量 `POINT_INFINITY`；equals/hashCode 值语义。

## 2. 使用方式
`new ECPoint(x,y)` 作为 ECParameterSpec 生成元 / ECPublicKeySpec 公钥点；无穷远点用常量。

## 3. 参考来源
- javadoc（x/y null→NPE；INFINITY 的坐标为 null）。

## 4. 测试用例设计 + 易错规范细节
- 构造：x 或 y null→NPE（"affine coordinate x or y is null"）；INFINITY.getAffineX/Y()==null（**不抛**，是 null 返回）。
- equals/hashCode：同坐标跨实例 true/hashCode 相等；x 异或 y 异→false；与 INFINITY 双向不等；null/异类型 false。
- INFINITY 为静态单例（同实例）；hashCode 稳定。
- 非 Serializable；toString 为 Object 默认形式。
