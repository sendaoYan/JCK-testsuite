# javax.naming.directory.BasicAttributes 设计案

## 1. API 功能
- 构造器：`BasicAttributes()`、`(boolean ignoreCase)`、`(String id, Object value)`、`(String id, Object value, boolean ignoreCase)`。
- 查询：`isCaseIgnored()`、`size()`、`get(String)`、`getAll()`、`getIDs()`。
- 修改：`put(String,Object)`、`put(Attribute)`、`remove(String)`。
- 工具：`clone()`、`equals`、`hashCode`、`toString()`。

## 2. Java 用户如何使用
典型链路：`new BasicAttributes(true)`（忽略大小写）→ `put("cn","Alice")` → `get("CN")`（大小写不敏感命中）→ `getIDs()` 枚举 → `put(new BasicAttribute(...))` → `remove("cn")`。
大小写敏感/忽略通过构造器开关控制；忽略大小写时 id 统一以小写存储。

## 3. 开源代码实际用法调研
- 来源：`~/git/jdk25u-dev/src/java.naming/share/classes/javax/naming/directory/BasicAttributes.java`（`ignoreCase` 语义：true 时 `toLowerCase(Locale.ENGLISH)`）。
- 来源：`~/git/jdk25u-dev/test/jdk` 中 `SearchResult`/`BasicAttributes` 组合构造搜索结果属性集。
- 来源：`javax/naming/module` 下 LDAP 模块用例用 `BasicAttributes` 组装属性。

## 4. 测试用例设计
黑盒断言（BasicAttributesTest.testBlackBox）：
- 默认大小写敏感：put("cn") 后 get("CN")==null、put("CN") 后 size==2。
- put(String,Object)/put(Attribute)/getIDs/getAll（排序后断言）。
- remove 语义（存在返回非 null、不存在返回 null）。
- 忽略大小写：put("cn") 后 get("CN") 命中且 size==1。
- equals/hashCode 一致性、值不同不相等、clone 内容一致且实例不同。
- toString 含 id。
