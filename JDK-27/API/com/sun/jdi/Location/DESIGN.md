# com.sun.jdi.Location 行为化测试设计

## 1. API 功能
类型成员与调用栈：Field/Method/TypeComponent/Location/LocalVariable/StackFrame/ThreadReference/ThreadGroupReference 等，描述成员与线程栈帧。

公开成员（JDK-27 javap）：
  - `public interface com.sun.jdi.Location extends com.sun.jdi.Mirror, java.lang.Comparable<com.sun.jdi.Location> {`
  - `public abstract com.sun.jdi.ReferenceType declaringType();`
  - `public abstract com.sun.jdi.Method method();`
  - `public abstract long codeIndex();`
  - `public abstract java.lang.String sourceName() throws com.sun.jdi.AbsentInformationException;`
  - `public abstract java.lang.String sourceName(java.lang.String) throws com.sun.jdi.AbsentInformationException;`
  - `public abstract java.lang.String sourcePath() throws com.sun.jdi.AbsentInformationException;`
  - `public abstract java.lang.String sourcePath(java.lang.String) throws com.sun.jdi.AbsentInformationException;`
  - `public abstract int lineNumber();`
  - `public abstract int lineNumber(java.lang.String);`
  - `public abstract boolean equals(java.lang.Object);`
  - `public abstract int hashCode();`

## 2. Java 用户如何使用
从 ReferenceType 拿 Field/Method，读 name/signature/type/修饰符；挂起线程后 `thread.frames()`/`frame.visibleVariables()` 取 LocalVariable 与值。

## 3. 开源代码实际用法调研
来源：~/git/jdk25u-dev/test/jdk/com/sun/jdi/ 的 FramesTest.java、Frames2Test.java、GetLocalVariables.java、FetchLocals.java、AccessSpecifierTest.java。

## 4. 测试用例设计 + 自动化实现
testBlackBox 挂起 debuggee 主线程，遍历 frames 找 main 帧，验证 location/visibleVariables/getValue 等。

实现见同目录 `LocationTest.java` 的 `testWhiteBox()`（结构/继承/成员形态）与 `testBlackBox()`（组合操作行为断言）。
