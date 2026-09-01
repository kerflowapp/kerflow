<template>
	<v-menu location="bottom" transition="slide-y-transition">
		<template #activator="{ props }">
			<v-btn class="font-weight-bold text-uppercase" min-width="36" v-bind="props" :title="$t('common.language')" variant="text">
				{{ currentLocale }}
			</v-btn>
		</template>
		<v-list width="150">
			<v-list-item
				v-for="(name, locale) in localeNames"
				:key="locale"
				:active="currentLocale === locale"
				:class="currentLocale === locale ? 'bg-primary bg-opacity-10' : ''"
				@click="setLocale(locale)"
			>
				<v-list-item-title class="d-flex justify-space-between align-center">
					<span class="text-body-2">{{ name }}</span>
					<v-chip
						class="text-uppercase font-weight-bold"
						:color="currentLocale === locale ? 'primary' : 'default'"
						size="small"
						variant="tonal"
					>
						{{ locale }}
					</v-chip>
				</v-list-item-title>
			</v-list-item>
		</v-list>
	</v-menu>
</template>

<script setup lang="ts">
import { useLanguage } from '~/composables';

const { currentLocale, localeNames, setLocale } = useLanguage();
</script>
