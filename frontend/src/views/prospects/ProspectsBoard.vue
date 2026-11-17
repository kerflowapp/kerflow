<template>
	<div>
		<ProspectsBoardToolbar
			v-model:filter-text="filterText"
			v-model:follow-ups-only="followUpsOnly"
			v-model:show-filters="showFilters"
			:active-filter-count="activeCount"
			:due-count="dueCount"
			@add="openAddDialog()"
			@import-csv="openCsvImport"
			@manage-columns="openManageColumns"
		/>

		<ProspectsFilters
			v-if="showFilters"
			v-model:categories="categories"
			v-model:departments="departments"
			v-model:min-stars="minStars"
			v-model:sources="sources"
			v-model:tags="tags"
			:active-count="activeCount"
			:category-options="categoryOptions"
			:department-options="departmentOptions"
			:source-options="sourceOptions"
			:star-options="starOptions"
			:tag-options="tagOptions"
			@reset="resetFilters"
		/>

		<v-progress-linear v-if="isFetching" class="mb-4" color="primary" indeterminate />

		<div v-if="viewMode === 'map' && !isFetching" class="mb-4">
			<v-card elevation="0">
				<v-card-text>
					<ProspectsMap
						:highlighted-id="highlightedProspectId"
						:prospects="filteredProspects"
						@hover="highlightedProspectId = $event"
						@select="handleSelectById"
					/>
				</v-card-text>
			</v-card>
		</div>

		<div v-else-if="!isFetching" class="kanban-board">
			<div v-for="column in columns" :key="column.key" class="kanban-col">
				<KanbanColumn
					:color="column.color || 'primary'"
					:drag-disabled="isFiltering"
					:icon="column.icon || 'mdi-view-column'"
					:prospects="listForColumn(column.key)"
					:readonly="isTrialExpired"
					:status-key="column.key"
					:title="columnTitle(column)"
					@add="openAddDialog"
					@delete="handleDelete"
					@drag-end="isDragging = false"
					@drag-start="isDragging = true"
					@edit="handleEdit"
					@reorder="handleReorder"
					@select="
						showDetail = true;
						selectedProspect = $event;
					"
				/>
			</div>
		</div>

		<ProspectDetailsDrawer
			v-model="showDetail"
			:is-deleting="isDeleting"
			:is-enriching="isEnriching"
			:is-updating-follow-up="isUpdatingFollowUp"
			:prospect="selectedProspect"
			:readonly="isTrialExpired"
			@delete="handleDelete"
			@edit="
				showDetail = false;
				handleEdit(selectedProspect!);
			"
			@enrich="handleEnrich"
			@update-follow-up-date="handleFollowUpDate"
		/>

		<ProspectEditDrawer v-model="showEdit" :is-updating="isUpdating" :prospect="selectedProspect" @update="handleUpdate" />

		<CsvImportDialog v-model="showCsvImport" />

		<TrialExpiredDialog v-model="showTrialExpired" />

		<ProspectAddDialog
			v-model="showAddDialog"
			:columns="columns"
			:initial-status-key="addStatusKey"
			:is-creating="isCreating"
			@submit="handleManualAdd"
		/>

		<!-- Manage columns -->
		<v-dialog v-model="showManageColumns" max-width="720">
			<v-card elevation="0">
				<v-card-title>{{ t('kanban.manage-columns') }}</v-card-title>
				<v-card-text>
					<div class="d-flex ga-2 align-center mb-4">
						<v-text-field
							v-model="newColumnName"
							density="comfortable"
							hide-details
							:label="t('kanban.column-name')"
							variant="outlined"
						/>
						<v-btn color="primary" :disabled="!newColumnName" variant="flat" @click="handleAddColumn">
							{{ t('common.add') }}
						</v-btn>
					</div>

					<v-list class="bg-transparent" density="compact">
						<v-list-item v-for="col in columns" :key="col.id">
							<template #prepend>
								<v-icon :icon="col.icon || 'mdi-view-column'" />
							</template>
							<v-text-field
								v-model="editedNames[col.id]"
								density="compact"
								hide-details
								:placeholder="columnTitle(col)"
								variant="outlined"
								@blur="handleRenameColumn(col.id)"
							/>
							<template #append>
								<v-btn icon="mdi-arrow-up" variant="text" @click="moveColumn(col.id, -1)" />
								<v-btn icon="mdi-arrow-down" variant="text" @click="moveColumn(col.id, 1)" />
								<v-btn color="error" icon="mdi-delete" variant="text" @click="handleDeleteColumn(col.id)" />
							</template>
						</v-list-item>
					</v-list>
					<div class="text-caption text-grey mt-2">{{ t('kanban.delete-rule') }}</div>
				</v-card-text>
				<v-card-actions>
					<v-spacer />
					<v-btn variant="text" @click="showManageColumns = false">{{ t('common.button.close') }}</v-btn>
				</v-card-actions>
			</v-card>
		</v-dialog>
	</div>
