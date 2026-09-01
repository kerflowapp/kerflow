/**
 * Utility functions for URL construction
 */

/**
 * Get the base URL for API calls using the current window location
 * @param port Optional port number. If not provided, will use default ports (80 for http, 443 for https)
 * @returns The base URL for API calls
 */

const BACKEND_URL = import.meta.env.VITE_BACKEND_URL;
const BACKEND_PORT = import.meta.env.VITE_BACKEND_PORT;

export function getApiBaseUrl(port?: string | number): string {
	if (BACKEND_URL) {
		return BACKEND_URL;
	}
	if (typeof window === 'undefined') {
		// Fallback for testing environments
		return BACKEND_URL || 'http://localhost:3000';
	}

	const protocol = window.location.protocol;
	const hostname = window.location.hostname;

	if (port) {
		return `${protocol}//${hostname}:${port}`;
	}

	// Use current port if available, otherwise use default ports
	const currentPort = window.location.port;
	if (currentPort && currentPort !== '80' && currentPort !== '443') {
		return `${protocol}//${hostname}:${currentPort}`;
	}

	return `${protocol}//${hostname}`;
}

/**
 * Get the backend API base URL
 */
export function getBackendUrl(): string {
	return getApiBaseUrl(BACKEND_PORT);
}

/**
 * Get the URL of the Terms of Service page.
 *
 * The legal terms belong to whoever operates the instance, so they are not
 * bundled with the code: point VITE_TERMS_URL at your own page. When it is
 * left empty the links to the terms are simply not rendered.
 */
export function getTermsUrl(): string {
	return import.meta.env.VITE_TERMS_URL ?? '';
}
