<template>
  <div class="index-page">
    <a-input-search
      v-model:value="searchParams.text"
      placeholder="请输入搜索关键词"
      enter-button="搜索"
      size="large"
      @search="onSearch"
    />
    {{ JSON.stringify(searchParams) }}
    <MyDivider />
    <a-tabs v-model:activeKey="activeKey" @change="onTabChange">
      <a-tab-pane key="post" tab="文章">
        <PostList :post-list="postList" />
      </a-tab-pane>
      <a-tab-pane key="picture" tab="图片">
        <PictureList :picture-list="pictureList" />
      </a-tab-pane>
      <a-tab-pane key="user" tab="用户">
        <UserList :user-list="userList" />
      </a-tab-pane>
    </a-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref, watchEffect } from "vue";
import PostList from "@/components/PostList.vue";
import PictureList from "@/components/PictureList.vue";
import UserList from "@/components/UserList.vue";
import MyDivider from "@/components/MyDivider.vue";
import { useRoute, useRouter } from "vue-router";
import myAxios from "@/plugins/myAxios";
import { message } from "ant-design-vue";

const postList = ref([]);
const userList = ref([]);
const pictureList = ref([]);

// /**
//  * 第一版：加载数据
//  * @param params
//  */
// const loadDataOld = (params: any) => {
//   const postQuery = {
//     ...params,
//     searchText: params.text,
//   };
//   myAxios.post("post/list/page/vo", postQuery).then((res: any) => {
//     postList.value = res.records;
//   });
//
//   const userQuery = {
//     ...params,
//     userName: params.text,
//   };
//   myAxios.post("user/list/page/vo", userQuery).then((res: any) => {
//     userList.value = res.records;
//   });
//
//   const pictureQuery = {
//     ...params,
//     searchText: params.text,
//   };
//   myAxios.post("picture/list/page/vo", pictureQuery).then((res: any) => {
//     pictureList.value = res.records;
//   });
// };

/**
 * 加载聚合数据
 * @param params
 */
const loadData = (params: any) => {
  const query = {
    ...params,
    searchText: params.text,
  };
  myAxios.post("search/all", query).then((res: any) => {
    postList.value = res.postList;
    userList.value = res.userList;
    pictureList.value = res.pictureList;
  });
};

const route = useRoute();
const router = useRouter();
const activeKey = route.params.category;

const initSearchParams = {
  type: activeKey,
  text: "",
  pageSize: 10,
  pageNum: 1,
};

/*记录搜索关键字*/
const searchParams = ref(initSearchParams);
// ⾸次请求
loadData(initSearchParams);

watchEffect(() => {
  searchParams.value = {
    // 我们把这个初始值作为⼀个兜底
    ...initSearchParams,
    //它改变的变量    text: route.query.text,
    type: route.params.category,
  } as any;
});

const onSearch = (value: string) => {
  console.log(value);
  router.push({
    query: searchParams.value,
  });
  // 根据条件查询
  loadData(searchParams.value);
};

// 绑定url
const onTabChange = (key: string) => {
  router.push({
    path: `/${key}`,
    query: searchParams.value,
  });
};
</script>
