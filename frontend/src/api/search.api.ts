import type { AxiosObservable } from 'axios-observable';

import type { SearchRequest, SearchResponseDto } from '~/api/dtos/search.dto';
import { useHttp } from '~/composables/useHttp';

const { axiosInstance } = useHttp();

export function searchPlaces$(request: SearchRequest): AxiosObservable<SearchResponseDto> {
	return axiosInstance.post('/v1/search/places', request);
}
