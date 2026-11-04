# java.beans.PropertyChangeListener — 设计案

## 一、API 功能
属性变更监听器接口：`extends java.util.EventListener`，唯一方法
`void propertyChange(PropertyChangeEvent evt)`。JDK-8 与 JDK-27 公开 API 完全一致
（javap 双侧实证：均只声明该方法，无默认方法、无静态方法）。

## 二、使用方式
实现 `propertyChange` 接收属性变更通知；`PropertyChangeSupport.addPropertyChangeListener`
注册（全局或按属性名命名注册）。实现形态：命名类、匿名类、lambda、方法引用（Java 8+ 均可）。
按 JavaBeans 规范，监听器通知顺序与其注册顺序一致。

## 三、参考来源
- Oracle javadoc：Java SE 8 API Specification（`java/beans/PropertyChangeListener.html`）；
  Java SE 27 同名页面（内容一致）。
- JavaBeans Specification 1.01-A，Events 章节（监听器注册/通知模型、注册顺序语义）。
- OpenJDK `test/jdk/java/beans/PropertyChangeSupport/` 事件分发回归思路（仅借鉴思路，代码自写）。

## 四、测试用例设计 + 易错规范细节
| 方法 | 覆盖维度 |
|---|---|
| testLegacyInventory | 保留原白盒断言（接口/无父类/extends EventListener/声明 propertyChange） |
| testInventory | 精确方法清单：恰好 1 个声明方法、void 返回、单参 PropertyChangeEvent、public abstract、唯一超接口 EventListener |
| testImplementations | 命名类/匿名类/lambda/方法引用四种形态 + 事件逐字段断言（source/name/old/new/propagationId 默认 null） |
| testSupportIntegration | PCS：注册顺序=通知顺序、所有监听器收到同一事件对象、移除生效、重复移除 no-op、hasListeners、命名注册过滤、null 属性名仍通知全局 |
| testFireOverloads | int/boolean/Object 三个 fire 重载：值装箱 Integer/Boolean、同值抑制、(null,null) 仍通知 |

### 易错规范细节
- 接口 `getDeclaredMethods().length == 1`——没有其他抽象方法或静态常量方法。
- int/boolean 重载把新旧值装箱为 `Integer`/`Boolean` 后投递。
- 同值（`equals` 为真）不触发；但 Object 重载 old=new=null **仍触发**（JDK-8/JDK-27 实测一致）。
- null 属性名的事件仍通知全局监听器（实现上全局键为 null）。
- 通知顺序=注册顺序（列表语义），跨多次触发稳定。
