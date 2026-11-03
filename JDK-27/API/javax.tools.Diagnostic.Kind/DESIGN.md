# javax.tools.Diagnostic.Kind 测试设计

## 1. API 功能
`Diagnostic.Kind` 是诊断种类枚举，按严重程度顺序定义：`ERROR`、`WARNING`、`MANDATORY_WARNING`、`NOTE`、`OTHER`。它由 `Diagnostic.getKind()` 返回，用户据此区分错误/警告/提示。

## 2. Java 用户如何使用
```java
for (Diagnostic<? extends JavaFileObject> d : collector.getDiagnostics()) {
    if (d.getKind() == Diagnostic.Kind.ERROR) { /* 处理错误 */ }
}
```
`valueOf`/`values`/`name`/`ordinal` 提供标准枚举能力，`ERROR.ordinal()==0` 可作为严重程度排序依据。

## 3. 开源代码实际用法调研
- `~/git/jdk25u-dev/test/langtools/tools/lib/snippets/SnippetUtils.java`：通过 `collector.getDiagnostics().forEach(showDiag)` 遍历，调用 `d.getKind()` 判断诊断级别。
- javac 内部 `com.sun.tools.javac.util.JCDiagnostic.DiagnosticType` 与 `Diagnostic.Kind` 相互映射（`ERROR`→`DiagnosticType.ERROR` 等）。
- `~/git/jdk25u-dev/src/java.compiler/share/classes/javax/tools/Diagnostic.java`：枚举常量及注释定义（ERROR/WARNING/MANDATORY_WARNING/NOTE/OTHER）。

## 4. 测试用例设计 + 自动化实现
- **白盒**：`Class.forName("javax.tools.Diagnostic$Kind")` 断言枚举、继承 `Enum`、5 个常量。
- **黑盒**：
  1. `values()` 长度 5 且顺序为 ERROR/WARNING/MANDATORY_WARNING/NOTE/OTHER。
  2. 每个常量 `name()` 精确匹配。
  3. `valueOf` 往返、`ordinal`（ERROR=0、OTHER=4）。
