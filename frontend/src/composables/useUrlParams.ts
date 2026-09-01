import { ref, watchEffect } from 'vue';
import { useRoute } from 'vue-router';

/**
 * Composable for handling URL parameters
 * @returns Object containing functions to work with URL parameters
 */
export function useUrlParams() {
	const route = useRoute();

	/**
	 * Get the value of a specific URL parameter
	 * @param paramName - The name of the parameter to retrieve
	 * @returns The value of the parameter or null if not found
	 */
	const getParam = (paramName: string) => {
		const value = ref<string | null>(null);

		watchEffect(() => {
			const param = route.query[paramName];
			value.value = param ? (Array.isArray(param) ? param[0] : param) : null;
		});

		return value;
	};

	return {
		getParam
	};
}
