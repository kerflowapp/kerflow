<template>
	<v-row>
		<v-col :cols="mobile ? 12 : 3">
			<v-card elevation="0">
				<v-list>
					<v-list-item v-for="item in settingsMenuItems" :key="item.titleKey" :to="item.to" :value="item.titleKey">
						<template #prepend>
							<v-icon :icon="item.icon"></v-icon>
						</template>
						<v-list-item-title>{{ $t(item.titleKey) }}</v-list-item-title>
					</v-list-item>
				</v-list>
			</v-card>
			<v-card class="mt-4" elevation="0">
				<v-list>
					<v-list-item @click="logout">
						<template #prepend>
							<v-icon color="error" icon="mdi-logout"></v-icon>
						</template>
						<v-list-item-title>{{ $t('common.logout') }}</v-list-item-title>
					</v-list-item>
				</v-list>
			</v-card>
		</v-col>
		<v-col :cols="mobile ? 12 : 9">
			<v-card elevation="0">
				<v-card-title>{{ $t('settings.title') }}</v-card-title>
				<v-card-text>
					<router-view></router-view>
				</v-card-text>
			</v-card>
		</v-col>
	</v-row>
</template>

<script setup lang="ts">
import { useDisplay } from 'vuetify';

import { useAuth } from '~/composables/useAuth';

const { mobile } = useDisplay();
const { logout } = useAuth();

const settingsMenuItems = [
	{
		titleKey: 'settings.profile.title',
		icon: 'mdi-account-outline',
		to: '/settings/profile'
	},
	{
		titleKey: 'settings.security.title',
		icon: 'mdi-shield-outline',
		to: '/settings/security'
	},
	{
		titleKey: 'settings.billing.title',
		icon: 'mdi-credit-card-outline',
		to: '/settings/billing'
	},
	{
		titleKey: 'settings.api-tokens.title',
		icon: 'mdi-robot-outline',
		to: '/settings/api-tokens'
	}
];
</script>
