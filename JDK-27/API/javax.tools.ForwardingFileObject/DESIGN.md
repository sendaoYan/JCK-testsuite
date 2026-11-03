# javax.tools.ForwardingFileObject 测试设计

## 1. API 功能
`ForwardingFileObject<F extends FileObject>` 是 `FileObject` 的转发器：持有 `protected final F fileObject`，把 `FileObject` 的所有方法委托给被包裹对象。用途是在不改变行为的前提下，通过子类覆盖个别方法实现装饰。

## 2. Java 用户如何使用
```java
ForwardingFileObject<FileObject> f = new ForwardingFileObject<FileObject>(delegate) {
    @Override public String getName() { return super.getName() + ".wrapped"; }
};
```
构造器为 protected，需匿名子类化。

## 3. 开源代码实际用法调研
- `~/git/jdk25u-dev/src/java.compiler/share/classes/javax/tools/ForwardingFileObject.java`：所有方法直接 `return fileObject.xxx()`。
- `~/git/jdk25u-dev/src/java.compiler/share/classes/javax/tools/ForwardingJavaFileObject.java`：继承本类并扩展 JavaFileObject 转发。
- javac 测试中有以转发器包一层再改写行为的装饰用法。

## 4. 测试用例设计 + 自动化实现
- **白盒**：非接口、实现 `FileObject`、`fileObject` 字段 protected final。
- **黑盒**：用带 `getCharContent` 的 `SimpleJavaFileObject` 作委托；断言 `toUri`/`getName`/`getLastModified`/`getCharContent` 转发一致；`openReader` 读回内容；`openInputStream`/`openOutputStream` 抛 `UnsupportedOperationException`；`delete()` 返回 false。
