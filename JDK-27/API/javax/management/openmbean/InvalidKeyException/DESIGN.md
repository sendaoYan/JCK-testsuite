# javax.management.openmbean.InvalidKeyException — 测试设计

## 1. API 功能
open data 键非法时抛出的受检异常：继承 IllegalArgumentException。两个构造器：无参（null message）与
String message；getMessage 返回传入 message。

## 2. Java 用户如何使用
CompositeDataSupport.get(itemName) 在 item 名不存在时抛 InvalidKeyException；用户 catch 它以区分键非法。
链路：get(缺失键) → throw new InvalidKeyException("...") → getMessage 断言。

## 3. 开源代码实际用法调研
- JDK 自用：CompositeDataSupport/TabularDataSupport 在键不存在或非法时抛 InvalidKeyException
  （`~/git/jdk25u-dev/src/java.management/share/classes/javax/management/openmbean/`）。
- JDK 测试：`~/git/jdk25u-dev/test/jdk/javax/management/openmbean/InvalidKeyExceptionTest.java` 覆盖构造与 message。
- 来源为 JDK 源码与 test/jdk（web 不可用）。

## 4. 测试用例设计（InvalidKeyExceptionTest.java）
- 白盒：public、extends IllegalArgumentException、两个构造器存在、Serializable。
- 黑盒：无参构造 getMessage==null、带 message 构造 getMessage 精确；实例 instanceof IllegalArgumentException；
  CompositeDataSupport.get(缺失键) 抛该类型。

## 深化（2026-11，批 G）
- 父类精确 IllegalArgumentException；**不是** KeyAlreadyExistsException 子类（兄弟）；unchecked；
  两构造器；SUID=4224269443946322062L；round-trip。
- 真实触发点精确消息：get("missing") → "Argument key=\"missing\" is not an existing item name
  for this CompositeData instance."；对照 get(null) → 普通 IAE "Argument key cannot be a null or
  empty String."（空/坏 key 与不存在 key 走不同异常）。
- 版本差异：零差异。
