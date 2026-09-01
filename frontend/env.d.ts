/// <reference types="vite/client" />

interface ImportMetaEnv {
	/** Base URL of the Kerflow backend, e.g. http://localhost:8090 */
	readonly VITE_BACKEND_URL?: string;
	/** Backend port, used only when VITE_BACKEND_URL is left empty */
	readonly VITE_BACKEND_PORT?: string;
	/** 'true' to load the PostHog plugin */
	readonly VITE_POSTHOG_ENABLED?: string;
	/** Your own PostHog project key */
	readonly VITE_POSTHOG_KEY?: string;
	/** PostHog host, defaults to https://eu.i.posthog.com */
	readonly VITE_POSTHOG_HOST?: string;
	/** URL of the terms of service of your instance, empty to hide the links */
	readonly VITE_TERMS_URL?: string;
}

interface ImportMeta {
	readonly env: ImportMetaEnv;
}

// Vue 3 <script setup> compiler macros
declare function defineProps<T>(props?: T): T;
declare function defineEmits<E extends string[]>(events?: E): { (event: E[number], ...args: any[]): void };
declare function defineExpose<T extends object>(exposed?: T): void;
declare function withDefaults<T extends object, D extends Partial<T>>(props: T, defaults: D): T & D;
