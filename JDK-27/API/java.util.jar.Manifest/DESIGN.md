# java.util.jar.Manifest 测试方案

白盒结构断言在 `ManifestTest.testWhiteBox()`，黑盒行为断言在 `ManifestTest.testBlackBox()`。本文件聚焦黑盒方案，自动化实现见 `ManifestTest.java` 黑盒部分。

## 1. API 功能（按方法分组）

- **构造**：`Manifest()`、`Manifest(InputStream)`、`Manifest(Manifest)`（拷贝）。
- **查询**：`getMainAttributes()`、`getEntries()`（`Map<String,Attributes>`）、`getAttributes(String)`。
- **修改**：`clear()`、`read(InputStream)`（合并入当前）。
- **I/O/工具**：`write(OutputStream)`（CRLF、72 字节折行）、`equals`、`hashCode`、`clone`。JDK-27 无 `getVersion/setVersion`（`javap` 实测）。

## 2. Java 用户如何使用

- 构造：`new Manifest()`；`new Manifest(manifestInputStream)`；`new Manifest(existing)`。
- 典型调用：`m.getMainAttributes().put(Name.MANIFEST_VERSION, "1.0")`；`m.getEntries().put("pkg/", attrs)`；`m.getAttributes("pkg/")`；`m.write(out)`。
- 真实组合链路：`put 主属性/段属性 → write 字节 → 读回 Manifest → equals/hashCode`；`read()` 合并主属性与条目。

## 3. 开源代码实际用法调研

> 本环境 WebSearch 不可用，调研基于 JDK-27 实现源码 `~/git/jdk25u-dev` 与自带测试 `test/jdk`，逐条标注来源，未凭记忆编造。

- `test/jdk/java/util/jar/Manifest/WriteBinaryStructure.java:45-47`：`Manifest mf = new Manifest(); mf.getMainAttributes().put(Name.MANIFEST_VERSION, "1.0"); mf.getMainAttributes().put(new Name("Key"), "Value");` —— 主属性写入。
- `test/jdk/java/util/jar/Attributes/TestAttrsNL.java:96`：`new Manifest(new ByteArrayInputStream(manifestStr.getBytes(UTF_8)))` —— 从输入流构造。
- `src/java.base/share/classes/java/util/jar/Manifest.java:116`：`attr.putAll(man.getMainAttributes());` —— 拷贝构造主属性来源。

## 4. 测试用例设计 + 自动化实现

- **空**：默认构造主属性与条目皆空；`getAttributes(未知/null)` 为 null。
- **主属性/段属性**：`put` 后 `getValue` 往返；`getEntries().put` 后 `getAttributes` 返回同对象。
- **写读往返**：`write` 输出含 `Manifest-Version: 1.0\r\n` 与 `Name: pkg/\r\n`；读回后主属性/段属性/`equals`/`hashCode` 一致。
- **拷贝/克隆**：`new Manifest(m)` 相等且主属性独立；`clone()` 相等且 `clear` 不影响原对象。
- **read 合并**：向已有条目的 Manifest `read` 另一个 manifest 后主属性被合并、新旧条目共存。
- **边界/异常**：`new Manifest(畸形输入)` 抛 `IOException`；`clear` 清空主属性与条目。
- **自动化**：`ManifestTest.testBlackBox()` 内 `testEmpty/testMainAndEntries/testWriteReadRoundTrip/testCopyAndClone/testReadMerges/testMalformedInput/testClearAndToString`。
- **健壮性**：固定 UTF-8、无墙钟/随机、无全局状态/文件/网络/线程。
