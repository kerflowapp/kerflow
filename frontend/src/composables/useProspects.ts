import type { Ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { toast } from '~/composables/useToast';

import type { CreateProspectDto, ProspectDto, UpdateProspectDto } from '~/api/dtos/prospect.dto';
import { ProspectSource } from '~/api/dtos/prospect.dto';
import type { SearchResultDto } from '~/api/dtos/search.dto';
import {
	createProspect$,
	deleteProspect$,
	enrichProspect$,
	getProspects$,
	reorderProspects$,
	updateProspect$,
	updateProspectStatus$
} from '~/api/prospects.api';
import { useProspectsStore } from '~/stores/prospects.store';

import { useTrigger } from './useTrigger';

export function useProspects(): {
	isFetching: Ref<boolean>;
	isCreating: Ref<boolean>;
	isUpdating: Ref<boolean>;
	isDeleting: Ref<boolean>;
	isEnriching: Ref<boolean>;
	fetchProspects: () => void;
	addProspect: (data: CreateProspectDto) => void;
	addFromSearchResult: (result: SearchResultDto, searchKeywords?: string) => void;
	editProspect: (id: string, data: UpdateProspectDto) => void;
	enrichProspect: (id: string, onEnriched?: (prospect: ProspectDto) => void) => void;
	moveProspect: (id: string, statusKey: string) => void;
	reorderProspects: (statusKey: string, orderedIds: string[]) => void;
	removeProspect: (id: string) => void;
} {
	const { t } = useI18n();
	const store = useProspectsStore();
	const { trigger: triggerFetch, loading: isFetching } = useTrigger();
	const { trigger: triggerCreate, loading: isCreating } = useTrigger();
	const { trigger: triggerUpdate, loading: isUpdating } = useTrigger();
	const { trigger: triggerDelete, loading: isDeleting } = useTrigger();
	const { trigger: triggerEnrich, loading: isEnriching } = useTrigger();
	const { trigger: triggerReorder } = useTrigger();

	const fetchProspects = () => {
		triggerFetch(getProspects$(), {
			onSuccess: (response: { data: ProspectDto[] }) => {
				store.setProspects(response.data);
			},
			onError: () => {
				toast.error(t('errors.fetch-prospects-failed'));
			}
		});
	};

	const addProspect = (data: CreateProspectDto) => {
		triggerCreate(createProspect$(data), {
			onSuccess: (response: { data: ProspectDto }) => {
				store.addProspect(response.data);
				toast.success(t('success.prospect-created'));
			},
			onError: () => {
				toast.error(t('errors.create-prospect-failed'));
			}
		});
	};

	const addFromSearchResult = (result: SearchResultDto, searchKeywords?: string) => {
		const tags = searchKeywords ? searchKeywords.split(/[\s,]+/).filter(Boolean) : [];
		const data: CreateProspectDto = {
			name: result.name,
			address: result.address,
			phone: result.phone,
			website: result.website,
			lat: result.lat,
			lng: result.lng,
			socialLinks: {
				instagram: result.instagram,
				facebook: result.facebook,
				linkedin: result.linkedin
			},
			tags,
			signals: result.signals,
			googlePlaceId: result.placeId,
			googleTypes: result.types,
			googleRating: result.rating,
			googleUserRatingsTotal: result.userRatingsTotal,
			googleEditorialSummary: result.editorialSummary,
			searchQuery: searchKeywords,
			source: ProspectSource.SEARCH,
			statusKey: 'NEW'
		};
		addProspect(data);
	};

	const editProspect = (id: string, data: UpdateProspectDto) => {
		triggerUpdate(updateProspect$(id, data), {
			onSuccess: (response: { data: ProspectDto }) => {
				store.updateProspect(response.data);
				toast.success(t('success.prospect-updated'));
			},
			onError: () => {
				toast.error(t('errors.update-prospect-failed'));
			}
		});
	};

	const enrichProspect = (id: string, onEnriched?: (prospect: ProspectDto) => void) => {
		triggerEnrich(enrichProspect$(id), {
			onSuccess: (response: { data: ProspectDto }) => {
				store.updateProspect(response.data);
				onEnriched?.(response.data);
				toast.success(t('success.prospect-enriched'));
			},
			onError: () => {
				toast.error(t('errors.enrich-prospect-failed'));
			}
		});
	};

	const moveProspect = (id: string, statusKey: string) => {
		store.moveProspect(id, statusKey);
		triggerUpdate(updateProspectStatus$(id, statusKey), {
			onError: () => {
				toast.error(t('errors.update-prospect-failed'));
				fetchProspects();
			}
		});
	};

	const reorderProspects = (statusKey: string, orderedIds: string[]) => {
		store.reorderProspects(statusKey, orderedIds);
		triggerReorder(reorderProspects$({ statusKey, orderedIds }), {
			onSuccess: (response: { data: ProspectDto[] }) => {
				response.data.forEach(p => store.updateProspect(p));
			},
			onError: () => {
				toast.error(t('errors.reorder-prospects-failed'));
				fetchProspects();
			}
		});
	};

	const removeProspect = (id: string) => {
		triggerDelete(deleteProspect$(id), {
			onSuccess: () => {
				store.removeProspect(id);
				toast.success(t('success.prospect-deleted'));
			},
			onError: () => {
				toast.error(t('errors.delete-prospect-failed'));
			}
		});
	};

	return {
		isFetching,
		isCreating,
		isUpdating,
		isDeleting,
		isEnriching,
		fetchProspects,
		addProspect,
		addFromSearchResult,
		editProspect,
		enrichProspect,
		moveProspect,
		reorderProspects,
		removeProspect
	};
}
