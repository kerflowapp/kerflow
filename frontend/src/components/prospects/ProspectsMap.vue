<template>
	<div class="prospects-map-container">
		<l-map ref="mapRef" :center="mapCenter" :use-global-leaflet="false" :zoom="zoom" @ready="onMapReady">
			<l-tile-layer
				attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
				layer-type="base"
				url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
			/>

			<l-marker
				v-for="(prospect, index) in mappableProspects"
				:key="prospect.id"
				:lat-lng="[prospect.lat, prospect.lng]"
				@click="emit('select', prospect.id)"
				@mouseout="emit('hover', null)"
				@mouseover="emit('hover', prospect.id)"
			>
				<l-icon
					class-name="custom-marker-icon"
					:icon-anchor="highlightedId === prospect.id ? [16, 16] : [13, 13]"
					:icon-size="highlightedId === prospect.id ? [32, 32] : [26, 26]"
				>
					<div class="marker-number" :class="{ 'marker-highlighted': highlightedId === prospect.id }">
						{{ index + 1 }}
					</div>
				</l-icon>
				<l-popup>
					<div class="map-popup">
						<div class="font-weight-bold text-body-2">{{ prospect.name }}</div>
						<div v-if="prospect.address" class="text-caption text-grey mt-1">{{ prospect.address }}</div>
					</div>
				</l-popup>
			</l-marker>
		</l-map>
	</div>
</template>

<style scoped>
.prospects-map-container {
	width: 100%;
	height: 100%;
	min-height: 520px;
	border-radius: 8px;
	overflow: hidden;
}

.prospects-map-container :deep(.leaflet-container) {
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
import { LIcon, LMap, LMarker, LPopup, LTileLayer } from '@vue-leaflet/vue-leaflet';
import type { LatLngBoundsExpression, Map as LeafletMap } from 'leaflet';
import { computed, nextTick, ref, watch } from 'vue';

import type { ProspectDto } from '~/api/dtos/prospect.dto';

import 'leaflet/dist/leaflet.css';

interface Props {
	prospects: ProspectDto[];
	highlightedId?: string | null;
}

interface Emits {
	(e: 'hover', prospectId: string | null): void;
	(e: 'select', prospectId: string): void;
}

const props = withDefaults(defineProps<Props>(), {
	highlightedId: null
});

const emit = defineEmits<Emits>();

const leafletMap = ref<LeafletMap | null>(null);
const zoom = ref(12);

const mappableProspects = computed(() =>
	props.prospects.filter((p): p is ProspectDto & { lat: number; lng: number } => typeof p.lat === 'number' && typeof p.lng === 'number')
);

const mapCenter = computed<[number, number]>(() => {
	const first = mappableProspects.value[0];
	return first ? [first.lat, first.lng] : [46.603354, 1.888334];
});

const onMapReady = (map: LeafletMap) => {
	leafletMap.value = map;
	fitBounds();
};

const fitBounds = () => {
	if (!leafletMap.value || mappableProspects.value.length === 0) return;
	const latLngs = mappableProspects.value.map(p => [p.lat, p.lng] as [number, number]);
	leafletMap.value.fitBounds(latLngs as LatLngBoundsExpression, { padding: [40, 40] });
};

watch(
	() => mappableProspects.value.map(p => p.id).join(','),
	() => {
		nextTick(() => fitBounds());
	}
);
</script>
