<template>
	<v-navigation-drawer v-model="modelValue" location="right" temporary width="450">
		<v-card v-if="result" flat>
			<v-card-title class="d-flex justify-space-between align-start pa-4">
				<div class="d-flex flex-column flex-grow-1 overflow-hidden ga-3 min-w-0">
					<div class="d-flex align-center ga-2 flex-wrap min-w-0">
						<span class="text-h6 text-truncate">{{ result.name }}</span>
						<v-chip v-if="result.rating" class="flex-shrink-0" color="secondary" size="small" variant="flat">
							<v-icon size="14" start>mdi-star</v-icon>
							{{ result.rating }} ({{ result.userRatingsTotal }})
						</v-chip>
					</div>
					<ProspectScoreCard v-if="result.scoreDetails" :score="result.scoreDetails" />
				</div>
				<v-btn class="flex-shrink-0" icon="mdi-close" variant="text" @click="modelValue = false" />
			</v-card-title>

			<v-card-text>
				<!-- Open / Closed badge -->
				<v-chip
					v-if="result.openingHours"
					class="mb-4"
					:color="result.openingHours.openNow ? 'success' : 'error'"
					size="small"
					variant="tonal"
				>
					<v-icon size="14" start>{{ result.openingHours.openNow ? 'mdi-check-circle-outline' : 'mdi-clock-outline' }}</v-icon>
					{{ result.openingHours.openNow ? t('search.detail.open-now') : t('search.detail.closed') }}
				</v-chip>

				<!-- Detected signals -->
				<template v-if="result.signals?.length">
					<div class="text-subtitle-2 font-weight-bold mb-2">{{ t('signals.title') }}</div>
					<SignalsList class="mb-3" :signals="result.signals" />
					<v-divider class="my-3" />
				</template>

				<!-- Contact section -->
				<div class="text-subtitle-2 font-weight-bold mb-2">{{ t('search.detail.contact') }}</div>

				<div v-if="result.address" class="d-flex align-center text-body-2 mb-2">
					<v-icon class="mr-2" size="18">mdi-map-marker-outline</v-icon>
					<span>{{ result.address }}</span>
				</div>
				<div v-if="result.phone" class="d-flex align-center text-body-2 mb-2">
					<v-icon class="mr-2" size="18">mdi-phone-outline</v-icon>
					<a class="text-decoration-none" :href="`tel:${result.phone}`">{{ result.phone }}</a>
				</div>
				<div v-if="result.website" class="d-flex align-center text-body-2 mb-2">
					<v-icon class="mr-2" size="18">mdi-web</v-icon>
					<a class="text-decoration-none text-truncate" :href="result.website" rel="noopener noreferrer" target="_blank">
						{{ result.website }}
					</a>
				</div>
				<div v-if="result.googleMapsUrl" class="d-flex align-center text-body-2 mb-2">
					<v-icon class="mr-2" size="18">mdi-google-maps</v-icon>
					<a class="text-decoration-none" :href="result.googleMapsUrl" rel="noopener noreferrer" target="_blank">
						{{ t('search.detail.view-on-google-maps') }}
					</a>
				</div>

				<!-- Social links -->
				<template v-if="hasSocialLinks">
					<v-divider class="my-3" />
					<div class="text-subtitle-2 font-weight-bold mb-2">{{ t('search.detail.social-links') }}</div>
					<div class="d-flex ga-1 flex-wrap">
						<v-btn
							v-if="result.facebook"
							color="blue"
							:href="result.facebook"
							prepend-icon="mdi-facebook"
							size="small"
							target="_blank"
							variant="tonal"
						>
							Facebook
						</v-btn>
						<v-btn
							v-if="result.instagram"
							color="pink"
							:href="result.instagram"
							prepend-icon="mdi-instagram"
							size="small"
							target="_blank"
							variant="tonal"
						>
							Instagram
						</v-btn>
						<v-btn
							v-if="result.linkedin"
							color="blue-darken-3"
							:href="result.linkedin"
							prepend-icon="mdi-linkedin"
							size="small"
							target="_blank"
							variant="tonal"
						>
							LinkedIn
						</v-btn>
						<v-btn
							v-if="result.twitter"
							:href="result.twitter"
							prepend-icon="mdi-twitter"
							size="small"
							target="_blank"
							variant="tonal"
						>
							X / Twitter
						</v-btn>
						<v-btn
							v-if="result.tiktok"
							:href="result.tiktok"
							prepend-icon="mdi-music-note"
							size="small"
							target="_blank"
							variant="tonal"
						>
							TikTok
						</v-btn>
						<v-btn
							v-if="result.youtube"
							color="red"
							:href="result.youtube"
							prepend-icon="mdi-youtube"
							size="small"
							target="_blank"
							variant="tonal"
						>
							YouTube
						</v-btn>
					</div>
				</template>

				<!-- Opening hours -->
				<template v-if="result.openingHours?.weekdayText?.length">
					<v-divider class="my-3" />
					<div class="text-subtitle-2 font-weight-bold mb-2">{{ t('search.detail.opening-hours') }}</div>
					<v-list class="pa-0" density="compact">
						<v-list-item
							v-for="(line, idx) in result.openingHours.weekdayText"
							:key="idx"
							class="px-0"
							:class="{ 'font-weight-bold text-primary': idx === todayIndex }"
							density="compact"
						>
							<template #prepend>
								<v-icon class="mr-1" size="16">mdi-clock-outline</v-icon>
							</template>
							{{ line }}
						</v-list-item>
					</v-list>
				</template>

				<!-- Reviews -->
				<v-divider class="my-3" />
				<div class="text-subtitle-2 font-weight-bold mb-2">{{ t('search.detail.reviews') }}</div>

				<div v-if="!result.reviews?.length" class="text-body-2 text-grey">
					{{ t('search.detail.no-reviews') }}
				</div>

				<div v-else class="d-flex flex-column ga-3">
					<v-card v-for="(review, idx) in result.reviews" :key="idx" class="pa-3" elevation="0" variant="outlined">
						<div class="d-flex align-center ga-2 mb-1">
							<v-avatar color="grey-lighten-3" size="28">
								<v-img v-if="review.profilePhotoUrl" :src="review.profilePhotoUrl" />
								<v-icon v-else size="18">mdi-account</v-icon>
							</v-avatar>
							<div>
								<div class="text-body-2 font-weight-bold">{{ review.authorName }}</div>
								<div class="text-caption text-grey">{{ review.relativeTimeDescription }}</div>
							</div>
						</div>
						<v-rating
							class="mb-1"
							color="amber"
							density="compact"
							half-increments
							:model-value="review.rating"
							readonly
							size="14"
						/>
						<div v-if="review.text" class="text-body-2 review-text">{{ review.text }}</div>
					</v-card>
				</div>
			</v-card-text>

			<v-card-actions class="pa-4">
				<v-btn block color="primary" :loading="isAdding" prepend-icon="mdi-plus" variant="flat" @click="emit('add', result)">
					{{ t('search.add-to-prospects') }}
				</v-btn>
			</v-card-actions>
		</v-card>
	</v-navigation-drawer>
</template>

<style scoped>
.review-text {
	white-space: pre-line;
	max-height: 120px;
	overflow-y: auto;
}
</style>

<script setup lang="ts">
import { computed } from 'vue';
import { useI18n } from 'vue-i18n';

import type { SearchResultDto } from '~/api/dtos/search.dto';
import ProspectScoreCard from '~/components/signals/ProspectScoreCard.vue';
import SignalsList from '~/components/signals/SignalsList.vue';

interface Props {
	result: SearchResultDto | null;
	isAdding?: boolean;
}

interface Emits {
	(e: 'add', result: SearchResultDto): void;
}

const props = withDefaults(defineProps<Props>(), { isAdding: false });
const emit = defineEmits<Emits>();
const { t } = useI18n();

const modelValue = defineModel<boolean>();

const hasSocialLinks = computed(() => {
	if (!props.result) return false;
	return !!(
		props.result.facebook ||
		props.result.instagram ||
		props.result.linkedin ||
		props.result.twitter ||
		props.result.tiktok ||
		props.result.youtube
	);
});

const todayIndex = computed(() => {
	const day = new Date().getDay();
	return day === 0 ? 6 : day - 1;
});
</script>
