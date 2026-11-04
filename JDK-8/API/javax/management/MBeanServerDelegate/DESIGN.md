# javax.management.MBeanServerDelegate 契约测试设计（2026-11 新建）

## 1. API 功能
- 每个 server 的元数据 MBean：MBeanServerId/Specification*/Implementation* 七属性 + 注册/注销通知广播。

## 2. Java 用户典型用法
见 JDK-27 侧同目录设计与下节深化用例（双侧同代码）。

## 3. 参考来源
- Oracle javadoc: javax.management.MBeanServerDelegate。
- OpenJDK `test/jdk/javax/management/` 对应目录（思路借鉴，代码自写）。
## 4. 测试用例设计
见下节深化清单。

## 深化（2026-11）：作为 MBean 的 delegate 全属性闭环

### 新增覆盖
1. **本地 server 上的 delegate**（DELEGATE_NAME 预注册）：7 属性非空/常量精确（SpecificationName=="Java Management Extensions"、SpecificationVersion=="1.4" 双侧一致、ImplementationName=="JMX"；ImplementationVersion 为 JVM 版本串仅断非空）；MBeanInfo 属性名集合恰为 7 常量、**全部只读**（逐个断言 isReadable&&!isWritable）、0 操作、1 通知 info。
2. **写拒绝**：delegate 只读属性 setAttribute → `AttributeNotFoundException`（消息 "X not accessible"）。
3. **直接实例**：两个新 delegate 的 MBeanServerId 互异；delegate 可作为普通 MBean 注册到自定义名下且属性路由到同一实例。

### 参考来源
- Oracle javadoc: MBeanServerDelegate。
- OpenJDK `test/jdk/javax/management/ImplementationVersion/`（版本属性暴露思路）。
