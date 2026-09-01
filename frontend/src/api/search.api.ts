import type { AxiosObservable } from 'axios-observable';

import type { SearchRequest, SearchResponseDto } from '~/api/dtos';
import { useHttp } from '~/composables';

export function searchPlaces$(request: SearchRequest): AxiosObservable<SearchResponseDto> {
	const { axiosInstance } = useHttp();
	return axiosInstance.post('/v1/search/places', request);
}
