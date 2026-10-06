import type { ComputedRef } from 'vue';
import { computed } from 'vue';

import { useProspectsStore } from '~/stores/prospects.store';

import { useProspectPipeline } from './useProspectPipeline';

/**
 * Tells which Google places are already in the user's pipeline, and in which column.
 * Matches on the Google place id, so prospects added by hand or from a CSV are never matched.
 */
export function useExistingProspects(): {
	existingColumns: ComputedRef<Record<string, string>>;
} {
	const store = useProspectsStore();
	const { fetchColumns, columnTitle } = useProspectPipeline();

	// Prospects are loaded once at login by FullLayout; the columns only by the board
	if (store.pipelineColumns.length === 0) fetchColumns();

	const existingColumns = computed(() => {
		const columnsByKey = new Map(store.pipelineColumns.map(col => [col.key, col]));
		const result: Record<string, string> = {};
		for (const prospect of store.prospects) {
			if (!prospect.googlePlaceId) continue;
			const key = prospect.statusKey || 'NEW';
			const column = columnsByKey.get(key);
			result[prospect.googlePlaceId] = column ? columnTitle(column) : key;
		}
		return result;
	});

	return { existingColumns };
}
