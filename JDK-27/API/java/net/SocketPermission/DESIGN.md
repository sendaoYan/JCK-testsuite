# SocketPermission 设计文档

## 一、API 功能

按主机+端口范围与动作(accept/connect/listen/resolve)定义的网络权限。

## 二、Java 用户如何使用

用户在策略文件写 java.net.SocketPermission "host:port", "connect,accept"。

## 三、开源代码实际用法调研

安全管理器下沙箱网络访问控制。

## 四、测试用例设计

构造 name/actions、getActions 规范化、implies 主机/端口/动作层级、equals/hashCode、非法参数异常。

## 五、缺口分析（批次 H 深化追加）

原测试仅 28 断言。深化至 **276 断言**（jtreg Passed），新增覆盖：

- 动作规范化 30+：connect/listen/accept 自动并入 resolve、大小写/去重/空格/乱序→规范序。
- 名字形态 19：空端口/`*`/范围/前导零/通配/IPv6/空主机（规范化 "localhost"）。
- 构造负例 13：精确消息表（见 DESIGN 上文）+ 端口宽进（65536/-1 合法但只蕴含自身）。
- implies 矩阵 95+：主机（通配/localhost↔127.0.0.1/IPv6 压缩等价；排除 DNS 依赖用例）、端口代数（边界/开区间/等价形态）、动作掩码。
- equals/hashCode 25+：`:080`==`:80`、`:*`==`:`、掩码规范化、localhost==127.0.0.1。
- 序列化（范围/通配/IPv6）、PermissionCollection（并集/缺口/readonly）、toString 精确格式。

## 六、版本差异（JDK-27 实证）

- 无行为差异：核心 252 条断言在 JDK-8 与 JDK-27 逐条一致（探针双跑验证）；JDK-25 起 deprecated for removal，JDK-27 侧注明并照常验证。
