<template>
    <div class="app-root">
        <router-view v-slot="{ Component }">
            <transition name="page-fade" mode="out-in">
                <component :is="Component" />
            </transition>
        </router-view>
    </div>
</template>
<script>
export default {
    name: 'App',
}
</script>
<style>
.app-root {
  height: 100%;
  overflow: hidden;
}
</style>