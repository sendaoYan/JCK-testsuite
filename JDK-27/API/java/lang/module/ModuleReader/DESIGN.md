# java.lang.module.ModuleReader — 设计方案

## 1. API 功能
模块资源读取接口（extends Closeable）：`find(name)`→Optional<URI>、`open(name)`→Optional<InputStream>（default）、`read(name)`→Optional<ByteBuffer>（default）、`release(ByteBuffer)`（default no-op）、`list()`→Stream<String>（模块内全部资源，'/'分隔）、`close()`。

## 2. Java 用户如何使用
```java
try (ModuleReader reader = module.getLayer()... /* or */ mref.open()) {
    try (InputStream in = reader.open("com/acme/Res.properties").orElseThrow()) { ... }
    try (Stream<String> names = reader.list()) { names.filter(n -> n.endsWith(".xml"))... }
}
```

## 3. 开源代码实际用法调研
- JDK `src/java.base/share/classes/java/lang/module/ModuleReader.java`。
- Spring Boot 可执行 jar 的 LaunchedURLClassLoader 借鉴该模型按模块读资源。
- JDK `test/jdk/java/lang/module/ModuleReader/ModuleReaderTest.java`（test/jdk 下大量用例：jimage/jmod/jar 后端一致性）。

## 4. 测试用例设计 + 自动化实现
白盒：接口、Closeable 父接口、6 个方法、open/read/release 为 default、find/list 抽象。
黑盒：java.base 读者 find/open/read 三种途径读到同一 class 魔数 CAFEBABE；find 返回 jrt URI 且 path 匹配；read 后 buffer 位置推进；absent/空名 → Optional.empty 不抛；list 含 Object.class/String.class 且名字无前导斜杠；第二读者独立；close 幂等。
对应 `ModuleReaderTest.java` 的 `testBlackBox()`。

## 契约深化（2026-11）

### 1. API 功能（本轮新增维度）
- 关闭后契约：read/open/find/list 四路在 close 后全部抛 IOException "ModuleReader is closed"（精确消息锁定）。
- read：module-info.class 载荷 >1000、magic CA FE BA BE、两次 read 独立 buffer（位置互不影响）、release 可重复调用（advisory）、缺席/空名 → empty Optional。
- open：同一资源两次 open 全量内容相等、流独立、available() 可调用。
- list：资源计数 >1000、.class 计数 >900、名字唯一、无反斜杠、module-info.class 在列。
- find：`jrt` scheme、path 尾匹配、缺席名 empty；重复 find 不保证 URI equals（实测）只断言仍 present。
- 默认方法闭环：file URI 背书的自定义 reader —— 默认 `read` 读出文件字节、默认 `open` 流式同源、缺席名 empty、默认 `release` no-op 可重复。

### 2. 使用方式
- 系统 reader（java.base/java.logging）+ 文件背书自定义 reader 双轨验证；全部 try-with-resources。

### 3. 参考来源
- Oracle javadoc ModuleReader（default 方法语义）；OpenJDK ModuleReader 测试思路（自写）。

### 4. 测试用例设计 + 易错规范细节
- 断言数 35→102。
- 易错点：
  - `list()` 返回 Stream 必须 try-with-resources（红线）；
  - 关闭后的行为实测是 IOException 而非 empty Optional——按实测锁定；
  - `release(bb)` 后再 release 不抛；
  - java.logging 的 reader 看不到 java.base 的资源（模块隔离正断言）。
