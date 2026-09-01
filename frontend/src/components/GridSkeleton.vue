<template>
	<v-row v-model="isLoading">
		<v-col cols="12">
			<v-skeleton-loader type="list-item"></v-skeleton-loader>
		</v-col>
		<v-col cols="12" lg="3" md="6">
			<v-skeleton-loader type="card-avatar, actions"></v-skeleton-loader>
		</v-col>
		<v-col cols="12" lg="3" md="6">
			<v-skeleton-loader type="card-avatar, actions"></v-skeleton-loader>
		</v-col>
		<v-col cols="12" lg="3" md="6">
			<v-skeleton-loader type="card-avatar, actions"></v-skeleton-loader>
		</v-col>
		<v-col cols="12" lg="3" md="6">
			<v-skeleton-loader type="card-avatar, actions"></v-skeleton-loader>
		</v-col>
	</v-row>
</template>

<script setup lang="ts">
const isLoading = defineModel<boolean>();
</script>
