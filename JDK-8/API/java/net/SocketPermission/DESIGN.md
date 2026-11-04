# SocketPermission 设计文档

## 一、API 功能

`java.net.SocketPermission` 套接字访问权限：目标 `"host[:port|:port-range]"` + 动作 `connect,listen,accept,resolve`。final、Serializable、含专用 PermissionCollection。主机支持单主机、IP 字面量（v4/v6）、通配 `*.domain`、裸 `*`；端口支持单值、`n-m`、`n-`、`-n`、空、`*`。

## 二、Java 用户如何使用

策略文件 `permission java.net.SocketPermission "localhost:1024-", "connect"`；SecurityManager 下 Socket/ServerSocket 构造与连接前 `checkPermission`；代码中用于自定义访问控制矩阵。

## 三、开源代码实际用法调研（参考来源）

OpenJDK jdk8u `java.net.SocketPermission` 源码：`getMask` 解析动作（大小写折叠、逗号+空格分隔、去重）；`init` 中 `this.mask = mask | RESOLVE`（任何动作自动并入 resolve）；端口解析与 `invalid port range: X` 消息；`impliesIgnoreMask`（掩码子集 + 主机匹配：通配 endsWith(cname)、IP 等值、DNS 解析后比较、UnknownHostException 回退 compareHostnames regionMatches）；equals/hashCode 走 impliesIgnoreMask 双向。

## 四、测试用例设计（252 断言）

- **白盒**：final、直接 Permission 子类、Serializable、单构造器、newPermissionCollection。
- **动作规范化**（30+）：connect/listen/accept 自动加 resolve；大小写折叠；去重；空格跳过；乱序输入→规范序 `connect,listen,accept,resolve`。
- **名字形态**（19）：无端口/空端口/`*`/范围/前导零/通配主机/IPv6/空主机（规范化为 "localhost"）原样保留于 getName。
- **构造负例**（13）：空动作 IAE "action can't be empty"、null 动作 NPE "action can't be null"、未知词 IAE "invalid permission: foo"、`connect,,accept` IAE、端口 `abc`/`80-abc`/`90-80`/`1-1024-` 均 IAE "invalid port range: X"；65536/70000/-1 合法（旧解析器宽进）。
- **implies 主机**（35）：`*`、`*.example.com`（含多级、大小写、通配对通配）、localhost↔127.0.0.1 双向、IPv6 `[::1]`↔全写形式——**排除**外部主机名大小写对比（DNS 依赖，结果不稳定）。
- **implies 端口**（40+）：单值/范围/含边界/`n-`/`-n`/`0-`/空端口/`*` 等价全范围/越界端口只蕴含自身。
- **implies 动作**（20）：子集、resolve 随行、自蕴含短路、implies(null) false、跨类型 false。
- **equals/hashCode**（25+）：`:080`==`:80`、`:*`==`:`、无端口==空端口、主机大小写不敏感、掩码规范化相等、IPv6 压缩==全写、localhost==127.0.0.1。
- **序列化**（12）：范围/通配/IPv6 完整往返。
- **PermissionCollection**：并集蕴含、缺口不蕴含、elements 计数、readonly SecurityException。
- **toString** 精确格式 `("java.net.SocketPermission" "localhost:80" "connect,resolve")`。

## 五、易错规范细节（探针实证）

- **RESOLVE 自动并入**：`new SocketPermission(h,"connect").getActions()` 是 `"connect,resolve"`；但 `"resolve"` 不蕴含 `"connect"`。
- **端口串宽松**：`080`==`80`、`80-80`==`80`、`65536`/`-1` 不抛（旧解析器），但越界端口只 equals/implies 自身。
- **空主机规范化为 "localhost"**（getByName("") 返回回环）；`":"` 名字原样保留。
- **equals 即双向 implies**：`localhost:80` **equals** `127.0.0.1:80`（本地解析成功）。
- 通配匹配在 DNS 失败时走 regionMatches 回退，`*.example.com` 蕴含多级子域；非通配主机大小写比较依赖 DNS，**不可断言**。
- 域名解析仅允许 localhost/127.0.0.1/[::1]（本地 hosts），全部探针用字面量。

## 六、版本差异（JDK-8 vs JDK-27）

- 全部 252 条行为断言两侧逐条一致（SocketPermission 于 JDK-25 起 deprecated for removal，JDK-27 仍完整工作；两侧均验证）。
