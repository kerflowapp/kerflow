import { onUnmounted, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { useRouter } from 'vue-router';
import { toast } from 'vuetify-sonner';

import { checkEmail$, resendVerificationCode$, signup$, verifyAccount$ } from '~/api/auth.api';
import { useAuthStore } from '~/stores/auth.store';

import { useTrigger } from './useTrigger';

export type AuthStep = 'email' | 'signin' | 'signup' | 'otp';

export function useAuthFlow() {
	const router = useRouter();
	const { t } = useI18n();
	const authStore = useAuthStore();

	const step = ref<AuthStep>('email');
	const email = ref('');
	const password = ref('');
	const confirmPassword = ref('');
	const firstName = ref('');
	const lastName = ref('');
	const phoneNumber = ref('');
	const isPasswordValid = ref(false);
	const otpCode = ref('');

	// Resend countdown
	const resendDisabled = ref(false);
	const resendCountdown = ref(60);
	let countdownInterval: ReturnType<typeof setInterval> | null = null;

	const { trigger: triggerCheckEmail, loading: isCheckingEmail } = useTrigger();
	const { trigger: triggerSignin, loading: isSigningIn } = useTrigger();
	const { trigger: triggerSignup, loading: isSigningUp } = useTrigger();
	const { trigger: triggerVerifyOtp, loading: isVerifyingOtp } = useTrigger();
	const { trigger: triggerResendOtp, loading: isResendingOtp } = useTrigger();

	const startResendCountdown = () => {
		resendDisabled.value = true;
		resendCountdown.value = 60;
		countdownInterval = setInterval(() => {
			resendCountdown.value -= 1;
			if (resendCountdown.value <= 0) {
				resendDisabled.value = false;
				clearInterval(countdownInterval!);
			}
		}, 1000);
	};

	const checkEmail = () => {
		if (!email.value) return;

		triggerCheckEmail(checkEmail$(email.value.toLowerCase().trim()), {
			onSuccess: response => {
				const data = (response as { data: { exists: boolean; pendingConfirmation?: boolean } }).data;
				if (data.pendingConfirmation) {
					// Account exists but was never OTP-confirmed: skip the password step,
					// go straight to OTP entry and send a fresh code.
					otpCode.value = '';
					step.value = 'otp';
					sendFreshOtp();
				} else {
					step.value = data.exists ? 'signin' : 'signup';
				}
			},
			onError: () => {
				toast.error(t('auth.errors.check-email-failed'));
			}
		});
	};

	const sendFreshOtp = () => {
		triggerResendOtp(resendVerificationCode$({ email: email.value.toLowerCase().trim() }), {
			onSuccess: () => {
				toast.info(t('auth.verify.otp-sent-pending'));
				startResendCountdown();
			},
			onError: () => {
				toast.error(t('auth.errors.resend-failed'));
			}
		});
	};

	const handleSignin = () => {
		triggerSignin(authStore.login({ username: email.value.toLowerCase().trim(), password: password.value }), {
			onSuccess: () => {
				authStore.refreshUserInformation().subscribe({
					next: () => {
						router.push('/dashboard');
					}
				});
			},
			onError: () => {
				toast.error(t('auth.errors.signin-failed'));
			}
		});
	};

	const handleSignup = () => {
		if (password.value !== confirmPassword.value) {
			toast.error(t('validation.required'));
			return;
		}

		triggerSignup(
			signup$({
				login: email.value.toLowerCase().trim(),
				password: password.value,
				firstName: firstName.value,
				lastName: lastName.value,
				phoneNumber: phoneNumber.value
			}),
			{
				onSuccess: () => {
					otpCode.value = '';
					step.value = 'otp';
					startResendCountdown();
				},
				onError: () => {
					toast.error(t('auth.errors.signup-failed'));
				}
			}
		);
	};

	const handleVerifyOtp = () => {
		const normalizedEmail = email.value.toLowerCase().trim();

		// Signup flow: the password is already in hand, so verify + log in directly.
		if (password.value) {
			triggerVerifyOtp(authStore.verifyAndLogin(normalizedEmail, otpCode.value, password.value), {
				onSuccess: () => {
					authStore.refreshUserInformation().subscribe({
						next: () => {
							router.push('/dashboard');
						}
					});
				},
				onError: () => {
					toast.error(t('auth.errors.otp-failed'));
				}
			});
			return;
		}

		// Pending account from signin (email only, no password): just confirm the
		// account, then send the user to the signin step to enter their password.
		triggerVerifyOtp(verifyAccount$({ email: normalizedEmail, verificationCode: otpCode.value }), {
			onSuccess: () => {
				toast.success(t('auth.signin.account-confirmed'));
				otpCode.value = '';
				step.value = 'signin';
			},
			onError: () => {
				toast.error(t('auth.errors.otp-failed'));
			}
		});
	};

	const handleResendOtp = () => {
		if (resendDisabled.value) return;

		triggerResendOtp(resendVerificationCode$({ email: email.value.toLowerCase().trim() }), {
			onSuccess: () => {
				toast.success(t('auth.verify.resend-success'));
				startResendCountdown();
			},
			onError: () => {
				toast.error(t('auth.errors.resend-failed'));
			}
		});
	};

	const goBack = () => {
		step.value = 'email';
		password.value = '';
		confirmPassword.value = '';
		otpCode.value = '';
		if (countdownInterval) clearInterval(countdownInterval);
	};

	onUnmounted(() => {
		if (countdownInterval) clearInterval(countdownInterval);
	});

	return {
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
	};
}
