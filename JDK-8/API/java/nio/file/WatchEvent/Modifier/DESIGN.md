# java.nio.file.WatchEvent.Modifier 设计文档

## 1. API 功能

注册修饰符接口：唯一方法 name()（带名字的标记）；JDK 附带 com.sun.nio.file.SensitivityWatchEventModifier（HIGH/MEDIUM/LOW）。

（签名以 jdk8u372 javap -public 实测为准。）

## 2. Java 用户如何使用

- 常规用法：register(ws, kinds, modifiers) 第三参传入修饰符数组（通常为空）。
- 失败路径：未知修饰符 UnsupportedOperationException（消息 "Modifier not supported"）。

## 3. 开源代码实际用法调研

典型开源使用方式（按社区常见形态归纳）：
- 敏感度调节（内部 API，社区少用，官方回归有涉及）
- 用法特点：绝大多数调用传空数组或不传。

## 4. 测试用例设计 + 自动化实现

文件：`ModifierTest.java`（白盒 + 黑盒由 main 依次调用）。

设计：白盒（接口仅一个声明方法 name()、abstract、非泛型、无父接口）；自制修饰符 name() 契约；SensitivityWatchEventModifier 实测（三档 HIGH MEDIUM LOW、实现 Modifier、档位互异）；黑盒：空修饰符数组注册成功、HIGH 修饰注册后事件仍送达（context 精确）、未知修饰符 → UnsupportedOperationException 且消息精确（实测 "Modifier not supported"）。

自动化与健壮性：
- 输出 `PASS (N assertions)` 计数断言；失败抛 AssertionError 立即定位。
- 临时目录 + finally 清理；poll 有界；headless 安全，不加 @key。
