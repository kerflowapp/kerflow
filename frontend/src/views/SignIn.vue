<template>
	<div class="login-container">
		<div class="login-right">
			<v-sheet :class="computedClass" style="background-color: rgb(var(--v-theme-background))">
				<div class="text-center mb-4">
					<v-icon color="primary" size="48">mdi-radar</v-icon>
				</div>
				<h1 class="logo">{{ $t('app-name') }}</h1>

				<v-window v-model="step" class="mt-2">
					<!-- Step 1: Email -->
					<v-window-item value="email">
						<p class="subtitle">{{ t('auth.email-step.title') }}</p>
						<p class="text-medium-emphasis text-body-2 text-center mb-4">{{ t('auth.email-step.subtitle') }}</p>
						<v-form @keyup.enter="checkEmail">
							<v-text-field
								v-model="email"
								autofocus
								bg-color="surface"
								:label="t('auth.signin.email')"
								prepend-inner-icon="mdi-email-outline"
								:rules="[v => !!v || t('validation.required')]"
								type="email"
								variant="outlined"
							/>
							<v-btn
								block
								class="login-btn mb-4"
								color="primary"
								:disabled="isCheckingEmail || !email"
								:loading="isCheckingEmail"
								size="large"
								@click="checkEmail"
							>
								{{ t('auth.email-step.submit') }}
							</v-btn>
						</v-form>
						<div v-if="termsUrl" class="text-center text-body-2 mt-4">
							<a class="text-primary" :href="termsUrl" rel="noopener noreferrer" target="_blank">
								{{ t('footer.terms') }}
							</a>
						</div>
					</v-window-item>

					<!-- Step 2a: Signin (user exists) -->
					<v-window-item value="signin">
						<p class="subtitle">{{ t('auth.signin.welcome-back') }}</p>
						<p class="text-medium-emphasis text-body-2 text-center mb-4">{{ email }}</p>
						<v-form @keyup.enter="handleSignin">
							<v-text-field
								v-model="password"
								:append-inner-icon="showPassword ? 'mdi-eye' : 'mdi-eye-off'"
								autofocus
								bg-color="surface"
								:label="t('auth.signin.password')"
								prepend-inner-icon="mdi-lock-outline"
								:rules="[v => !!v || t('validation.required')]"
								:type="showPassword ? 'text' : 'password'"
								variant="outlined"
								@click:append-inner="showPassword = !showPassword"
							/>
							<v-btn
								block
								class="login-btn mb-4"
								color="primary"
								:disabled="isSigningIn || !password"
								:loading="isSigningIn"
								size="large"
								@click="handleSignin"
							>
								{{ t('auth.signin.submit') }}
							</v-btn>
						</v-form>
						<div class="text-center mb-3">
							<router-link class="reset-link" to="/reset-password">{{ t('auth.signin.forgot-password') }}</router-link>
						</div>
						<div class="text-center">
							<v-btn prepend-icon="mdi-arrow-left" size="small" variant="text" @click="goBack">
								{{ t('auth.signin.back') }}
							</v-btn>
						</div>
					</v-window-item>

					<!-- Step 2b: Signup (new user) -->
					<v-window-item value="signup">
						<p class="subtitle">{{ t('auth.signup.new-account') }}</p>
						<p class="text-medium-emphasis text-body-2 text-center mb-4">{{ email }}</p>
						<v-form v-model="isFormValid" @keyup.enter="handleSignup">
							<v-row class="ma-0">
								<v-col class="pa-0 pr-sm-2" cols="12" sm="6">
									<v-text-field
										v-model="firstName"
										bg-color="surface"
										:label="t('auth.signup.first-name')"
										prepend-inner-icon="mdi-account-outline"
										:rules="[v => !!v || t('validation.required')]"
										variant="outlined"
									/>
								</v-col>
								<v-col class="pa-0 pl-sm-2" cols="12" sm="6">
									<v-text-field
										v-model="lastName"
										bg-color="surface"
										:label="t('auth.signup.last-name')"
										prepend-inner-icon="mdi-account-outline"
										:rules="[v => !!v || t('validation.required')]"
										variant="outlined"
									/>
								</v-col>
							</v-row>

							<v-text-field
								v-model="phoneNumber"
								bg-color="surface"
								:label="t('auth.signup.phone')"
								prepend-inner-icon="mdi-phone-outline"
								variant="outlined"
							/>

							<v-text-field
								v-model="password"
								:append-inner-icon="showPassword ? 'mdi-eye' : 'mdi-eye-off'"
								bg-color="surface"
								:label="t('auth.signup.password')"
								prepend-inner-icon="mdi-lock-outline"
								:rules="[v => !!v || t('validation.required')]"
								:type="showPassword ? 'text' : 'password'"
								variant="outlined"
								@click:append-inner="showPassword = !showPassword"
							/>

							<password-checklist v-model="isPasswordValid" :password="password" />

							<v-text-field
								v-model="confirmPassword"
								:append-inner-icon="showConfirmPassword ? 'mdi-eye' : 'mdi-eye-off'"
								bg-color="surface"
								:label="t('auth.signup.confirm-password')"
								prepend-inner-icon="mdi-lock-outline"
								:rules="[v => !!v || t('validation.required'), v => v === password || t('validation.required')]"
								:type="showConfirmPassword ? 'text' : 'password'"
								variant="outlined"
								@click:append-inner="showConfirmPassword = !showConfirmPassword"
							/>

							<v-btn
								block
								class="login-btn mb-4"
								color="primary"
								:disabled="isSigningUp || !isFormValid || !isPasswordValid"
								:loading="isSigningUp"
								size="large"
								@click="handleSignup"
							>
								{{ t('auth.signup.submit') }}
							</v-btn>
						</v-form>

						<div class="text-center">
							<v-btn prepend-icon="mdi-arrow-left" size="small" variant="text" @click="goBack">
								{{ t('auth.signup.back') }}
							</v-btn>
						</div>

						<div v-if="termsUrl" class="text-center text-body-2 mt-4">
							<a class="text-primary" :href="termsUrl" rel="noopener noreferrer" target="_blank">
								{{ t('footer.terms') }}
							</a>
						</div>
					</v-window-item>

					<!-- Step 3: OTP verification -->
					<v-window-item value="otp">
						<p class="subtitle">{{ t('auth.verify.title') }}</p>
						<p class="text-medium-emphasis text-body-2 text-center mb-6">{{ email }}</p>

						<div class="d-flex justify-center mb-6">
							<v-otp-input
								v-model="otpCode"
								:length="6"
								:loading="isVerifyingOtp"
								plain
								type="number"
								@finish="handleVerifyOtp"
							/>
						</div>

						<v-btn
							block
							class="login-btn mb-4"
							color="primary"
							:disabled="isVerifyingOtp || otpCode.length < 6"
							:loading="isVerifyingOtp"
							size="large"
							@click="handleVerifyOtp"
						>
							{{ t('auth.verify.submit') }}
						</v-btn>

						<div class="text-center mt-4">
							<v-btn
								color="primary"
								:disabled="resendDisabled || isResendingOtp"
								:loading="isResendingOtp"
								size="small"
								variant="text"
								@click="handleResendOtp"
							>
								{{ resendDisabled ? `${t('auth.verify.resend')} (${resendCountdown}s)` : t('auth.verify.resend') }}
							</v-btn>
						</div>

						<div class="text-center mt-2">
							<v-btn prepend-icon="mdi-arrow-left" size="small" variant="text" @click="goBack">
								{{ t('auth.signup.back') }}
							</v-btn>
						</div>
					</v-window-item>
				</v-window>
			</v-sheet>
		</div>
	</div>
</template>

<script setup lang="ts">
import PasswordChecklist from '@/components/PasswordChecklist.vue';
import { computed, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { useDisplay } from 'vuetify';

import { useAuthFlow } from '~/composables';
import { getTermsUrl } from '~/utils';

import '@/assets/styles/signin.css';

const { t } = useI18n();
const { mobile } = useDisplay();
const termsUrl = getTermsUrl();

const {
	step,
	email,
	password,
	confirmPassword,
	firstName,
	lastName,
	phoneNumber,
	isPasswordValid,
	otpCode,
	resendDisabled,
	resendCountdown,
	isCheckingEmail,
	isSigningIn,
	isSigningUp,
	isVerifyingOtp,
	isResendingOtp,
	checkEmail,
	handleSignin,
	handleSignup,
	handleVerifyOtp,
	handleResendOtp,
	goBack
} = useAuthFlow();

const showPassword = ref(false);
const showConfirmPassword = ref(false);
const isFormValid = ref(false);

const computedClass = computed(() => (mobile.value ? '' : 'login-box flex-1-1 pa-6 px-md-10 py-md-12'));
</script>
