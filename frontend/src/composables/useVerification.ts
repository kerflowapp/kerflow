import { of } from 'rxjs';
import { catchError, finalize, tap } from 'rxjs/operators';
import { computed, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { toast } from '~/composables/useToast';

import { resendVerificationCode$, verifyAccount$ } from '~/api/auth.api';

export function useVerification() {
	const router = useRouter();
	const route = useRoute();

	// Form data
	const otpCode = ref('');
	const loading = ref(false);
	const errorMessage = ref('');
	const successMessage = ref('');

	// Resend code functionality
	const resendDisabled = ref(false);
	const resendCountdown = ref(60);
	let countdownInterval: number | null = null;

	// Get email from route query or localStorage
	const email = computed(() => {
		return route.query.email?.toString() || localStorage.getItem('pendingVerificationEmail') || '';
	});

	// Handle verification submission
	const verifyAccount = () => {
		if (otpCode.value.length !== 6) {
			errorMessage.value = 'Please enter a valid 6-digit verification code';
			return;
		}

		loading.value = true;
		errorMessage.value = '';
		successMessage.value = '';

		verifyAccount$({
			email: email.value,
			verificationCode: otpCode.value
		})
			.pipe(
				tap(() => {
					successMessage.value = 'Your account has been successfully verified!';

					// Remove the stored email
					localStorage.removeItem('pendingVerificationEmail');

					// Redirect to login after a short delay
					setTimeout(() => {
						router.push({ name: 'signin' });
					}, 2000);
				}),
				catchError(error => {
					console.error('Verification error:', error);
					errorMessage.value = 'Invalid verification code. Please try again.';
					return of(null);
				}),
				finalize(() => {
					loading.value = false;
				})
			)
			.subscribe();
	};

	// Handle resend verification code
	const resendVerificationCode = () => {
		if (resendDisabled.value || !email.value) return;

		loading.value = true;
		errorMessage.value = '';

		resendVerificationCode$({ email: email.value })
			.pipe(
				tap(() => {
					toast.success('A new verification code has been sent to your email');

					// Start countdown
					startResendCountdown();
				}),
				catchError(error => {
					console.error('Resend code error:', error);
					errorMessage.value = 'Failed to resend verification code. Please try again later.';
					return of(null);
				}),
				finalize(() => {
					loading.value = false;
				})
			)
			.subscribe();
	};

	// Start countdown for resend button
	const startResendCountdown = () => {
		resendDisabled.value = true;
		resendCountdown.value = 60;

		countdownInterval = window.setInterval(() => {
			resendCountdown.value -= 1;

			if (resendCountdown.value <= 0) {
				resendDisabled.value = false;
				clearInterval(countdownInterval as number);
			}
		}, 1000);
	};

	// Initialize verification
	const initVerification = () => {
		if (route.query.email) {
			// Store the email in localStorage for persistence
			localStorage.setItem('pendingVerificationEmail', route.query.email.toString());
		}

		// If no email is available, redirect to login
		if (!email.value) {
			router.push({ name: 'signin' });
		}
	};

	// Cleanup
	const cleanupVerification = () => {
		if (countdownInterval) {
			clearInterval(countdownInterval);
		}
	};

	return {
		otpCode,
		loading,
		errorMessage,
		successMessage,
		resendDisabled,
		resendCountdown,
		email,
		verifyAccount,
		resendVerificationCode,
		initVerification,
		cleanupVerification
	};
}
