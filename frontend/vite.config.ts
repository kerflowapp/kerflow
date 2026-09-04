import vue from '@vitejs/plugin-vue';
import vueJsx from '@vitejs/plugin-vue-jsx';
import { URL, fileURLToPath } from 'node:url';
import path from 'node:path';
import { defineConfig } from 'vite';

// https://vite.dev/config/
export default defineConfig({
	plugins: [vue(), vueJsx()],
	resolve: {
		alias: {
			'@': fileURLToPath(new URL('./src', import.meta.url)),
			'~': path.resolve(import.meta.dirname, 'src')
		}
	}
});
