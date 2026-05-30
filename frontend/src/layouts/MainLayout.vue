<template>
  <a-layout style="min-height: 100vh">
    <!-- 侧边栏 -->
    <a-layout-sider
      v-model:collapsed="collapsed"
      :trigger="null"
      collapsible
      theme="dark"
      :width="220"
    >
      <div class="logo">
        <span v-if="!collapsed">EcoSaaS 采购</span>
        <span v-else>E</span>
      </div>

      <a-menu
        v-model:selectedKeys="selectedKeys"
        theme="dark"
        mode="inline"
        @click="handleMenuClick"
      >
        <template v-for="item in menuItems" :key="item.key">
          <!-- 有子菜单 -->
          <a-sub-menu v-if="item.children" :key="item.key">
            <template #icon><component :is="item.icon" /></template>
            <template #title>{{ item.label }}</template>
            <a-menu-item v-for="child in item.children" :key="child.key">
              {{ child.label }}
            </a-menu-item>
          </a-sub-menu>
          <!-- 无子菜单 -->
          <a-menu-item v-else :key="item.key">
            <template #icon><component :is="item.icon" /></template>
            <span>{{ item.label }}</span>
          </a-menu-item>
        </template>
      </a-menu>
    </a-layout-sider>

    <!-- 主内容区 -->
    <a-layout>
      <!-- 顶部栏 -->
      <a-layout-header class="header">
        <div class="header-left">
          <menu-unfold-outlined
            v-if="collapsed"
            class="trigger"
            @click="collapsed = false"
          />
          <menu-fold-outlined
            v-else
            class="trigger"
            @click="collapsed = true"
          />
        </div>
        <div class="header-right">
          <span class="user-name">{{ authStore.user?.name }}</span>
          <a-tag :color="roleColor">{{ roleLabel }}</a-tag>
          <a-dropdown>
            <a-button type="text" size="small">
              <template #icon><setting-outlined /></template>
            </a-button>
            <template #overlay>
              <a-menu>
                <a-menu-item @click="showChangePassword = true">修改密码</a-menu-item>
                <a-menu-divider />
                <a-menu-item @click="handleLogout">退出登录</a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
        </div>
      </a-layout-header>

      <!-- 内容区 -->
      <a-layout-content class="content">
        <router-view />
      </a-layout-content>
    </a-layout>

    <ChangePasswordDialog v-model:open="showChangePassword" />
  </a-layout>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import {
  MenuUnfoldOutlined,
  MenuFoldOutlined,
  SettingOutlined,
} from '@ant-design/icons-vue';
import { useAuthStore } from '@/modules/auth/presentation/stores/auth.store';
import { getMenuItems } from '@/config/menu';
import ChangePasswordDialog from '@/modules/auth/presentation/components/ChangePasswordDialog.vue';

const router = useRouter();
const route = useRoute();
const authStore = useAuthStore();

const collapsed = ref(false);
const showChangePassword = ref(false);
const selectedKeys = ref<string[]>([]);

const menuItems = computed(() => {
  const role = authStore.user?.role;
  if (!role) return [];
  return getMenuItems(role);
});

const roleLabel = computed(() => {
  const map: Record<string, string> = {
    ADMIN: '采购经理',
    BUYER: '采购员',
    BUSINESS_USER: '业务人员',
    SUPPLIER: '供应商',
  };
  return map[authStore.user?.role || ''] || '';
});

const roleColor = computed(() => {
  const map: Record<string, string> = {
    ADMIN: 'red',
    BUYER: 'blue',
    BUSINESS_USER: 'green',
    SUPPLIER: 'orange',
  };
  return map[authStore.user?.role || ''] || 'default';
});

// 同步路由到菜单选中状态
watch(() => route.path, (path) => {
  selectedKeys.value = [path];
}, { immediate: true });

function handleMenuClick({ key }: { key: string }) {
  router.push(key);
}

async function handleLogout() {
  await authStore.logout();
  router.push('/internal/login');
}
</script>

<style scoped>
.logo {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 18px;
  font-weight: 600;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.header {
  background: #fff;
  padding: 0 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  height: 64px;
  line-height: 64px;
}

.header-left {
  display: flex;
  align-items: center;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.trigger {
  font-size: 18px;
  cursor: pointer;
  transition: color 0.3s;
}

.trigger:hover {
  color: #1890ff;
}

.user-name {
  font-weight: 500;
}

.content {
  margin: 16px;
  padding: 24px;
  background: #fff;
  border-radius: 8px;
  min-height: 280px;
}
</style>
