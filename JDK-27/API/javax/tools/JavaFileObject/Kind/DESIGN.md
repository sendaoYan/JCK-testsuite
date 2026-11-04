# javax.tools.JavaFileObject.Kind 测试设计

## 1. API 功能
`JavaFileObject.Kind` 是 Java 文件对象种类枚举：`SOURCE(".java")`、`CLASS(".class")`、`HTML(".html")`、`OTHER("")`。公开 final 字段 `extension` 表示常规扩展名，无约定的为空串。

## 2. Java 用户如何使用
```java
if (jfo.getKind() == JavaFileObject.Kind.SOURCE) { ... }
String ext = JavaFileObject.Kind.CLASS.extension; // ".class"
```

## 3. 开源代码实际用法调研
- `~/git/jdk25u-dev/src/java.compiler/share/classes/javax/tools/JavaFileObject.java`：`enum Kind` 定义与 `extension` 字段。
- `~/git/jdk25u-dev/src/java.compiler/share/classes/javax/tools/SimpleJavaFileObject.java`：`isNameCompatible` 使用 `kind.extension` 拼出期望文件名。
- javac `com.sun.tools.javac.util` 等地方按 `Kind` 区分源码/类文件。

## 4. 测试用例设计 + 自动化实现
- **白盒**：枚举、继承 `Enum`、4 常量、公开 final `extension` 字段为 String。
- **黑盒**：`values()` 顺序 SOURCE/CLASS/HTML/OTHER；`extension` 分别为 `.java`/`.class`/`.html`/`""`；`name`/`valueOf`/`ordinal` 断言。
