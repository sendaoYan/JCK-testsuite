# javax.tools.Diagnostic 测试设计

## 1. API 功能
`Diagnostic<S>` 是 javac/javadoc 诊断信息的接口，泛型 `S` 表示产生诊断的来源类型（通常是 `JavaFileObject`）。方法分四组：
- **种类/来源**：`getKind()` 返回 `Diagnostic.Kind` 枚举；`getSource()` 返回 `S`。
- **位置**：`getPosition()` / `getStartPosition()` / `getEndPosition()` 返回字符偏移，`NOPOS`（= -1）表示无位置。
- **行列**：`getLineNumber()` / `getColumnNumber()` 返回 1-based 行列号，无位置时返回 `NOPOS`。
- **消息**：`getCode()` 返回诊断码（如 `compiler.err.illegal.start.of.expr`）；`getMessage(Locale)` 返回本地化消息。

## 2. Java 用户如何使用
用户一般不直接实现 `Diagnostic`，而是从 `DiagnosticCollector` 或 `DiagnosticListener` 拿到编译器产生的实例：
```java
DiagnosticCollector<JavaFileObject> dc = new DiagnosticCollector<>();
boolean ok = compiler.getTask(null, null, dc, opts, null, sources).call();
for (Diagnostic<? extends JavaFileObject> d : dc.getDiagnostics()) {
    d.getKind(); d.getLineNumber(); d.getColumnNumber(); d.getMessage(Locale.ROOT);
}
```
常量 `Diagnostic.NOPOS` 用于判断“没有位置信息”。

## 3. 开源代码实际用法调研
- `~/git/jdk25u-dev/test/langtools/tools/lib/snippets/SnippetUtils.java`：`parse(String body, Consumer<? super Diagnostic<? extends JavaFileObject>> showDiag)` 通过 `DiagnosticCollector` 收集后遍历 `d.getKind()/getMessage()` 等（来源：JDK langtools 测试库）。
- `~/git/jdk25u-dev/test/langtools/tools/javac/T6956638.java`：`DiagnosticCollector<JavaFileObject>` 与 `compiler.getTask(...)` 组合（来源：JDK langtools 测试）。
- javac 内部 `com.sun.tools.javac.util.JCDiagnostic` 实现该接口，真实诊断由 `JavacTask` 产生。

## 4. 测试用例设计 + 自动化实现
- **白盒**：`Class.forName("javax.tools.Diagnostic")` 断言接口、无父接口、9 个方法、`NOPOS` 字段。
- **黑盒**：
  1. `Diagnostic.NOPOS == -1`。
  2. 匿名实现 `Diagnostic<Object>` → `DiagnosticCollector.report` → 读回，断言 8 个字段逐一 round-trip，且 `getMessage(ROOT)`/`getMessage(null)` 一致。
  3. 真实失败编译（`class Y { void m() { int x = ; } }`）→ `DiagnosticCollector` 拿到 javac 诊断，断言 `getKind()==ERROR`、`lineNumber==1`、`columnNumber>0`、`position>=0`、code/message 非空、`getSource()` 是源文件对象。
