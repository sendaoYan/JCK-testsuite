# javax.naming.directory.BasicAttribute 设计案

## 1. API 功能
- 构造器：`BasicAttribute(String id)`、`(String id, Object value)`、`(String id, boolean ordered)`、`(String id, Object value, boolean ordered)`。
- 查询：`getID()`、`get()`（首个值）、`get(int)`、`size()`、`contains(Object)`、`isOrdered()`、`getAll()`（枚举值）。
- 修改：`add(Object)`、`add(int,Object)`、`remove(Object)`、`remove(int)`、`set(int,Object)`、`clear()`。
- 转换/工具：`clone()`、`equals`、`hashCode`、`toString()`。
- Schema 方法（默认抛 `OperationNotSupportedException`）：`getAttributeSyntaxDefinition()`、`getAttributeDefinition()`。

## 2. Java 用户如何使用
典型链路：`new BasicAttribute("cn", "Alice", true)` → `add("Bob")` → `add(0,"Zoe")` → `get(0)` / `contains` → `set(0,...)` → `remove(...)` → `getAll()` 枚举遍历。
无序属性（`ordered=false`）用于集合语义：重复 `add` 返回 false，`add(int,dup)` 抛 `IllegalStateException`。
与 `Attributes`/`ModificationItem`/`SearchResult` 组合使用。

## 3. 开源代码实际用法调研
- 来源：`~/git/jdk25u-dev/src/java.naming/share/classes/javax/naming/directory/BasicAttribute.java`（自身实现语义：`add` 无序去重、`find` 支持数组值逐元素比较、schema 方法默认 `OperationNotSupportedException`）。
- 来源：`~/git/jdk25u-dev/test/jdk` 中 LDAP/JNDI 用例大量使用 `new BasicAttribute("...", value)` 组装目录条目。
- 来源：`com/sun/jndi/ldap/blits/AddTests/AddNewEntry.java` 用 `BasicAttribute` 构造添加条目。

## 4. 测试用例设计
黑盒断言（BasicAttributeTest.testBlackBox）：
- 有序生命周期：构造→add→add(0)→contains→set→remove(int)→remove(Object)→clear。
- 无序语义：重复 add 返回 false、`add(int,dup)` 抛 `IllegalStateException`。
- 边界：`get(99)` 抛 `IndexOutOfBoundsException`。
- Schema 方法默认抛 `OperationNotSupportedException`。
- equals/hashCode 一致性、不同 id 不相等、clone 深拷贝（id/size 一致、实例不同）。
- `getAll()` 有序枚举、`toString()` 含 id。
