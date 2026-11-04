# javax.swing.text.html.AccessibleHTML.IconElementInfo.IconAccessibleContext — DESIGN

## 1. API 功能
`protected class IconAccessibleContext extends HTMLAccessibleContext implements AccessibleIcon`，表示 `<img>` 元素的可访问信息（角色 ICON、alt 文本、宽高）。关键方法：
- `getAccessibleRole()` 返回 `AccessibleRole.ICON`。
- `getAccessibleName()`/`getAccessibleIconDescription()` 返回 alt 文本。
- `getAccessibleIconWidth()/getAccessibleIconHeight()` 返回 WIDTH/HEIGHT 属性。
- `getAccessibleIcon()` 返回自身数组；`setAccessibleIconDescription` 为文档化 no-op。

## 2. Java 用户如何使用
由 `AccessibleHTML.createElementInfo` 对 IMG 创建 `IconElementInfo`，其 `getAccessibleContext()` 惰性创建本类。测试通过 `kit.getAccessibleContext()` 递归遍历（findContext 后缀 "IconAccessibleContext"）获取。

## 3. 开源用法调研（来源：JDK 源码 + test/jdk）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/AccessibleHTML.java`：`class IconElementInfo`（第 1234 行附近）+ `class IconAccessibleContext`（第 1287 行附近）。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/accessibility/AccessibleIcon.java`：接口定义。

## 4. 测试用例设计（黑盒部分，`IconAccessibleContextTest.testBlackBox`）
1. findContext 得到 IconAccessibleContext、instanceof、`instanceof AccessibleIcon`。
2. 角色 ICON、`getAccessibleName()=="hello"`（alt 文本）。
3. `getAccessibleIconDescription()=="hello"`、宽 100、高 50。
4. 反射 `getAccessibleIcon()` 返回长度 1 且 `[0]==this`。
5. `setAccessibleIconDescription("other")` 后描述不变（no-op）。
