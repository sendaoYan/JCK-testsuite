# GuardedObject — 设计案

## 1. API 功能
守卫容器：构造 (Object, Guard)（两者皆可 null）+ getObject()。implements Serializable。

## 2. 使用方式
guard 为 null → 无条件放行；guard 抛 SecurityException → getObject 传播；guard 正常返回 → 返回内部对象。序列化时（writeObject/readObject）同样咨询 guard。

## 3. 参考来源
- javadoc `java.security.GuardedObject`（序列化守卫语义）。
- 双侧探针实测。

## 4. 测试用例设计 + 易错规范细节
- 放行矩阵：null guard 放行且可重复；null 对象 + null guard 合法（getObject 返回 null）。
- 拒绝矩阵：guard 抛 SecurityException → getObject 传播且消息逐字；**非 Security 的 unchecked（IllegalStateException）原样传播不转译**。
- 频率契约：**每次 getObject 都咨询 guard**（计数断言 3 次），结果不缓存。
- 序列化契约（易错重点）：**writeObject 调用 guard.checkGuard——拒绝型可序列化 guard 会让 ObjectOutputStream.writeObject 抛 SecurityException**；放行型可序列化 guard round-trip 后 getObject 等值、新实例。
- 结构：Serializable、唯一 getObject 方法。
- 易错点：guard 自身必须 Serializable 才能 round-trip；getObject 的守卫失败不能缓存（连续两次拒绝都要抛）。
