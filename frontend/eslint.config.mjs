import eslint from '@eslint/js';
import eslintConfigPrettier from 'eslint-config-prettier';
import pluginVue from 'eslint-plugin-vue';
import globals from 'globals';
import tseslint from 'typescript-eslint';

export default tseslint.config({
	// ignore following patterns
	ignores: ['*.d.ts', 'dist/**', 'coverage/**', 'node_modules/**'],
	// extend other configs and rules
	extends: [
		// recommended eslint conf and rules
		eslint.configs.recommended,
		// recommended tslint conf and rules
		...tseslint.configs.recommended,
		// recommended eslint-plugin-vue config and rules for vue3
		...pluginVue.configs['flat/recommended'],
		// prettier specific rules to prevent collision with eslint
		eslintConfigPrettier
	],
	// enable typescript specific parser
	languageOptions: {
		globals: {
			...globals.browser,
			...globals.node
		},
		parserOptions: {
			parser: '@typescript-eslint/parser'
		}
	},
	// override and add rules
	rules: {
		// https://eslint.vuejs.org/rules/component-tags-order.html
		// rule is deprecated but still used in some underlying conf, so we need to disable it.
		'vue/component-tags-order': 'off',
		// https://eslint.vuejs.org/rules/block-order.html
		'vue/block-order': [
			'error',
			{
				order: ['template', 'style', 'script']
			}
		],
		// https://eslint.vuejs.org/rules/attributes-order#options
		'vue/attributes-order': [
			'error',
			{
				order: [
					'DEFINITION',
					'LIST_RENDERING',
					'CONDITIONALS',
					'RENDER_MODIFIERS',
					'GLOBAL',
					['UNIQUE', 'SLOT'],
					'TWO_WAY_BINDING',
					'OTHER_DIRECTIVES',
					'OTHER_ATTR',
					'EVENTS',
					'CONTENT'
				],
				alphabetical: true
			}
		],
		// https://typescript-eslint.io/rules/no-explicit-any/
		'@typescript-eslint/no-explicit-any': 'off',
		// https://typescript-eslint.io/rules/no-unused-vars
		'@typescript-eslint/no-unused-vars': [
			'error',
			{
				args: 'none',
				caughtErrors: 'none',
				ignoreRestSiblings: true
			}
		],
		// https://eslint.vuejs.org/rules/require-default-prop.html
		'vue/require-default-prop': 'off',
		// https://eslint.vuejs.org/rules/return-in-computed-property.html
		'vue/return-in-computed-property': 'off',
		// https://eslint.vuejs.org/rules/multi-word-component-names.html
		'vue/multi-word-component-names': 'off',
		// https://eslint.vuejs.org/rules/no-v-html.html
		'vue/no-v-html': 'off',
		// https://eslint.vuejs.org/rules/no-template-shadow.html
		'vue/no-template-shadow': ['error', { allow: ['props'] }],
		// https://eslint.vuejs.org/rules/valid-v-slot.html
		'vue/valid-v-slot': [
			'error',
			{
				allowModifiers: true
			}
		]
	}
});
