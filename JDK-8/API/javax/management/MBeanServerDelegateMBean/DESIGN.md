# javax.management.MBeanServerDelegateMBean 契约测试设计（2026-11 新建）

## 1. API 功能
- delegate 的标准 MBean 管理接口：7 个无参 String getter。

## 2. Java 用户典型用法
见 JDK-27 侧同目录设计与下节深化用例（双侧同代码）。

## 3. 参考来源
- Oracle javadoc: javax.management.MBeanServerDelegateMBean。
- OpenJDK `test/jdk/javax/management/` 对应目录（思路借鉴，代码自写）。
## 4. 测试用例设计
见下节深化清单。

## 深化（2026-11）：接口形状 + 三种到达路径

### 新增覆盖
1. **接口形状精确**：恰 7 个 getter，全部无参、返回 String、无 checked 异常；MBeanServerDelegate 实现之。
2. **三路径同值**：接口引用直调；注册为 MBean 后 server.getAttribute 七属性全非空且内省属性名集合==接口 getter 集；MBeanServerInvocationHandler 代理（以本接口为类型）getSpecificationName/getMBeanServerId 路由到真实 delegate。

### 参考来源
- Oracle javadoc: MBeanServerDelegateMBean。
