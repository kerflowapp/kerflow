import { defineStore } from 'pinia';
import { ref } from 'vue';

import type { SearchResponseDto, SearchResultDto } from '~/api/dtos/search.dto';
import type { CitySuggestion } from '~/composables/useNominatim';

/** What the user typed for the last search, kept to refill the form and recenter the map. */
export interface LastSearchQuery {
	keywords: string;
	city: CitySuggestion;
	radius: number;
	lat: number;
	lng: number;
}

/** Last search and its results, kept in memory so leaving the search page does not lose them. */
export const useSearchStore = defineStore('search-store', () => {
	const lastQuery = ref<LastSearchQuery | null>(null);
	const results = ref<SearchResultDto[]>([]);
	const nextPageToken = ref<string | null>(null);
	const hasSearched = ref(false);

	function setResults(query: LastSearchQuery, response: SearchResponseDto) {
		lastQuery.value = query;
		results.value = response.results;
		nextPageToken.value = response.nextPageToken;
		hasSearched.value = true;
	}

	function clear() {
		lastQuery.value = null;
		results.value = [];
		nextPageToken.value = null;
		hasSearched.value = false;
	}

	return {
		lastQuery,
		results,
		nextPageToken,
		hasSearched,
		setResults,
		clear
	};
});
