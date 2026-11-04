# NetPermission 设计文档

## 一、API 功能

网络相关安全权限的名称表示(如 setDefaultAuthenticator)。

## 二、Java 用户如何使用

用户在策略文件或代码中构造 NetPermission("name") 授予网络操作。

## 三、开源代码实际用法调研

安全管理器时代用于控制网络全局状态修改。

## 四、测试用例设计

构造 name/actions(空)、implies 同名、equals/hashCode、空名/空指针异常。

## 五、缺口分析（批次 H 深化追加）

原测试仅 16 断言。深化至 **107 断言**（jtreg Passed），新增覆盖：

- 9 个文档化名字矩阵 + 任意名（"get*"、"a*b" 等）可构造（无白名单）。
- actions 忽略：1/2 参、多动作串均 getActions ""、equals/implies/hashCode 不受影响。
- BasicPermission 通配语义："*" 全量；"a.*" 蕴含 a.b/a.b.c/a.b.*、不蕴含 a/ab；"set*" 字面名；大小写敏感。
- 负例精确消息：null→NPE "name can't be null"、""→IAE "name can't be empty"（两构造器）；implies(null)/跨类型 false。
- 序列化往返（含通配）；PermissionCollection 联合蕴含/elements/readonly SecurityException。

## 六、版本差异（JDK-27 实证）

- JDK-25 起 NetPermission deprecated for removal；JDK-27 仍完整工作，全部行为与 JDK-8 一致（两侧同套断言验证）。
