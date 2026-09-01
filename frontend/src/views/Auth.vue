<template>
	<div class="login-container">
		<div class="login-right">
			<v-sheet :class="computedClass" style="background-color: rgb(var(--v-theme-background))">
				<h1 class="logo">{{ $t('app-name') }}</h1>

				<!-- STEP: email -->
				<template v-if="step === 'email'">
					<p class="subtitle">{{ $t('auth.unified.email-step') }}</p>
					<v-form ref="emailForm" v-model="isEmailValid" @submit.prevent="handleEmailSubmit">
						<v-text-field
							v-model="email"
							autofocus
							bg-color="surface"
							:label="$t('common.email')"
							prepend-inner-icon="mdi-email-outline"
							:rules="emailRules"
							variant="outlined"
						/>
						<v-btn
							block
							class="login-btn mb-4"
							color="primary"
							:disabled="loading || !isEmailValid"
							:loading="loading"
							size="large"
							type="submit"
						>
							{{ $t('common.continue') }}
						</v-btn>
					</v-form>
				</template>

				<!-- STEP: signin (email exists, ask password) -->
				<template v-else-if="step === 'signin'">
					<p class="subtitle">{{ $t('auth.unified.signin-step') }}</p>
					<p class="text-body-2 text-center mb-4">{{ email }}</p>
					<v-form ref="signinForm" @submit.prevent="handleSigninSubmit">
						<v-text-field
							v-model="password"
							:append-inner-icon="showPassword ? 'mdi-eye' : 'mdi-eye-off'"
							autofocus
							bg-color="surface"
							:label="$t('common.password')"
							prepend-inner-icon="mdi-lock-outline"
							:rules="[v => !!v || $t('settings.profile.required')]"
							:type="showPassword ? 'text' : 'password'"
							variant="outlined"
							@click:append-inner="showPassword = !showPassword"
						/>
						<v-text-field
							v-if="isBetaEnabled"
							v-model="impersonateEmail"
							bg-color="surface"
							class="mb-2"
							:hint="$t('auth.impersonate.hint')"
							:label="$t('auth.impersonate.label')"
							persistent-hint
							prepend-inner-icon="mdi-account-switch-outline"
							variant="outlined"
						/>
						<v-btn
							block
							class="login-btn mb-4"
							color="primary"
							:disabled="loading"
							:loading="loading"
							size="large"
							type="submit"
						>
							{{ $t('common.login') }}
						</v-btn>
					</v-form>
					<div class="text-center">
						<router-link class="reset-link" to="/reset-password">
							{{ $t('auth.reset-password.title') }}
						</router-link>
					</div>
					<div class="text-center mt-4">
						<a class="text-primary" href="#" @click.prevent="resetToEmail">
							{{ $t('auth.unified.use-different-email') }}
						</a>
					</div>
				</template>

				<!-- STEP: signup (email new, ask box + password) -->
				<template v-else-if="step === 'signup'">
					<p class="subtitle">
						{{ $t('auth.unified.signup-step') }}
					</p>
					<p class="text-body-2 text-center mb-4">{{ email }}</p>
					<v-form ref="signupForm" v-model="isSignupValid" @submit.prevent="handleSignupSubmit">
						<v-text-field
							v-model="password"
							:append-inner-icon="showPassword ? 'mdi-eye' : 'mdi-eye-off'"
							:autofocus="isInvitation"
							bg-color="surface"
							:label="$t('common.password')"
							prepend-inner-icon="mdi-lock-outline"
							:rules="[v => !!v || $t('auth.password-rules.required')]"
							:type="showPassword ? 'text' : 'password'"
							variant="outlined"
							@click:append-inner="showPassword = !showPassword"
						/>
						<password-checklist v-model="isPasswordValid" :password="password" />
						<v-text-field
							v-model="confirmPassword"
							:append-inner-icon="showConfirmPassword ? 'mdi-eye' : 'mdi-eye-off'"
							bg-color="surface"
							:label="$t('auth.confirm-password')"
							prepend-inner-icon="mdi-lock-outline"
							:rules="[
								v => !!v || $t('auth.password-rules.required'),
								v => v === password || $t('auth.password-checklist.match')
							]"
							:type="showConfirmPassword ? 'text' : 'password'"
							variant="outlined"
							@click:append-inner="showConfirmPassword = !showConfirmPassword"
						/>
						<v-btn
							block
							class="login-btn mb-4"
							color="primary"
							:disabled="loading || !isSignupValid || !isPasswordValid"
							:loading="loading"
							size="large"
							type="submit"
						>
							{{ $t('common.button.signup') }}
						</v-btn>
					</v-form>
					<div v-if="!isInvitation" class="text-center mt-4">
						<a class="text-primary" href="#" @click.prevent="resetToEmail">
							{{ $t('auth.unified.use-different-email') }}
						</a>
					</div>
				</template>

				<!-- STEP: otp -->
				<template v-else-if="step === 'otp'">
					<p class="subtitle">{{ $t('auth.unified.otp-step') }}</p>
					<p class="text-body-1 text-center mb-6">
						{{ $t('auth.unified.otp-description', { email }) }}
					</p>
					<v-form @submit.prevent="handleOtpSubmit">
						<div class="otp-container mb-6">
							<v-otp-input v-model="otpCode" :length="6" :loading="loading" plain type="number" />
						</div>
						<v-btn
							block
							class="login-btn mb-4"
							color="primary"
							:disabled="otpCode.length < 6 || loading"
							:loading="loading"
							size="large"
							type="submit"
						>
							{{ $t('auth.verify.submit') }}
						</v-btn>
						<div class="text-center mt-6">
							<p class="text-body-2 mb-2">{{ $t('auth.verify.no-code-received') }}</p>
							<v-btn color="primary" :disabled="resendDisabled || loading" variant="text" @click="handleResend">
								{{ resendDisabled ? `${$t('auth.verify.resend-code-in')} ${resendCountdown}s` : $t('auth.verify.resend') }}
							</v-btn>
						</div>
					</v-form>
				</template>
			</v-sheet>
		</div>
	</div>
