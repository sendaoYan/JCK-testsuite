# URLPermission 设计文档

## 一、API 功能

表示访问某个 URL 及其动作的权限，用于安全管理器下对 URLConnection 访问做授权。

## 二、Java 用户如何使用

用户在策略文件中写 permission java.net.URLPermission "http://host/*", "GET:HEAD" 授予网络访问。

## 三、开源代码实际用法调研

Java EE/安全管理器时代的服务器与沙箱程序用 URLPermission 限制网络。

## 四、测试用例设计

构造 name 与 actions，getName/getActions/implies 层级判断；getMethods 返回动作集合；非法参数抛异常。

## 五、缺口分析（批次 H 深化追加）

原测试仅 25 断言。深化至 **175 断言**（jtreg Passed），新增覆盖：

- 构造/getActions 20+：1 参 `*:*`；排序后逗号拼接 `"DELETE,GET,POST:"`、`"A,M,Z:"`、`"GET:X-Bar,X-Foo"`；`GET,,POST` 空词跳过；空/`:`/`:header` 合法规范化；query 从 name 剥离。
- 构造负例 8：空格 IAE 精确消息、双冒号 IAE "Invalid actions string"、`file:///tmp/x` IAE "Invalid URL: authority is empty"（JDK-8 为 SIOOBE）、无 scheme。
- implies 方法代数 22：**排序后连续子列表**（indexOfSubList）——`GET,POST,DELETE` 不蕴含 `POST,DELETE` 但 `PUT,POST,DELETE` 蕴含；`*`/`*,GET`；空目标列表被蕴含；空/`:Header` this 返回 false（JDK-8 抛 IOOBE）。
- implies 路径 24：`/-` 递归、`/*` 单层、query 不敏感（双向 true，JDK-8 为 false）。
- implies authority 15 / 头 14（整值 `*` 才通配）。
- equals/hashCode 25：方法序/大小写/默认端口 80/query 剥离后相等。
- 序列化：单方法与**多方法**均完整往返（JDK-8 多方法损坏）。
- newPermissionCollection() 返回 null。

## 六、版本差异（JDK-27 实证）

| 行为 | JDK-8 | JDK-27 |
|---|---|---|
| getActions 分隔符 | `GETPOST:`（无逗号 bug） | `GET,POST:` |
| name 中的 query | 保留 | 剥离 |
| query 对 implies | 双向 false | 双向 true |
| 空/`:Header` this 的 implies | IndexOutOfBoundsException | false |
| `file:///tmp/x` | StringIndexOutOfBoundsException | IAE "Invalid URL: authority is empty" |
| 多方法序列化往返 | 损坏 | 完好 |
