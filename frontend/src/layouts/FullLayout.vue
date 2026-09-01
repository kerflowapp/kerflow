<template>
	<v-app>
		<v-layout>
			<v-navigation-drawer
				v-if="isLoggedIn"
				v-model="rail"
				color="background"
				:permanent="!mobile"
				style="border: none"
				:temporary="mobile"
			>
				<v-list-item class="py-4 pl-2" nav>
					<template #prepend>
						<v-avatar image="/logo.svg" />
					</template>
					<v-app-bar-title class="text-primary font-weight-bold">Kerflow</v-app-bar-title>
				</v-list-item>
				<v-divider />

				<v-list density="compact" nav>
					<v-list-item
						v-for="item in menuItems"
						:key="item.titleKey"
						active-class="bg-primary"
						:class="{ 'text-disabled': isLocked(item) }"
						color="white"
						:prepend-icon="item.icon"
						:title="$t(item.titleKey)"
						:to="isLocked(item) ? undefined : item.to"
						:value="item.titleKey"
						@click="isLocked(item) && (showTrialExpiredDialog = true)"
					/>
				</v-list>

				<template #append>
					<v-menu :close-on-content-click="false" location="top end" offset="8">
						<template #activator="{ props }">
							<v-list class="pa-2" density="compact" nav>
								<v-list-item
									v-bind="props"
									append-icon="mdi-dots-horizontal"
									prepend-icon="mdi-account-circle"
									rounded="lg"
									:subtitle="user?.email"
									:title="userDisplayName"
								/>
							</v-list>
						</template>

						<v-list density="compact" min-width="240" rounded="lg">
							<v-menu :close-on-content-click="true" location="end" transition="slide-x-transition">
								<template #activator="{ props: langProps }">
									<v-list-item
										v-bind="langProps"
										append-icon="mdi-chevron-right"
										prepend-icon="mdi-translate"
										:title="$t('common.language')"
									/>
								</template>

								<v-list density="compact" rounded="lg" width="160">
									<v-list-item
										v-for="(name, locale) in localeNames"
										:key="locale"
										:active="currentLocale === locale"
										@click="setLocale(locale)"
									>
										<v-list-item-title class="d-flex justify-space-between align-center">
											<span class="text-body-2">{{ name }}</span>
											<v-chip
												class="text-uppercase font-weight-bold"
												:color="currentLocale === locale ? 'primary' : 'default'"
												size="x-small"
												variant="tonal"
											>
												{{ locale }}
											</v-chip>
										</v-list-item-title>
									</v-list-item>
								</v-list>
							</v-menu>

							<v-list-item prepend-icon="mdi-cog" :title="$t('navigation.settings')" to="/settings" />

							<v-divider class="my-1" />

							<v-list-item base-color="error" prepend-icon="mdi-logout" :title="$t('common.logout')" @click="logout" />
						</v-list>
					</v-menu>
				</template>
			</v-navigation-drawer>

			<!-- Floating burger (mobile only, when drawer is closed) -->
			<v-btn
				v-if="isLoggedIn && mobile && !rail"
				class="floating-menu-btn"
				color="background"
				icon="mdi-menu"
				variant="flat"
				@click="rail = true"
			/>

			<v-main class="main bg-background">
				<v-container class="app-container">
					<trial-banner />
					<trial-expired-dialog v-model="showTrialExpiredDialog" />
					<router-view></router-view>
				</v-container>
			</v-main>
		</v-layout>
	</v-app>
</template>

<style scoped>
.main {
	position: relative;
	display: flex;
	flex-direction: column;
	min-height: 100vh;
	max-height: 100vh;
}
.app-container {
	height: 100vh;
	overflow-y: auto;
}

.floating-menu-btn {
	position: fixed !important;
	top: 12px;
	left: 12px;
	z-index: 1005;
}
</style>

<script setup lang="ts">
import TrialBanner from '@/components/TrialBanner.vue';
import TrialExpiredDialog from '@/components/TrialExpiredDialog.vue';
import { computed, ref } from 'vue';
import { useDisplay } from 'vuetify';

import { useAuth, useSubscription } from '~/composables';
import { useLanguage } from '~/composables/useLanguage';

const { user, isLoggedIn, logout } = useAuth();
const { isTrialExpired } = useSubscription();
const { mobile } = useDisplay();
const { currentLocale, localeNames, setLocale } = useLanguage();

// Workaround for Vetur false-positives (used in template)
void currentLocale;
void localeNames;
void setLocale;

const rail = ref(!mobile.value);

interface MenuItem {
	titleKey: string;
	icon: string;
	to: string;
	lockedWhenExpired?: boolean;
}

const menuItems: MenuItem[] = [
	{ titleKey: 'navigation.dashboard', icon: 'mdi-view-dashboard', to: '/dashboard', lockedWhenExpired: true },
	{ titleKey: 'navigation.search', icon: 'mdi-magnify', to: '/search', lockedWhenExpired: true },
	{ titleKey: 'navigation.prospects', icon: 'mdi-account-group', to: '/prospects' },
	{ titleKey: 'navigation.settings', icon: 'mdi-cog', to: '/settings' }
];

const showTrialExpiredDialog = ref(false);

const isLocked = (item: MenuItem): boolean => isTrialExpired.value && !!item.lockedWhenExpired;

const userDisplayName = computed(() => {
	if (!user.value) return 'Kerflow';
	if (user.value.firstName && user.value.lastName) {
		return `${user.value.firstName} ${user.value.lastName}`;
	}
	return user.value.login || user.value.email || 'Kerflow';
});
</script>
