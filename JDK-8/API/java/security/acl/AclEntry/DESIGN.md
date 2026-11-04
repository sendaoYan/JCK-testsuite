# java.security.acl.AclEntry — 设计案

## 1. API 功能
ACL 条目接口（已弃用，JDK-17 起移除）。`AclEntry extends Cloneable`：`setPrincipal`/`getPrincipal`、`setNegativePermissions`/`isNegative`、`addPermission`/`removePermission`/`checkPermission`/`permissions`、`clone`、`toString`。

## 2. 使用方式
一条 entry 绑定一个 Principal 与其 Permission 集合，标记正/负（deny）。principal 只能设置一次（javadoc：重复 set 返回 false）；负权限单向不可逆；clone 产生可独立变更的副本。

## 3. 参考来源
- javadoc `java.security.acl.AclEntry`（setPrincipal 一次性、clone 可用性）。
- `javap java.security.acl.AclEntry`（10 方法、无受检异常、clone 返回 Object）。

## 4. 测试用例设计 + 易错规范细节
- 白盒：声明方法集恰为 10 个；唯一父接口 Cloneable；全部方法无受检异常；clone 返回 Object、四个变更方法返回 boolean。
- 黑盒（记录型实现 RecEntry）：principal 初始 null、首次 setPrincipal true/二次 false 且不换人；addPermission 新增 true/重复 false；removePermission 存在 true/缺失 false；checkPermission 命中/缺失；permissions() 枚举恰为已加集合；setNegativePermissions 后 isNegative true 且重复调用仍 true（单向）；clone 同类新实例、状态携带、改 clone 不影响原体。
- 易错点：clone 必须深拷贝权限集合否则互相泄漏；permissions() 返回的枚举应是快照（避免并发修改）；负 entry 语义（deny）由 Acl 实现解释，entry 自身只存标志。
