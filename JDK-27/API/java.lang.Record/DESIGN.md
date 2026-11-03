# Record — 设计方案

## 1. API 功能
`java.lang.Record`（abstract）是所有 record 类型的超类，声明三个抽象方法 `equals/hashCode/toString`（由编译器为具体 record 生成），protected 无参构造器。

## 2. Java 用户如何使用
- 声明 `record Point(int x, int y) {}` 自动得到访问器、值语义 `equals/hashCode/toString`。
```java
record Point(int x, int y) {}
Point p = new Point(1, 2);
p.equals(new Point(1, 2));  // true
```

## 3. 开源代码实际用法调研
- Java 16+ 的 record 广泛用于 DTO/值对象；JDK `test/jdk/java/lang/Record` 系列验证 record 语义与反射 `isRecord/getRecordComponents`。

## 4. 测试用例设计 + 自动化实现
白盒：`Class.forName("java.lang.Record")` 验证 abstract、父类 `Object`、protected 构造器、抽象三方法。
黑盒：`instanceof Record`；访问器；值语义 `equals/hashCode/toString`；`isRecord`；`getRecordComponents`。
对应 `<Simple>Test.java` 黑盒部分。
