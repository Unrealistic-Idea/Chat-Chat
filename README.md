# Chat-Chat Android Application

一个功能丰富的Android聊天应用，支持与AI和真实用户的交流。

## 功能特性

- ✅ JWT身份认证和密码加密
- ✅ 本地数据库持久化 (Room)
- ✅ 轻盈简约的Material Design界面
- ✅ 聊天列表页面
- ✅ 个人信息页面
- ✅ 名片分享功能
- ⏳ AI聊天集成
- ⏳ 群聊功能
- ⏳ 图片、语音、表情支持
- ⏳ Markdown和图表显示
- ⏳ 消息撤回和已读状态
- ⏳ 云端数据同步
- ⏳ 推送通知
- ⏳ 自定义头像和挂件

## 技术栈

- **开发语言**: Java
- **数据库**: Room (SQLite)
- **认证**: JWT
- **加密**: Android Keystore + AES
- **UI框架**: Material Design Components
- **网络请求**: Retrofit + OkHttp
- **图片加载**: Glide
- **Markdown**: Markwon
- **图表**: MPAndroidChart

## 项目结构

```
app/src/main/java/com/chatchat/
├── auth/           # JWT认证工具
├── database/       # Room数据库层
├── model/          # 数据模型
├── network/        # 网络请求
├── ui/             # 用户界面
│   ├── auth/       # 登录界面
│   ├── chat/       # 聊天界面
│   ├── profile/    # 个人资料
│   └── adapter/    # RecyclerView适配器
└── utils/          # 加密等工具类
```

## 安装和运行

1. 克隆项目
2. 使用Android Studio打开
3. 构建并运行到设备或模拟器

## 许可证

Apache License 2.0 - 详见 [LICENSE](LICENSE) 文件