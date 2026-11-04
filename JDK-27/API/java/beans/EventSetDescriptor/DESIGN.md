# java.beans.EventSetDescriptor — 设计案

## 1. API 功能
事件集描述符（extends FeatureDescriptor）。getListenerType/getListenerMethods/getAddListenerMethod/getRemoveListenerMethod/isUnicast/isInDefaultEventSet。

## 2. Java 用户如何使用
`new EventSetDescriptor(Source.class, "foo", FooListener.class, "fooHappened")` 描述事件集；IDE/工具据此生成监听器代码。

## 3. 开源代码实际用法调研
JDK 源码 `EventSetDescriptor.java`；`Introspector` 构造之。

## 4. 测试用例设计 + 自动化实现
白盒：extends FeatureDescriptor。黑盒（`testBlackBox`）：构造器推导 listener type/add/remove/listenerMethods 及 unicast/inDefaultEventSet。

## 5. 深化扩展（top-up）
| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 全部 6 个构造器：6 参（name/listenerType 身份/监听方法解析/add/remove 身份/get null/lmd 派生）、7 参（get 名解析；错误参数个数静默 null）、4 参（推导名）、5/6 参 Method[]、MD[] | testSixArgStringCtor/testSevenArgStringCtor/testFourArgDerivedNames/testMethodArrayCtors/testMethodDescriptorArrayCtor |
| P0 | 4 参负例：缺常规 add→IE；事件名不合规→IE；未知监听方法名→IE；**嵌套监听器派生名带 $**→IE | testFourArgNegatives |
| P0 | vetoableChange 特例豁免事件名检查 | testVetoableSpecialCase |
| P0 | Method[] 构造器 null add/remove 合法、**不做监听器类型校验**；MD[] 防御性拷贝、空/null 数组语义；null Method[] → getListenerMethods null | testMethodArrayCtors/testMethodDescriptorArrayCtor/testNullMethodArray |
| P0 | null 矩阵（sourceClass/eventSetName/listenerType/监听方法名 → NPE）；坏名矩阵（未知 add/remove/监听方法名、静态 add → IE） | testNullArgumentMatrix/testBadNameMatrix |
| P1 | unicast：默认 false、构造器不自动检测 throws 子句、**Introspector 检测**（uni 事件集 true / 普通 bean false）；inDefaultEventSet 往返；无公共 setter（6 个禁用名）；恰 6 个公共构造器 | testFlagSemantics/testIntrospectorUnicastDetection/testWhiteBox |
| P1 | 继承行为/toString；非 Serializable | testInheritedFeatureBehavior/testNotSerializable |

### 易错规范细节（双侧实测一致）
- 4 参推导名源于**监听器类简单名**（与 eventSetName 无关）：add/remove + SimpleName、
  get + SimpleName + "s"；监听方法首参类名须以 `capitalize(eventSetName)+"Event"` 结尾否则
  `Method "..." should have argument "..."`；vetoableChange 特例豁免。
- **嵌套监听器接口与 4 参构造器不兼容**（派生名 `addEventSetDescriptorTest$MultiListener` 必 IE）；
  4 参测试须用顶层监听器（PropertyChangeListener/VetoableChangeListener）。
- Introspector 识别要求 add/remove 名以 "Listener" 结尾且监听器参数类名以后缀结尾；
  add throws TooManyListenersException → isUnicast true。
- 7 参对 get 名宽容（不抛）；Method[]/MD[] 无校验；返回描述符数组为 clone。
- 非序列化家族成员。
