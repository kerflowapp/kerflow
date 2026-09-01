<template>
	<div v-if="sheet">
		<!-- eslint-disable-next-line vue/no-v-html — content sanitized with DOMPurify -->
		<div class="text-body-2 sheet-content" v-html="renderedContent" />

		<div class="text-caption text-medium-emphasis mt-3">
			{{ t('sheet.generated-by', { by: sheet.generatedBy, date: generatedAt }) }}
		</div>
	</div>
	<div v-else class="text-body-2 text-medium-emphasis">{{ t('sheet.empty') }}</div>
</template>

<style scoped>
.sheet-content :deep(h1),
.sheet-content :deep(h2),
.sheet-content :deep(h3) {
	font-size: 0.95rem;
	font-weight: 600;
	margin: 12px 0 4px;
}

.sheet-content :deep(p) {
	margin: 4px 0;
}

.sheet-content :deep(ul),
.sheet-content :deep(ol) {
	padding-left: 20px;
	margin: 4px 0;
}

.sheet-content :deep(li) {
	margin: 2px 0;
}
</style>

<script setup lang="ts">
import DOMPurify from 'dompurify';
import { marked } from 'marked';
import { computed } from 'vue';
import { useI18n } from 'vue-i18n';

import type { ProfileSheetDto } from '~/api/dtos/prospect.dto';

interface Props {
	sheet?: ProfileSheetDto | null;
}

const props = defineProps<Props>();
const { t, locale } = useI18n();

const renderedContent = computed(() => {
	if (!props.sheet?.content) return '';
	return DOMPurify.sanitize(marked.parse(props.sheet.content, { async: false }));
});

const generatedAt = computed(() => {
	if (!props.sheet?.generatedAt) return '';
	const date = new Date(props.sheet.generatedAt);
	return Number.isNaN(date.getTime()) ? '' : date.toLocaleDateString(locale.value);
});
</script>
