<template>
	<div>
		<h3 class="text-h6 font-weight-bold mb-6">{{ t('settings.prospecting.title') }}</h3>

		<v-form ref="formRef" @submit.prevent="submit">
			<v-card elevation="0">
				<v-card-title class="text-subtitle-1 font-weight-bold py-3 px-4 bg-grey-lighten-4">
					{{ t('settings.prospecting.follow-ups') }}
				</v-card-title>
				<v-card-text class="pa-4">
					<v-row>
						<v-col cols="12" md="6">
							<v-text-field
								v-model.number="followUpDelayDays"
								density="comfortable"
								:hint="t('settings.prospecting.follow-up-delay-hint')"
								:label="t('settings.prospecting.follow-up-delay')"
								:max="MAX_FOLLOW_UP_DELAY_DAYS"
								:min="MIN_FOLLOW_UP_DELAY_DAYS"
								persistent-hint
								prepend-inner-icon="mdi-bell-ring-outline"
								:rules="delayRules"
								:suffix="t('settings.prospecting.days-suffix')"
								type="number"
								variant="outlined"
							/>
						</v-col>
					</v-row>

					<v-row class="mt-6">
						<v-col class="d-flex justify-end" cols="12">
							<v-btn color="primary" :loading="isSaving" type="submit">
								{{ t('common.save') }}
							</v-btn>
						</v-col>
					</v-row>
				</v-card-text>
			</v-card>
		</v-form>
	</div>
</template>

<script setup lang="ts">
import { watchImmediate } from '@vueuse/core';
import { ref } from 'vue';
import { useI18n } from 'vue-i18n';
import type { VForm } from 'vuetify/components';

import { useAuth } from '~/composables/useAuth';
import { useFollowUps } from '~/composables/useFollowUps';
import { usePreferences } from '~/composables/usePreferences';
import { MAX_FOLLOW_UP_DELAY_DAYS, MIN_FOLLOW_UP_DELAY_DAYS } from '~/utils/follow-up';

const { t } = useI18n();
const { user } = useAuth();
const { delayDays } = useFollowUps();
const { isSaving, saveFollowUpDelay } = usePreferences();

const formRef = ref<VForm>();
const followUpDelayDays = ref<number>(delayDays.value);

const delayRules = [
	(v: number) =>
		(Number.isInteger(v) && v >= MIN_FOLLOW_UP_DELAY_DAYS && v <= MAX_FOLLOW_UP_DELAY_DAYS) ||
		t('settings.prospecting.delay-range', { min: MIN_FOLLOW_UP_DELAY_DAYS, max: MAX_FOLLOW_UP_DELAY_DAYS })
];

const submit = async () => {
	const { valid } = (await formRef.value?.validate()) ?? { valid: false };
	if (valid) saveFollowUpDelay(followUpDelayDays.value);
};

watchImmediate(user, () => {
	followUpDelayDays.value = delayDays.value;
});
</script>