</template>

<style scoped>
.otp-container {
	display: flex;
	justify-content: center;
}
.reset-link {
	color: rgb(var(--v-theme-primary));
	text-decoration: none;
}
</style>

<script setup lang="ts">
import PasswordChecklist from '@/components/PasswordChecklist.vue';
import { AxiosResponse } from 'axios';
import { computed, onBeforeUnmount, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { useRoute, useRouter } from 'vue-router';
import { useDisplay } from 'vuetify';
import { toast } from 'vuetify-sonner';

import { checkEmail$, resendVerificationCode$, verifyAccount$ } from '~/api/auth.api';
import { useBetaMode, useTrigger } from '~/composables';
import { useAuthStore } from '~/stores';
import { getApiErrorCode } from '~/utils';

import '@/assets/styles/signin.css';

type Step = 'email' | 'signin' | 'signup' | 'otp';

const route = useRoute();
const router = useRouter();
const { t } = useI18n();
const { mobile } = useDisplay();
const authStore = useAuthStore();
const { trigger, loading } = useTrigger();
const { isBetaEnabled } = useBetaMode();

const step = ref<Step>('email');
const email = ref('');
const password = ref('');
const impersonateEmail = ref('');
const confirmPassword = ref('');
const otpCode = ref('');
const showPassword = ref(false);
const showConfirmPassword = ref(false);
const isEmailValid = ref(false);
const isSignupValid = ref(false);
const isPasswordValid = ref(false);

const invitationCode = ref<string | null>(null);
const isInvitation = computed(() => !!invitationCode.value);

const resendDisabled = ref(false);
const resendCountdown = ref(60);
let countdownInterval: number | null = null;

const computedClass = computed(() => (mobile.value ? '' : 'login-box flex-1-1 pa-6 px-md-10 py-md-12'));

const emailRules = [
	(v: string) => !!v || t('auth.email-rules.required'),
	(v: string) => !v?.includes(' ') || t('auth.email-rules.no-spaces'),
	(v: string) => /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/.test(v) || t('auth.email-rules.ascii-only')
];

const resetToEmail = () => {
	step.value = 'email';
	password.value = '';
	impersonateEmail.value = '';
	confirmPassword.value = '';
	otpCode.value = '';
};

const handleEmailSubmit = () => {
	trigger(checkEmail$(email.value.toLowerCase().trim()), {
		onSuccess: (response: AxiosResponse) => {
			if (response.data.pendingConfirmation) {
				// Account exists but was never OTP-confirmed: skip the password step,
				// go straight to OTP entry and send a fresh code.
				otpCode.value = '';
				step.value = 'otp';
				sendFreshOtp();
			} else {
				step.value = response.data.exists ? 'signin' : 'signup';
			}
		},
		onError: () => {
			toast.error(t('common.errors.unknown'));
		}
	});
};

const sendFreshOtp = () => {
	trigger(resendVerificationCode$({ email: email.value.toLowerCase().trim() }), {
		onSuccess: () => {
			toast.info(t('auth.otp-sent-for-pending'));
			startResendCountdown();
		},
		onError: () => {
			toast.error(t('verify.resend-error'));
		}
	});
};

// Only same-origin app paths: an attacker-supplied absolute URL here would turn the login
// page into an open redirect.
const redirectTarget = (): string => {
	const redirect = route.query.redirect;
	return typeof redirect === 'string' && redirect.startsWith('/') && !redirect.startsWith('//') ? redirect : '/dashboard';
};

const loginAndRedirect = () => {
	const impersonate = impersonateEmail.value.trim().toLowerCase();
	trigger(
		authStore.login({
			username: email.value,
			password: password.value,
			...(impersonate ? { impersonateEmail: impersonate } : {})
		}),
		{
			onSuccess: () => {
				authStore.refreshUserInformation().subscribe({
					next: () => {
						router.push(redirectTarget());
					}
				});
			},
			onError: err => {
				const code = getApiErrorCode(err);
				if (impersonate && code === 'UNAUTHORIZED') {
					toast.error(t('auth.impersonate.not-allowed'));
				} else if (impersonate && code === 'USER_NOT_FOUND') {
					toast.error(t('auth.impersonate.user-not-found'));
				} else {
					toast.error(t('auth.login-error'));
				}
			}
		}
	);
};

const handleSigninSubmit = () => {
	loginAndRedirect();
};

const validateSignupPassword = (): boolean => {
	if (password.value?.includes(' ')) {
		toast.error(t('auth.password-rules.no-spaces'));
		return false;
	}
	if (password.value !== confirmPassword.value) {
		toast.error(t('auth.password-rules.match'));
		return false;
	}
	if (!/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z\d])\S{8,}$/.test(password.value)) {
		toast.error(t('auth.password-rules.complex'));
		return false;
	}
	return true;
};