</template>

<style scoped>
.kanban-board {
	display: flex;
	gap: 12px;
	overflow-x: auto;
	padding-bottom: 16px;
}
.kanban-col {
	min-width: 260px;
	flex: 1;
}
</style>

<script setup lang="ts">
import { computed, defineAsyncComponent, onMounted, reactive, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';

import type { CreateProspectDto, ProspectDto, ProspectPipelineColumnDto, UpdateProspectDto } from '~/api/dtos/prospect.dto';
import ProspectsBoardToolbar from '~/components/prospects/ProspectsBoardToolbar.vue';
import ProspectsFilters from '~/components/prospects/ProspectsFilters.vue';
import { useFollowUps } from '~/composables/useFollowUps';
import { useProspectFilters } from '~/composables/useProspectFilters';
import { useProspectPipeline } from '~/composables/useProspectPipeline';
import { useProspects } from '~/composables/useProspects';
import { useSubscription } from '~/composables/useSubscription';
import { useProspectsStore } from '~/stores/prospects.store';

const CsvImportDialog = defineAsyncComponent(() => import('~/components/prospects/CsvImportDialog.vue'));
const TrialExpiredDialog = defineAsyncComponent(() => import('~/components/TrialExpiredDialog.vue'));
const KanbanColumn = defineAsyncComponent(() => import('~/components/prospects/KanbanColumn.vue'));
const ProspectDetailsDrawer = defineAsyncComponent(() => import('~/components/prospects/ProspectDetailsDrawer.vue'));
const ProspectAddDialog = defineAsyncComponent(() => import('~/components/prospects/ProspectAddDialog.vue'));
const ProspectEditDrawer = defineAsyncComponent(() => import('~/components/prospects/ProspectEditDrawer.vue'));
const ProspectsMap = defineAsyncComponent(() => import('~/components/prospects/ProspectsMap.vue'));

const { t } = useI18n();
const store = useProspectsStore();
const {
	fetchProspects,
	isFetching,
	addProspect,
	isCreating,
	editProspect,
	isUpdating,
	isDeleting,
	enrichProspect,
	isEnriching,
	updateFollowUpDate,
	isUpdatingFollowUp,
	reorderProspects,
	removeProspect
} = useProspects();
const { fetchColumns, createColumn, updateColumn, reorderColumns, deleteColumn, columnTitle } = useProspectPipeline();
const { isTrialExpired } = useSubscription();
const { dueCount } = useFollowUps();
const {
	text: filterText,
	followUpsOnly,
	departments,
	minStars,
	categories,
	tags,
	sources,
	departmentOptions,
	categoryOptions,
	tagOptions,
	sourceOptions,
	starOptions,
	activeCount,
	isFiltering,
	matches: matchesFilters,
	reset: resetFilters
} = useProspectFilters();
// Open on load when the URL already carries filters, so they are never active out of sight
const showFilters = ref(activeCount.value > 0);
const viewMode = ref<'board' | 'map'>('board');
const showDetail = ref(false);
const showEdit = ref(false);
const showCsvImport = ref(false);
const showAddDialog = ref(false);
const showManageColumns = ref(false);
const showTrialExpired = ref(false);
const selectedProspect = ref<ProspectDto | null>(null);
const highlightedProspectId = ref<string | null>(null);
const newColumnName = ref('');
const editedNames = reactive<Record<string, string>>({});

const addStatusKey = ref('NEW');

const openCsvImport = () => {
	if (isTrialExpired.value) {
		showTrialExpired.value = true;
		return;
	}
	showCsvImport.value = true;
};

const openAddDialog = (statusKey = 'NEW') => {
	if (isTrialExpired.value) {
		showTrialExpired.value = true;
		return;
	}
	addStatusKey.value = statusKey;
	showAddDialog.value = true;
};

const openManageColumns = () => {
	if (isTrialExpired.value) {
		showTrialExpired.value = true;
		return;
	}
	showManageColumns.value = true;
};

const handleEdit = (prospect: ProspectDto) => {
	if (isTrialExpired.value) {
		showTrialExpired.value = true;
		return;
	}
	selectedProspect.value = prospect;
	showEdit.value = true;
};

const columns = computed<ProspectPipelineColumnDto[]>(() => store.pipelineColumns);

// Stable per-column arrays owned by vuedraggable (mutated in place during drag). Rebuilt from
// the store only when NOT dragging, so an optimistic reorder mid-drag never clobbers a
// cross-column move in flight.
const columnLists = reactive<Record<string, ProspectDto[]>>({});
const isDragging = ref(false);

const syncColumnLists = () => {
	const grouped = store.prospectsByStatus;
	const keys = new Set([...columns.value.map(c => c.key), ...Object.keys(grouped)]);
	for (const key of keys) {
		columnLists[key] = grouped[key] ? [...grouped[key]] : [];
	}
	for (const key of Object.keys(columnLists)) {
		if (!keys.has(key)) delete columnLists[key];
	}
};

watch(
	[() => store.prospects, columns],
	() => {
		if (!isDragging.value) syncColumnLists();
	},
	{ deep: true, immediate: true }
);

// While filtering, drag is disabled → a fresh filtered copy is safe. Otherwise hand vuedraggable
// the stable column array it owns.
const listForColumn = (statusKey: string): ProspectDto[] => {
	if (!isFiltering.value) return columnLists[statusKey] || [];
	return (store.prospectsByStatus[statusKey] || []).filter(matchesFilters);
};

const filteredProspects = computed(() => (isFiltering.value ? store.prospects.filter(matchesFilters) : store.prospects));

const handleReorder = (payload: { statusKey: string; orderedIds: string[] }) => {
	if (isTrialExpired.value) {
		showTrialExpired.value = true;
		return;
	}
	reorderProspects(payload.statusKey, payload.orderedIds);
};

const handleSelect = (prospect: ProspectDto) => {
	selectedProspect.value = prospect;
	showDetail.value = true;
	showEdit.value = false;
};

const handleSelectById = (id: string) => {
	const prospect = store.prospects.find(p => p.id === id);
	if (!prospect) return;
	handleSelect(prospect);
};

const handleEnrich = (id: string) => {
	if (isTrialExpired.value) {
		showTrialExpired.value = true;
		return;
	}
	enrichProspect(id, prospect => {
		selectedProspect.value = prospect;
	});
};

const handleUpdate = (payload: { id: string; data: UpdateProspectDto }) => {
	if (isTrialExpired.value) {
		showTrialExpired.value = true;
		return;
	}
	editProspect(payload.id, payload.data);
};

const handleFollowUpDate = (id: string, followUpDate: string | null) => {
	if (isTrialExpired.value) {
		showTrialExpired.value = true;
		return;
	}
	updateFollowUpDate(id, followUpDate, prospect => {
		selectedProspect.value = prospect;
	});
};

const handleDelete = (id: string) => {
	if (isTrialExpired.value) {
		showTrialExpired.value = true;
		return;
	}
	removeProspect(id);
	showEdit.value = false;
	showDetail.value = false;
};

const handleManualAdd = (data: CreateProspectDto) => {
	addProspect(data);
	showAddDialog.value = false;
};

onMounted(() => {
	fetchColumns();
	fetchProspects();
});

const handleAddColumn = () => {
	if (!newColumnName.value) return;
	createColumn({ name: newColumnName.value });
	newColumnName.value = '';
};

const handleRenameColumn = (id: string) => {
	const value = editedNames[id];
	if (!value) return;
	const col = store.pipelineColumns.find(c => c.id === id);
	if (!col) return;
	updateColumn(id, { name: value, color: col.color, icon: col.icon });
};

const moveColumn = (id: string, delta: number) => {
	const current = [...store.pipelineColumns].sort((a, b) => a.sortOrder - b.sortOrder);
	const idx = current.findIndex(c => c.id === id);
	if (idx === -1) return;
	const nextIdx = idx + delta;
	if (nextIdx < 0 || nextIdx >= current.length) return;
	const swapped = [...current];
	[swapped[idx], swapped[nextIdx]] = [swapped[nextIdx], swapped[idx]];
	reorderColumns(swapped.map(c => c.id));
};

const handleDeleteColumn = (id: string) => {
	deleteColumn(id);
};
</script>
