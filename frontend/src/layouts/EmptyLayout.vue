<template>
	<v-app>
		<v-layout>
			<v-main class="custom">
				<v-btn
					v-if="isBetaEnabled"
					class="ms-2"
					color="secondary"
					icon="mdi-rocket"
					:title="$t('beta.mode')"
					@click="toggleBeta"
				></v-btn>
				<router-view />
			</v-main>
		</v-layout>
	</v-app>
</template>

<style>
.custom {
	align-items: center;
}
</style>

<script lang="ts" setup>
import { RouterView } from 'vue-router';

import { useBetaMode } from '~/composables';

const { isBetaEnabled, toggleBeta } = useBetaMode();
</script>
