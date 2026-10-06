import { storeToRefs } from 'pinia';
import type { Ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { toast } from '~/composables/useToast';

import type { SearchResponseDto, SearchResultDto } from '~/api/dtos/search.dto';
import { searchPlaces$ } from '~/api/search.api';
import type { LastSearchQuery } from '~/stores/search.store';
import { useSearchStore } from '~/stores/search.store';

import { useTrigger } from './useTrigger';

export function useSearch(): {
	searchResults: Ref<SearchResultDto[]>;
	isSearching: Ref<boolean>;
	hasSearched: Ref<boolean>;
	nextPageToken: Ref<string | null>;
	lastQuery: Ref<LastSearchQuery | null>;
	searchPlaces: (query: LastSearchQuery) => void;
	clearResults: () => void;
} {
	const { t } = useI18n();
	const store = useSearchStore();
	const { results: searchResults, hasSearched, nextPageToken, lastQuery } = storeToRefs(store);
	const { trigger: triggerSearch, loading: isSearching } = useTrigger();

	const searchPlaces = (query: LastSearchQuery) => {
		const city =
			query.city.address.city ||
			query.city.address.town ||
			query.city.address.village ||
			query.city.address.municipality ||
			'';
		triggerSearch(
			searchPlaces$({ keywords: query.keywords, city, radius: query.radius, lat: query.lat, lng: query.lng }),
			{
				onSuccess: (response: { data: SearchResponseDto }) => {
					store.setResults(query, response.data);
				},
				onError: () => {
					toast.error(t('errors.search-failed'));
				}
			}
		);
	};

	const clearResults = () => {
		store.clear();
	};

	return {
		searchResults,
		isSearching,
		hasSearched,
		nextPageToken,
		lastQuery,
		searchPlaces,
		clearResults
	};
}
