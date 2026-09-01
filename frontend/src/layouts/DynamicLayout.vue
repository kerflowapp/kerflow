<template>
	<v-progress-linear v-if="isLoading" color="primary" indeterminate />
	<FullLayout v-else-if="isLoggedIn" />
	<router-view v-else />
</template>

<script setup lang="ts">
import { computed, defineAsyncComponent } from 'vue';

import { useAuth } from '~/composables';

const FullLayout = defineAsyncComponent(() => import('./FullLayout.vue'));

const { isLoggedIn, user } = useAuth();

const isLoading = computed(() => isLoggedIn.value && !user.value);
</script>
