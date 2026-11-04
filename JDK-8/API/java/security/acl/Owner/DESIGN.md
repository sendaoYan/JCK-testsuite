# java.security.acl.Owner — 设计案

## 1. API 功能
owner 集合管理接口（已弃用包，JDK-17 起移除）：`addOwner(Principal caller, Principal newOwner)`、`deleteOwner(Principal caller, Principal owner)`、`isOwner(Principal)`。addOwner throws NotOwnerException；deleteOwner throws NotOwnerException + LastOwnerException。

## 2. 使用方式
Acl extends Owner。owner 身份以 Principal 表达；变更操作以 caller 门控；deleteOwner 需保护最后一个 owner。

## 3. 参考来源
- javadoc `java.security.acl.Owner`（boolean 返回：已存在 add false / 不存在 delete false；LastOwnerException 时机）。
- `javap java.security.acl.Owner`（3 方法、throws 精确集）。

## 4. 测试用例设计 + 易错规范细节
- 白盒：方法集恰 3 个、无父接口；addOwner throws 恰为 {NotOwnerException}、deleteOwner throws 恰为 {NotOwnerException, LastOwnerException}；参数均 (Principal,Principal)、返回 boolean；isOwner 单参。
- 黑盒（记录型 RecOwner，以接口类型多态使用）：isOwner 认主/不认 stranger/null；addOwner 新增 true/重复 false/非 owner 抛；deleteOwner 成员 true/非成员 false/非 owner 抛/最后一人抛；抛异常后 owner 集合不变；"所有权转移"序列（加 B → B 删 A）合法。
- 易错点：deleteOwner 的 LastOwnerException 与 NotOwnerException 是并列 throws——非 owner 删最后一人应先报 NotOwner（caller 校验在前）；自删在还有他人时合法。
