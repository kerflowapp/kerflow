<template>
	<div>
		<div v-if="results.length > 0" class="d-flex justify-space-between align-center mb-4">
			<div class="text-body-1 text-grey-darken-1">
				{{ t('search.results-count', { count: results.length }) }}
			</div>
		</div>

		<div v-if="results.length > 0" class="results-scroll-list d-flex flex-column ga-3">
			<div v-for="(result, index) in results" :key="result.placeId" :ref="el => setCardRef(result.placeId, el)">
				<SearchResultCard
					:highlighted="highlightedId === result.placeId"
					:index="index"
					:is-adding="isCreating"
					:result="result"
					@add="onAdd"
					@hover="onHover"
					@select="onSelect"
				/>
			</div>
		</div>

		<v-card v-else-if="hasSearched" class="text-center pa-8" elevation="0">
			<v-icon class="mb-4" color="grey-lighten-1" size="64">mdi-map-search-outline</v-icon>
			<div class="text-h6 text-grey-darken-1 mb-2">{{ t('search.no-results-title') }}</div>
			<div class="text-body-2 text-grey">{{ t('search.no-results-description') }}</div>
		</v-card>
	</div>
</template>

<script setup lang="ts">
import type { ComponentPublicInstance } from 'vue';
import { watch } from 'vue';
import { useI18n } from 'vue-i18n';

import type { SearchResultDto } from '~/api/dtos/search.dto';

import SearchResultCard from './SearchResultCard.vue';

interface Props {
	results: SearchResultDto[];
	hasSearched: boolean;
	isCreating: boolean;
	highlightedId?: string | null;
}

interface Emits {
	(e: 'add', result: SearchResultDto): void;
	(e: 'hover', placeId: string | null): void;
	(e: 'select', placeId: string): void;
}

const props = withDefaults(defineProps<Props>(), { highlightedId: null });
const emit = defineEmits<Emits>();
const { t } = useI18n();

const cardRefs = new Map<string, HTMLElement>();

const setCardRef = (placeId: string, el: Element | ComponentPublicInstance | null) => {
	const htmlEl = el as HTMLElement | null;
	if (htmlEl) {
		cardRefs.set(placeId, htmlEl);
	} else {
		cardRefs.delete(placeId);
	}
};

const onAdd = (result: SearchResultDto) => {
	emit('add', result);
};

const onHover = (placeId: string | null) => {
	emit('hover', placeId);
};

const onSelect = (placeId: string) => {
	emit('select', placeId);
};

watch(
	() => props.highlightedId,
	id => {
		if (!id) return;
		const el = cardRefs.get(id);
		if (el) {
			el.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
		}
	}
);
</script>
