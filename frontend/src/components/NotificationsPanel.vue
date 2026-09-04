<template>
	<v-navigation-drawer v-model="modelValue" location="right" temporary width="400">
		<v-card flat>
			<v-card-title class="d-flex justify-space-between align-center pa-4">
				{{ $t('notifications.title') }}
				<v-btn icon="mdi-close" variant="text" @click="modelValue = false"></v-btn>
			</v-card-title>

			<v-card-text>
				<!-- Tabs for filtering -->
				<v-tabs v-model="activeTab" class="mb-4" grow>
					<v-tab value="unread">{{ $t('notifications.unread') }}</v-tab>
					<v-tab value="read">{{ $t('notifications.read') }}</v-tab>
				</v-tabs>

				<v-btn
					v-if="activeTab === 'unread' && filteredNotifications?.length > 0"
					block
					class="mt-4"
					variant="text"
					@click="doMarkAllAsRead"
				>
					{{ $t('notifications.mark-all-as-read') }}
				</v-btn>

				<div v-if="filteredNotifications?.length === 0" class="text-center pa-4">
					{{ activeTab === 'unread' ? $t('notifications.no-unread-notifications') : $t('notifications.no-read-notifications') }}
				</div>

				<v-list v-else>
					<v-list-item
						v-for="notification in filteredNotifications"
						:key="notification.id"
						:class="{ unread: !notification.isRead }"
						style="cursor: pointer"
						:subtitle="formatDate(notification?.creationDate)"
						@click="showNotificationDetails(notification)"
					>
						<template #prepend>
							<v-avatar :color="getNotificationColor(notification.type)" size="36">
								<v-icon color="white" :icon="getNotificationIcon(notification.type)"></v-icon>
							</v-avatar>
						</template>

						<v-list-item-title>{{ notification.title }}</v-list-item-title>
						<v-list-item-subtitle class="text-truncate">{{ notification.content }}</v-list-item-subtitle>

						<template #append>
							<v-btn
								v-if="!notification.isRead"
								icon="mdi-check"
								size="small"
								:title="$t('notifications.mark-as-read')"
								variant="text"
								@click.stop="doMarkAsRead(notification.id)"
							></v-btn>
							<v-btn
								v-else
								icon="mdi-undo"
								size="small"
								:title="$t('notifications.mark-as-unread')"
								variant="text"
								@click.stop="doMarkAsUnread(notification.id)"
							></v-btn>
						</template>
					</v-list-item>
				</v-list>
			</v-card-text>
		</v-card>
	</v-navigation-drawer>

	<!-- Notification details dialog -->
	<v-dialog v-model="dialogOpen" max-width="500">
		<v-card v-if="selectedNotification">
			<v-card-title class="d-flex align-center">
				<v-avatar class="me-3" :color="getNotificationColor(selectedNotification.type)" size="36">
					<v-icon color="white" :icon="getNotificationIcon(selectedNotification.type)"></v-icon>
				</v-avatar>
				{{ selectedNotification.title }}
			</v-card-title>

			<v-card-subtitle>{{ formatDate(selectedNotification.creationDate) }}</v-card-subtitle>

			<v-card-text class="pt-4">
				<div style="white-space: pre-line">{{ selectedNotification.content }}</div>
			</v-card-text>

			<v-card-actions>
				<v-spacer></v-spacer>
				<v-btn variant="text" @click="dialogOpen = false">
					{{ $t('common.button.close') }}
				</v-btn>
				<v-btn
					v-if="!selectedNotification.isRead"
					color="secondary"
					variant="flat"
					@click="doMarkAsReadAndClose(selectedNotification.id)"
				>
					{{ $t('notifications.mark-as-read') }}
				</v-btn>
			</v-card-actions>
		</v-card>
	</v-dialog>
</template>

<style scoped>
.unread {
	background-color: rgba(var(--v-theme-primary), 0.05);
}
</style>

<script setup lang="ts">
import { computed, ref } from 'vue';

import { useAuth } from '~/composables/useAuth';
import { useNotifications } from '~/composables/useNotifications';
import { formatDate } from '~/utils/dateUtils';

/** Component setup *******************************************************************************/

const { user } = useAuth();
const { markAsRead, markAllAsRead, markAsUnread } = useNotifications();

const modelValue = defineModel<boolean>();
const activeTab = ref('unread'); // Default to unread tab
const dialogOpen = ref(false);
const selectedNotification = ref(null);

/** Computed  *************************************************************************************/

const notifications = computed(() => user.value?.notifications || []);
const filteredNotifications = computed(() => {
	if (activeTab.value === 'unread') return notifications.value.filter(n => !n.isRead);
	if (activeTab.value === 'read') return notifications.value.filter(n => n.isRead);
	return notifications.value;
});

/** Methods  **************************************************************************************/

const getNotificationColor = (type: string) => {
	const colors: { [key: string]: string } = {
		payment: 'success',
		maintenance: 'warning',
		lease: 'primary',
		document: 'info'
	};
	return colors[type] || 'grey';
};

const getNotificationIcon = (type: string) => {
	const icons: { [key: string]: string } = {
		payment: 'mdi-cash',
		maintenance: 'mdi-tools',
		lease: 'mdi-file-document',
		document: 'mdi-file'
	};
	return icons[type] || 'mdi-bell';
};

const showNotificationDetails = notification => {
	selectedNotification.value = notification;
	dialogOpen.value = true;
};

const doMarkAsRead = (id: string) => {
	if (user.value?.id) {
		markAsRead(user.value?.id, id);
	}
	const notification = notifications.value.find(n => n.id === id);
	if (notification) {
		notification.isRead = true;
	}
};

const doMarkAsReadAndClose = (id: string) => {
	doMarkAsRead(id);
	dialogOpen.value = false;
};

const doMarkAsUnread = (id: string) => {
	if (user.value?.id) {
		markAsUnread(user.value?.id, id);
	}
	const notification = notifications.value.find(n => n.id === id);
	if (notification) {
		notification.isRead = false;
	}
};

const doMarkAllAsRead = () => {
	if (user.value?.id) {
		markAllAsRead(user.value?.id);
	}
	notifications.value.forEach(notification => {
		notification.isRead = true;
	});
};
</script>
