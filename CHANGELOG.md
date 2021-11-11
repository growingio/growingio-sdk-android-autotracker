# [](https://github.com/growingio/growingio-sdk-android-autotracker/compare/v3.3.1-11111-SNAPSHOT...v) (2021-11-11)



## [3.3.1-11111-SNAPSHOT](https://github.com/growingio/growingio-sdk-android-autotracker/compare/v3.3.1...v3.3.1-11111-SNAPSHOT) (2021-11-11)


### Bug Fixes

* databse or disk is full ([c83e64f](https://github.com/growingio/growingio-sdk-android-autotracker/commit/c83e64f712b94cce2942ac77dcfa756f5dc78ad6))
* inject webview ([95eb1df](https://github.com/growingio/growingio-sdk-android-autotracker/commit/95eb1dfa9b221b226aedd4dfb80865cd29950258))



## [3.3.1](https://github.com/growingio/growingio-sdk-android-autotracker/compare/v3.3.0...v3.3.1) (2021-10-28)


### Bug Fixes

* unknow contentprovider ([3de0829](https://github.com/growingio/growingio-sdk-android-autotracker/commit/3de082941cbd30952cb47964a3cac636d9af80c5))


### Features

* saas demo转为cdp demo ([#117](https://github.com/growingio/growingio-sdk-android-autotracker/issues/117)) ([58330cf](https://github.com/growingio/growingio-sdk-android-autotracker/commit/58330cfaa144b1dcd5e9e6353df81c117357adb7))



# [3.3.0](https://github.com/growingio/growingio-sdk-android-autotracker/compare/v3.2.3-09141-SNAPSHOT...v3.3.0) (2021-10-09)


### Bug Fixes

* bridgeWebView无效 ([2c63642](https://github.com/growingio/growingio-sdk-android-autotracker/commit/2c63642f85ab50700a62ba07e50a9b4aa5dbbc69))
* ci中ndk环境升级 ([#107](https://github.com/growingio/growingio-sdk-android-autotracker/issues/107)) ([d5924e6](https://github.com/growingio/growingio-sdk-android-autotracker/commit/d5924e6e80354e7ee856bb5421f6372f678f03bb))
* menuitem的title如果为null导致npe ([5f1e9f0](https://github.com/growingio/growingio-sdk-android-autotracker/commit/5f1e9f0a31c2bbaca0aedd171fbcd3cd793f56f1))
* webview looper检查 ([69e6c1a](https://github.com/growingio/growingio-sdk-android-autotracker/commit/69e6c1ad248488c2a4f21baf24ec5ea8f4f0d26a))
* 增加oaid本地发版配置 ([ded0395](https://github.com/growingio/growingio-sdk-android-autotracker/commit/ded0395950973c2bd1e19344f4834c99197111e0))
* 外部函数对参数校验 ([34d86c2](https://github.com/growingio/growingio-sdk-android-autotracker/commit/34d86c2e275eb09f14e18308fc2ba7c47e363c42))
* 过滤事件及字段of函数传null崩溃 ([a0b02dd](https://github.com/growingio/growingio-sdk-android-autotracker/commit/a0b02dd34891ec92142779560fbac4b9bfd4bd8f))
* 适配中文 ([31e41ff](https://github.com/growingio/growingio-sdk-android-autotracker/commit/31e41ffd2aa288effe362f2d27e2208e79f33b08))
* 配置增加模块注册 ([3ddae53](https://github.com/growingio/growingio-sdk-android-autotracker/commit/3ddae532dc43c461c7ac5da3b26ff2def20e3f05))


### Features

* idmapping ([5a4bac8](https://github.com/growingio/growingio-sdk-android-autotracker/commit/5a4bac88fc33f2ec1396b64583d5984224abae9b))
* 适配oaid1.0.27版本 ([b216efa](https://github.com/growingio/growingio-sdk-android-autotracker/commit/b216efa533c7e2c9e869c47aa652f6725c407cf3))



## [3.2.3-09141-SNAPSHOT](https://github.com/growingio/growingio-sdk-android-autotracker/compare/v3.2.2...v3.2.3-09141-SNAPSHOT) (2021-09-14)


### Bug Fixes

* 延迟初始化访问导致NPE ([965f776](https://github.com/growingio/growingio-sdk-android-autotracker/commit/965f776c21be398200fe962e7f8e8dd932ba8ebe))



## [3.2.2](https://github.com/growingio/growingio-sdk-android-autotracker/compare/v3.2.1...v3.2.2) (2021-09-09)


### Bug Fixes

* memory leak ([4b74679](https://github.com/growingio/growingio-sdk-android-autotracker/commit/4b74679ad3ba86d91e660ed34249d645ca471a0c))
* 业务和配置逻辑分离 ([2c6dc5a](https://github.com/growingio/growingio-sdk-android-autotracker/commit/2c6dc5a33ea0866b8e8a48157896310cbac1e7fe))
* 多线程导致前后台判断异常 ([f109d3b](https://github.com/growingio/growingio-sdk-android-autotracker/commit/f109d3b255beac5903cc19786ba30aa3266b19c9))
* 子线程初始化问题 ([0b06618](https://github.com/growingio/growingio-sdk-android-autotracker/commit/0b06618740fe64c7d9eb2dd99ac61b0107c6e48c))



## [3.2.1](https://github.com/growingio/growingio-sdk-android-autotracker/compare/v3.2.0...v3.2.1) (2021-08-31)


### Bug Fixes

* activityPage为空 ([352698f](https://github.com/growingio/growingio-sdk-android-autotracker/commit/352698f9592fa8297d48014f9180b34dd1df163a))
* api保持一致 ([3a49948](https://github.com/growingio/growingio-sdk-android-autotracker/commit/3a499489d4f0a4fb1cbdcae79c9bc6f65f27a3ca))
* debugger 不发送logger_data ([16e613f](https://github.com/growingio/growingio-sdk-android-autotracker/commit/16e613ffd01cc9a1da2895cbc2c7b2b8f4e85753))
* disable然后enable会存在计数为负的情况 ([7108b91](https://github.com/growingio/growingio-sdk-android-autotracker/commit/7108b9153f6874753c02fa22e40de297fc8687f5))
* pausetime不受采集开关影响 ([2d9dfbb](https://github.com/growingio/growingio-sdk-android-autotracker/commit/2d9dfbb799170c77c080de7ab160004ea5ec6757))
* PI-35149 按照协议发送client_info ([3a26c19](https://github.com/growingio/growingio-sdk-android-autotracker/commit/3a26c1914a745f3d1f09f32650334e2beb540de8))
* resend时vst的timestamp可能为0 ([#87](https://github.com/growingio/growingio-sdk-android-autotracker/issues/87)) ([df534a7](https://github.com/growingio/growingio-sdk-android-autotracker/commit/df534a7c7edf3e0742f01854b98ebc62742e67cf))
* sonarcloud check ([#84](https://github.com/growingio/growingio-sdk-android-autotracker/issues/84)) ([479d10c](https://github.com/growingio/growingio-sdk-android-autotracker/commit/479d10cba39ed4026765b5a0b4b0854ab3ae6e94))
* workspace为url, 特殊字符导致异常 ([a045710](https://github.com/growingio/growingio-sdk-android-autotracker/commit/a04571068f35824e20c246f26354311b70cfd731))
* 修复未初始化时，webview注入崩溃的问题 ([#74](https://github.com/growingio/growingio-sdk-android-autotracker/issues/74)) ([0e04e71](https://github.com/growingio/growingio-sdk-android-autotracker/commit/0e04e717473921dc8ecadd4f5683da4be42d76cc))
* 修改事件过滤日志打印 ([258fd82](https://github.com/growingio/growingio-sdk-android-autotracker/commit/258fd82b59759909315a985f2da96c2a3b032103))
* 修正测试用例 ([3d51b78](https://github.com/growingio/growingio-sdk-android-autotracker/commit/3d51b786f3e8a669f5005b9ef8e8cadaf7e8d664))
* 区分support包 ([#89](https://github.com/growingio/growingio-sdk-android-autotracker/issues/89)) ([bf4bfd1](https://github.com/growingio/growingio-sdk-android-autotracker/commit/bf4bfd1eeea6a173d2a3f9d347e0a1c2e2aa6aac))
* 增加字段过滤 ([937caee](https://github.com/growingio/growingio-sdk-android-autotracker/commit/937caee1acf6ed0db603704a07fc92d76bd3b4a0))
* 增加字段过滤日志 ([fc618fc](https://github.com/growingio/growingio-sdk-android-autotracker/commit/fc618fc174c9adfc781c1dd9da55a76ebf4495c1))
* 增加过滤事件日志 ([dbba44b](https://github.com/growingio/growingio-sdk-android-autotracker/commit/dbba44bbde6cd5206bacfeab98d1ff7216be6549))
* 多进程session保持同步 ([a76182f](https://github.com/growingio/growingio-sdk-android-autotracker/commit/a76182fc81c3e507097ff46fb30b7df33d1f7132))
* 数据采集接口设置无效 ([a1c92b0](https://github.com/growingio/growingio-sdk-android-autotracker/commit/a1c92b043858cedc8674e9bca809378a74463f19))
* 注入时invoke-static调用virtual方法 ([3306916](https://github.com/growingio/growingio-sdk-android-autotracker/commit/3306916cf3684d8cac279da178e0a57d6b750543))
* 适配更多场景 ([579d12f](https://github.com/growingio/growingio-sdk-android-autotracker/commit/579d12f0f429f1405687c4288d8759ef428e8f7f))



# [3.2.0](https://github.com/growingio/growingio-sdk-android-autotracker/compare/v3.1.0...v3.2.0) (2021-06-03)


### Bug Fixes

* PI-33483 修复多进程访问db导致的崩溃问题 ([#66](https://github.com/growingio/growingio-sdk-android-autotracker/issues/66)) ([d9c0fdb](https://github.com/growingio/growingio-sdk-android-autotracker/commit/d9c0fdb3b33959b6398396a6e2ddfedc27e4178a))
* PI-33712 代码触发click在page之前 ([#68](https://github.com/growingio/growingio-sdk-android-autotracker/issues/68)) ([3095b3e](https://github.com/growingio/growingio-sdk-android-autotracker/commit/3095b3e78367a7c1086483515d2eb07ecab0e0b6))



# [3.1.0](https://github.com/growingio/growingio-sdk-android-autotracker/compare/v3.0.0...v3.1.0) (2021-04-26)


### Bug Fixes

* page判断异常 ([a9aeebe](https://github.com/growingio/growingio-sdk-android-autotracker/commit/a9aeebe5884405fe1ab0d963efa601292804f13b))
* SimException: default or static interface method ([42a62e8](https://github.com/growingio/growingio-sdk-android-autotracker/commit/42a62e895b8f1ba3b4b78cd2811af00f010ddf28))


### Features

* 添加sonarcloud ([72da437](https://github.com/growingio/growingio-sdk-android-autotracker/commit/72da437fdb9dea5a3583d077029f517d91cbd473))



# 3.1.0-SNAPSHOT (2021-04-13)


### Bug Fixes

* 解决位置信息没有正确写入Visit事件 ([aca40ba](https://github.com/growingio/growingio-sdk-android-autotracker/commit/aca40ba22a4d2ac09f5a16144d874fa60a281008))



# [3.0.0](https://github.com/growingio/growingio-sdk-android-autotracker/compare/v0.0.1...v3.0.0) (2020-12-16)


### Bug Fixes

* change事件无法圈选 ([87ca230](https://github.com/growingio/growingio-sdk-android-autotracker/commit/87ca2306bb8b41464afb9e778fa7881e539c7e9a))
* fix WebView导错包名 ([0d0ef96](https://github.com/growingio/growingio-sdk-android-autotracker/commit/0d0ef967fba5f57dd2b47d056437f92698ec1ce9))
* transform 无法处理module-info文件 ([243f978](https://github.com/growingio/growingio-sdk-android-autotracker/commit/243f978e2f6c70da851bebad6bec27a75d44e85b))
* 优化圈选退出的交互 ([5ebec9a](https://github.com/growingio/growingio-sdk-android-autotracker/commit/5ebec9a27a850ebd9b6800da2fa5b2cfcff4868f))
* 层级通过dfs设置 ([a591e22](https://github.com/growingio/growingio-sdk-android-autotracker/commit/a591e22e894e9985ebbaafb45701a424de056b6e))
* 解决圈选服务在websocket线程中更新UI ([60ececf](https://github.com/growingio/growingio-sdk-android-autotracker/commit/60ececf9bdaf2e5e4d96e14d3c74d14d8faf682d))
* 解决在Visit事件前发送事件导致sessionId不正常 ([f5393f1](https://github.com/growingio/growingio-sdk-android-autotracker/commit/f5393f1c265d3a1a4e969cc0daa317e3af4b66de))
* 解决多线程情况下sessionId不安全的问题 ([437ea61](https://github.com/growingio/growingio-sdk-android-autotracker/commit/437ea61fe310a9150f5f6d901eab4404fafc8484))


### Features

* getDeviceId API由异步改为同步 ([6fa83e0](https://github.com/growingio/growingio-sdk-android-autotracker/commit/6fa83e0c6a9a291879b9c207eca7f6a47f4c8ae6))
* 修改圈选提示框文案和样式 ([9015c2a](https://github.com/growingio/growingio-sdk-android-autotracker/commit/9015c2a31a0edaccaa87bdc3c9fac0fbd91ce151))
* 埋点SDK和无埋点SDK的API ([04522ee](https://github.com/growingio/growingio-sdk-android-autotracker/commit/04522ee184422affb7685bc5cadd5b64418969e6))
* 增加urlscheme打开debug ([c198848](https://github.com/growingio/growingio-sdk-android-autotracker/commit/c19884822438e7948a26f5eb7da5823eea2725b2))
* 更改ConversionVariablesEvent的测量协议 ([946678e](https://github.com/growingio/growingio-sdk-android-autotracker/commit/946678e1d7f6c0b3addd6fd77a77e28055a92ec5))
* 更改Page事件的测量协议 ([1d2a0e3](https://github.com/growingio/growingio-sdk-android-autotracker/commit/1d2a0e3527740bac2e98e6fbaa8eda0449b82c38))
* 添加onActivityNewIntent API ([dedff75](https://github.com/growingio/growingio-sdk-android-autotracker/commit/dedff75750f56959f2b4e98613f8e4e848cdba88))
* 添加发布SNAPSHOT版本Task ([3409d48](https://github.com/growingio/growingio-sdk-android-autotracker/commit/3409d4818d7e72808f5fbc8baecd960be224a2a6))
* 适配oaid1.0.22 ([fd249f1](https://github.com/growingio/growingio-sdk-android-autotracker/commit/fd249f159311867e84a1b2aab1c13def92764a37))


### Performance Improvements

* 优化TrackConfiguration的实例化 ([76fb955](https://github.com/growingio/growingio-sdk-android-autotracker/commit/76fb955a176babfb8b5bf90cd902ce4fe293b152))



## [0.0.1](https://github.com/growingio/growingio-sdk-android-autotracker/compare/c4ef809b7c324a32970eb2acedd8c50eaada30eb...v0.0.1) (2020-11-04)


### Bug Fixes

* fix build failed ([ee74f56](https://github.com/growingio/growingio-sdk-android-autotracker/commit/ee74f56a9ddbbd3529a4a1a64c0384709787b212))
* webview的nodetype类型修正 ([ba921bf](https://github.com/growingio/growingio-sdk-android-autotracker/commit/ba921bf9fd2688631c158fc66a4281037105081e))
* 修复在Android 11 bate1版本无法获取WindowManagerGlobal实例 ([ae8a5a2](https://github.com/growingio/growingio-sdk-android-autotracker/commit/ae8a5a245d7940c2832c027fda707556edf7fca0))
* 圈选部分问题修复 ([3406485](https://github.com/growingio/growingio-sdk-android-autotracker/commit/340648534aef5f583be6bd85e6482848a08138f5))
* 大写y仅在API24之后支持 ([91ef783](https://github.com/growingio/growingio-sdk-android-autotracker/commit/91ef7836bf27af357109ea294b90193b2dd1bae9))
* 改变demo arrays资源文件所属目录 ([9934e4a](https://github.com/growingio/growingio-sdk-android-autotracker/commit/9934e4a137ccb9f99b1f222bfc4aa89723de86f0))
* 解决部分字段json序列化异常 ([6f289f5](https://github.com/growingio/growingio-sdk-android-autotracker/commit/6f289f5c60f0dee9db4b41ebf09a642cabee6ec6))


### Features

* click事件中使用cid计算xpath ([c4ef809](https://github.com/growingio/growingio-sdk-android-autotracker/commit/c4ef809b7c324a32970eb2acedd8c50eaada30eb))
* conflict reslove ([7790e5b](https://github.com/growingio/growingio-sdk-android-autotracker/commit/7790e5b72627b61beb05a424d2e7073cd49e4acd))
* inject模块支持重复注解，减少注解代码复杂度 ([3d839aa](https://github.com/growingio/growingio-sdk-android-autotracker/commit/3d839aab442be3bce27637fc245d05bc93831b6d))
* 圈选截图不包含悬浮提示View ([81be133](https://github.com/growingio/growingio-sdk-android-autotracker/commit/81be1338e1e772303a0d31cb60b5e61f8f2b55e5))
* 对接新的测量协议 ([4973579](https://github.com/growingio/growingio-sdk-android-autotracker/commit/4973579dca972a47d38aa9bfa2b6f9b002143ed3))
* 打包脚本增加javadoc及sources ([dcc7d23](https://github.com/growingio/growingio-sdk-android-autotracker/commit/dcc7d23554ce3695b918606b3e08706342674945))
* 拆分包 ([ec04e33](https://github.com/growingio/growingio-sdk-android-autotracker/commit/ec04e33ce0966ae6a5059bf19f06d0471841e7b0))
* 支持菜单栏MenuItem的圈选 ([736bd30](https://github.com/growingio/growingio-sdk-android-autotracker/commit/736bd30e84441b5cf1a97f38e5439b5f32fd207d))
* 无埋点 api 对齐，代码checkstyle ([a77404e](https://github.com/growingio/growingio-sdk-android-autotracker/commit/a77404e79edb82ede971f360ba78ed6ad674d8bb))
* 添加事件拦截器 ([6614558](https://github.com/growingio/growingio-sdk-android-autotracker/commit/6614558012db4c00e4ce556f73e888f3ce3053e7))
* 添加圈选时显示运行状态的悬浮Tips ([f395c6c](https://github.com/growingio/growingio-sdk-android-autotracker/commit/f395c6c3b97b427f7702863409fcdc57fbcfc700))
* 调整工程结构，拆分门面层和业务层 ([4f1b0a2](https://github.com/growingio/growingio-sdk-android-autotracker/commit/4f1b0a28628268ca093c59ba13bd76a99fa70acd))


### Performance Improvements

* module和artifact id保持同名 ([925bff4](https://github.com/growingio/growingio-sdk-android-autotracker/commit/925bff46a1b7e1766ebd7811816d42a8a13de292))
* page按照新的逻辑修改 ([ce50e77](https://github.com/growingio/growingio-sdk-android-autotracker/commit/ce50e77459f83cc594751a7f0cde43b3d04ff7ef))
* page遗漏 ([0abcb56](https://github.com/growingio/growingio-sdk-android-autotracker/commit/0abcb56488a801bddb092e4d76d6b70c7a875023))
* 优化click事件采集 ([74c3951](https://github.com/growingio/growingio-sdk-android-autotracker/commit/74c39513cfddfaaa8e55af0c6393583bc6f369e8))
* 优化click事件采集，并添加各种测试控件 ([bcd083d](https://github.com/growingio/growingio-sdk-android-autotracker/commit/bcd083d417b9a992e5c74d997e8f61c85cb391dc))
* 优化log模块 ([79c326b](https://github.com/growingio/growingio-sdk-android-autotracker/commit/79c326b28a922fda17d3f7549f4cc620cd811751))
* 优化page和impression事件采集 ([bcbe1b9](https://github.com/growingio/growingio-sdk-android-autotracker/commit/bcbe1b96deb6ce57844a34296bea751e55a82dbc))
* 优化事件模型继承链 ([0c34b53](https://github.com/growingio/growingio-sdk-android-autotracker/commit/0c34b53bcffa2850b3b8be15512aa24bd98b53ba))
* 优化多进程的数据交换、事件生成和发送 ([2c939c0](https://github.com/growingio/growingio-sdk-android-autotracker/commit/2c939c003cc398c6350ee5f85d70d876487df0fd))
* 优化对外API ([2aab56a](https://github.com/growingio/growingio-sdk-android-autotracker/commit/2aab56a0f13bf987a3a0b80ceb36ceb4ce9addb3))
* 保证初始prefix赋值 ([85dac52](https://github.com/growingio/growingio-sdk-android-autotracker/commit/85dac5251398a1372a9cf39e2deef3056f8d8c52))
* 修正格式 ([090a434](https://github.com/growingio/growingio-sdk-android-autotracker/commit/090a434437564d0da01e1241537fdf26e6c6d4ec))
* 兼容ASM 7.0 ([b6084a6](https://github.com/growingio/growingio-sdk-android-autotracker/commit/b6084a6e6b2f17154a3980ffd368d41ad92fc04b))



