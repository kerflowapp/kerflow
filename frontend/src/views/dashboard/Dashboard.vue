<template>
	<div>
		<h2 class="text-h5 font-weight-bold mb-2">{{ t('dashboard.title') }}</h2>
		<p class="text-body-1 text-grey-darken-1 mb-6">{{ t('dashboard.subtitle') }}</p>

		<v-progress-linear v-if="isFetching" class="mb-4" color="primary" indeterminate />

		<v-row class="mb-6">
			<v-col cols="12" md="3" sm="6">
				<DashboardMetricCard
					color="primary"
					icon="mdi-account-group"
					:label="t('dashboard.metrics.total-prospects')"
					:value="store.totalCount"
				/>
			</v-col>
			<v-col cols="12" md="3" sm="6">
				<DashboardMetricCard
					color="info"
					icon="mdi-star-outline"
					:label="t('dashboard.metrics.new-prospects')"
					:value="store.prospectsByStatus.NEW?.length || 0"
				/>
			</v-col>
			<v-col cols="12" md="3" sm="6">
				<DashboardMetricCard
					color="success"
					icon="mdi-check-circle-outline"
					:label="t('dashboard.metrics.won-prospects')"
					:value="store.wonCount"
				/>
			</v-col>
			<v-col cols="12" md="3" sm="6">
				<DashboardMetricCard
					color="secondary"
					icon="mdi-percent"
					:label="t('dashboard.metrics.conversion-rate')"
					:value="`${store.conversionRate}%`"
				/>
			</v-col>
		</v-row>

		<v-row>
			<v-col cols="12" md="6">
				<v-card elevation="0">
					<v-card-title class="text-subtitle-1 font-weight-bold">
						{{ t('dashboard.recent-prospects') }}
					</v-card-title>
					<v-card-text>
						<v-list v-if="recentProspects.length > 0">
							<v-list-item
								v-for="prospect in recentProspects"
								:key="prospect.id"
								:subtitle="prospect.address || prospect.email || prospect.phone"
								:title="prospect.name"
							>
								<template #prepend>
									<v-avatar :color="statusColor(prospect.statusKey)" size="36">
										<v-icon color="white" size="18">mdi-account</v-icon>
									</v-avatar>
								</template>
								<template #append>
									<v-chip :color="statusColor(prospect.statusKey)" size="x-small" variant="tonal">
										{{ t(`kanban.columns.${statusKey(prospect.statusKey)}`) }}
									</v-chip>
								</template>
							</v-list-item>
						</v-list>
						<div v-else class="text-center pa-4 text-grey">
							{{ t('dashboard.no-prospects') }}
						</div>
					</v-card-text>
				</v-card>
			</v-col>

			<v-col cols="12" md="6">
				<v-card elevation="0">
					<v-card-title class="text-subtitle-1 font-weight-bold">
						{{ t('dashboard.quick-actions') }}
					</v-card-title>
					<v-card-text>
						<v-list>
							<v-list-item
								prepend-icon="mdi-magnify"
								:subtitle="t('dashboard.actions.new-search-description')"
								:title="t('dashboard.actions.new-search')"
								to="/search"
							/>
							<v-list-item
								prepend-icon="mdi-account-group"
								:subtitle="t('dashboard.actions.view-prospects-description')"
								:title="t('dashboard.actions.view-prospects')"
								to="/prospects"
							/>
							<v-list-item
								prepend-icon="mdi-file-delimited"
								:subtitle="t('dashboard.actions.import-csv-description')"
								:title="t('dashboard.actions.import-csv')"
								@click="$router.push('/prospects')"
							/>
						</v-list>
					</v-card-text>
				</v-card>
			</v-col>
		</v-row>
	</div>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue';
import { useI18n } from 'vue-i18n';

import { KanbanStatus } from '~/api/dtos/prospect.dto';
import DashboardMetricCard from '~/components/dashboard/DashboardMetricCard.vue';
import { useProspects } from '~/composables';
import { useProspectsStore } from '~/stores';

const { t } = useI18n();
const store = useProspectsStore();
const { fetchProspects, isFetching } = useProspects();

const recentProspects = computed(() => {
	return [...store.prospects].sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()).slice(0, 5);
});

const statusColor = (status: string): string => {
	const colors: Record<string, string> = {
		[KanbanStatus.NEW]: 'info',
		[KanbanStatus.CONTACTED]: 'warning',
		[KanbanStatus.IN_DISCUSSION]: 'primary',
		[KanbanStatus.WON]: 'success',
		[KanbanStatus.LOST]: 'error'
	};
	return colors[status] || 'grey';
};

const statusKey = (status: string): string => {
	const keys: Record<string, string> = {
		[KanbanStatus.NEW]: 'new',
		[KanbanStatus.CONTACTED]: 'contacted',
		[KanbanStatus.IN_DISCUSSION]: 'in-discussion',
		[KanbanStatus.WON]: 'won',
		[KanbanStatus.LOST]: 'lost'
	};
	return keys[status] || 'new';
};

onMounted(() => {
	fetchProspects();
});
</script>
