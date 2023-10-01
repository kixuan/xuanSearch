<template>
  <div class="index-page">
    <a-input-search
      v-model:value="searchParams.text"
      placeholder="请输入搜索关键词"
      enter-button="搜索"
      size="large"
      @search="onSearch"
    />
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

const postList = ref([]);
const userList = ref([]);
const pictureList = ref([]);

const route = useRoute();
const router = useRouter();
const activeKey = route.params.category;

const initSearchParams = {
  type: activeKey,
  text: "",
  pageSize: 10,
  pageNum: 1,
};

const searchParams = ref(initSearchParams);
const searchText = ref(route.query.text || "");

watchEffect(() => {
  searchParams.value = {
    ...initSearchParams,
    text: route.query.text,
    type: route.params.category,
  } as any;
  // loadData(searchParams.value);
});

const onSearch = (value: string) => {
  router.push({
    query: {
      ...searchParams.value,
      text: value,
    },
  });
};

// 绑定url
const onTabChange = (key: string) => {
  router.push({
    path: `/${key}`,
    query: searchParams.value,
  });
};
</script>
