# jdk.jfr.ValueDescriptor 设计案

## 1. API 功能
字段/值描述符：构造（类型+名称+注解），getName/getTypeName/getLabel/isArray。

## 2. Java 用户如何使用
new ValueDescriptor(String.class, "name", annotations) 读取元数据。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.jfr/share/classes/jdk/jfr/ValueDescriptor.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。白盒 关键方法；黑盒 构造 + name/type/label/注解。
