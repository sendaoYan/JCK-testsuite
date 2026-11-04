# javax.tools.ForwardingJavaFileManager 测试设计

## 1. API 功能
`ForwardingJavaFileManager<M extends JavaFileManager>` 实现 `JavaFileManager`，持有 `protected final M fileManager`，把 `JavaFileManager` 全部方法委托给被包裹的文件管理器。用于在标准文件管理器上做装饰（如自定义 `getJavaFileForOutput` 改写输出位置、拦截 `list`）。

## 2. Java 用户如何使用
```java
StandardJavaFileManager base = compiler.getStandardFileManager(null, null, null);
JavaFileManager fm = new ForwardingJavaFileManager<JavaFileManager>(base) {
    @Override public JavaFileObject getJavaFileForOutput(...) { /* 自定义 */ }
};
```

## 3. 开源代码实际用法调研
- `~/git/jdk25u-dev/src/java.compiler/share/classes/javax/tools/ForwardingJavaFileManager.java`：全部方法 `return fileManager.xxx()`。
- javac 内部 `com.sun.tools.javac.api.ClientCodeWrapper.WrappedJavaFileManager` 用类似的委托做边界隔离。
- 注解处理器测试常用转发器改写 `inferBinaryName` 或 `getJavaFileForOutput`。

## 4. 测试用例设计 + 自动化实现
- **白盒**：非接口、实现 `JavaFileManager`、`fileManager` 字段 protected final。
- **黑盒**：包裹真实 `StandardJavaFileManager`；断言 `getClassLoader`/`hasLocation`/`isSupportedOption("-classpath")==1`、`isSupportedOption("--bogus")==-1`、`handleOption` 转发；`setLocation` 由底层 fm 设置后 `getFileForOutput` 返回非空、`isSameFile` true、`list` 非空；`flush`/`close` 正常。
