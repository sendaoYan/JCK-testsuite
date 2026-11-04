# java.util.Formatter.BigDecimalLayoutForm 测试设计案（JDK-8）

> 覆盖目标：枚举完整覆盖 + 与 %e/%f 科学/十进制布局的对应关系。

## 1. API 功能

- 枚举常量：SCIENTIFIC / DECIMAL_FLOAT

## 2. 使用方式

Formatter 内部布局策略枚举；通过 Formatter(BigDecimal) 的 %e（科学）与 %f（十进制浮点）输出形态间接锁定。

## 3. 参考来源

- Oracle javadoc `java/util/Formatter.BigDecimalLayoutForm`
- JDK 源码 Formatter 打印 BigDecimal 的布局选择

## 4. 测试用例设计

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 枚举常量集 values()/valueOf 精确（SCIENTIFIC、DECIMAL_FLOAT） | testBigDecimalLayoutForm |
| P0 | %e 产生科学计数形态、%f 产生十进制形态 | testLayoutContracts |
| P0 | 契约深化：枚举常量全集/valueOf/ordinal 稳定 | testLayoutContracts |
