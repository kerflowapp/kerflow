<template>
	<div class="kanban-column">
		<div class="d-flex align-center justify-space-between mb-3 px-2">
			<div class="d-flex align-center">
				<v-icon class="mr-2" :color="color" size="18">{{ icon }}</v-icon>
				<span class="text-subtitle-2 font-weight-bold">{{ title }}</span>
			</div>
			<v-chip :color="color" size="x-small" variant="tonal">{{ prospects.length }}</v-chip>
		</div>

		<draggable
			:animation="200"
			class="kanban-column-body"
			drag-class="kanban-card-drag"
			ghost-class="kanban-card-ghost"
			:group="{ name: 'prospects', pull: !dragDisabled, put: !dragDisabled }"
			handle=".drag-handle"
			item-key="id"
			:list="prospects"
			:sort="!dragDisabled"
			@change="onChange"
			@end="emit('drag-end')"
			@start="emit('drag-start')"
		>
			<template #item="{ element }">
				<div>
					<KanbanCard
						:prospect="element"
						:readonly="readonly"
						@delete="emit('delete', element.id)"
						@edit="emit('edit', element)"
						@select="emit('select', element)"
					/>
				</div>
			</template>
			<template #footer>
				<div v-if="prospects.length === 0" class="text-center text-caption text-grey pa-4">
					{{ t('kanban.empty-column') }}
				</div>
			</template>
		</draggable>
	</div>
</template>

<style scoped>
.kanban-column {
	background: rgba(0, 0, 0, 0.02);
	border-radius: 8px;
	padding: 12px 8px;
	min-height: 300px;
}
.kanban-column-body {
	min-height: 200px;
}
/* Placeholder showing where the card will drop */
.kanban-column-body :deep(.kanban-card-ghost) {
	opacity: 0.6;
	border: 2px dashed rgb(var(--v-theme-primary));
	background: rgba(var(--v-theme-primary), 0.08);
}
.kanban-column-body :deep(.kanban-card-ghost) > * {
	visibility: hidden;
}
</style>

<script setup lang="ts">
import { computed, defineAsyncComponent } from 'vue';
import { useI18n } from 'vue-i18n';
import draggable from 'vuedraggable';

import type { ProspectDto } from '~/api/dtos/prospect.dto';

const KanbanCard = defineAsyncComponent(() => import('./KanbanCard.vue'));

interface Props {
	title: string;
	statusKey: string;
	prospects: ProspectDto[];
	color: string;
	icon: string;
	readonly?: boolean;
	dragDisabled?: boolean;
}

interface Emits {
	(e: 'reorder', payload: { statusKey: string; orderedIds: string[] }): void;
	(e: 'drag-start'): void;
	(e: 'drag-end'): void;
	(e: 'edit', prospect: ProspectDto): void;
	(e: 'delete', id: string): void;
	(e: 'select', prospect: ProspectDto): void;
}

const props = defineProps<Props>();
const emit = defineEmits<Emits>();
const { t } = useI18n();

const dragDisabled = computed(() => props.readonly || props.dragDisabled);

// vuedraggable has already mutated the bound list in place; persist this column's new order.
// Only persist the column that GAINED a card ('added') or was reordered internally ('moved').
// Skip the source column's 'removed' event: it would fire a SECOND concurrent reorder call, and
// useTrigger's single subscription cancels the first — dropping the target's move (needed 2 drags
// to stick). The source's position gaps are harmless (ordering is by position asc).
const onChange = (evt: { added?: unknown; moved?: unknown; removed?: unknown }) => {
	if (!evt.added && !evt.moved) return;
	const orderedIds = props.prospects.map(p => p.id);
	if (!orderedIds.length) return;
	emit('reorder', { statusKey: props.statusKey, orderedIds });
};
</script>
