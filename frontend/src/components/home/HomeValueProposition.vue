<template>
	<div class="py-12">
		<h2 class="text-h4 font-weight-bold text-center mb-2">{{ t('home.value.title') }}</h2>
		<p class="text-body-1 text-grey-darken-1 text-center mb-8">{{ t('home.value.subtitle') }}</p>

		<v-row>
			<v-col v-for="step in steps" :key="step.titleKey" cols="12" md="4">
				<v-card class="text-center pa-6 h-100" elevation="0">
					<v-avatar class="mb-4" :color="step.color" size="64">
						<v-icon color="white" :icon="step.icon" size="32" />
					</v-avatar>
					<div class="text-h6 font-weight-bold mb-2">{{ t(step.titleKey) }}</div>
					<div class="text-body-2 text-grey-darken-1">{{ t(step.descriptionKey) }}</div>
				</v-card>
			</v-col>
		</v-row>
	</div>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n';

const { t } = useI18n();

const steps = [
	{
		icon: 'mdi-magnify',
		color: 'primary',
		titleKey: 'home.value.step-1-title',
		descriptionKey: 'home.value.step-1-description'
	},
	{
		icon: 'mdi-account-plus',
		color: 'secondary',
		titleKey: 'home.value.step-2-title',
		descriptionKey: 'home.value.step-2-description'
	},
	{
		icon: 'mdi-view-column',
		color: 'success',
		titleKey: 'home.value.step-3-title',
		descriptionKey: 'home.value.step-3-description'
	}
];
</script>
