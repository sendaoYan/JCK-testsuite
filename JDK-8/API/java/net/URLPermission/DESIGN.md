# URLPermission 设计文档

## 一、API 功能

`java.net.URLPermission`（JDK-8 引入）HTTP/资源访问权限：`url`（scheme://authority/path）+ `actions` = `"methods:headers"`。方法与请求头均为列表，构造时**排序**后存储；implies 要求目标是 this 排序后的**连续子列表**（`Collections.indexOfSubList`），除非 this 列表首元素为 `*`。路径支持 `/*`（单层）与 `/-`（递归）通配。`newPermissionCollection()` 返回 null。

## 二、Java 用户如何使用

策略文件 `permission java.net.URLPermission "http://*.example.com/-", "GET,POST:Accept"`；HttpClient/HttpURLConnection 安全检查。

## 三、开源代码实际用法调研（参考来源）

OpenJDK jdk8u `java.net.URLPermission` 源码：`init`（actions 唯一冒号切分、多冒号 IAE "Invalid actions string"、normalizeMethods 大写化/排序、normalizeHeaders 首字母大写化/排序、`Collections.sort`）；`implies`（方法/头连续子列表、scheme 精确、authority 通配、路径 `/-` 前缀与 `/*` 无斜杠后缀）；`actions()` 拼接字符串（**JDK-8 无分隔符——丢失逗号**）；序列化写 actions 字符串、读回重新解析。

## 四、测试用例设计（154 断言）

- **白盒**：final、直接 Permission 子类、Serializable、newPermissionCollection **返回 null**、无 getMethodNames/getHeaderNames/getURL。
- **构造/getActions**（20+）：1 参默认 `*:*`；`GET`→`GET:`；JDK-8 多方法/多头**无逗号**拼接 `"GETPOST:"`、`"GET:BarX-Foo"`；排序后输出 `"AMZ:"`、`"DELETEGETPOST:"`；空动作/`:`/`:header` 合法且规范化 `":"`/`":Header"`；query 保留在 name。
- **构造负例**（8）：方法/头内空格 IAE 精确消息；双冒号 IAE "Invalid actions string"；null url NPE；`file:///tmp/x` 抛 **StringIndexOutOfBoundsException**（JDK-8 泄漏）；无 scheme IAE。
- **implies 方法代数**（22）：排序后连续子列表——`GET,POST,DELETE` 不蕴含 `POST,DELETE`（排序 [DELETE,GET,POST] 无连续 [DELETE,POST]）但蕴含 DELETE/GET/GET,POST；`*`/`*,GET` 全蕴含；大小写不敏感；空目标列表被蕴含。
- **implies 路径**（21）：精确相等；`/-` 递归前缀（`/a/-` 不蕴含 `/a` 与 `/ab`）；`/*` 单层（`/*`→`/x` 真、`/anything/here` 假）；JDK-8 **query 参与精确比较**（带 query 的 name 双向均不蕴含无 query 版）。
- **implies scheme/authority**（15）：scheme 精确（http 不蕴含 https）；默认端口 80 等价；主机大小写不敏感；`*.example.com` 蕴含子域不含裸域。
- **implies 头**（14）：整值 `*` 通配、`X-*`/`X-Foo*` **不通配**、大小写不敏感、多头要求连续子集、无头不蕴含有头、有头蕴含无头。
- **equals/hashCode**（24）：方法序无关、大小写、默认端口等价、路径/scheme/端口/头差异不等、`GET`≠`GET:Accept`。
- **序列化**：单方法往返完好；**JDK-8 多方法往返损坏**（actions 串无逗号→读回解析成单个方法 "GETPOST"），equals 仍真（内部列表存活）但 implies 子集失效——负向断言。
- **JDK-8 专属怪癖**（5）：空动作/`:`/`:Header` 作 this 的 implies 抛 **IndexOutOfBoundsException**；file URL SIOOBE。

## 五、易错规范细节（探针实证）

- **排序+连续子列表**：implies 不是集合包含！`"GET,POST,DELETE"` 不蕴含 `"POST,DELETE"`（但 `"PUT,POST,DELETE"` 蕴含——排序后 [DELETE,POST,PUT] 含连续 [DELETE,POST]）。这是规范文档没写清、源码 `indexOfSubList` 决定的行为。
- **getActions 丢逗号（JDK-8 bug）**：`"GET,POST"` → `"GETPOST:"`；equals 用排序串比较所以不受影响，但序列化往返会损坏。
- 头部通配只有整值 `*`；`X-Foo*` 是字面值。
- `/-` 是"前缀以 `/-` 去掉 `-` 后开头"即 `/x/`；`/*` 要求剩余段不含 `/` 且不为 `-`。
- query 留在 name 中参与比较（JDK-8）；`http://h/a/b` 与 `http://h/a/b?x=1` 互不蕴含。
- `newPermissionCollection()` 返回 null（两侧一致）——与其他 Permission 不同，无集合语义。

## 六、版本差异（JDK-8 vs JDK-27）

| 行为 | JDK-8 | JDK-27 |
|---|---|---|
| getActions 分隔符 | `GETPOST:` / `GET:BarX-Foo`（无逗号，bug） | `GET,POST:` / `GET:Bar,X-Foo` |
| name 中的 query | 保留 `...?x=1` | 剥离 |
| query 对 implies | 参与精确比较（双向 false） | 双向 true（query 敏感度取消） |
| 空/`:Header` this 的 implies | IndexOutOfBoundsException（bug） | 返回 false |
| `file:///tmp/x` | StringIndexOutOfBoundsException | IAE "Invalid URL: authority is empty" |
| 多方法序列化往返 | 损坏（implies 子集失效） | 完好 |
| null url NPE 消息 | 裸 "null" | 帮助性消息（断类型） |
