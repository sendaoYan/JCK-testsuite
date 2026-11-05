# javax.management.QueryExp 契约测试设计

## 1. API 功能
- 查询谓词接口：apply(ObjectName)→boolean、setMBeanServer(MBeanServer)；extends Serializable。

## 2. Java 用户典型用法
```java
QueryExp q = Query.and(Query.gt(Query.attr("Count"), Query.value(0)), ...);
server.queryNames(null, q);
```

## 3. 参考来源
- Oracle javadoc：javax.management.QueryExp。

## 4. 测试用例设计
- 既有：接口结构反射。

## 深化（2026-11，批次 e）
新增维度：接口契约、**自定义 QueryExp 经真实 server 查询**、标准算子组合自定义 exp、序列化 round-trip。
- 接口：apply(ObjectName)→boolean、setMBeanServer(MBeanServer)→void、extends Serializable（反射锁定）。
- RecordingExp 经 server.queryNames(范围, exp)：apply 每个候选 MBean 恰好调用一次；结果按 apply 返回值精确过滤；**server 不会经 setMBeanServer 推送自己**（该路由已废弃，seenServer 保持 null）。
- Query.and/not/or 组合自定义 exp 真过滤；自定义 exp 抛 RuntimeException → server 静默排除全部候选（空结果、不传播）。
- 序列化：Query.eq/gt 构造的 QueryExp round-trip 后 apply(null) 行为不变。
### 版本差异
- 无。
