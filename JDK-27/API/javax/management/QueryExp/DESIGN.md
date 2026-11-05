# javax.management.QueryExp 黑盒行为测试设计

## 1. API 功能
- 接口：`apply(ObjectName)`、`setMBeanServer(MBeanServer)`。

## 2. Java 用户如何使用
MBeanServer.queryNames/queryMBeans 对每个候选 ObjectName 调 apply 决定是否匹配。
```java
boolean match = queryExp.apply(candidate);
```

## 3. 开源代码实际用法调研
- `test/jdk/javax/management/query/*Test.java`。
- 实现权威：`src/java.management/share/classes/javax/management/QueryExp.java`。

## 4. 测试用例设计 + 自动化实现
- 黑盒：Query.eq/and 等组合表达式实现 QueryExp，注册 MBean 后 queryNames 匹配预期对象。
- 白盒：public 接口、extends Serializable、含 apply/setMBeanServer。

## 深化（2026-11，批次 e）
新增维度：接口契约、**自定义 QueryExp 经真实 server 查询**、标准算子组合自定义 exp、序列化 round-trip。
- 接口：apply(ObjectName)→boolean、setMBeanServer(MBeanServer)→void、extends Serializable（反射锁定）。
- RecordingExp 经 server.queryNames(范围, exp)：apply 每个候选 MBean 恰好调用一次；结果按 apply 返回值精确过滤；**server 不会经 setMBeanServer 推送自己**（该路由已废弃，seenServer 保持 null）。
- Query.and/not/or 组合自定义 exp 真过滤；自定义 exp 抛 RuntimeException → server 静默排除全部候选（空结果、不传播）。
- 序列化：Query.eq/gt 构造的 QueryExp round-trip 后 apply(null) 行为不变。
### 版本差异
- 无。
