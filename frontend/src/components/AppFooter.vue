<template>
	<v-footer class="bg-grey-darken-4 text-white px-0 py-0 mt-0">
		<v-container>
			<v-row>
				<v-col cols="12" md="4" sm="6">
					<div class="d-flex align-center mb-2">
						<v-icon class="mr-2" color="primary" size="28">mdi-radar</v-icon>
						<span class="text-h6 font-weight-bold">Kerflow</span>
					</div>
					<p class="text-body-2 text-grey-lighten-1">
						{{ t('home.hero.subtitle') }}
					</p>
				</v-col>

				<v-col cols="12" md="4" sm="6">
					<p class="text-subtitle-2 font-weight-bold mb-2">{{ t('navigation.dashboard') }}</p>
					<div class="d-flex flex-column">
						<v-btn class="justify-start px-0" color="grey-lighten-1" to="/search" variant="text">
							{{ t('navigation.search') }}
						</v-btn>
						<v-btn class="justify-start px-0" color="grey-lighten-1" to="/prospects" variant="text">
							{{ t('navigation.prospects') }}
						</v-btn>
					</div>
				</v-col>

				<v-col v-if="termsUrl" cols="12" md="4" sm="6">
					<p class="text-subtitle-2 font-weight-bold mb-2">{{ t('footer.terms') }}</p>
					<div class="d-flex flex-column">
						<v-btn
							class="justify-start px-0"
							color="grey-lighten-1"
							:href="termsUrl"
							rel="noopener noreferrer"
							size="small"
							target="_blank"
							variant="text"
						>
							{{ t('footer.terms') }}
						</v-btn>
					</div>
				</v-col>
			</v-row>

			<v-divider class="my-2 border-opacity-25" />

			<v-row>
				<v-col class="text-center text-body-2 text-grey-lighten-1">
					&copy; {{ currentYear }} Kerflow. {{ t('footer.rights') }}
				</v-col>
			</v-row>
		</v-container>
	</v-footer>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n';

import { getTermsUrl } from '~/utils/urlUtils';

const { t } = useI18n();
const currentYear = new Date().getFullYear();
const termsUrl = getTermsUrl();
</script>
