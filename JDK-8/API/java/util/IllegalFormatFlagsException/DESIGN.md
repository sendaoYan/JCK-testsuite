# java.util.IllegalFormatFlagsException 测试设计案（JDK-8） 测试设计案

> 覆盖目标：完整契约：异常层次/构造器与读回方法/Formatter 真实触发路径/序列化兼容。触发源均为内存格式化，纯 headless。

## 1. API 功能

- unchecked 异常：`IllegalFormatFlagsException`，继承 `IllegalFormatException`（`java.lang.RuntimeException` 族）
- 构造器 `(String f)`
- 读回 `getFlags()`

## 2. 使用方式

`String.format(...)`（或 `new Formatter` + `toString()`）用非法格式串触发 → 断言精确异常类型，再直接构造异常对象断言读回方法。

## 3. 参考来源

- Oracle Java SE javadoc `java/util/IllegalFormatFlagsException`（Formatter 规范的异常一节）
- JDK 源码 `java/util/Formatter.java` 各 parse* 触发点

## 4. 测试用例设计

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | Formatter 触发 `%+ d`（+ 与空格同用） | testFormatterTrigger |
| P0 | ctor/getFlags 读回 | testConstructorsAndGetters |
| P0 | 族内 unchecked 继承 | testHierarchy |
| P1 | 序列化 round-trip | testSerializationRoundTrip |
