# java.beans.VetoableChangeListener — 设计案

## 1. API 功能
受约束属性监听器接口（extends EventListener），单一方法 `vetoableChange(PropertyChangeEvent) throws PropertyVetoException`。

## 2. Java 用户如何使用
监听器在属性变更前检查并抛 `PropertyVetoException` 拒绝变更。

## 3. 开源代码实际用法调研
JDK 源码 `VetoableChangeListener.java`；`VetoableChangeSupport` 事件模型。

## 4. 测试用例设计 + 自动化实现
白盒：接口 + extends EventListener。黑盒（`testBlackBox`）：匿名实现验证 vetoableChange 分发。

---

## 追加覆盖（契约深化，与 JDK-8 侧同源；JDK-8 与 JDK-27 行为一致，均为实测）

### 新增测试方法
| 方法 | 覆盖维度 |
|---|---|
| testInventory | 精确方法清单：恰好 1 个方法、void、单参、throws 子句**恰好声明 PropertyVetoException**、唯一超接口 EventListener |
| testImplementations | 命名/匿名/lambda 形态 + 事件逐字段断言；Vetoer 直调抛出**同一异常对象** |
| testSupportPassPath | VCS 通过路径：注册顺序=通知顺序、同一事件对象、int 重载装箱 Integer、同值抑制、移除/重复移除、hasListeners、命名注册过滤 |
| testVetoPath | veto 路径：异常**身份**透传给调用方；vetoer 之后的监听器不被调用（居中/居首两种顺序）；回滚事件（old/new 反转）只投递给先于 vetoer 的监听器；RuntimeException 不被包装 |

### 新增易错细节（实测）
- 回滚事件属性名相同、old/new **反转**（NEW->OLD），只给已通过的监听器；vetoer 本人及其后监听器都收不到。
- 调用方收到的是监听器抛出的**同一个**异常对象（非拷贝、非包装）。
- throws 子句属于接口契约：getExceptionTypes() == [PropertyVetoException]。
