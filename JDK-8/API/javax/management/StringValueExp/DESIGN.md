# javax.management.StringValueExp 黑盒行为测试设计

## 1. API 功能
- 实现 ValueExp：`getValue()`、`apply(ObjectName)`、`setMBeanServer(MBeanServer)`、`toString()`。

## 2. Java 用户如何使用
代表字符串常量值的 ValueExp，通常由 Query.value(String) 创建。
```java
ValueExp v = new StringValueExp("hello");
String s = ((StringValueExp) v).getValue();
```

## 3. 开源代码实际用法调研
- `test/jdk/javax/management/query/QueryFactory.java`。
- 实现权威：`src/java.management/share/classes/javax/management/StringValueExp.java`。

## 4. 测试用例设计 + 自动化实现
- 黑盒：getValue 返回构造字符串；toString 返回 value；apply 返回 this（或等价 StringValueExp）；setMBeanServer 不抛异常。
- 白盒：public 类、implements ValueExp、implements Serializable、含 getValue/apply。

## 深化（2026-11，批次 e）
新增维度：apply 恒等性、setMBeanServer no-op 实证、默认 equals 语义、序列化与 serialVersionUID。
- apply(任意名) 返回**自身同一实例**（含无参构造实例）；setMBeanServer 是 deprecated no-op（调用后 apply 行为不变）。
- equals 无值语义（Object 同一性）：两个同值实例 equals=false；同一实例 equals=true；hashCode 稳定。
- Query.value(String) 产出 StringValueExp 且 getValue 相等。
- 序列化 round-trip 保值（含 null 值实例）；serialVersionUID 锁定 **-3256390509806284044**（两侧实测一致）。
### 版本差异
- 无。
