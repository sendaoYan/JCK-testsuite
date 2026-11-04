# java.security.interfaces.ECKey — 设计案

## 1. API 功能
EC 密钥公共父接口：getParams() 返回 ECParameterSpec。公私两侧（ECPublicKey/ECPrivateKey）继承；本身非 Key 非 Serializable。

## 2. 使用方式
`((ECKey) key).getParams()` 读曲线/生成元/阶；ECGenParameterSpec("secp256r1") 初始化后两侧一致。

## 3. 参考来源
- javadoc `java.security.interfaces.ECKey`、`java.security.spec.ECParameterSpec`。
- SEC 2 secp256r1 曲线参数（用于构造记录型 ECParameterSpec）。

## 4. 测试用例设计 + 易错规范细节
- 记录型最小实现：getParams 恒等透传 + null 透传（内置 secp256r1 全参数构造）。
- 继承格：无父接口、非 Key、非 Serializable、无 serialVersionUID；ECPublicKey/ECPrivateKey 可赋值。
- 真实闭环（secp256r1）：两侧均实现 ECKey；ECParameterSpec equals 相同；field size 256、曲线系数 a/b 非空；再生成后参数等价。
- equals：自反、公私不等、与记录实现不等。
- 易错点：ECParameterSpec 实现了 equals（值语义），两侧可直接 equals；fieldSize 来自 curve.getField()。
