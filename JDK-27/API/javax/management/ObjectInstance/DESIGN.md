# javax.management.ObjectInstance 黑盒行为测试设计

## 1. API 功能
- 构造：`(String objectName, String className)`、`(ObjectName, String)`。
- 查询：`getObjectName()`、`getClassName()`；`equals/hashCode/toString`。

## 2. Java 用户如何使用
registerMBean 的返回值，描述注册后的 ObjectName 与实现类名。
```java
ObjectInstance oi = mbs.registerMBean(bean, on);
String className = oi.getClassName();
```

## 3. 开源代码实际用法调研
- `test/jdk/javax/management/ObjectInstance/ObjectInstanceNullTest.java`、`ToStringMethodTest.java`。
- 实现权威：`src/java.management/share/classes/javax/management/ObjectInstance.java`。

## 4. 测试用例设计 + 自动化实现
- 黑盒：getObjectName 返回解析后的 ObjectName、getClassName 返回类名；equals/hashCode 对两者敏感；null objectName 构造抛异常（源码确认）；toString 包含两者。
- 白盒：public 类、implements Serializable、含 getObjectName/getClassName。

## 深化（2026-11）新建
### 1. API 功能
- 构造：`ObjectInstance(ObjectName, String)`、`ObjectInstance(String, String)`（字符串名会走 ObjectName 解析，坏名抛 MalformedObjectNameException）。
- 查询：`getObjectName()`、`getClassName()`；equals/hashCode 基于 (name, className)；toString 为 `className[objectName]`。

### 2. Java 用户如何使用
`registerMBean`/`getObjectInstance`/`queryMBeans` 的返回元素；按名+类做等值判重。
```java
ObjectInstance oi = mbs.registerMBean(new Counter(), new ObjectName("d:type=Counter"));
oi.getClassName(); // Counter 的实现类全名
```

### 3. 参考来源
- Oracle javadoc：javax.management.ObjectName/ObjectInstance 章节（构造约束、equals 契约）。
- OpenJDK `test/jdk/javax/management/ObjectName/*` 与 `MBeanServer/*` 用例思路（借鉴思路，代码自写）。
- 实现权威：`src/java.management/share/classes/javax/management/ObjectInstance.java`（javap 双侧对照）。

### 4. 测试用例设计 + 易错规范细节
- 构造矩阵：null ObjectName / null String 名 → NPE；`new ObjectInstance("no-colon", "c")` → MalformedObjectNameException；**null className 合法**（equals/hashCode 仍一致，toString 输出 `null[d:k=v]`）。
- equals/hashCode：同 (name,class) 相等、跨构造器等价；不同 name / 不同 className / null / 异型不等。
- toString 精确格式 `className[objectName]`。
- serialVersionUID = `-4099952623687795850L`；序列化 round-trip（含 null className 保留）。
- 真闭环：`registerMBean` 返回的 ObjectInstance 的 getObjectName 精确等于注册名、getClassName 为实现类全名、`getObjectInstance` 等值。

## 深化（2026-11）版本差异记录（本目录实测，JDK-8u372 vs JDK-27.0.1）
- 两侧 public API（javap -public）、serialVersionUID、异常类型与消息全部一致（见上文逐项）。
- 与本目录相关的全局版本差异：
  - `MBeanServer.registerMBean(null, name)`：JDK-8 抛 `NullPointerException`；JDK-27 抛 `RuntimeOperationsException`(包装 IllegalArgumentException)。
  - `javax.management.loading.MLet`：JDK-27 已移除（JDK-8 存在）。
