# java.rmi.Naming 测试设计案（离线）

> 覆盖目标：离线契约（final 类、五静态方法、非法 URL→MalformedURLException、null Remote→NPE）。
> **不真起 RMI 服务、不绑端口、不联网**——所有断言在发生任何连接前就抛出。

## 1. API 功能
- 静态方法：`lookup`/`bind`/`unbind`/`rebind`/`list`，均声明 `MalformedURLException`。

## 2. 使用方式
- 通过 `rmi://host:port/name` 访问远程注册表。

## 3. 参考来源
- Java SE 8/27 javadoc（`java/rmi/Naming.html`）。
- OpenJDK `test/jdk/java/rmi/Naming`；JCK `api/java_rmi/Naming`。

## 4. 测试用例设计
| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | final + 具体类 | testStructure |
| P0 | 五方法对非法 scheme/fragment/query/非法 URL 均先抛 MalformedURLException | testMalformedURL |
| P0 | bind/rebind null Remote→NPE（连接前） | testNullRemote |

## 关键规范细节
- 使用 `"http://host/x"`（非法 scheme）或带 `#frag`/`?query`/含空格的 URL 触发 `MalformedURLException`，**绝不**用裸 `rmi://` 地址（会真的尝试连接 localhost）。
- `bind`/`rebind` 在 `parseURL` 之后、`getRegistry` 之前检查 null Remote，故 null 检查是离线安全的。
