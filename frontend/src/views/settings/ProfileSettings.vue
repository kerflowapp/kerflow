<template>
	<div>
		<h3 class="text-h6 font-weight-bold mb-6">{{ t('settings.profile.title') }}</h3>

		<v-form ref="formRef" @submit.prevent="submitUpdateProfile">
			<v-card elevation="0">
				<v-card-title class="text-subtitle-1 font-weight-bold py-3 px-4 bg-grey-lighten-4">
					{{ t('settings.profile.personal-info') }}
				</v-card-title>
				<v-card-text class="pa-4">
					<v-row>
						<v-col cols="12" md="6">
							<v-text-field
								v-model="form.firstName"
								density="comfortable"
								:label="t('settings.profile.first-name')"
								:rules="requiredRule"
								variant="outlined"
							/>
						</v-col>
						<v-col cols="12" md="6">
							<v-text-field
								v-model="form.lastName"
								density="comfortable"
								:label="t('settings.profile.last-name')"
								:rules="requiredRule"
								variant="outlined"
							/>
						</v-col>
						<v-col cols="12" md="6">
							<v-text-field
								v-model="form.phoneNumber"
								density="comfortable"
								:label="t('settings.profile.phone-number')"
								variant="outlined"
							/>
						</v-col>
						<v-col cols="12" md="6">
							<v-text-field
								v-model="loginField"
								density="comfortable"
								disabled
								:label="t('settings.profile.email')"
								variant="outlined"
							/>
						</v-col>
					</v-row>

					<v-row class="mt-6">
						<v-col class="d-flex justify-end" cols="12">
							<v-btn color="primary" :loading="isUpdating" type="submit">
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
import { computed, reactive } from 'vue';
import { useI18n } from 'vue-i18n';
import { toast } from '~/composables/useToast';

import { updateUser$ } from '~/api/users.api';
import { useAuth } from '~/composables/useAuth';
import { useTrigger } from '~/composables/useTrigger';

const { t } = useI18n();
const { user } = useAuth();
const { trigger, loading: isUpdating } = useTrigger();

const form = reactive({
	firstName: '',
	lastName: '',
	phoneNumber: ''
});

const loginField = computed(() => user.value?.login || user.value?.email || '');

const requiredRule = [(v: string) => !!v || t('validation.required')];

const submitUpdateProfile = () => {
	if (!user.value?.id) return;
	trigger(updateUser$(user.value.id, form.firstName, form.lastName, form.phoneNumber), {
		onSuccess: () => {
			toast.success(t('success.profile-updated'));
		},
		onError: () => {
			toast.error(t('errors.update-profile-failed'));
		}
	});
};

watchImmediate(user, newUser => {
	if (newUser) {
		form.firstName = newUser.firstName || '';
		form.lastName = newUser.lastName || '';
		form.phoneNumber = newUser.phoneNumber || '';
	}
});
</script>
