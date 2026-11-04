# java.security.spec.ECPublicKeySpec — 设计案

## 1. API 功能
EC 公钥的透明规格：`ECPublicKeySpec(ECPoint w, ECParameterSpec params)` + `getW()/getParams()`；implements KeySpec。

## 2. 使用方式
`KeyFactory.getInstance("EC").generatePublic(spec)` 的输入；与 ECPrivateKeySpec 对应。

## 3. 参考来源
- javadoc（w/params null→NPE）。

## 4. 测试用例设计 + 易错规范细节
- 构造矩阵（实测）：null w→NPE "w is null"；null params→NPE "params is null"；**w==POINT_INFINITY→IAE "w is ECPoint.POINT_INFINITY"（实测，javadoc 未明说）**。
- getter 往返：w 坐标值、params.equals；两次构造同输入互不干扰。
- **无 equals/hashCode 覆写**（identity，实测）；equals null false。
- implements KeySpec；非 Serializable；toString 为 Object 默认形式。
