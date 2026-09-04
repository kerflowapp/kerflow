<template>
	<div class="login-container">
		<div class="login-right">
			<v-sheet :class="computedClass" style="background-color: rgb(var(--v-theme-background))">
				<div class="text-center mb-4">
					<v-icon color="primary" size="48">mdi-radar</v-icon>
				</div>
				<h1 class="logo">{{ $t('app-name') }}</h1>
				<p class="subtitle">{{ t('auth.verify.title') }}</p>

				<v-form ref="form" @submit.prevent="handleVerify">
					<div class="d-flex justify-center mb-6">
						<v-otp-input v-model="otpCode" :length="6" :loading="loading" plain type="number" />
					</div>

					<v-alert v-if="errorMessage" class="mb-4" closable type="error" variant="tonal" @click:close="errorMessage = ''">
						{{ errorMessage }}
					</v-alert>

					<v-alert v-if="successMessage" class="mb-4" closable type="success" variant="tonal" @click:close="successMessage = ''">
						{{ successMessage }}
					</v-alert>

					<v-btn
						block
						class="login-btn mb-4"
						color="primary"
						:disabled="otpCode.length < 6"
						:loading="loading"
						size="large"
						type="submit"
					>
						{{ t('auth.verify.submit') }}
					</v-btn>

					<div class="text-center mt-6">
						<v-btn color="primary" :disabled="resendDisabled || loading" variant="text" @click="handleResendCode">
							{{ resendDisabled ? `${t('auth.verify.resend')} (${resendCountdown}s)` : t('auth.verify.resend') }}
						</v-btn>
					</div>

					<div class="text-center mt-4">
						<router-link class="text-primary" to="/signin">
							{{ t('auth.signin.title') }}
						</router-link>
					</div>
				</v-form>
			</v-sheet>
		</div>
	</div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted } from 'vue';
import { useI18n } from 'vue-i18n';
import { useDisplay } from 'vuetify';

import { useVerification } from '~/composables/useVerification';

import '@/assets/styles/signin.css';

const { t } = useI18n();
const { mobile } = useDisplay();

const {
	otpCode,
	loading,
	errorMessage,
	successMessage,
	resendDisabled,
	resendCountdown,
	verifyAccount,
	resendVerificationCode,
	initVerification,
	cleanupVerification
} = useVerification();

const computedClass = computed(() => (mobile.value ? '' : 'login-box flex-1-1 pa-6 px-md-10 py-md-12'));

const handleVerify = () => {
	verifyAccount();
};

const handleResendCode = () => {
	resendVerificationCode();
};

onMounted(() => {
	initVerification();
});

onBeforeUnmount(() => {
	cleanupVerification();
});
</script>
