# java.security.spec.ECPrivateKeySpec — 设计案

## 1. API 功能
EC 私钥的透明规格：`ECPrivateKeySpec(BigInteger s, ECParameterSpec params)` + `getS()/getParams()`；implements KeySpec。

## 2. 使用方式
`KeyFactory.getInstance("EC").generatePrivate(spec)` 的输入；与 ECPublicKeySpec 对应。

## 3. 参考来源
- javadoc（s/params null→NPE）。

## 4. 测试用例设计 + 易错规范细节
- 构造矩阵（实测）：null s→NPE "s is null"；null params→NPE "params is null"。
- **标量无范围校验**（实测）：0、负数、超过 order 的值均接受原样返回（合法性留给使用方）。
- getter 往返；P-256 order 大小标量保真。
- **无 equals/hashCode 覆写**（identity，实测）；implements KeySpec；非 Serializable。
