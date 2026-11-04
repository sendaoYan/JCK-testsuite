# java.security.spec.ECField — 设计案

## 1. API 功能
椭圆曲线域字段接口（JDK 1.5+）。单一契约方法 `int getFieldSize()`；无父接口（standalone）。

## 2. 使用方式
由 `ECFieldFp`（素域）/`ECFieldF2m`（二进制域）实现；`EllipticCurve.getField()` 返回本接口类型，按字段类型多态取精度。

## 3. 参考来源
- javadoc `java.security.spec.ECField`（getFieldSize 返回字段精度 bit 数）。
- NIST SEC 2 / X9.62 域参数（P-256、B-163、B-233 精度）。

## 4. 测试用例设计 + 易错规范细节
- 接口形态：`getDeclaredMethods()` 恰为 `[getFieldSize]`（int 返回、无参、public abstract）；无父接口。
- 实现者矩阵：ECFieldFp/ECFieldF2m 实现；EllipticCurve/ECParameterSpec/BigInteger 不实现。
- 多态：`ECField[]` 混装两种实现按各自精度求和（23→5、113→113、ks 域 163）。
- 易错点：getFieldSize 是"bit 精度"不是字节数；Fp 的精度==p.bitLength()（非位串长度+1）。
