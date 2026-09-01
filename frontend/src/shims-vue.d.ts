declare module '*.vue' {
	import type { DefineComponent } from 'vue';
	const component: DefineComponent<object, object, any>;
	export default component;
}

// Vue 3 <script setup> compiler macros
declare function defineProps<T>(props: T): T;
declare function defineEmits<T = any>(): T;
declare function defineEmits<T extends string[]>(events: T): { (event: T[number], ...args: any[]): void };
declare function defineExpose<T extends object>(exposed: T): void;
declare function withDefaults<T extends object, D extends Partial<T>>(props: T, defaults: D): T & D;
