/**
 * URL Slug Utilities for Geographic SEO
 *
 * Provides functions to convert French geographic names to URL-safe slugs
 * and vice versa for pSEO URLs like /annonces/bretagne/ille-et-vilaine/rennes
 */

/**
 * Converts a French geographic name to a URL-safe slug
 *
 * @example
 * nameToSlug("Île-de-France") // "ile-de-france"
 * nameToSlug("Provence-Alpes-Côte d'Azur") // "provence-alpes-cote-d-azur"
 * nameToSlug("Côtes-d'Armor") // "cotes-d-armor"
 * nameToSlug("Ille-et-Vilaine") // "ille-et-vilaine"
 */
export function nameToSlug(name: string): string {
	if (!name) return '';

	return (
		name
			// Normalize Unicode to decomposed form (é -> e + combining accent)
			.normalize('NFD')
			// Remove combining diacritical marks (accents)
			.replace(/[\u0300-\u036f]/g, '')
			// Convert to lowercase
			.toLowerCase()
			// Replace apostrophes with hyphens
			.replace(/['']/g, '-')
			// Replace spaces with hyphens
			.replace(/\s+/g, '-')
			// Remove any remaining non-alphanumeric characters except hyphens
			.replace(/[^a-z0-9-]/g, '')
			// Collapse multiple consecutive hyphens into one
			.replace(/-+/g, '-')
			// Remove leading/trailing hyphens
			.replace(/^-|-$/g, '')
	);
}

/**
 * Converts a slug back to a display-friendly name (capitalized words)
 * Note: This is a simple conversion and won't restore accents
 *
 * @example
 * slugToDisplayName("ile-de-france") // "Ile De France"
 * slugToDisplayName("provence-alpes-cote-d-azur") // "Provence Alpes Cote D Azur"
 */
export function slugToDisplayName(slug: string): string {
	if (!slug) return '';

	return slug
		.split('-')
		.map(word => word.charAt(0).toUpperCase() + word.slice(1))
		.join(' ');
}

/**
 * Creates a bidirectional slug map from a Record of code -> name
 *
 * @returns Map where key is the slug and value is {code, name, slug}
 */
export function createSlugMap<T extends string>(sourceMap: Record<T, string>): Map<string, { code: T; name: string; slug: string }> {
	const slugMap = new Map<string, { code: T; name: string; slug: string }>();

	for (const [code, name] of Object.entries(sourceMap) as [T, string][]) {
		const slug = nameToSlug(name);
		slugMap.set(slug, { code, name, slug });
	}

	return slugMap;
}

/**
 * Creates a reverse lookup map from code to slug
 */
export function createCodeToSlugMap(sourceMap: Record<string, string>): Map<string, string> {
	const codeToSlug = new Map<string, string>();

	for (const [code, name] of Object.entries(sourceMap)) {
		codeToSlug.set(code, nameToSlug(name));
	}

	return codeToSlug;
}
