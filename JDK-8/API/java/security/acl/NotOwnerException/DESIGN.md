# java.security.acl.NotOwnerException — 设计案

## 1. API 功能
非 owner 调用 owner 门控操作时抛出的受检异常（已弃用包，JDK-17 起移除）。直接 extends Exception，只有无参构造器，message 恒 null。`Acl.setName/addEntry/removeEntry` 与 `Owner.addOwner/deleteOwner` 的 throws 声明。

## 2. 使用方式
ACL 变更类 API 第一参数为 caller Principal；实现校验 caller ∈ owners，否则 `throw new NotOwnerException()`。

## 3. 参考来源
- javadoc `java.security.acl.NotOwnerException`、`java.security.acl.Acl`（throws 子句）。
- `javap`：唯一无参公共构造器。

## 4. 测试用例设计 + 易错规范细节
- 构造器矩阵：恰 1 个无参构造器；message/cause null；initCause 一次/二次 ISE；toString 全类名前缀。
- 语义触发：记录型 OwnerGatedAcl 对非 owner 的 setName/addEntry/removeEntry 三操作均抛本异常且 ACL 状态不变；owner 操作正常生效。
- 序列化：round-trip 保类保 null message；serialVersionUID 一致性。
- 易错点：与 AccessControlException 无关（不同包不同树）；抛出后不应有任何状态半提交。
