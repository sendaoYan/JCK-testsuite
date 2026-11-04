# java.security.spec.ECFieldFp — 设计案

## 1. API 功能
素域 Fp 字段：`ECFieldFp(BigInteger p)` + `getP()`/`getFieldSize()`；equals/hashCode 值语义。

## 2. 使用方式
`new ECFieldFp(p)` → `EllipticCurve(field, a, b)`；getFieldSize()==p.bitLength()。

## 3. 参考来源
- javadoc（p 需为正；未强制素性校验）。
- NIST SP 800-186 P-256 素数。

## 4. 测试用例设计 + 易错规范细节
- 构造矩阵：null→NPE；p<=0（负/0）→IAE "p is not positive"；**p==1 合法**（只查 signum）；复合数（22）合法——**不做素性校验**。
- getFieldSize==bitLength：23→5、2→2、P-256 p→256、1→1。
- equals/hashCode：同 p 跨实例 true 且 hashCode 相等；异 p/null/异类型 false；不同来源等值 BigInteger 实例相等。
- 非 Serializable；toString 为 Object 默认形式（类名@hash）。
