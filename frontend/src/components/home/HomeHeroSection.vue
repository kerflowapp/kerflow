<template>
	<div class="hero-section py-16 text-center">
		<v-container>
			<v-icon class="mb-6" color="primary" size="80">mdi-radar</v-icon>
			<h1 class="text-h3 font-weight-bold mb-4 text-primary">
				{{ t('home.hero.title') }}
			</h1>
			<p class="text-h6 text-grey-darken-1 mb-8 mx-auto" style="max-width: 600px">
				{{ t('home.hero.subtitle') }}
			</p>
			<div class="d-flex justify-center ga-4 flex-wrap">
				<v-btn color="primary" prepend-icon="mdi-rocket-launch" size="x-large" to="/signup" variant="flat">
					{{ t('home.hero.cta-primary') }}
				</v-btn>
				<v-btn color="primary" prepend-icon="mdi-login" size="x-large" to="/signin" variant="outlined">
					{{ t('home.hero.cta-secondary') }}
				</v-btn>
			</div>
		</v-container>
	</div>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n';

const { t } = useI18n();
</script>
