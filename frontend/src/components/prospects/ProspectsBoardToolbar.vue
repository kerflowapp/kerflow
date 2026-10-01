<template>
	<div>
		<div class="d-flex align-center justify-space-between mb-4 flex-wrap ga-2">
			<h2 class="text-h5 font-weight-bold">{{ t('prospects.title') }}</h2>
			<div class="d-flex ga-2 flex-wrap">
				<v-btn prepend-icon="mdi-tune" variant="outlined" @click="emit('manage-columns')">
					{{ t('kanban.manage-columns') }}
				</v-btn>
				<v-btn color="primary" prepend-icon="mdi-file-delimited" variant="outlined" @click="emit('import-csv')">
					{{ t('prospects.import-csv') }}
				</v-btn>
				<v-btn color="primary" prepend-icon="mdi-plus" variant="flat" @click="emit('add')">
					{{ t('prospects.add-manually') }}
				</v-btn>
			</div>
		</div>

		<div class="d-flex align-center flex-wrap ga-2 mb-4">
			<v-text-field
				v-model="filterText"
				clearable
				density="compact"
				hide-details
				:placeholder="t('prospects.filter-placeholder')"
				prepend-inner-icon="mdi-magnify"
				style="max-width: 400px"
				variant="outlined"
			/>
			<v-chip
				:append-icon="followUpsOnly ? 'mdi-close' : undefined"
				:color="followUpsOnly ? 'error' : undefined"
				:disabled="!dueCount && !followUpsOnly"
				prepend-icon="mdi-bell-ring-outline"
				:variant="followUpsOnly ? 'flat' : 'outlined'"
				@click="followUpsOnly = !followUpsOnly"
			>
				{{ t('follow-ups.filter', { count: dueCount }) }}
			</v-chip>
		</div>
	</div>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n';

interface Props {
	dueCount: number;
}

interface Emits {
	(e: 'manage-columns'): void;
	(e: 'import-csv'): void;
	(e: 'add'): void;
}

defineProps<Props>();
const emit = defineEmits<Emits>();
const filterText = defineModel<string>('filterText', { default: '' });
const followUpsOnly = defineModel<boolean>('followUpsOnly', { default: false });
const { t } = useI18n();
</script>
