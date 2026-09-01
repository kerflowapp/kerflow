<!-- src/views/settings/SecuritySettings.vue -->
<template>
	<div>
		<h3 class="text-h5 font-weight-bold mb-6">{{ $t('settings.security.title') }}</h3>

		<v-form ref="passwordForm" v-model="isPasswordFormValid" @submit.prevent="changePassword">
			<!-- Password Change Section -->
			<v-card class="mb-6" variant="outlined">
				<v-card-title class="text-subtitle-1 font-weight-bold py-3 px-4 bg-grey-lighten-4">
					{{ $t('settings.security.password-change') }}
				</v-card-title>
				<v-card-text class="pa-4">
					<v-row>
						<v-col cols="12">
							<v-text-field
								v-model="passwordData.currentPassword"
								density="comfortable"
								:label="$t('settings.security.current-password')"
								:rules="[v => !!v || $t('settings.profile.required')]"
								type="password"
								variant="outlined"
							></v-text-field>
						</v-col>
						<v-col cols="12">
							<v-text-field
								v-model="passwordData.newPassword"
								density="comfortable"
								:label="$t('settings.security.new-password')"
								:rules="[v => !!v || $t('settings.profile.required')]"
								type="password"
								variant="outlined"
							></v-text-field>

							<password-checklist v-model="isPasswordValid" :password="passwordData.newPassword" />
						</v-col>
						<v-col cols="12">
							<v-text-field
								v-model="passwordData.confirmPassword"
								density="comfortable"
								:label="$t('settings.security.confirm-password')"
								:rules="[
									v => !!v || $t('settings.profile.required'),
									v => v === passwordData.newPassword || $t('settings.security.password-match')
								]"
								type="password"
								variant="outlined"
							></v-text-field>
						</v-col>
					</v-row>

					<div class="d-flex justify-end mt-2">
						<v-btn
							color="primary"
							:disabled="!isPasswordFormValid || !isPasswordValid"
							:loading="isPasswordLoading"
							type="submit"
						>
							{{ $t('settings.security.update-password') }}
						</v-btn>
					</div>
				</v-card-text>
			</v-card>
		</v-form>

		<!-- Success Snackbar -->
		<v-snackbar v-model="showSnackbar" :color="snackbarColor" :timeout="3000">
			{{ snackbarText }}
			<template #actions>
				<v-btn variant="text" @click="showSnackbar = false">
					{{ $t('common.close') }}
				</v-btn>
			</template>
		</v-snackbar>

		<!-- Confirmation Dialog -->
		<v-dialog v-model="showConfirmDialog" max-width="500">
			<v-card>
				<v-card-title class="text-h5">{{ confirmDialogTitle }}</v-card-title>
				<v-card-text>{{ confirmDialogText }}</v-card-text>
				<v-card-actions>
					<v-spacer></v-spacer>
					<v-btn color="grey-darken-1" variant="text" @click="showConfirmDialog = false">
						{{ $t('settings.security.cancel') }}
					</v-btn>
					<v-btn color="error" variant="text" @click="confirmAction">
						{{ $t('settings.security.confirm') }}
					</v-btn>
				</v-card-actions>
			</v-card>
		</v-dialog>
	</div>
</template>

<style scoped>
.v-card-title {
	border-bottom: 1px solid rgba(0, 0, 0, 0.12);
}

.v-list-item {
	min-height: 64px;
}
</style>

<script setup lang="ts">
import PasswordChecklist from '@/components/PasswordChecklist.vue';
import { reactive, ref } from 'vue';

// Form refs and validation
const isPasswordFormValid = ref(false);
const isPasswordValid = ref(false);
const isPasswordLoading = ref(false);

// Password change data
const passwordData = reactive({
	currentPassword: '',
	newPassword: '',
	confirmPassword: ''
});

// Snackbar
const showSnackbar = ref(false);
const snackbarText = ref('');
const snackbarColor = ref('success');

// Confirmation dialog
const showConfirmDialog = ref(false);
const confirmDialogTitle = ref('');
const confirmDialogText = ref('');
const pendingAction = ref<() => void>(() => {});

// Change password
const changePassword = async () => {
	try {
		isPasswordLoading.value = true;

		// In a real app, you would send the password data to an API
		// For now, we'll just simulate a delay
		await new Promise(resolve => setTimeout(resolve, 1000));

		// Show success message
		snackbarText.value = 'Password updated successfully';
		snackbarColor.value = 'success';
		showSnackbar.value = true;

		// Reset form
		passwordData.currentPassword = '';
		passwordData.newPassword = '';
		passwordData.confirmPassword = '';

		isPasswordLoading.value = false;
	} catch (error) {
		console.error('Failed to change password:', error);
		snackbarText.value = 'Failed to update password';
		snackbarColor.value = 'error';
		showSnackbar.value = true;
		isPasswordLoading.value = false;
	}
};

// Confirm action
const confirmAction = () => {
	pendingAction.value();
};
</script>
