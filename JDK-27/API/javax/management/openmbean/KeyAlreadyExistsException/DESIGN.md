# javax.management.openmbean.KeyAlreadyExistsException — 测试设计

## 1. API 功能
open data 表格键已存在时抛出的受检异常：继承 IllegalArgumentException。两个构造器：无参（null message）与
String message；getMessage 返回传入 message。

## 2. Java 用户如何使用
TabularDataSupport.put(行) 在索引列值已存在（index 重复）时抛 KeyAlreadyExistsException；用户 catch 以处理重复。
链路：put(重复索引行) → throw new KeyAlreadyExistsException → getMessage 断言。

## 3. 开源代码实际用法调研
- JDK 自用：TabularDataSupport.put 在 index 重复时抛 KeyAlreadyExistsException
  （`~/git/jdk25u-dev/src/java.management/share/classes/javax/management/openmbean/`）。
- JDK 测试：`~/git/jdk25u-dev/test/jdk/javax/management/openmbean/KeyAlreadyExistsExceptionTest.java` 覆盖构造与 message。
- 来源为 JDK 源码与 test/jdk（web 不可用）。

## 4. 测试用例设计（KeyAlreadyExistsExceptionTest.java）
- 白盒：public、extends IllegalArgumentException、两个构造器存在、Serializable。
- 黑盒：无参构造 getMessage==null、带 message 构造 getMessage 精确；instanceof IllegalArgumentException；
  TabularDataSupport.put(重复索引) 抛该类型。

## 深化（2026-11，批 G）
- 父类精确 IllegalArgumentException；unchecked；两构造器；SUID=1845183636745282866L；round-trip。
- 触发语义锁定（易错）：
  - putAll 批内同索引（空表）→ "Argument elements values[1] and values[0] have the same indexes,
    calculated according to this TabularData instance's tabularType."
  - put/putAll 撞已有行、Map 视图 put(Object[],v) → "Argument value's index, calculated according
    to this TabularData instance's tabularType, already refers to a value in this table."
  - put 非替换语义：重复索引一律抛（Map 视图也不替换）；remove 后才可重放；putAll 失败零副作用。
- 版本差异：零差异。
