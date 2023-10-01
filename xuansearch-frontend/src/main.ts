import { createApp } from "vue";
import App from "./App.vue";
import router from "./router";
import Antd from "ant-design-vue";
// 注意这里的reset.css和官方文档给的文件名不同
import "ant-design-vue/dist/reset.css";

createApp(App).use(Antd).use(router).mount("#app");
