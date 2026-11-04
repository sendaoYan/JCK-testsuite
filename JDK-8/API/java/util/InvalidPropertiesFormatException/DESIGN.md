# java.util.InvalidPropertiesFormatException 测试设计案（JDK-8）

> 覆盖目标：完整契约：继承 IOException（checked）、两构造器、消息实现相关容忍。

## 1. API 功能

- 构造器：无参 / (String)（消息实现依赖）；继承 java.io.IOException

## 2. 使用方式

按规范抛出场景构造（消息实现相关断言宽松），断言 checked 层次必须捕获。

## 3. 参考来源

- Oracle Java SE 8 javadoc `java/util/InvalidPropertiesFormatException`（XML 加载失败的异常类型）

## 4. 测试用例设计

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 继承 IOException（checked 编译强制捕获） | testIpfContracts |
| P0 | 构造器消息 null-or-derived 容忍 | testIpfContracts |
| P0 | 契约深化：两种构造器语义/cause 无包装 | testIpfContracts |
