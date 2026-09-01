<template>
	<v-card class="h-100" elevation="0">
		<v-card-text class="d-flex align-center pa-4">
			<v-avatar class="mr-4" :color="color" size="48">
				<v-icon color="white" :icon="icon" size="24" />
			</v-avatar>
			<div>
				<div class="text-h5 font-weight-bold">{{ value }}</div>
				<div class="text-body-2 text-grey-darken-1">{{ label }}</div>
			</div>
		</v-card-text>
	</v-card>
</template>

<script setup lang="ts">
interface Props {
	icon: string;
	color: string;
	value: string | number;
	label: string;
}

defineProps<Props>();
</script>
