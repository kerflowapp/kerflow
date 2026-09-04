import type { Ref } from 'vue';
import { ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { toast } from '~/composables/useToast';

import type { SearchRequest, SearchResponseDto, SearchResultDto } from '~/api/dtos/search.dto';
import { searchPlaces$ } from '~/api/search.api';

import { useTrigger } from './useTrigger';

export function useSearch(): {
	searchResults: Ref<SearchResultDto[]>;
	isSearching: Ref<boolean>;
	hasSearched: Ref<boolean>;
	nextPageToken: Ref<string | null>;
	searchPlaces: (request: SearchRequest) => void;
	clearResults: () => void;
} {
	const { t } = useI18n();
	const searchResults = ref<SearchResultDto[]>([]);
	const hasSearched = ref(false);
	const nextPageToken = ref<string | null>(null);
	const { trigger: triggerSearch, loading: isSearching } = useTrigger();

	const searchPlaces = (request: SearchRequest) => {
		triggerSearch(searchPlaces$(request), {
			onSuccess: (response: { data: SearchResponseDto }) => {
				searchResults.value = response.data.results;
				nextPageToken.value = response.data.nextPageToken;
				hasSearched.value = true;
			},
			onError: () => {
				toast.error(t('errors.search-failed'));
			}
		});
	};

	const clearResults = () => {
		searchResults.value = [];
		hasSearched.value = false;
	};

	return {
		searchResults,
		isSearching,
		hasSearched,
		nextPageToken,
		searchPlaces,
		clearResults
	};
}
