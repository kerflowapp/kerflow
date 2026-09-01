import { ref, watch } from 'vue';

const SANDBOX_CODE = ['s', 'a', 'n', 'd', 'b', 'o', 'x'];
const SANDBOX_MODE_KEY = 'kerflow_sandbox_mode';
const MOBILE_CLICK_COUNT = 7; // Number of clicks needed to activate sandbox mode on mobile
const sequence: string[] = [];
const clickCounter = ref(0);
const clickTimeout = ref<number | null>(null);

// Initialize with stored value
const isSandboxEnabled = ref(localStorage.getItem(SANDBOX_MODE_KEY) === 'true');

export function useSandboxMode() {
	// Persist sandbox mode changes to localStorage
	watch(isSandboxEnabled, newValue => {
		localStorage.setItem(SANDBOX_MODE_KEY, String(newValue));
	});

	const handleKeydown = (event: KeyboardEvent) => {
		sequence.push(event.key);

		if (sequence.length > SANDBOX_CODE.length) {
			sequence.shift();
		}

		if (sequence.join(',') === SANDBOX_CODE.join(',')) {
			isSandboxEnabled.value = true;
			sequence.length = 0;
		}
	};

	const handleLogoClick = () => {
		// Increment click counter
		clickCounter.value++;

		// Reset the counter after 2 seconds of inactivity
		if (clickTimeout.value !== null) {
			window.clearTimeout(clickTimeout.value);
		}

		clickTimeout.value = window.setTimeout(() => {
			clickCounter.value = 0;
			clickTimeout.value = null;
		}, 2000);

		// Check if we've reached the required number of clicks
		if (clickCounter.value >= MOBILE_CLICK_COUNT) {
			isSandboxEnabled.value = true;
			clickCounter.value = 0;
		}
	};

	const toggleSandbox = () => {
		isSandboxEnabled.value = !isSandboxEnabled.value;
	};

	// Initialize sandbox mode from localStorage (should be called in App.vue)
	const initSandboxMode = () => {
		// Sandbox mode is already initialized from localStorage on module load
		// This method is a hook for additional initialization if needed
		document.addEventListener('keydown', handleKeydown);
	};

	return {
		isSandboxEnabled,
		handleKeydown,
		handleLogoClick,
		toggleSandbox,
		initSandboxMode
	};
}
