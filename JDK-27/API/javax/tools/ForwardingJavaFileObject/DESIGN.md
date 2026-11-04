# javax.tools.ForwardingJavaFileObject 测试设计

## 1. API 功能
`ForwardingJavaFileObject<F extends JavaFileObject>` 继承 `ForwardingFileObject<F>` 并实现 `JavaFileObject`，把 `JavaFileObject` 特有方法 `getKind()`/`isNameCompatible()`/`getNestingKind()`/`getAccessLevel()` 委托给被包裹对象。

## 2. Java 用户如何使用
```java
ForwardingJavaFileObject<JavaFileObject> f = new ForwardingJavaFileObject<JavaFileObject>(delegate) {
    @Override public String getName() { /* 自定义 */ }
};
```

## 3. 开源代码实际用法调研
- `~/git/jdk25u-dev/src/java.compiler/share/classes/javax/tools/ForwardingJavaFileObject.java`：`getKind` 等直接 `return fileObject.xxx()`。
- JDK 测试中用于包装 `JavaFileObject` 改写 `getCharContent` 或 `getName`。

## 4. 测试用例设计 + 自动化实现
- **白盒**：非接口、实现 `JavaFileObject`、父类 `ForwardingFileObject`。
- **黑盒**：包裹 SOURCE 型 `SimpleJavaFileObject`；断言 `getKind()==SOURCE`、`isNameCompatible("X", SOURCE)` true、`("Y", SOURCE)` false、`("X", CLASS)` false、`getNestingKind()==null`、`getAccessLevel()==null`、`getCharContent` 与 `toUri` 转发一致。
