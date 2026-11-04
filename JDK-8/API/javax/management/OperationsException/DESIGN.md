## 深化（2026-11）新建
### 1. API 功能
- 受检异常族根（JMException 直接子类）：`() ` / `(String)`；不携带 cause 链特有访问器。
- 子类归属（本批范围）：AttributeNotFoundException、InstanceAlreadyExistsException、InstanceNotFoundException、IntrospectionException、InvalidAttributeValueException、ListenerNotFoundException、MalformedObjectNameException、NotCompliantMBeanException、ServiceNotFoundException。

### 2. Java 用户如何使用
MBeanServer 各种操作失败的公共受检基类；catch (OperationsException) 统一兜底。

### 3. 参考来源
- Oracle javadoc javax.management.OperationsException；javap 双侧对照。
- 实现权威：`src/java.management/share/classes/javax/management/OperationsException.java`。

### 4. 测试用例设计 + 易错规范细节
- 构造链：`()` message null、`(String)` 精确保留、`(String)null` → null；getCause null。
- serialVersionUID = `-4967597595580536216L`（两侧同值）；序列化 round-trip。
- 子类归属表：9 个子类逐一 `isAssignableFrom` 断言（每类自身的真实触发见各自目录）。

## 深化（2026-11）版本差异记录（本目录实测，JDK-8u372 vs JDK-27.0.1）
- 两侧 public API（javap -public）、serialVersionUID、异常类型与消息全部一致（见上文逐项）。
- 与本目录相关的全局版本差异：
  - `MBeanServer.registerMBean(null, name)`：JDK-8 抛 `NullPointerException`；JDK-27 抛 `RuntimeOperationsException`(包装 IllegalArgumentException)。
  - `javax.management.loading.MLet`：JDK-27 已移除（JDK-8 存在）。
