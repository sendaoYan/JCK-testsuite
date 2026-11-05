# javax.management.StandardMBean 黑盒行为测试设计

## 1. API 功能
- 实现 DynamicMBean 与 MBeanRegistration。
- 构造：`(T implementation, Class<T> mbeanInterface)`、`(Class<T> mbeanInterface, boolean isMXBean)`。
- 查询：`getMBeanInterface()`、`getImplementation()`、`getImplementationClass()`、`getMBeanInfo()`；get/setAttribute、invoke。

## 2. Java 用户如何使用
把标准 MBean 接口与实现包装后注册到 MBeanServer；内省自动生成元数据。
```java
StandardMBean m = new StandardMBean(impl, ThingMBean.class);
mbs.registerMBean(m, on);
```

## 3. 开源代码实际用法调研
- `test/jdk/javax/management/standardmbean/DeadlockTest.java`。
- `test/jdk/javax/management/Introspector/FeatureOrderTest.java`、`IsMethodTest.java`。
- 实现权威：`src/java.management/share/classes/javax/management/StandardMBean.java`。

## 4. 测试用例设计 + 自动化实现
- 黑盒：getMBeanInterface/getImplementation/getImplementationClass；getMBeanInfo 的 className 为实现类名、属性/操作内省正确；getAttribute/setAttribute/invoke 往返；注册后 MBeanServer 调用与 isInstanceOf（实现类名）；注销。
- 白盒：public 类、implements DynamicMBean、implements MBeanRegistration。

## 深化（2026-11，批次 e）
新增维度：构造形态矩阵、内省元数据精确化、MBeanInfo 缓存语义、setImplementation、注册后服务器路由、非合规负例。
- 构造形态：`(impl, iface)` 分离形态；子类自身形态（`extends StandardMBean` + `super(ifaceClass)`，getImplementation()==this）；`(impl, null)` 被容忍（接口从实现类推断，实测 getMBeanInterface() 返回 `StdCounterMBean`）。
- 负例实测（JDK-8 与 JDK-27 一致）：
  - `new StandardMBean(impl, 非接口.class)` → **NotCompliantMBeanException**（"Not an interface"，注意不是 IAE）；
  - `new StandardMBean(null, null)` / `(null, iface)` → **IllegalArgumentException**（"implementation is null"）。
- 内省矩阵：属性集合精确 {Count, Ready}（Count: int 读写非 is；Ready: boolean 只读 isIs）；操作集合精确 {reset, add}（add: 返回 int、签名 1×int、impact=UNKNOWN(3)）——**操作顺序两侧不同（JDK-8 [reset,add]、JDK-27 [add,reset]），测试用集合断言**。
- 构造器内省形态差异：`(impl, iface)` 形态 getConstructors() 为空；自身子类形态暴露自己的 public 构造器（实测两侧一致）。
- 缓存语义：两次 getMBeanInfo() 返回**同一实例**；setImplementation 换实现后 getMBeanInfo() **仍是旧缓存实例**（缓存不失效），getImplementation/getImplementationClass/路由立即更新；setImplementation(非合规对象) → NotCompliantMBeanException 且原实现保留。
- 服务器路由：注册 pair 形态后 getAttribute/setAttribute/invoke/isIs 全路由到实现真值；错型 setAttribute → InvalidAttributeValueException；未知属性 → AttributeNotFoundException；未知操作 → ReflectionException。
- isInstanceOf 语义（实测）：pair 形态注册时 isInstanceOf(实现类名)=true、isInstanceOf(StandardMBean)=true、isInstanceOf(管理接口名)=false；命名约定直接注册时 isInstanceOf(接口名)=true。
- registerMBean(纯类) → NotCompliantMBeanException（不走 Standard/MXBean 任何约定）。
### 版本差异
- getMBeanInfo().getOperations() 顺序：JDK-8 [reset,add]，JDK-27 [add,reset]（断言用集合）。
- setAttribute(null) 的 NPE 消息：JDK-8 为 null，JDK-27 带 helpful message（只断言 NPE 类型）。
