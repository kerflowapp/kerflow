import { defineStore } from 'pinia';
import { computed, ref } from 'vue';

import type { ProspectDto, ProspectPipelineColumnDto } from '~/api/dtos/prospect.dto';

export const useProspectsStore = defineStore('prospects-store', () => {
	const prospects = ref<ProspectDto[]>([]);
	const pipelineColumns = ref<ProspectPipelineColumnDto[]>([]);

	const prospectsByStatus = computed(() => {
		const grouped: Record<string, ProspectDto[]> = {};
		for (const prospect of prospects.value) {
			const key = prospect.statusKey || 'NEW';
			if (!grouped[key]) grouped[key] = [];
			grouped[key].push(prospect);
		}
		// Order each column by explicit position; legacy rows (null position) keep fetch order
		for (const key of Object.keys(grouped)) {
			grouped[key].sort((a, b) => {
				if (a.position == null && b.position == null) return 0;
				if (a.position == null) return 1;
				if (b.position == null) return -1;
				return a.position - b.position;
			});
		}
		return grouped;
	});

	const totalCount = computed(() => prospects.value.length);
	const wonCount = computed(() => prospects.value.filter(p => p.statusKey === 'WON').length);
	const conversionRate = computed(() => (totalCount.value > 0 ? Math.round((wonCount.value / totalCount.value) * 100) : 0));

	function setProspects(data: ProspectDto[]) {
		prospects.value = data;
	}

	function setPipelineColumns(data: ProspectPipelineColumnDto[]) {
		pipelineColumns.value = [...data].sort((a, b) => a.sortOrder - b.sortOrder);
	}

	function upsertPipelineColumn(column: ProspectPipelineColumnDto) {
		const idx = pipelineColumns.value.findIndex(c => c.id === column.id);
		if (idx === -1) {
			pipelineColumns.value.push(column);
		} else {
			pipelineColumns.value[idx] = column;
		}
		pipelineColumns.value = [...pipelineColumns.value].sort((a, b) => a.sortOrder - b.sortOrder);
	}

	function removePipelineColumn(id: string) {
		pipelineColumns.value = pipelineColumns.value.filter(c => c.id !== id);
	}

	function addProspect(prospect: ProspectDto) {
		prospects.value.push(prospect);
	}

	function updateProspect(updated: ProspectDto) {
		const index = prospects.value.findIndex(p => p.id === updated.id);
		if (index !== -1) {
			prospects.value[index] = updated;
		}
	}

	function moveProspect(id: string, statusKey: string) {
		const prospect = prospects.value.find(p => p.id === id);
		if (prospect) {
			prospect.statusKey = statusKey;
		}
	}

	function reorderProspects(statusKey: string, orderedIds: string[]) {
		orderedIds.forEach((id, index) => {
			const prospect = prospects.value.find(p => p.id === id);
			if (prospect) {
				prospect.statusKey = statusKey;
				prospect.position = index;
			}
		});
	}

	function removeProspect(id: string) {
		prospects.value = prospects.value.filter(p => p.id !== id);
	}

	return {
		prospects,
		pipelineColumns,
		prospectsByStatus,
		totalCount,
		wonCount,
		conversionRate,
		setProspects,
		setPipelineColumns,
		upsertPipelineColumn,
		removePipelineColumn,
		addProspect,
		updateProspect,
		moveProspect,
		reorderProspects,
		removeProspect
	};
});
