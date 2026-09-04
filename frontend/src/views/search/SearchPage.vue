<template>
	<div class="search-page">
		<h2 class="text-h5 font-weight-bold mb-4">{{ t('search.title') }}</h2>
		<p class="text-body-1 text-grey-darken-1 mb-6">{{ t('search.description') }}</p>

		<SearchBar :is-searching="isSearching" @search="handleSearch" />

		<v-progress-linear v-if="isSearching" class="mb-4" color="primary" indeterminate />

		<!-- Mobile toggle -->
		<div v-if="hasSearched && searchResults.length > 0" class="d-flex d-md-none mb-3">
			<v-btn-toggle v-model="mobileView" color="primary" density="comfortable" mandatory>
				<v-btn size="small" value="list">
					<v-icon start>mdi-view-list</v-icon>
					{{ t('search.view-list') }}
				</v-btn>
				<v-btn size="small" value="map">
					<v-icon start>mdi-map-outline</v-icon>
					{{ t('search.view-map') }}
				</v-btn>
			</v-btn-toggle>
		</div>

		<!-- Desktop: side-by-side -->
		<v-row v-if="hasSearched" class="search-content" :class="{ 'is-stacked': smAndDown }" no-gutters>
			<v-col class="results-panel" :class="{ 'd-none d-md-block': mobileView === 'map' }" cols="12" md="4">
				<SearchResultsList
					:has-searched="hasSearched"
					:highlighted-id="hoveredPlaceId"
					:is-creating="isCreating"
					:results="searchResults"
					@add="handleAddProspect"
					@hover="onHover"
					@select="onSelectResult"
				/>
			</v-col>
			<v-col
				v-if="searchResults.length > 0"
				class="map-panel"
				:class="{ 'd-none d-md-block': mobileView === 'list' }"
				cols="12"
				md="7"
			>
				<SearchMap
					:center="searchCenter"
					:highlighted-id="hoveredPlaceId"
					:radius="searchRadius"
					:results="searchResults"
					@hover="onHover"
					@select="onMapSelect"
				/>
			</v-col>
		</v-row>

		<SearchResultDetailDrawer v-model="drawerOpen" :is-adding="isCreating" :result="selectedResult" @add="handleAddProspect" />
	</div>
</template>

<style scoped>
.search-content {
	gap: 16px;
}

.results-panel {
	max-height: calc(100vh - 280px);
	overflow-y: auto;
	padding-right: 8px;
}

.map-panel {
	position: sticky;
	top: 80px;
	height: calc(100vh - 280px);
	min-height: 400px;
	padding-left: 8px;
}

.search-content.is-stacked .results-panel {
	max-height: none;
	overflow-y: visible;
	padding-right: 0;
}

.search-content.is-stacked .map-panel {
	position: relative;
	top: auto;
	height: calc(100vh - 340px);
	min-height: 350px;
	padding-left: 0;
}
</style>

<script setup lang="ts">
import { computed, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { useDisplay } from 'vuetify';

import type { SearchResultDto } from '~/api/dtos/search.dto';
import SearchBar from '~/components/search/SearchBar.vue';
import SearchMap from '~/components/search/SearchMap.vue';
import SearchResultDetailDrawer from '~/components/search/SearchResultDetailDrawer.vue';
import SearchResultsList from '~/components/search/SearchResultsList.vue';
import { useProspects } from '~/composables/useProspects';
import { useSearch } from '~/composables/useSearch';

const { t } = useI18n();
const { smAndDown } = useDisplay();
const { searchResults, isSearching, hasSearched, searchPlaces } = useSearch();
const { addFromSearchResult, isCreating } = useProspects();

const hoveredPlaceId = ref<string | null>(null);
const searchCenter = ref<{ lat: number; lng: number } | null>(null);
const searchRadius = ref(20);
const currentKeywords = ref('');
const mobileView = ref<'list' | 'map'>('list');
const drawerOpen = ref(false);
const selectedPlaceId = ref<string | null>(null);

const selectedResult = computed<SearchResultDto | null>(() => {
	if (!selectedPlaceId.value) return null;
	return searchResults.value.find(r => r.placeId === selectedPlaceId.value) ?? null;
});

const handleSearch = (payload: { keywords: string; city: string; radius: number; lat: number; lng: number }) => {
	searchCenter.value = { lat: payload.lat, lng: payload.lng };
	searchRadius.value = payload.radius;
	currentKeywords.value = payload.keywords;
	searchPlaces({
		keywords: payload.keywords,
		city: payload.city,
		radius: payload.radius,
		lat: payload.lat,
		lng: payload.lng
	});
};

const handleAddProspect = (result: SearchResultDto) => {
	addFromSearchResult(result, currentKeywords.value);
};

const onHover = (placeId: string | null) => {
	hoveredPlaceId.value = placeId;
};

const onSelectResult = (placeId: string) => {
	selectedPlaceId.value = placeId;
	drawerOpen.value = true;
};

const onMapSelect = (placeId: string) => {
	onSelectResult(placeId);
};
</script>
