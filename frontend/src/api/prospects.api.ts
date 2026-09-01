import type { AxiosObservable } from 'axios-observable';

import type {
	CreateProspectDto,
	CreateProspectPipelineColumnDto,
	ProspectDto,
	ProspectPipelineColumnDto,
	ReorderProspectPipelineColumnsDto,
	ReorderProspectsDto,
	UpdateProspectDto,
	UpdateProspectPipelineColumnDto
} from '~/api/dtos';
import { useHttp } from '~/composables';

export function getProspects$(): AxiosObservable<ProspectDto[]> {
	const { axiosInstance } = useHttp();
	return axiosInstance.get('/v1/prospects');
}

export function createProspect$(data: CreateProspectDto): AxiosObservable<ProspectDto> {
	const { axiosInstance } = useHttp();
	return axiosInstance.post('/v1/prospects', data);
}

export function updateProspect$(id: string, data: UpdateProspectDto): AxiosObservable<ProspectDto> {
	const { axiosInstance } = useHttp();
	return axiosInstance.put(`/v1/prospects/${id}`, data);
}

export function updateProspectStatus$(id: string, statusKey: string): AxiosObservable<ProspectDto> {
	const { axiosInstance } = useHttp();
	return axiosInstance.patch(`/v1/prospects/${id}/status`, { statusKey });
}

export function reorderProspects$(data: ReorderProspectsDto): AxiosObservable<ProspectDto[]> {
	const { axiosInstance } = useHttp();
	return axiosInstance.put('/v1/prospects/_reorder', data);
}

export function enrichProspect$(id: string): AxiosObservable<ProspectDto> {
	const { axiosInstance } = useHttp();
	return axiosInstance.post(`/v1/prospects/${id}/enrich`);
}

export function deleteProspect$(id: string): AxiosObservable<void> {
	const { axiosInstance } = useHttp();
	return axiosInstance.delete(`/v1/prospects/${id}`);
}

export function importProspectsCsv$(formData: FormData): AxiosObservable<ProspectDto[]> {
	const { axiosInstance, multipartConfig } = useHttp();
	return axiosInstance.post('/v1/prospects/import', formData, multipartConfig);
}

export function getProspectPipelineColumns$(): AxiosObservable<ProspectPipelineColumnDto[]> {
	const { axiosInstance } = useHttp();
	return axiosInstance.get('/v1/prospects/pipeline-columns');
}

export function createProspectPipelineColumn$(data: CreateProspectPipelineColumnDto): AxiosObservable<ProspectPipelineColumnDto> {
	const { axiosInstance } = useHttp();
	return axiosInstance.post('/v1/prospects/pipeline-columns', data);
}

export function updateProspectPipelineColumn$(
	id: string,
	data: UpdateProspectPipelineColumnDto
): AxiosObservable<ProspectPipelineColumnDto> {
	const { axiosInstance } = useHttp();
	return axiosInstance.put(`/v1/prospects/pipeline-columns/${id}`, data);
}

export function reorderProspectPipelineColumns$(data: ReorderProspectPipelineColumnsDto): AxiosObservable<ProspectPipelineColumnDto[]> {
	const { axiosInstance } = useHttp();
	return axiosInstance.put('/v1/prospects/pipeline-columns/_reorder', data);
}

export function deleteProspectPipelineColumn$(id: string): AxiosObservable<void> {
	const { axiosInstance } = useHttp();
	return axiosInstance.delete(`/v1/prospects/pipeline-columns/${id}`);
}
