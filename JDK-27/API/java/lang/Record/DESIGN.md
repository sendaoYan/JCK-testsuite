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

## 契约深化（2026-11）

### API 功能（新增覆盖维度）
九个自造 record 全维契约：toString 精确形式、equals/hashCode 自动实现、null 组件、compact 构造器变换、静态成员/嵌套 record、空/泛型/多组件 record、数组组件语义、序列化、record 模式匹配、局部 record、反射轻断、Record 类层面修饰符。

### 使用方式
```java
record Point(int x, String y) {}
new Point(1, "a").toString()  // "Point[x=1, y=a]"
record Clamped(int v) { public Clamped { v = Math.max(v, 0); } }  // compact ctor
```

### 参考来源
JLS 8.10（record）；JEP 384/395（record 与模式匹配）；java.lang.Record javadoc。

### 测试用例设计 + 易错规范细节
- **toString 形式 `<SimpleName>[k=v, ...]`**；空 record `Empty[]`；null 组件渲染字面 `null`。
- **数组组件是引用语义（易错）**：int[] 不覆写 equals，等值内容的不同数组实例 records 不等；toString 打印 `[I@hash`；访问器返回原数组引用（不拷贝）。
- **record 默认不实现 Serializable**：序列化需显式 `implements java.io.Serializable`；round-trip 产生**新实例**（equals true 但 !=，与 enum 的 == 单例相对——本批对照招牌）；非 Serializable record 直接 writeObject → NotSerializableException。
- **compact 构造器**：赋值即改写组件（new Clamped(-5).v()==0、两个不同负入参 collapse 为相等 record）。
- record 类是 final；getSuperclass()==Record.class；Record 本身 abstract/public/非 final/非 sealed/getPermittedSubclasses()==null/isRecord()==false。
- record 模式匹配 `o instanceof Point(int x, String y)` 与局部 record 均为 16+ 语义。
- getRecordComponents 名/序轻断（深测归 Class 批，不重复）。
- RecordTest.java 现有 PASS 断言：99。
