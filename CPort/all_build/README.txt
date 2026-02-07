# 本来想默认编译angle的，后来发现太麻烦，反正也不是必须，运行时边上有库文件就行，去chrome里拿过来就能用……默认不编译angle了，除非批处理加上-angle标志
# cmake的库下载，本质是调用git，所以身处神奇网路环境的，别忘了科学设置，某些神奇环境不设置不开全局是下不了东西的，例如（具体端口设成自己的）
# 此为控制台指令，控制台执行即可
# git config --global http.proxy http://127.0.0.1:7890
# git config --global https.proxy http://127.0.0.1:7890
