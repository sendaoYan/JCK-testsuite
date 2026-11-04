# java.security.acl.LastOwnerException — 设计案

## 1. API 功能
删除最后一个 owner 时抛出的受检异常（已弃用包，JDK-17 起移除）。直接 extends Exception，只有无参构造器，message 恒 null。`Owner.deleteOwner` 的 throws 子句声明之一。

## 2. 使用方式
`Owner.deleteOwner(caller, owner)` 在 owners 只剩一人且要删时抛出，防止 ACL 失去管理者。

## 3. 参考来源
- javadoc `java.security.acl.LastOwnerException`、`java.security.acl.Owner#deleteOwner`。
- `javap`：唯一无参公共构造器。

## 4. 测试用例设计 + 易错规范细节
- 构造器矩阵：恰 1 个无参构造器；message/cause 默认 null；initCause 一次/二次 ISE；fillInStackTrace 自返回；toString 全类名前缀。
- 语义触发：记录型 Owner 实现在 owners.size()==1 时 deleteOwner 抛本异常；两个 owner 时删除成功、回到单 owner 再删又抛——完整状态机。
- 序列化：round-trip 保类保 null message；serialVersionUID 字段与序列化运行时一致。
- 易错点：异常在 GeneralSecurityException 树之外；"最后一个 owner"判定按集合大小而非"删除自己"。
