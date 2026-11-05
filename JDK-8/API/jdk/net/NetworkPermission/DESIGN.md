# jdk.net.NetworkPermission 设计案（JDK-8）

## 1. API 功能
`jdk.net.NetworkPermission extends java.security.BasicPermission`（public final，serialVersionUID=-2012939586906722291L）。表示访问 jdk.net 扩展网络能力的权限：只有 target 名、无 actions。JDK-8 文档化的合法 target 全集：
- `setOption.SO_FLOW_SLA`：允许在支持的 socket 上设置 SO_FLOW_SLA。
- `getOption.SO_FLOW_SLA`：允许读取 SO_FLOW_SLA。

构造器：`NetworkPermission(String name)`、`NetworkPermission(String name, String actions)`（actions 应为 null，非 null 被忽略）。

## 2. 使用方式
- SecurityManager 场景下作为 policy 条目；`implies` 做蕴含判定。
- 行为全部继承 BasicPermission：getActions() 恒为 ""；target 支持末尾 `*` 通配与 `前缀.*` 目录式通配；不同 Permission 类之间互不蕴含。
- Serializable：可写入/读出 ObjectStream。

## 3. 参考来源
- Oracle javadoc（Java SE 8 jdk.net.NetworkPermission，含 target 表）。
- OpenJDK 源码：~/git/jdk8u-dev/jdk/src/share/classes/jdk/net/NetworkPermission.java（两构造器直通 BasicPermission，无自有逻辑；svUID 常量 -2012939586906722291L）。
- 实测记录（/tmp/probe-dnet/ProbePerm.java，jdk8u372-b07）：
  - ctor(null) → NPE；ctor("") → IllegalArgumentException（两构造器同）。
  - implies：同名 true/异名 false/null false/自蕴含 true；`*` 蕴含两文档 target、反向 false；`setOption.*` 蕴含 `setOption.SO_FLOW_SLA` 但不蕴含 `getOption.*`。
  - 与 RuntimePermission("setOption.SO_FLOW_SLA") 同名互不蕴含、equals false（BasicPermission.implies 检查 `getClass()` 相等）。
  - 序列化 round-trip 后 equals/hashCode/getName/getActions 全保留；ObjectStreamClass.lookup 读 svUID 成功。
  - newPermissionCollection() 返回 BasicPermissionCollection，add 后 implies 生效。
  - toString 格式 `("jdk.net.NetworkPermission" "setOption.SO_FLOW_SLA")`。

## 4. 测试设计 + 易错细节
**测试设计**（6→45 断言）：
1. 文档 target 两名构造 + getName/getActions 回读。
2. 非法 target 矩阵：null/空串 × 两构造器 → NPE/IAE。
3. implies 全矩阵：自/同/异/null/通配 `*`/前缀 `setOption.*`/跨类 RuntimePermission。
4. equals/hashCode 契约：自等、对称、null/String false、等值实例 hashCode 相等、两次调用稳定。
5. actions 被忽略：不同 actions 字符串仍 equals，getActions 恒 ""。
6. Serializable：内存字节流 round-trip（ObjectOutputStream→ByteArrayInputStream→ObjectInputStream）5 项保留断言；ObjectStreamClass 直锁 svUID。
7. newPermissionCollection：非 null、add 后 implies 同名 true 异名 false。
8. toString 含类全名与 target；JDK-8 无 @Deprecated；直接父类 BasicPermission；public final。

**易错细节**：
- 序列化测试用内存字节流，不落盘，免临时文件清理。
- svUID 反射读 private 字段在 JDK-27 会被模块系统拦（InaccessibleObjectException），必须走 `ObjectStreamClass.lookup(Class).getSerialVersionUID()`（两侧通用）。
- BasicPermission 的通配语义只有「整个 target 为 `*`」和「以 `.*` 结尾」两种，`set*` 不通配（不必测，但别写错断言）。
- equals 参数是 Object：`equals("同名字符串")` false 是常被漏掉的负例。

**8/27 差异表**

| 维度 | JDK-8 | JDK-27 |
|---|---|---|
| @Deprecated | 无 | @Deprecated(since="25", forRemoval=true) |
| 文档化 target 表 | setOption/getOption.SO_FLOW_SLA | 表已删（任意非空名仍可构造） |
| 行为（NPE/IAE/implies/通配/序列化/svUID） | 一致 | 一致 |
