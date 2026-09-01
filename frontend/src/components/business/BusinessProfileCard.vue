<template>
	<div v-if="profile">
		<div class="d-flex flex-wrap align-center ga-2">
			<v-chip :color="profile.category === 'OTHER' ? 'grey' : 'primary'" size="small" variant="tonal">
				<v-icon size="14" start>{{ categoryIcon }}</v-icon>
				{{ t(`business.categories.${profile.category}`) }}
			</v-chip>
			<v-chip v-if="profile.categorySource" class="px-0" size="x-small" variant="text">
				{{ t(`business.sources.${profile.categorySource}`) }}
			</v-chip>
		</div>

		<ul v-if="visibleFacts.length" class="text-body-2 mt-2 ps-4">
			<li v-for="fact in visibleFacts" :key="fact.key">{{ factText(fact) }}</li>
		</ul>

		<div v-if="googleEditorialSummary" class="mt-3">
			<div class="text-caption text-medium-emphasis">{{ t('business.google-summary') }}</div>
			<blockquote class="text-body-2 font-italic google-summary">{{ googleEditorialSummary }}</blockquote>
		</div>
	</div>
	<div v-else class="text-body-2 text-medium-emphasis">{{ t('business.empty') }}</div>
</template>

<style scoped>
.google-summary {
	border-left: 2px solid rgba(var(--v-border-color), var(--v-border-opacity));
	padding-left: 8px;
}
</style>

<script setup lang="ts">
import { computed } from 'vue';
import { useI18n } from 'vue-i18n';

import type { BusinessFactDto, BusinessProfileDto } from '~/api/dtos/prospect.dto';

interface Props {
	profile?: BusinessProfileDto | null;
	googleEditorialSummary?: string | null;
}

const props = defineProps<Props>();
const { t, te } = useI18n();

const BUSINESS_ICONS: Record<string, string> = {
	TAXI_VTC: 'mdi-taxi',
	AMBULANCE: 'mdi-ambulance',
	ROAD_FREIGHT: 'mdi-truck-outline',
	PASSENGER_TRANSPORT: 'mdi-bus',
	VEHICLE_RENTAL: 'mdi-car-key',
	AUTO_SERVICES: 'mdi-car-wrench',
	DRIVING_SCHOOL: 'mdi-car-cog',
	CONSTRUCTION: 'mdi-hard-hat',
	LANDSCAPING: 'mdi-shovel',
	CLEANING: 'mdi-broom',
	OTHER: 'mdi-domain'
};

const categoryIcon = computed(() => BUSINESS_ICONS[props.profile?.category ?? 'OTHER'] ?? 'mdi-domain');

// A fact key the backend added before this app knows it renders nothing, never a raw key.
const visibleFacts = computed(() => (props.profile?.facts ?? []).filter(fact => te(`business.facts.${fact.key}`)));

// The activity label comes from the category, so it has a single source of truth.
const factText = (fact: BusinessFactDto): string =>
	t(`business.facts.${fact.key}`, {
		...(fact.params ?? {}),
		activity: t(`business.categories.${props.profile!.category}`)
	});
</script>