const handleSignupSubmit = () => {
	if (!validateSignupPassword()) return;
	trigger(
		authStore.signup({
			login: email.value,
			password: password.value
		}),
		{
			onSuccess: () => {
				if (isInvitation.value) {
					// Invitation signup confirms the account immediately (no OTP) — log in directly.
					loginAndRedirect();
				} else {
					step.value = 'otp';
					startResendCountdown();
				}
			},
			onError: err => {
				const code = getApiErrorCode(err);
				if (code === 'INVITATION_CODE_NOT_FOUND' || code === 'USER_NOT_FOUND') {
					toast.error(t('auth.invitation.invalid'));
				} else if (code === 'EMAIL_NOT_WHITELISTED') {
					toast.error(t('early-access.description'));
				} else if (code === 'USER_ALREADY_EXISTS') {
					toast.error(t('auth.user-already-exists'));
				} else if (code === 'USER_PENDING_CONFIRMATION') {
					toast.info(t('auth.user-pending-confirmation'));
					step.value = 'otp';
					startResendCountdown();
				} else {
					toast.error(t('auth.signup-error'));
				}
			}
		}
	);
};

const handleOtpVerifyError = (err: unknown) => {
	const code = getApiErrorCode(err);
	if (code === 'INVALID_VERIFICATION_CODE') {
		toast.error(t('verify.invalid-code'));
	} else if (code === 'VERIFICATION_CODE_EXPIRED') {
		toast.error(t('verify.code-expired'));
	} else {
		toast.error(t('verify.verify-error'));
	}
};

const handleOtpSubmit = () => {
	// Signup flow: the password is already in hand, so verify + log in directly.
	if (password.value) {
		trigger(authStore.verifyAndLogin(email.value, otpCode.value, password.value), {
			onSuccess: () => {
				authStore.refreshUserInformation().subscribe({
					next: () => router.push(redirectTarget())
				});
			},
			onError: handleOtpVerifyError
		});
		return;
	}

	// Login flow on a pending account (email only, no password): just confirm the
	// account, then send the user to the signin step to enter their password.
	trigger(verifyAccount$({ email: email.value.toLowerCase().trim(), verificationCode: otpCode.value }), {
		onSuccess: () => {
			toast.success(t('auth.account-confirmed-signin'));
			otpCode.value = '';
			step.value = 'signin';
		},
		onError: handleOtpVerifyError
	});
};

const handleResend = () => {
	if (resendDisabled.value) return;
	trigger(resendVerificationCode$({ email: email.value }), {
		onSuccess: () => {
			toast.success(t('verify.code-resent'));
			startResendCountdown();
		},
		onError: () => {
			toast.error(t('verify.resend-error'));
		}
	});
};

const startResendCountdown = () => {
	resendDisabled.value = true;
	resendCountdown.value = 60;
	if (countdownInterval) clearInterval(countdownInterval);
	countdownInterval = window.setInterval(() => {
		resendCountdown.value -= 1;
		if (resendCountdown.value <= 0) {
			resendDisabled.value = false;
			if (countdownInterval) clearInterval(countdownInterval);
		}
	}, 1000);
};

onBeforeUnmount(() => {
	if (countdownInterval) clearInterval(countdownInterval);
});
</script>
