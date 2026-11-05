# javax.management.loading.MLetContent — 设计案

## 1. API 功能
`MLetContent`：MLET 标签解析结果值对象。`MLetParser` 把每个 `<MLET ...>` 标签解析成本类（`getAttributes` 为小写属性名→值的 Map，`types/values` 为嵌套 `<arg>` 参数），随后 `MLet.getMBeansFromURL` 逐元素实例化 MBean。子类可覆写 `MLet.check(version, codebase, jarfile, mlet)` 实现缓存/版本化。

分组：
- **构造（唯一公开）**：`MLetContent(URL documentURL, Map<String,String> attributes, List<String> types, List<String> values)`。
- **属性 getter（Map 键小写）**：`getJarFiles()`←`archive`、`getCode()`←`code`、`getSerializedObject()`←`object`、`getName()`←`name`、`getVersion()`←`version`。
- **位置**：`getDocumentBase()`（构造传入的同一 URL）、`getCodeBase()`（解析规则见下）。
- **参数**：`getParameterTypes()`/`getParameterValues()`（不可变视图）。
- **原始视图**：`getAttributes()`（不可变 Map）。

## 2. Java 用户典型用法
```java
Map<String,String> attrs = new HashMap<>();
attrs.put("code", "com.example.Foo");
attrs.put("archive", "foo.jar");
attrs.put("codebase", "lib/");
MLetContent mc = new MLetContent(new URL("http://host/docs/mlet.html"),
        attrs, types, values);
URL jarBase = mc.getCodeBase();   // http://host/docs/lib/
String code = mc.getCode();       // com.example.Foo
```

## 3. 参考来源
- Oracle javadoc：`javax.management.loading.MLetContent`（属性键小写约定、codebase 解析、参数列表说明）。
- OpenJDK `jdk/src/share/classes/javax/management/loading/MLetContent.java`（jdk8u-dev）：构造中的 `Collections.unmodifiableMap/unmodifiableList` 包装与 baseURL 三段回退逻辑（codebase 相对解析 → 文档目录 → 文档 URL 本身）。
- OpenJDK `test/jdk/javax/management/loading/MLetContentTest.java`：getter 往返思路。代码自写。

## 4. 测试用例设计（深化 2026-11）
维度与用例映射（断言 6 → 37，JDK-8 jtreg 全绿）：
- **构造/形态**：唯一公开构造 4 参 `(URL, Map, List, List)`（javap 对照）；全 getter 精确往返（六个属性键 + 参数双列表 + 属性 Map 内容与计数）。
- **codebase 解析矩阵**（易错核心）：相对 `cb`（无尾斜杠→补 `/` 再相对文档解析）、`cb/`（原样）、绝对 `http://other.com/x/`（整体替换）、缺失（回退文档目录 `http://example.com/docs/`）、file: 文档（回退其父目录含尾 `/`）、根级文档 `/mlet.txt`（回退站点根 `/`）、空路径文档（无 `/` → 文档 URL 本身）。
- **不可变视图**：`getAttributes().put` / `getParameterTypes().add` / `getParameterValues().remove` → `UnsupportedOperationException`；内容稳定性。
- **键大小写 quirk**：getter 只查小写键——`ARCHIVE`/`Code` 大写键使 `getJarFiles()`/`getCode()` 返回 null。
- **缺失键**：archive/code/object/name/version 全缺 → 各 getter 返回 null（不抛异常）。
- **身份语义**：`getDocumentBase() == 构造传入 URL`（同一实例）。
- **易错规范细节**：URL 相等性比较需用 `new URL(doc, spec)` 构造期望值（`File.toURI().toURL()` 的目录形式无尾斜杠，与回退结果差一个 `/`，equals 为 false——实测踩坑记录）。

## 版本差异
- JDK-27 已移除 MLet 家族，本目录仅 JDK-8 存在。
