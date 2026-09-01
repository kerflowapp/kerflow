<template>
	<div class="search-map-container">
		<l-map ref="mapRef" :center="mapCenter" :use-global-leaflet="false" :zoom="zoom" @ready="onMapReady">
			<l-tile-layer
				attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
				layer-type="base"
				url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
			/>

			<l-circle
				v-if="center"
				:color="'rgb(var(--v-theme-primary))'"
				:dash-array="'6, 6'"
				:fill-color="'rgb(var(--v-theme-primary))'"
				:fill-opacity="0.08"
				:lat-lng="[center.lat, center.lng]"
				:radius="radiusMeters"
				:weight="1.5"
			/>

			<l-marker
				v-for="(result, index) in results"
				:key="result.placeId"
				:lat-lng="[result.lat, result.lng]"
				@click="emit('select', result.placeId)"
				@mouseout="emit('hover', null)"
				@mouseover="emit('hover', result.placeId)"
			>
				<l-icon
					class-name="custom-marker-icon"
					:icon-anchor="highlightedId === result.placeId ? [16, 16] : [13, 13]"
					:icon-size="highlightedId === result.placeId ? [32, 32] : [26, 26]"
				>
					<div class="marker-number" :class="{ 'marker-highlighted': highlightedId === result.placeId }">
						{{ index + 1 }}
					</div>
				</l-icon>
				<l-popup>
					<div class="map-popup">
						<div class="font-weight-bold text-body-2">{{ result.name }}</div>
						<div v-if="result.rating" class="d-flex align-center ga-1 mt-1 text-caption">
							<span>{{ result.rating }}</span>
							<span class="text-amber">&#9733;</span>
							<span class="text-grey">({{ result.userRatingsTotal }})</span>
						</div>
						<div v-if="result.address" class="text-caption text-grey mt-1">{{ result.address }}</div>
					</div>
				</l-popup>
			</l-marker>
		</l-map>
	</div>
</template>

<style scoped>
.search-map-container {
	width: 100%;
	height: 100%;
	min-height: 400px;
	border-radius: 8px;
	overflow: hidden;
}

.search-map-container :deep(.leaflet-container) {
	width: 100%;
	height: 100%;
	border-radius: 8px;
}

.marker-number {
	display: flex;
	align-items: center;
	justify-content: center;
	width: 26px;
	height: 26px;
	border-radius: 50%;
	background: rgb(var(--v-theme-primary));
	color: white;
	font-size: 12px;
	font-weight: 700;
	border: 2px solid white;
	box-shadow: 0 2px 6px rgba(0, 0, 0, 0.3);
	transition: all 0.15s ease;
}

.marker-highlighted {
	width: 32px;
	height: 32px;
	font-size: 14px;
	background: rgb(var(--v-theme-secondary));
	box-shadow: 0 3px 10px rgba(0, 0, 0, 0.4);
}

.map-popup {
	min-width: 160px;
	max-width: 240px;
}
</style>

<script setup lang="ts">
import { LCircle, LIcon, LMap, LMarker, LPopup, LTileLayer } from '@vue-leaflet/vue-leaflet';
import type { LatLngBoundsExpression, Map as LeafletMap } from 'leaflet';
import { computed, nextTick, ref, watch } from 'vue';

import type { SearchResultDto } from '~/api/dtos/search.dto';

import 'leaflet/dist/leaflet.css';

interface Props {
	results: SearchResultDto[];
	center?: { lat: number; lng: number } | null;
	radius?: number;
	highlightedId?: string | null;
}

interface Emits {
	(e: 'hover', placeId: string | null): void;
	(e: 'select', placeId: string): void;
}

const props = withDefaults(defineProps<Props>(), {
	center: null,
	radius: 20,
	highlightedId: null
});

const emit = defineEmits<Emits>();

const leafletMap = ref<LeafletMap | null>(null);
const zoom = ref(12);
const mapCenter = computed<[number, number]>(() => (props.center ? [props.center.lat, props.center.lng] : [46.603354, 1.888334]));
const radiusMeters = computed(() => (props.radius ?? 20) * 1000);

const onMapReady = (map: LeafletMap) => {
	leafletMap.value = map;
	fitBounds();
};

const fitBounds = () => {
	if (!leafletMap.value || props.results.length === 0) return;

	const latLngs = props.results.map(r => [r.lat, r.lng] as [number, number]);
	if (props.center) {
		latLngs.push([props.center.lat, props.center.lng]);
	}
	leafletMap.value.fitBounds(latLngs as LatLngBoundsExpression, { padding: [40, 40] });
};

watch(
	() => props.results,
	() => {
		nextTick(() => fitBounds());
	}
);

watch(
	() => props.center,
	newCenter => {
		if (newCenter && leafletMap.value) {
			leafletMap.value.setView([newCenter.lat, newCenter.lng], zoom.value);
		}
	}
);
</script>
