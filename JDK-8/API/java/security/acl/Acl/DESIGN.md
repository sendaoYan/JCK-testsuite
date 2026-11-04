# java.security.acl.Acl — 设计案

## 1. API 功能
已弃用的访问控制表接口（JDK-1.1 体系，JDK-17 起整包移除）。`Acl extends Owner`：`setName(Principal,String)`/`getName`、`addEntry(Principal,AclEntry)`/`removeEntry(Principal,AclEntry)`、`getPermissions(Principal)`、`entries()`、`checkPermission(Principal,Permission)`、`toString`；owner 变更三方法继承自 Owner。

## 2. 使用方式
持有者（owner）把 `AclEntry`（绑定 Principal 与 Permission 集合）加入 Acl；查询用 `getPermissions`/`checkPermission`；所有变更操作以 caller Principal 为第一参数，非 owner 抛 `NotOwnerException`。JDK 无公开实现，须自行实现接口。

## 3. 参考来源
- javadoc `java.security.acl.Acl`（throws NotOwnerException 契约、boolean 返回语义：add 首次 true/重复 false）。
- `javap java.security.acl.Acl`（8 方法精确签名、throws 子句）。

## 4. 测试用例设计 + 易错规范细节
- 白盒：声明方法集恰为 8 个 javadoc 方法；直接父接口恰为 Owner；setName/addEntry/removeEntry 的 throws 恰为 NotOwnerException；getPermissions/entries 返回 Enumeration。
- 黑盒（记录型实现 RecAcl）：owner 改名生效、非 owner 三操作全抛 NotOwnerException 且状态不变；addEntry 首次 true/重复 false；removeEntry 存在 true/缺失 false；getPermissions 按 principal 聚合；checkPermission 命中/未命中；entries 枚举计数；继承的 addOwner/isOwner/deleteOwner 可用（含 LastOwnerException 路径）。
- 易错点：接口无公开实现——语义断言由记录型实现自定义并自洽；NotOwnerException 是"调用者不是 owner"而非参数非法；equals 未在接口重声明（用 Object 语义比较 entry 时需实现自身 equals）。
