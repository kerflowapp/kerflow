<template>
	<div class="login-container">
		<div class="login-right">
			<v-sheet :class="computedClass" style="background-color: rgb(var(--v-theme-background))">
				<div class="text-center mb-4">
					<v-icon color="primary" size="48">mdi-radar</v-icon>
				</div>
				<h1 class="logo">{{ $t('app-name') }}</h1>
				<p class="subtitle">{{ t('auth.reset-password.title') }}</p>

				<div v-if="!confirmationCode">
					<v-form @submit.prevent="doSubmit">
						<v-text-field
							v-model="email"
							class="mb-6"
							:label="t('auth.reset-password.email')"
							prepend-inner-icon="mdi-email-outline"
							:rules="[v => !!v || t('validation.required')]"
							type="email"
							variant="outlined"
						/>

						<v-btn
							block
							class="login-btn mb-6"
							color="primary"
							:disabled="loading || !email"
							:loading="loading"
							size="large"
							type="submit"
						>
							{{ t('auth.reset-password.submit') }}
						</v-btn>
					</v-form>
				</div>

				<div v-else>
					<v-form @submit.prevent="doConfirm">
						<v-text-field
							v-model="email"
							class="mb-4"
							:disabled="true"
							:label="t('auth.reset-password.email')"
							prepend-inner-icon="mdi-email-outline"
							type="email"
							variant="outlined"
						/>
						<v-text-field
							v-model="newPassword"
							:append-inner-icon="showPassword ? 'mdi-eye' : 'mdi-eye-off'"
							class="mb-4"
							:label="t('auth.reset-password.new-password')"
							prepend-inner-icon="mdi-lock-outline"
							:rules="[v => !!v || t('validation.required')]"
							:type="showPassword ? 'text' : 'password'"
							variant="outlined"
							@click:append-inner="showPassword = !showPassword"
						/>

						<v-btn
							block
							class="login-btn mb-6"
							color="primary"
							:disabled="loading || !newPassword"
							:loading="loading"
							size="large"
							type="submit"
						>
							{{ t('common.confirm') }}
						</v-btn>
					</v-form>
				</div>

				<div class="text-center">
					<router-link class="text-primary" to="/signin">
						{{ t('auth.signin.title') }}
					</router-link>
				</div>
			</v-sheet>
		</div>
	</div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { useRouter } from 'vue-router';
import { useDisplay } from 'vuetify';
import { toast } from 'vuetify-sonner';

import { UpdatePasswordAction } from '~/api/dtos/auth.dto';
import { useTrigger } from '~/composables/useTrigger';
import { useUrlParams } from '~/composables/useUrlParams';
import { useAuthStore } from '~/stores/auth.store';

import '@/assets/styles/signin.css';

const { updatePassword } = useAuthStore();
const { t } = useI18n();
const { mobile } = useDisplay();
const { getParam } = useUrlParams();
const confirmationCode = getParam('code');
const loginParam = getParam('login');
const router = useRouter();
const { trigger, loading } = useTrigger();
const showPassword = ref(false);

const email = ref(loginParam.value || '');
const newPassword = ref('');

const computedClass = computed(() => (mobile.value ? '' : 'login-box flex-1-1 pa-6 px-md-10 py-md-12'));

const doSubmit = () => {
	if (!email.value) return;
	trigger(updatePassword({ action: UpdatePasswordAction.RESET, email: email.value.toLowerCase().trim() }), {
		onSuccess: () => {
			toast.success(t('success.profile-updated'));
			router.push('/signin');
		}
	});
};

const doConfirm = () => {
	if (!newPassword.value || !email.value) return;
	trigger(
		updatePassword({
			action: UpdatePasswordAction.CONFIRM,
			email: email.value.toLowerCase().trim(),
			password: newPassword.value,
			confirmationCode: confirmationCode.value
		}),
		{
			onSuccess: () => {
				toast.success(t('success.profile-updated'));
				router.push('/signin');
			}
		}
	);
};
</script>
