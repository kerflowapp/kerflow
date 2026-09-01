<template>
	<v-card
		class="search-result-card h-100"
		:class="{ 'search-result-card--highlighted': highlighted }"
		elevation="0"
		@click="emit('select', result.placeId)"
		@mouseenter="emit('hover', result.placeId)"
		@mouseleave="emit('hover', null)"
	>
		<v-card-text>
			<div class="d-flex align-start justify-space-between">
				<div class="d-flex align-center ga-2 flex-grow-1 min-w-0">
					<v-avatar v-if="index != null" class="flex-shrink-0" color="primary" size="28">
						<span class="text-caption font-weight-bold text-white">{{ index + 1 }}</span>
					</v-avatar>
					<div class="text-subtitle-1 font-weight-bold text-truncate">{{ result.name }}</div>
					<v-tooltip v-if="result.scoreDetails" location="top" max-width="360">
						<template #activator="{ props: tipProps }">
							<span v-bind="tipProps" class="d-inline-flex flex-shrink-0" @click.stop>
								<v-rating
									active-color="warning"
									color="warning"
									density="compact"
									:model-value="result.scoreDetails.stars"
									readonly
									size="x-small"
								/>
							</span>
						</template>
						<ScoreReasonsList :reasons="result.scoreDetails.reasons" />
					</v-tooltip>
				</div>
				<v-chip v-if="result.rating" color="secondary" size="small" variant="flat">
					<v-icon size="14" start>mdi-star</v-icon>
					{{ result.rating }} ({{ result.userRatingsTotal }})
				</v-chip>
			</div>

			<div class="mt-2">
				<div v-if="result.address" class="d-flex align-center text-body-2 mb-1">
					<v-icon class="mr-1" size="16">mdi-map-marker-outline</v-icon>
					{{ result.address }}
				</div>
				<div v-if="result.phone" class="d-flex align-center text-body-2 mb-1">
					<v-icon class="mr-1" size="16">mdi-phone-outline</v-icon>
					<a class="text-decoration-none" :href="`tel:${result.phone}`" @click.stop>{{ result.phone }}</a>
				</div>
				<div v-if="result.website" class="d-flex align-center text-body-2 mb-1">
					<v-icon class="mr-1" size="16">mdi-web</v-icon>
					<a
						class="text-decoration-none text-truncate"
						:href="result.website"
						rel="noopener noreferrer"
						target="_blank"
						@click.stop
					>
						{{ result.website }}
					</a>
				</div>
			</div>

			<div
				v-if="result.instagram || result.facebook || result.linkedin || result.twitter || result.tiktok || result.youtube"
				class="d-flex ga-1 mt-2"
			>
				<v-btn v-if="result.facebook" :href="result.facebook" icon size="x-small" target="_blank" variant="text" @click.stop>
					<v-icon size="18">mdi-facebook</v-icon>
				</v-btn>
				<v-btn v-if="result.instagram" :href="result.instagram" icon size="x-small" target="_blank" variant="text" @click.stop>
					<v-icon size="18">mdi-instagram</v-icon>
				</v-btn>
				<v-btn v-if="result.linkedin" :href="result.linkedin" icon size="x-small" target="_blank" variant="text" @click.stop>
					<v-icon size="18">mdi-linkedin</v-icon>
				</v-btn>
				<v-btn v-if="result.twitter" :href="result.twitter" icon size="x-small" target="_blank" variant="text" @click.stop>
					<v-icon size="18">mdi-twitter</v-icon>
				</v-btn>
				<v-btn v-if="result.tiktok" :href="result.tiktok" icon size="x-small" target="_blank" variant="text" @click.stop>
					<v-icon size="18">mdi-music-note</v-icon>
				</v-btn>
				<v-btn v-if="result.youtube" :href="result.youtube" icon size="x-small" target="_blank" variant="text" @click.stop>
					<v-icon size="18">mdi-youtube</v-icon>
				</v-btn>
			</div>
		</v-card-text>

		<v-card-actions>
			<v-spacer />
			<v-btn
				color="primary"
				:loading="isAdding"
				prepend-icon="mdi-plus"
				size="small"
				variant="flat"
				@click.stop="emit('add', result)"
			>
				{{ t('search.add-to-prospects') }}
			</v-btn>
		</v-card-actions>
	</v-card>
</template>

<style scoped>
.search-result-card {
	transition:
		box-shadow 0.15s ease,
		border-color 0.15s ease;
	border: 2px solid transparent;
	cursor: pointer;
}

.search-result-card--highlighted {
	border-color: rgb(var(--v-theme-primary));
	box-shadow: 0 0 0 1px rgb(var(--v-theme-primary));
}
</style>

<script setup lang="ts">
import { useI18n } from 'vue-i18n';

import type { SearchResultDto } from '~/api/dtos/search.dto';
import ScoreReasonsList from '~/components/signals/ScoreReasonsList.vue';

interface Props {
	result: SearchResultDto;
	index?: number | null;
	isAdding?: boolean;
	highlighted?: boolean;
}

interface Emits {
	(e: 'add', result: SearchResultDto): void;
	(e: 'hover', placeId: string | null): void;
	(e: 'select', placeId: string): void;
}

withDefaults(defineProps<Props>(), {
	index: null,
	isAdding: false,
	highlighted: false
});
const emit = defineEmits<Emits>();
const { t } = useI18n();
</script>
