<template>
	<div class="d-flex align-center flex-wrap ga-2 mb-4">
		<v-autocomplete
			v-model="departments"
			chips
			class="filter-field"
			closable-chips
			density="compact"
			hide-details
			:items="departmentOptions"
			:label="t('prospects.filters.department')"
			multiple
			prepend-inner-icon="mdi-map-marker-outline"
			variant="outlined"
		/>
		<v-select
			v-model="minStars"
			class="filter-field"
			clearable
			density="compact"
			hide-details
			:items="starOptions"
			:label="t('prospects.filters.min-stars')"
			prepend-inner-icon="mdi-star-outline"
			variant="outlined"
		/>
		<v-autocomplete
			v-model="categories"
			chips
			class="filter-field"
			closable-chips
			density="compact"
			hide-details
			:items="categoryOptions"
			:label="t('prospects.filters.category')"
			multiple
			prepend-inner-icon="mdi-briefcase-outline"
			variant="outlined"
		/>
		<v-autocomplete
			v-model="tags"
			chips
			class="filter-field"
			closable-chips
			density="compact"
			hide-details
			:items="tagOptions"
			:label="t('prospects.filters.tags')"
			multiple
			prepend-inner-icon="mdi-tag-outline"
			variant="outlined"
		/>
		<v-select
			v-model="sources"
			chips
			class="filter-field"
			closable-chips
			density="compact"
			hide-details
			:items="sourceOptions"
			:label="t('prospects.filters.source')"
			multiple
			prepend-inner-icon="mdi-import"
			variant="outlined"
		/>
		<v-btn v-if="activeCount > 0" prepend-icon="mdi-filter-remove-outline" variant="text" @click="emit('reset')">
			{{ t('prospects.filters.reset') }}
		</v-btn>
	</div>
</template>

<style scoped>
.filter-field {
	min-width: 200px;
	max-width: 320px;
}
</style>

<script setup lang="ts">
import { useI18n } from 'vue-i18n';

import type { BusinessCategory, ProspectSource } from '~/api/dtos/prospect.dto';
import type { FilterOption } from '~/composables/useProspectFilters';

interface Props {
	departmentOptions: FilterOption[];
	starOptions: FilterOption<number>[];
	categoryOptions: FilterOption<BusinessCategory>[];
	tagOptions: FilterOption[];
	sourceOptions: FilterOption<ProspectSource>[];
	activeCount: number;
}

interface Emits {
	(e: 'reset'): void;
}

defineProps<Props>();
const emit = defineEmits<Emits>();
const departments = defineModel<string[]>('departments', { default: () => [] });
const minStars = defineModel<number | null>('minStars', { default: null });
const categories = defineModel<BusinessCategory[]>('categories', { default: () => [] });
const tags = defineModel<string[]>('tags', { default: () => [] });
const sources = defineModel<ProspectSource[]>('sources', { default: () => [] });
const { t } = useI18n();
</script>
