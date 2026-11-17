<template>
	<v-card class="kanban-card mb-2" elevation="0" @click="emit('select', prospect)">
		<v-card-text class="pa-3">
			<div class="d-flex align-start justify-space-between">
				<div class="font-weight-medium text-body-2">{{ prospect.name }}</div>
				<div class="d-flex align-center">
					<v-icon v-if="!readonly" class="drag-handle" color="grey" icon="mdi-drag" size="18" @click.stop />
					<v-menu v-if="!readonly">
						<template #activator="{ props }">
							<v-btn icon="mdi-dots-vertical" size="x-small" variant="text" v-bind="props" @click.stop />
						</template>
						<v-list density="compact">
							<v-list-item prepend-icon="mdi-pencil" :title="t('prospects.edit')" @click="emit('edit', prospect)" />
							<v-list-item prepend-icon="mdi-delete" :title="t('prospects.delete')" @click="emit('delete', prospect.id)" />
							<template v-if="moveTargets.length">
								<v-divider class="my-1" />
								<v-list-subheader>{{ t('prospects.move-to') }}</v-list-subheader>
								<v-list-item
									v-for="target in moveTargets"
									:key="target.key"
									:title="target.title"
									@click="emit('move', prospect.id, target.key)"
								>
									<template #prepend>
										<v-icon :color="target.color" :icon="target.icon" />
									</template>
								</v-list-item>
							</template>
						</v-list>
					</v-menu>
				</div>
			</div>

			<FollowUpChip :prospect="prospect" />

			<div v-if="prospect.score" class="d-flex align-center ga-1 mt-1">
				<v-rating
					active-color="warning"
					color="warning"
					density="compact"
					:model-value="prospect.score.stars"
					readonly
					size="x-small"
				/>
				<span class="text-caption text-medium-emphasis">{{ prospect.score.score }}/100</span>
			</div>

			<div v-if="prospect.address" class="text-caption text-grey-darken-1 mt-1">
				<v-icon class="mr-1" size="12">mdi-map-marker-outline</v-icon>
				{{ prospect.address }}
			</div>

			<div v-if="prospect.phone || prospect.email" class="text-caption text-grey-darken-1 mt-1">
				<v-icon class="mr-1" size="12">mdi-phone-outline</v-icon>
				{{ prospect.phone || prospect.email }}
			</div>

			<div v-if="prospect.tags?.length" class="d-flex ga-1 mt-2">
				<v-chip v-for="tag in prospect.tags.slice(0, 3)" :key="tag" color="primary" size="x-small" variant="tonal">
					{{ tag }}
				</v-chip>
			</div>
		</v-card-text>
	</v-card>
</template>

<style scoped>
.kanban-card {
	cursor: pointer;
	border: 1px solid rgba(0, 0, 0, 0.08);
	transition: box-shadow 0.2s;
}
.kanban-card:hover {
	box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}
.drag-handle {
	cursor: grab;
}
.drag-handle:active {
	cursor: grabbing;
}
</style>

<script setup lang="ts">
import { computed } from 'vue';
import { useI18n } from 'vue-i18n';

import type { ProspectDto } from '~/api/dtos/prospect.dto';
import FollowUpChip from '~/components/prospects/FollowUpChip.vue';

export interface KanbanMoveTarget {
	key: string;
	title: string;
	icon: string;
	color: string;
}

interface Props {
	prospect: ProspectDto;
	readonly?: boolean;
	columns?: KanbanMoveTarget[];
}

interface Emits {
	(e: 'edit', prospect: ProspectDto): void;
	(e: 'delete', id: string): void;
	(e: 'select', prospect: ProspectDto): void;
	(e: 'move', id: string, statusKey: string): void;
}

const props = withDefaults(defineProps<Props>(), { columns: () => [] });
const emit = defineEmits<Emits>();
const { t } = useI18n();

const moveTargets = computed(() => props.columns.filter(c => c.key !== props.prospect.statusKey));
</script>
