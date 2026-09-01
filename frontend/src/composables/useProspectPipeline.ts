import type { Ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { toast } from 'vuetify-sonner';

import type {
	CreateProspectPipelineColumnDto,
	ProspectPipelineColumnDto,
	ReorderProspectPipelineColumnsDto,
	UpdateProspectPipelineColumnDto
} from '~/api/dtos/prospect.dto';
import {
	createProspectPipelineColumn$,
	deleteProspectPipelineColumn$,
	getProspectPipelineColumns$,
	reorderProspectPipelineColumns$,
	updateProspectPipelineColumn$
} from '~/api/prospects.api';
import { useProspectsStore } from '~/stores';

import { useTrigger } from './useTrigger';

export function useProspectPipeline(): {
	isFetching: Ref<boolean>;
	isSaving: Ref<boolean>;
	fetchColumns: () => void;
	createColumn: (data: CreateProspectPipelineColumnDto) => void;
	updateColumn: (id: string, data: UpdateProspectPipelineColumnDto) => void;
	reorderColumns: (orderedIds: string[]) => void;
	deleteColumn: (id: string) => void;
} {
	const { t } = useI18n();
	const store = useProspectsStore();
	const { trigger: triggerFetch, loading: isFetching } = useTrigger();
	const { trigger: triggerSave, loading: isSaving } = useTrigger();

	const fetchColumns = () => {
		triggerFetch(getProspectPipelineColumns$(), {
			onSuccess: (response: { data: ProspectPipelineColumnDto[] }) => {
				store.setPipelineColumns(response.data);
			},
			onError: () => {
				toast.error(t('errors.fetch-prospects-failed'));
			}
		});
	};

	const createColumn = (data: CreateProspectPipelineColumnDto) => {
		triggerSave(createProspectPipelineColumn$(data), {
			onSuccess: (response: { data: ProspectPipelineColumnDto }) => {
				store.upsertPipelineColumn(response.data);
				toast.success(t('common.add'));
			},
			onError: () => {
				toast.error(t('errors.update-prospect-failed'));
			}
		});
	};

	const updateColumn = (id: string, data: UpdateProspectPipelineColumnDto) => {
		triggerSave(updateProspectPipelineColumn$(id, data), {
			onSuccess: (response: { data: ProspectPipelineColumnDto }) => {
				store.upsertPipelineColumn(response.data);
				toast.success(t('common.save'));
			},
			onError: () => {
				toast.error(t('errors.update-prospect-failed'));
			}
		});
	};

	const reorderColumns = (orderedIds: string[]) => {
		const payload: ReorderProspectPipelineColumnsDto = { orderedIds };
		triggerSave(reorderProspectPipelineColumns$(payload), {
			onSuccess: (response: { data: ProspectPipelineColumnDto[] }) => {
				store.setPipelineColumns(response.data);
			},
			onError: () => {
				toast.error(t('errors.update-prospect-failed'));
			}
		});
	};

	const deleteColumn = (id: string) => {
		triggerSave(deleteProspectPipelineColumn$(id), {
			onSuccess: () => {
				store.removePipelineColumn(id);
				toast.success(t('success.prospect-deleted'));
			},
			onError: () => {
				toast.error(t('errors.delete-prospect-failed'));
			}
		});
	};

	return { isFetching, isSaving, fetchColumns, createColumn, updateColumn, reorderColumns, deleteColumn };
}
