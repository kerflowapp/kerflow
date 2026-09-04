import type { Ref } from 'vue';
import { ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { toast } from 'vuetify-sonner';

import type { ProspectDto } from '~/api/dtos/prospect.dto';
import { importProspectsCsv$ } from '~/api/prospects.api';
import { useProspectsStore } from '~/stores/prospects.store';

import { useTrigger } from './useTrigger';

export interface CsvRow {
	[key: string]: string;
}

export function useCsvImport(): {
	parsedRows: Ref<CsvRow[]>;
	headers: Ref<string[]>;
	isImporting: Ref<boolean>;
	isParsing: Ref<boolean>;
	parseFile: (file: File) => void;
	importToBackend: (file: File) => void;
	clearParsed: () => void;
} {
	const { t } = useI18n();
	const store = useProspectsStore();
	const parsedRows = ref<CsvRow[]>([]);
	const headers = ref<string[]>([]);
	const isParsing = ref(false);
	const { trigger: triggerImport, loading: isImporting } = useTrigger();

	const parseFile = (file: File) => {
		isParsing.value = true;
		const reader = new FileReader();
		reader.onload = e => {
			const text = e.target?.result as string;
			if (!text) {
				isParsing.value = false;
				return;
			}
			const lines = text.split('\n').filter(line => line.trim().length > 0);
			if (lines.length === 0) {
				isParsing.value = false;
				return;
			}
			const headerLine = lines[0];
			const delimiter = headerLine.includes(';') ? ';' : ',';
			headers.value = headerLine.split(delimiter).map(h => h.trim().replace(/^"|"$/g, ''));

			parsedRows.value = lines.slice(1).map(line => {
				const values = line.split(delimiter).map(v => v.trim().replace(/^"|"$/g, ''));
				const row: CsvRow = {};
				headers.value.forEach((header, i) => {
					row[header] = values[i] || '';
				});
				return row;
			});
			isParsing.value = false;
		};
		reader.onerror = () => {
			isParsing.value = false;
			toast.error(t('errors.csv-parse-failed'));
		};
		reader.readAsText(file);
	};

	const importToBackend = (file: File) => {
		const formData = new FormData();
		formData.append('file', file);
		triggerImport(importProspectsCsv$(formData), {
			onSuccess: (response: { data: ProspectDto[] }) => {
				response.data.forEach(p => store.addProspect(p));
				toast.success(t('success.csv-imported', { count: response.data.length }));
			},
			onError: () => {
				toast.error(t('errors.csv-import-failed'));
			}
		});
	};

	const clearParsed = () => {
		parsedRows.value = [];
		headers.value = [];
	};

	return {
		parsedRows,
		headers,
		isImporting,
		isParsing,
		parseFile,
		importToBackend,
		clearParsed
	};
}
