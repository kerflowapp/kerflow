import type { ComputedRef, Ref } from 'vue';
import { computed, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import type { LocationQuery, LocationQueryRaw } from 'vue-router';
import { useRoute, useRouter } from 'vue-router';

import type { BusinessCategory, ProspectDto } from '~/api/dtos/prospect.dto';
import { BUSINESS_CATEGORIES, ProspectSource } from '~/api/dtos/prospect.dto';
import { useFollowUps } from '~/composables/useFollowUps';
import { DEPARTMENTS_MAP, getDepartmentName } from '~/composables/useNominatim';
import { useProspectsStore } from '~/stores/prospects.store';
import { departmentOf } from '~/utils/prospect-department';

export interface FilterOption<T extends string | number = string> {
	value: T;
	title: string;
}

const SOURCES = Object.values(ProspectSource);
const MAX_STARS = 5;

/** Query keys the filters own; any other key in the URL is left untouched. */
const QUERY_KEYS = {
	text: 'q',
	followUpsOnly: 'due',
	departments: 'dept',
	minStars: 'stars',
	categories: 'cat',
	tags: 'tags',
	sources: 'src'
} as const;

const firstValue = (query: LocationQuery, key: string): string | undefined => {
	const raw = query[key];
	const value = Array.isArray(raw) ? raw[0] : raw;
	return value ?? undefined;
};

const listValue = (query: LocationQuery, key: string): string[] =>
	(firstValue(query, key) ?? '')
		.split(',')
		.map(v => v.trim())
		.filter(Boolean);

const isOneOf = <T extends string>(allowed: readonly T[]) => (value: string): value is T => (allowed as readonly string[]).includes(value);

/** Counts occurrences and turns them into sorted select options labelled "<label> (<count>)". */
const toOptions = <T extends string>(
	values: T[],
	label: (value: T) => string = value => value,
	compare: (a: T, b: T) => number = (a, b) => label(a).localeCompare(label(b))
): FilterOption<T>[] => {
	const counts = new Map<T, number>();
	for (const value of values) counts.set(value, (counts.get(value) ?? 0) + 1);
	return [...counts.entries()]
		.sort(([a], [b]) => compare(a, b))
		.map(([value, count]) => ({ value, title: `${label(value)} (${count})` }));
};

/** Board filters: state kept in the URL query, options derived from the loaded prospects. */
export function useProspectFilters(): {
	text: Ref<string>;
	followUpsOnly: Ref<boolean>;
	departments: Ref<string[]>;
	minStars: Ref<number | null>;
	categories: Ref<BusinessCategory[]>;
	tags: Ref<string[]>;
	sources: Ref<ProspectSource[]>;
	departmentOptions: ComputedRef<FilterOption[]>;
	categoryOptions: ComputedRef<FilterOption<BusinessCategory>[]>;
	tagOptions: ComputedRef<FilterOption[]>;
	sourceOptions: ComputedRef<FilterOption<ProspectSource>[]>;
	starOptions: ComputedRef<FilterOption<number>[]>;
	activeCount: ComputedRef<number>;
	isFiltering: ComputedRef<boolean>;
	matches: (prospect: ProspectDto) => boolean;
	reset: () => void;
} {
	const { t } = useI18n();
	const route = useRoute();
	const router = useRouter();
	const store = useProspectsStore();
	const { dueCount, isDue } = useFollowUps();

	const query = route.query;
	const parsedStars = Number(firstValue(query, QUERY_KEYS.minStars));

	const text = ref(firstValue(query, QUERY_KEYS.text) ?? '');
	const followUpsOnly = ref(firstValue(query, QUERY_KEYS.followUpsOnly) === '1');
	const departments = ref(listValue(query, QUERY_KEYS.departments).filter(code => code in DEPARTMENTS_MAP));
	const minStars = ref<number | null>(Number.isInteger(parsedStars) && parsedStars >= 1 && parsedStars <= MAX_STARS ? parsedStars : null);
	const categories = ref<BusinessCategory[]>(listValue(query, QUERY_KEYS.categories).filter(isOneOf(BUSINESS_CATEGORIES)));
	const tags = ref(listValue(query, QUERY_KEYS.tags));
	const sources = ref<ProspectSource[]>(listValue(query, QUERY_KEYS.sources).filter(isOneOf(SOURCES)));

	const departmentOptions = computed(() =>
		toOptions(
			store.prospects.map(departmentOf).filter((code): code is string => !!code),
			code => `${code} — ${getDepartmentName(code)}`,
			(a, b) => a.localeCompare(b, 'fr', { numeric: true })
		)
	);

	const categoryOptions = computed(() =>
		toOptions(
			store.prospects.map(p => p.businessProfile?.category).filter((c): c is BusinessCategory => !!c),
			category => t(`business.categories.${category}`)
		)
	);

	const tagOptions = computed(() => toOptions(store.prospects.flatMap(p => p.tags ?? [])));

	const sourceOptions = computed(() =>
		toOptions(
			store.prospects.map(p => p.source).filter((s): s is ProspectSource => !!s),
			source => t(`prospects.filters.sources.${source}`)
		)
	);

	const starOptions = computed(() =>
		Array.from({ length: MAX_STARS }, (_, i) => i + 1).map(n => ({ value: n, title: t('prospects.filters.stars', { n }, n) }))
	);

	/** Filters behind the "Filters" button; text and follow-ups have their own controls. */
	const activeCount = computed(
		() =>
			[departments.value.length, minStars.value, categories.value.length, tags.value.length, sources.value.length].filter(Boolean)
				.length
	);

	const isFiltering = computed(() => !!text.value || followUpsOnly.value || activeCount.value > 0);

	const matchesText = (p: ProspectDto, q: string) =>
		p.name.toLowerCase().includes(q) ||
		p.email?.toLowerCase().includes(q) ||
		p.phone?.includes(q) ||
		p.tags?.some(tag => tag.toLowerCase().includes(q));

	const matches = (p: ProspectDto): boolean => {
		if (text.value && !matchesText(p, text.value.toLowerCase())) return false;
		if (followUpsOnly.value && !isDue(p)) return false;
		if (departments.value.length) {
			const department = departmentOf(p);
			if (!department || !departments.value.includes(department)) return false;
		}
		if (minStars.value && (p.score?.stars ?? 0) < minStars.value) return false;
		if (categories.value.length) {
			const category = p.businessProfile?.category;
			if (!category || !categories.value.includes(category)) return false;
		}
		if (tags.value.length && !tags.value.some(tag => p.tags?.includes(tag))) return false;
		if (sources.value.length && !sources.value.includes(p.source)) return false;
		return true;
	};

	const reset = () => {
		departments.value = [];
		minStars.value = null;
		categories.value = [];
		tags.value = [];
		sources.value = [];
	};

	// Following up the last due prospect empties the filter: drop it rather than show an empty board
	watch(dueCount, count => {
		if (count === 0) followUpsOnly.value = false;
	});

	watch([text, followUpsOnly, departments, minStars, categories, tags, sources], () => {
		const next: LocationQueryRaw = { ...route.query };
		const set = (key: string, value: string | undefined) => {
			if (value) next[key] = value;
			else delete next[key];
		};
		set(QUERY_KEYS.text, text.value || undefined);
		set(QUERY_KEYS.followUpsOnly, followUpsOnly.value ? '1' : undefined);
		set(QUERY_KEYS.departments, departments.value.join(','));
		set(QUERY_KEYS.minStars, minStars.value ? String(minStars.value) : undefined);
		set(QUERY_KEYS.categories, categories.value.join(','));
		set(QUERY_KEYS.tags, tags.value.join(','));
		set(QUERY_KEYS.sources, sources.value.join(','));
		router.replace({ query: next });
	});

	return {
		text,
		followUpsOnly,
		departments,
		minStars,
		categories,
		tags,
		sources,
		departmentOptions,
		categoryOptions,
		tagOptions,
		sourceOptions,
		starOptions,
		activeCount,
		isFiltering,
		matches,
		reset
	};
}
