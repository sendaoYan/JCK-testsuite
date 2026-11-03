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
