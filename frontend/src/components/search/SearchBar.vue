<template>
	<v-card class="mb-6" elevation="0">
		<v-card-text>
			<v-form @submit.prevent="onSearch">
				<v-row align="center">
					<v-col cols="12" md="4">
						<v-text-field
							v-model="keywords"
							clearable
							density="comfortable"
							hide-details
							:label="t('search.keywords-label')"
							:placeholder="t('search.keywords-placeholder')"
							prepend-inner-icon="mdi-magnify"
							variant="outlined"
						/>
					</v-col>
					<v-col cols="12" md="3">
						<v-autocomplete
							v-model="selectedCity"
							v-model:search="cityQuery"
							clearable
							density="comfortable"
							hide-details
							item-title="displayName"
							item-value="displayName"
							:items="citySuggestions"
							:label="t('search.city-label')"
							:loading="isCityLoading"
							no-filter
							:placeholder="t('search.city-placeholder')"
							prepend-inner-icon="mdi-map-marker"
							return-object
							variant="outlined"
						/>
					</v-col>
					<v-col cols="12" md="3">
						<v-slider
							v-model="radius"
							color="primary"
							hide-details
							:label="t('search.radius-label', { km: radius })"
							:max="100"
							:min="1"
							:step="1"
							thumb-label
						>
							<template #thumb-label="{ modelValue }"> {{ modelValue }} km </template>
						</v-slider>
					</v-col>
					<v-col cols="12" md="2">
						<v-btn
							block
							color="primary"
							:disabled="!keywords || !selectedCity"
							:loading="isSearching"
							size="large"
							type="submit"
						>
							<v-icon start>mdi-magnify</v-icon>
							{{ t('search.search-button') }}
						</v-btn>
					</v-col>
				</v-row>
			</v-form>
		</v-card-text>
	</v-card>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';

import { getDepartmentCodeFromPostcode, getDepartmentName, useNominatim } from '~/composables/useNominatim';
import type { NominatimResult } from '~/composables/useNominatim';

interface Emits {
	(e: 'search', payload: { keywords: string; city: string; radius: number; lat: number; lng: number }): void;
}

interface Props {
	isSearching?: boolean;
}

withDefaults(defineProps<Props>(), { isSearching: false });
const emit = defineEmits<Emits>();
const { t } = useI18n();
const { suggestions, isLoading: isCityLoading, searchAddress } = useNominatim();

const keywords = ref('');
const cityQuery = ref('');
/** What `citySuggestions` builds: a Nominatim hit plus the label shown in the list. */
type CitySuggestion = NominatimResult & { displayName: string };

const selectedCity = ref<CitySuggestion | null>(null);
const radius = ref(20);

const citySuggestions = computed(() => {
	return suggestions.value.map(s => {
		const city = s.address.city || s.address.town || s.address.village || s.address.municipality || s.display_name;
		const deptCode = s.address.postcode ? getDepartmentCodeFromPostcode(s.address.postcode) : undefined;
		const deptName = deptCode ? getDepartmentName(deptCode) : s.address.county;
		return {
			...s,
			displayName: deptName ? `${city}, ${deptName}` : city
		};
	});
});

watch(cityQuery, val => {
	if (val && val.length >= 3 && !selectedCity.value) {
		searchAddress(val);
	}
});

const onSearch = () => {
	if (!keywords.value || !selectedCity.value) return;
	const city =
		selectedCity.value.address.city ||
		selectedCity.value.address.town ||
		selectedCity.value.address.village ||
		selectedCity.value.address.municipality ||
		'';
	emit('search', {
		keywords: keywords.value,
		city,
		radius: radius.value,
		lat: parseFloat(selectedCity.value.lat),
		lng: parseFloat(selectedCity.value.lon)
	});
};
</script>
