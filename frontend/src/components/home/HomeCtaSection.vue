<template>
	<div class="py-12 text-center bg-accent rounded-lg my-8 pa-8">
		<h2 class="text-h4 font-weight-bold mb-4">{{ t('home.cta.title') }}</h2>
		<p class="text-body-1 text-grey-darken-1 mb-6 mx-auto" style="max-width: 500px">
			{{ t('home.cta.description') }}
		</p>
		<v-btn color="primary" prepend-icon="mdi-rocket-launch" size="x-large" to="/signup" variant="flat">
			{{ t('home.cta.button') }}
		</v-btn>
	</div>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n';

const { t } = useI18n();
</script>
