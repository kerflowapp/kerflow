import { ref, watch } from 'vue';





// Unlocks work-in-progress UI. Not a security boundary: the sequence is in the
// bundle, and anything that must stay restricted is enforced by the backend.
const BETA_CODE = ['b', 'e', 't', 'a', 'm', 'o', 'd', 'e'];
const BETA_MODE_KEY = 'kerflow_beta_mode';
const MOBILE_CLICK_COUNT = 7; // Number of clicks needed to activate beta mode on mobile
const sequence: string[] = [];
const clickCounter = ref(0);
const clickTimeout = ref<number | null>(null);

// Initialize with stored value
const isBetaEnabled = ref(localStorage.getItem(BETA_MODE_KEY) === 'true');

export function useBetaMode() {
	// Persist beta mode changes to localStorage
	watch(isBetaEnabled, newValue => {
		localStorage.setItem(BETA_MODE_KEY, String(newValue));
	});

	const handleKeydown = (event: KeyboardEvent) => {
		sequence.push(event.key);

		if (sequence.length > BETA_CODE.length) {
			sequence.shift();
		}

		if (sequence.join(',') === BETA_CODE.join(',')) {
			isBetaEnabled.value = true;
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
			isBetaEnabled.value = true;
			clickCounter.value = 0;
		}
	};

	const toggleBeta = () => {
		isBetaEnabled.value = !isBetaEnabled.value;
	};

	// Initialize beta mode from localStorage (should be called in App.vue)
	const initBetaMode = () => {
		// Beta mode is already initialized from localStorage on module load
		// This method is a hook for additional initialization if needed
		document.addEventListener('keydown', handleKeydown);
	};

	return {
		isBetaEnabled,
		handleKeydown,
		handleLogoClick,
		toggleBeta,
		initBetaMode
	};
}
