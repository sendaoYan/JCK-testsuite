# java.util.Formattable 测试设计案（JDK-8）

> 覆盖目标：完整契约：formatTo 回调参数（flags/width/precision）、Formatter 交互、异常路径。

## 1. API 功能

- 接口方法：formatTo(Formatter, int flags, int width, int precision)

## 2. 使用方式

实现 Formattable 的自定义类传给 String.format("%...") → 捕获回调 flags/width/precision 断言与 FormattableFlags 位对齐。

## 3. 参考来源

- Oracle Java SE 8 javadoc `java/util/Formattable`（Formatter 用户自定义格式化协议）
- JDK 源码 Formatter 对 Formattable 的分发

## 4. 测试用例设计

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | %s 回调 flags=0/width/precision 透传 | testDirectCallback |
| P0 | 标志位矩阵（- ↑ # 组合 → FormattableFlags 位） | testFlagDeliveryMatrix |
| P0 | precision 截断约定与 null 参数异常路径 | testNullAndExceptions |
| P1 | 回调内抛异常向调用方传播 | testFormatterReference |
