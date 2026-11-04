# javax.swing.text.html.FormSubmitEvent.MethodType — DESIGN

## 1. API 功能
表单提交方法枚举（`FormSubmitEvent` 的嵌套 enum，`extends Enum`，常量 GET、POST）。核心方法分组：
- 枚举常量：`GET`、`POST`。
- 继承 Enum：`values()`、`valueOf(String)`、`ordinal()`、`name()`。

## 2. Java 用户如何使用
用户从 `FormSubmitEvent.getMethod()` 拿到 MethodType 后比较常量：`evt.getMethod() == FormSubmitEvent.MethodType.POST`。需要按名字解析时用 `MethodType.valueOf("GET")`，遍历用 `values()`。典型链：`if (FormSubmitEvent.MethodType.valueOf("POST") == evt.getMethod()) ...`。

## 3. 开源用法调研（来源：JDK 源码）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/FormView.java:536-538`：`methodType = isPostMethod ? FormSubmitEvent.MethodType.POST : FormSubmitEvent.MethodType.GET;` 按是否 POST 二选一。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/FormSubmitEvent.java`：MethodType 定义并作为 getMethod 返回类型。

## 4. 测试用例设计（黑盒部分，`MethodTypeTest.testBlackBox`）
1. `values()` 长度 == 2，且 values[0]==GET、values[1]==POST。
2. `valueOf("GET"/"POST")` 返回对应常量。
3. GET.ordinal()==0、POST.ordinal()==1；name() 分别为 "GET"/"POST"。
4. `valueOf("NOPE")` 抛 IllegalArgumentException。
