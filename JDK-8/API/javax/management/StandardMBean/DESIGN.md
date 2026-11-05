# javax.management.StandardMBean 契约测试设计

## 1. API 功能
- 把"接口+实现"的标准 MBean 包装为 DynamicMBean：构造 `(impl, iface)`、`(impl, iface, allowPublicMethods)`、protected `(ifaceClass)` 子类形态；get/setAttribute、getAttributes/setAttributes、invoke、getMBeanInfo；getImplementation/getImplementationClass/getMBeanInterface/setImplementation；实现 MBeanRegistration。
- 服务器把 StandardMBean 当 DynamicMBean 路由，元数据由接口内省生成。

## 2. Java 用户典型用法
```java
StandardMBean m = new StandardMBean(new Counter(), CounterMBean.class);
MBeanServer server = MBeanServerFactory.newMBeanServer();
server.registerMBean(m, new ObjectName("d:type=Counter"));
server.getAttribute(name, "Count");
```

## 3. 参考来源
- Oracle javadoc：javax.management.StandardMBean（构造异常、缓存与内省规则）。
- OpenJDK `test/jdk/javax/management/standardmbean/`、`Introspector/FeatureOrderTest.java`（思路借鉴，代码自写）。

## 4. 测试用例设计
- 既有：包装基本 get/set/invoke/getMBeanInfo。

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
