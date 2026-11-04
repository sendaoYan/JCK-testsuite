# java.security.spec.EllipticCurve — 设计案

## 1. API 功能
椭圆曲线 `y^2 = x^3 + ax + b`：构造 `(field,a,b)` / `(field,a,b,seed)`；`getField/getA/getB/getSeed`；equals/hashCode 值语义。

## 2. 使用方式
作为 ECParameterSpec 的曲线分量；seed 为可选位串（生成该曲线参数的可选种子，ANSI X9.62）。

## 3. 参考来源
- javadoc（field null→NPE）。
- NIST P-256 曲线系数与 seed。

## 4. 测试用例设计 + 易错规范细节
- 构造矩阵（实测）：field null→NPE "field is null"；**a null→NPE "first coefficient is null"、b null→NPE "second coefficient is null"（javadoc 未声明，实测抛）**；四参 seed null 合法（getSeed()==null）。
- **equals/hashCode 不比较 seed**（实测）：3 参 vs 4 参同 field/a/b 相等；不同 seed 仍相等；field/a/b 任一不同→不等。
- 防御拷贝：构造传入 seed 数组改后不影响；getSeed() 每次返回新拷贝（两次调用引用不同）。
- F2m 域曲线与 Fp 域曲线各自值等价。
- 非 Serializable；toString 为 Object 默认形式。
