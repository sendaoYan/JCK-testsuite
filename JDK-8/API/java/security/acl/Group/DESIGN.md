# java.security.acl.Group — 设计案

## 1. API 功能
主体组接口（已弃用包，JDK-17 起移除）。`Group extends Principal`：`addMember(Principal)`、`removeMember(Principal)`、`isMember(Principal)`、`members()`（Enumeration<? extends Principal>）。

## 2. 使用方式
组本身是一个 Principal（getName/equals/hashCode 继承契约），成员增删后 isMember 与 members() 反映最新集合；ACL 可把权限授给组。

## 3. 参考来源
- javadoc `java.security.acl.Group`（成员操作 boolean 语义）。
- `javap java.security.acl.Group`（4 方法、无受检异常）。

## 4. 测试用例设计 + 易错规范细节
- 白盒：方法集恰 4 个；唯一父接口 java.security.Principal；members 返回 Enumeration；三个成员方法均 (Principal)→boolean、无受检异常。
- 黑盒（记录型 RecGroup）：addMember 新 true/重复 false；removeMember 存在 true/缺失 false；isMember 命中/非成员/null false；members() 枚举恰为成员集且反映删除；组以 Principal 身份使用（getName）；equals/hashCode 按名跨实例一致。
- 易错点：members() 的通配类型 `Enumeration<? extends Principal>`；组作为 Principal 的 equals 必须与 hashCode 配套；空组 members() 应为空枚举而非 null。
