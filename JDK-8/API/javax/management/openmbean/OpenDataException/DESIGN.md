# javax.management.openmbean.OpenDataException — 测试设计

## 1. API 功能
checked 异常（extends JMException → Exception，非 RuntimeException）。构造链仅 ()/(String) 两个
公开构造器，无 cause 构造形态；SUID=8346311255433349870L。

## 2. Java 用户典型用法
开放数据结构构建期的可恢复校验失败：CompositeType 重复 item 名、CompositeDataSupport 值错型/
item 名不匹配、OpenMBean*InfoSupport 约束校验（default/legal/min/max 不满足开放类型）。

## 3. 参考来源
- Oracle javadoc；OpenJDK 源码各触发点（CompositeDataSupport/OpenMBeanParameterInfoSupport 构造）。

## 4. 测试用例设计 + 易错细节（OpenDataExceptionTest，批 G）
- 层级/checked 性质/两构造器数锁定；initCause 单次语义；序列化 round-trip 消息保留 + SUID 锁定。
- **真实触发点（精确消息）**：
  - 值错型："Argument value of wrong type for item a: value str, type javax.management.openmbean.SimpleType(name=java.lang.Integer)"
  - item 名不匹配："Item names do not match CompositeType…"（前缀断言）
  - Info 构造错型 default：消息含 "defaultValue"
- 版本差异：两侧零差异。
