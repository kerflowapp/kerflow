import { ref } from 'vue';

export type ToastColor = 'success' | 'error' | 'info';

export interface ToastMessage {
	text: string;
	color: ToastColor;
}

/**
 * Shared across every caller: the queue is module-level so the single
 * <v-snackbar-queue> mounted in App.vue displays messages emitted from anywhere.
 */
const messages = ref<ToastMessage[]>([]);

function push(color: ToastColor, text: string): void {
	messages.value.push({ color, text });
}

export const toast = {
	success: (text: string): void => push('success', text),
	error: (text: string): void => push('error', text),
	info: (text: string): void => push('info', text)
};

export function useToast() {
	return { messages, toast };
}
