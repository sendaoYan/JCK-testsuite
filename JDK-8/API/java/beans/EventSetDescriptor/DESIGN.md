# java.beans.EventSetDescriptor 测试设计案

> 覆盖目标：完整契约（全部 6 个构造器、add/remove/get 名推导、事件名合规检查与
> vetoableChange 特例、Method[]/MethodDescriptor[] 无校验语义、unicast 语义、
> null/坏名负例矩阵、无公共 setter、非序列化、Introspector 集成）。headless 安全。

## 一、API 功能
- 事件集描述符（extends FeatureDescriptor）。6 个公共构造器：
  - `(Class, String eventSetName, Class listenerType, String listenerMethodName)`（4 参：名推导）
  - `(Class, String, Class, String[], String addName, String removeName)`（6 参）
  - `(Class, String, Class, String[], String, String, String getName)`（7 参）
  - `(String, Class, Method[], Method add, Method remove)`（5 参 Method 版）
  - `(String, Class, Method[], Method, Method, Method get)`（6 参 Method 版）
  - `(String, Class, MethodDescriptor[], Method, Method)`（MD[] 版）
- 读取器：getListenerType/getListenerMethods/getListenerMethodDescriptors/
  getAddListenerMethod/getRemoveListenerMethod/getGetListenerMethod。
- 语义标志：setUnicast/isUnicast、setInDefaultEventSet/isInDefaultEventSet（默认 true）。
  **没有** listenerType/add/remove/get/listenerMethods 的公共 setter。

## 二、使用方式
- 手工描述事件集（显式名或 Method 对象），或由 Introspector 从 `addXxxListener` 等命名约定推导。
- 4 参构造器的推导规则：`add/remove` + 监听器类简单名、`get` + 简单名 + `"s"`。

## 三、参考来源
- Oracle javadoc Java SE 8（`java/beans/EventSetDescriptor.html`）。
- JavaBeans 规范 1.01-A 第 6.5-6.8 节（事件集命名约定、unicast、TooManyListenersException）。
- OpenJDK `EventSetDescriptor.java`（4 参委托 7 参的显式 null 检查、getMethod 的静态方法拒绝、
  get 名宽容处理；4529996 的 vetoableChange 特例）；`Introspector.getTargetEventInfo`
  （listenerName 必须 "Listener" 结尾、add 抛 TooManyListenersException → setUnicast(true)）。

## 四、测试用例设计
| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 6 参字符串构造器：name/listenerType 身份/监听方法解析到接口/add/remove 身份/get null/lmd 派生 | testSixArgStringCtor |
| P0 | 7 参构造器：get 名解析与身份；get 名对应错误参数个数 → 静默 null | testSevenArgStringCtor |
| P0 | 4 参推导名：add/remove/get 从监听器简单名 + "s" 推导；源缺 getter → get null | testFourArgDerivedNames |
| P0 | 4 参负例：缺常规 add→IE；事件名不合规（"pc"≠PcEvent 后缀）→IE；未知监听方法名→IE；**嵌套监听器接口派生名带 $**→IE | testFourArgNegatives |
| P0 | vetoableChange 特例：监听方法参数名无需匹配事件名 | testVetoableSpecialCase |
| P0 | Method[] 构造器：null add/remove 合法、方法身份保持、**不做监听器类型校验**（外类方法也被接受，记录实测）；6 参带 get 身份 | testMethodArrayCtors |
| P0 | MD[] 构造器：描述符保持/派生 Method 身份/防御性拷贝；空数组长度 0；null 数组 → 两个 getter 均 null | testMethodDescriptorArrayCtor/testNullMethodArray |
| P0 | null 矩阵：sourceClass/eventSetName/listenerType/监听方法名 null → NPE | testNullArgumentMatrix |
| P0 | 坏名矩阵：未知 add/remove/监听方法名、**静态 add** → IntrospectionException | testBadNameMatrix |
| P1 | unicast：默认 false；构造器**不**自动检测 throws TooManyListenersException；Introspector 检测（uni 事件集 unicast=true，普通 bean false） | testFlagSemantics/testIntrospectorUnicastDetection |
| P1 | inDefaultEventSet 默认 true + 双向翻转；无公共 setter（6 个禁用名反射断言）；恰 6 个公共构造器 | testFlagSemantics/testWhiteBox |
| P1 | 继承行为 + toString 非空；非 Serializable → NotSerializableException | testInheritedFeatureBehavior/testNotSerializable |

## 易错规范细节（实测，JDK-8 与 JDK-27 一致）
- 4 参构造器 add/remove/get 名由**监听器类简单名**推导（`PropertyChangeListener` →
  `addPropertyChangeListener`/`getPropertyChangeListeners`），与 eventSetName 无关；
  监听方法必须存在且首参类名以 `capitalize(eventSetName)+"Event"` 结尾，否则
  `IntrospectionException: Method "..." should have argument "..."`；`vetoableChange` 特例豁免。
- **嵌套监听器接口与 4 参构造器不兼容**：派生名变成 `addEventSetDescriptorTest$MultiListener`
  （二进制名参与截取），必然 Method not found → IE。4 参测试须用顶层监听器
  （如 java.beans.PropertyChangeListener/VetoableChangeListener）。
- 7 参构造器对 get 名"宽容"：找不到或参数个数不符 → getGetListenerMethod() 为 null，不抛异常。
- Method[]/MD[] 构造器不做任何监听器类型/签名校验（含 null add/remove）。
- Introspector 侧事件集识别要求 add/remove 后缀名以 **"Listener"** 结尾且监听器参数类名以
  该后缀结尾；add 方法 throws TooManyListenersException → isUnicast() true。
- getListenerMethods()/getListenerMethodDescriptors() 返回内部缓存/克隆；MD[] 传入的数组被 clone。
- 非序列化家族成员（断言 NotSerializableException）。
