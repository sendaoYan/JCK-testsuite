# java.beans.VetoableChangeListener — 设计案

## 一、API 功能
受约束（constrained）属性监听器接口：`extends java.util.EventListener`，唯一方法
`void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException`。
JDK-8 与 JDK-27 公开 API 完全一致（javap 双侧实证）。

## 二、使用方式
监听器在属性赋值前审查变更，不满意即抛 `PropertyVetoException` 阻止赋值；
`VetoableChangeSupport.addVetoableChangeListener` 注册。实现形态：命名类、匿名类、lambda。

## 三、参考来源
- Oracle javadoc：Java SE 8 API Specification（`java/beans/VetoableChangeListener.html`）；
  Java SE 27 同名页面（一致）。
- JavaBeans Specification 1.01-A，Constrained Properties 章节（veto 语义、两阶段提交）。
- OpenJDK `test/jdk/java/beans/VetoableChangeSupport/` 回归思路（仅借鉴思路，代码自写）。

## 四、测试用例设计 + 易错规范细节
| 方法 | 覆盖维度 |
|---|---|
| testLegacyInventory | 保留原白盒断言 |
| testInventory | 精确方法清单：恰好 1 个方法、void、单参、throws 子句**恰好声明 PropertyVetoException**、唯一超接口 EventListener |
| testImplementations | 命名/匿名/lambda 形态 + 事件逐字段断言；Vetoer 直调抛出**同一异常对象** |
| testSupportPassPath | VCS 通过路径：注册顺序=通知顺序、同一事件对象、int 重载装箱 Integer、同值抑制、移除/重复移除、hasListeners、命名注册过滤 |
| testVetoPath | veto 路径：调用方收到**同一** PropertyVetoException 对象；vetoer 之后注册的监听器不被调用（vetoer 居中/居首两种顺序）；回滚事件（old/new 反转）只投递给先于 vetoer 通过的监听器；监听器抛 RuntimeException 以同一对象透传（VCS 不包装） |

### 易错规范细节
- throws 子句是接口契约的一部分：`getExceptionTypes()` 恰为 `[PropertyVetoException]`。
- veto 后 VCS 自动发出"回滚事件"（属性名相同、old/new **反转**），只投递给已通过的监听器；
  vetoer 本人及其后注册的监听器都收不到（JDK-8/JDK-27 实测一致）。
- 调用方收到的是监听器抛出的**同一个**异常对象（身份保持，非拷贝）。
- 同值（equals）的 fireVetoableChange 完全不通知（与 PCS 同构）。
