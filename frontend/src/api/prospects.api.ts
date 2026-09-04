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
} from '~/api/dtos/prospect.dto';
import { useHttp } from '~/composables/useHttp';

const { axiosInstance, multipartConfig } = useHttp();

export function getProspects$(): AxiosObservable<ProspectDto[]> {
	return axiosInstance.get('/v1/prospects');
}

export function createProspect$(data: CreateProspectDto): AxiosObservable<ProspectDto> {
	return axiosInstance.post('/v1/prospects', data);
}

export function updateProspect$(id: string, data: UpdateProspectDto): AxiosObservable<ProspectDto> {
	return axiosInstance.put(`/v1/prospects/${id}`, data);
}

export function updateProspectStatus$(id: string, statusKey: string): AxiosObservable<ProspectDto> {
	return axiosInstance.patch(`/v1/prospects/${id}/status`, { statusKey });
}

export function reorderProspects$(data: ReorderProspectsDto): AxiosObservable<ProspectDto[]> {
	return axiosInstance.put('/v1/prospects/_reorder', data);
}

export function enrichProspect$(id: string): AxiosObservable<ProspectDto> {
	return axiosInstance.post(`/v1/prospects/${id}/enrich`);
}

export function deleteProspect$(id: string): AxiosObservable<void> {
	return axiosInstance.delete(`/v1/prospects/${id}`);
}

export function importProspectsCsv$(formData: FormData): AxiosObservable<ProspectDto[]> {
	return axiosInstance.post('/v1/prospects/import', formData, multipartConfig);
}

export function getProspectPipelineColumns$(): AxiosObservable<ProspectPipelineColumnDto[]> {
	return axiosInstance.get('/v1/prospects/pipeline-columns');
}

export function createProspectPipelineColumn$(data: CreateProspectPipelineColumnDto): AxiosObservable<ProspectPipelineColumnDto> {
	return axiosInstance.post('/v1/prospects/pipeline-columns', data);
}

export function updateProspectPipelineColumn$(
	id: string,
	data: UpdateProspectPipelineColumnDto
): AxiosObservable<ProspectPipelineColumnDto> {
	return axiosInstance.put(`/v1/prospects/pipeline-columns/${id}`, data);
}

export function reorderProspectPipelineColumns$(data: ReorderProspectPipelineColumnsDto): AxiosObservable<ProspectPipelineColumnDto[]> {
	return axiosInstance.put('/v1/prospects/pipeline-columns/_reorder', data);
}

export function deleteProspectPipelineColumn$(id: string): AxiosObservable<void> {
	return axiosInstance.delete(`/v1/prospects/pipeline-columns/${id}`);
}
