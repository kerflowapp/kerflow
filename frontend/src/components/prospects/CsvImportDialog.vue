<template>
	<v-dialog v-model="modelValue" max-width="700" persistent>
		<v-card elevation="0">
			<v-card-title class="d-flex justify-space-between align-center">
				{{ t('csv-import.title') }}
				<v-btn icon="mdi-close" variant="text" @click="close" />
			</v-card-title>

			<v-card-text>
				<div v-if="!file">
					<v-file-input
						v-model="selectedFiles"
						accept=".csv"
						:label="t('csv-import.select-file')"
						prepend-icon="mdi-file-delimited"
						variant="outlined"
						@update:model-value="onFileSelected"
					/>
					<v-alert class="mt-2" type="info" variant="tonal">
						{{ t('csv-import.format-hint') }}
					</v-alert>
				</div>

				<div v-else>
					<div class="d-flex align-center mb-4">
						<v-icon class="mr-2" color="success">mdi-check-circle</v-icon>
						<span class="text-body-1">{{ file.name }} ({{ parsedRows.length }} {{ t('csv-import.rows') }})</span>
						<v-spacer />
						<v-btn size="small" variant="text" @click="resetFile">{{ t('csv-import.change-file') }}</v-btn>
					</div>

					<v-table v-if="parsedRows.length > 0" class="mb-4" density="compact" fixed-header height="300">
						<thead>
							<tr>
								<th v-for="header in headers" :key="header" class="text-caption">{{ header }}</th>
							</tr>
						</thead>
						<tbody>
							<tr v-for="(row, i) in parsedRows.slice(0, 10)" :key="i">
								<td v-for="header in headers" :key="header" class="text-caption">{{ row[header] }}</td>
							</tr>
						</tbody>
					</v-table>
					<div v-if="parsedRows.length > 10" class="text-caption text-grey mb-4">
						{{ t('csv-import.showing-preview', { shown: 10, total: parsedRows.length }) }}
					</div>
				</div>
			</v-card-text>

			<v-card-actions>
				<v-spacer />
				<v-btn variant="text" @click="close">{{ t('common.cancel') }}</v-btn>
				<v-btn color="primary" :disabled="!file" :loading="isImporting" variant="flat" @click="doImport">
					<v-icon start>mdi-upload</v-icon>
					{{ t('csv-import.import-button', { count: parsedRows.length }) }}
				</v-btn>
			</v-card-actions>
		</v-card>
	</v-dialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';

import { useCsvImport } from '~/composables/useCsvImport';

const modelValue = defineModel<boolean>();
const { t } = useI18n();
const { parsedRows, headers, isImporting, parseFile, importToBackend, clearParsed } = useCsvImport();

const file = ref<File | null>(null);
const selectedFiles = ref<File[]>([]);

const onFileSelected = (files: File[]) => {
	if (files && files.length > 0) {
		file.value = files[0];
		parseFile(files[0]);
	}
};

const resetFile = () => {
	file.value = null;
	selectedFiles.value = [];
	clearParsed();
};

const doImport = () => {
	if (file.value) {
		importToBackend(file.value);
	}
};

const close = () => {
	modelValue.value = false;
	resetFile();
};

watch(modelValue, val => {
	if (!val) {
		resetFile();
	}
});
</script>
