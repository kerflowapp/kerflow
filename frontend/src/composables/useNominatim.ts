import { ref } from 'vue';

import { createCodeToSlugMap, createSlugMap, nameToSlug } from '~/utils/slug-utils';

// Re-export nameToSlug for convenience
export { nameToSlug };

// Mapping des codes départements français vers leurs noms
export const DEPARTMENTS_MAP: Record<string, string> = {
	'01': 'Ain',
	'02': 'Aisne',
	'03': 'Allier',
	'04': 'Alpes-de-Haute-Provence',
	'05': 'Hautes-Alpes',
	'06': 'Alpes-Maritimes',
	'07': 'Ardèche',
	'08': 'Ardennes',
	'09': 'Ariège',
	'10': 'Aube',
	'11': 'Aude',
	'12': 'Aveyron',
	'13': 'Bouches-du-Rhône',
	'14': 'Calvados',
	'15': 'Cantal',
	'16': 'Charente',
	'17': 'Charente-Maritime',
	'18': 'Cher',
	'19': 'Corrèze',
	'2A': 'Corse-du-Sud',
	'2B': 'Haute-Corse',
	'21': "Côte-d'Or",
	'22': "Côtes-d'Armor",
	'23': 'Creuse',
	'24': 'Dordogne',
	'25': 'Doubs',
	'26': 'Drôme',
	'27': 'Eure',
	'28': 'Eure-et-Loire',
	'29': 'Finistère',
	'30': 'Gard',
	'31': 'Haute-Garonne',
	'32': 'Gers',
	'33': 'Gironde',
	'34': 'Hérault',
	'35': 'Ille-et-Vilaine',
	'36': 'Indre',
	'37': 'Indre-et-Loire',
	'38': 'Isère',
	'39': 'Jura',
	'40': 'Landes',
	'41': 'Loir-et-Cher',
	'42': 'Loire',
	'43': 'Haute-Loire',
	'44': 'Loire-Atlantique',
	'45': 'Loiret',
	'46': 'Lot',
	'47': 'Lot-et-Garonne',
	'48': 'Lozère',
	'49': 'Maine-et-Loire',
	'50': 'Manche',
	'51': 'Marne',
	'52': 'Haute-Marne',
	'53': 'Mayenne',
	'54': 'Meurthe-et-Moselle',
	'55': 'Meuse',
	'56': 'Morbihan',
	'57': 'Moselle',
	'58': 'Nièvre',
	'59': 'Nord',
	'60': 'Oise',
	'61': 'Orne',
	'62': 'Pas-de-Calais',
	'63': 'Puy-de-Dôme',
	'64': 'Pyrénées-Atlantiques',
	'65': 'Hautes-Pyrénées',
	'66': 'Pyrénées-Orientales',
	'67': 'Bas-Rhin',
	'68': 'Haut-Rhin',
	'69': 'Rhône',
	'70': 'Haute-Saône',
	'71': 'Saône-et-Loire',
	'72': 'Sarthe',
	'73': 'Savoie',
	'74': 'Haute-Savoie',
	'75': 'Paris',
	'76': 'Seine-Maritime',
	'77': 'Seine-et-Marne',
	'78': 'Yvelines',
	'79': 'Deux-Sèvres',
	'80': 'Somme',
	'81': 'Tarn',
	'82': 'Tarn-et-Garonne',
	'83': 'Var',
	'84': 'Vaucluse',
	'85': 'Vendée',
	'86': 'Vienne',
	'87': 'Haute-Vienne',
	'88': 'Vosges',
	'89': 'Yonne',
	'90': 'Territoire de Belfort',
	'91': 'Essonne',
	'92': 'Hauts-de-Seine',
	'93': 'Seine-Saint-Denis',
	'94': 'Val-de-Marne',
	'95': "Val-d'Oise",
	// DOM-TOM
	'971': 'Guadeloupe',
	'972': 'Martinique',
	'973': 'Guyane',
	'974': 'La Réunion',
	'976': 'Mayotte'
};

// Mapping des régions françaises (ISO 3166-2)
export const REGIONS_MAP: Record<string, string> = {
	'FR-ARA': 'Auvergne-Rhône-Alpes',
	'FR-BFC': 'Bourgogne-Franche-Comté',
	'FR-BRE': 'Bretagne',
	'FR-CVL': 'Centre-Val de Loire',
	'FR-COR': 'Corse',
	'FR-GES': 'Grand Est',
	'FR-HDF': 'Hauts-de-France',
	'FR-IDF': 'Île-de-France',
	'FR-NOR': 'Normandie',
	'FR-NAQ': 'Nouvelle-Aquitaine',
	'FR-OCC': 'Occitanie',
	'FR-PDL': 'Pays de la Loire',
	'FR-PAC': "Provence-Alpes-Côte d'Azur",
	'FR-GUA': 'Guadeloupe',
	'FR-MTQ': 'Martinique',
	'FR-GUF': 'Guyane',
	'FR-LRE': 'La Réunion',
	'FR-MAY': 'Mayotte'
};

// Mapping des départements vers leur région
export const DEPARTMENT_TO_REGION_MAP: Record<string, string> = {
	// Auvergne-Rhône-Alpes
	'01': 'FR-ARA',
	'03': 'FR-ARA',
	'07': 'FR-ARA',
	'15': 'FR-ARA',
	'26': 'FR-ARA',
	'38': 'FR-ARA',
	'42': 'FR-ARA',
	'43': 'FR-ARA',
	'63': 'FR-ARA',
	'69': 'FR-ARA',
	'73': 'FR-ARA',
	'74': 'FR-ARA',
	// Bourgogne-Franche-Comté
	'21': 'FR-BFC',
	'25': 'FR-BFC',
	'39': 'FR-BFC',
	'58': 'FR-BFC',
	'70': 'FR-BFC',
	'71': 'FR-BFC',
	'89': 'FR-BFC',
	'90': 'FR-BFC',
	// Bretagne
	'22': 'FR-BRE',
	'29': 'FR-BRE',
	'35': 'FR-BRE',
	'56': 'FR-BRE',
	// Centre-Val de Loire
	'18': 'FR-CVL',
	'28': 'FR-CVL',
	'36': 'FR-CVL',
	'37': 'FR-CVL',
	'41': 'FR-CVL',
	'45': 'FR-CVL',
	// Corse
	'2A': 'FR-COR',
	'2B': 'FR-COR',
	// Grand Est
	'08': 'FR-GES',
	'10': 'FR-GES',
	'51': 'FR-GES',
	'52': 'FR-GES',
	'54': 'FR-GES',
	'55': 'FR-GES',
	'57': 'FR-GES',
	'67': 'FR-GES',
	'68': 'FR-GES',
	'88': 'FR-GES',
	// Hauts-de-France
	'02': 'FR-HDF',
	'59': 'FR-HDF',
	'60': 'FR-HDF',
	'62': 'FR-HDF',
	'80': 'FR-HDF',
	// Île-de-France
	'75': 'FR-IDF',
	'77': 'FR-IDF',
	'78': 'FR-IDF',
	'91': 'FR-IDF',
	'92': 'FR-IDF',
	'93': 'FR-IDF',
	'94': 'FR-IDF',
	'95': 'FR-IDF',
	// Normandie
	'14': 'FR-NOR',
	'27': 'FR-NOR',
	'50': 'FR-NOR',
	'61': 'FR-NOR',
	'76': 'FR-NOR',
	// Nouvelle-Aquitaine
	'16': 'FR-NAQ',
	'17': 'FR-NAQ',
	'19': 'FR-NAQ',
	'23': 'FR-NAQ',
	'24': 'FR-NAQ',
	'33': 'FR-NAQ',
	'40': 'FR-NAQ',
	'47': 'FR-NAQ',
	'64': 'FR-NAQ',
	'79': 'FR-NAQ',
	'86': 'FR-NAQ',
	'87': 'FR-NAQ',
	// Occitanie
	'09': 'FR-OCC',
	'11': 'FR-OCC',
	'12': 'FR-OCC',
	'30': 'FR-OCC',
	'31': 'FR-OCC',
	'32': 'FR-OCC',
	'34': 'FR-OCC',
	'46': 'FR-OCC',
	'48': 'FR-OCC',
	'65': 'FR-OCC',
	'66': 'FR-OCC',
	'81': 'FR-OCC',
	'82': 'FR-OCC',
	// Pays de la Loire
	'44': 'FR-PDL',
	'49': 'FR-PDL',
	'53': 'FR-PDL',
	'72': 'FR-PDL',
	'85': 'FR-PDL',
	// Provence-Alpes-Côte d'Azur
	'04': 'FR-PAC',
	'05': 'FR-PAC',
	'06': 'FR-PAC',
	'13': 'FR-PAC',
	'83': 'FR-PAC',
	'84': 'FR-PAC',
	// DOM-TOM
	'971': 'FR-GUA',
	'972': 'FR-MTQ',
	'973': 'FR-GUF',
	'974': 'FR-LRE',
	'976': 'FR-MAY'
};

// Obtenir les départements d'une ou plusieurs régions
export const getDepartmentsByRegions = (regionCodes: string[]): Array<{ code: string; name: string }> => {
	return Object.entries(DEPARTMENT_TO_REGION_MAP)
		.filter(([, regionCode]) => regionCodes.includes(regionCode))
		.map(([deptCode]) => ({
			code: deptCode,
			name: DEPARTMENTS_MAP[deptCode] || deptCode
		}))
		.sort((a, b) => a.name.localeCompare(b.name, 'fr'));
};

// Obtenir toutes les régions triées
export const getAllRegions = (): Array<{ code: string; name: string }> => {
	return Object.entries(REGIONS_MAP)
		.map(([code, name]) => ({ code, name }))
		.sort((a, b) => a.name.localeCompare(b.name, 'fr'));
};

// Obtenir tous les départements triés
export const getAllDepartments = (): Array<{ code: string; name: string }> => {
	return Object.entries(DEPARTMENTS_MAP)
		.map(([code, name]) => ({ code, name }))
		.sort((a, b) => a.name.localeCompare(b.name, 'fr'));
};

// Obtenir le nom à partir du code
export const getDepartmentName = (code: string): string | undefined => {
	return DEPARTMENTS_MAP[code] ?? code;
};

// Obtenir le code à partir du nom (recherche inversée)
export const getDepartmentCode = (name: string): string | undefined => {
	return Object.entries(DEPARTMENTS_MAP).find(([, deptName]) => deptName.toLowerCase() === name.toLowerCase())?.[0];
};

// Extraire le code département depuis le code postal
export const getDepartmentCodeFromPostcode = (postcode: string): string | undefined => {
	if (!postcode || postcode.length < 2) return undefined;
	// Corse: codes postaux 20XXX avec distinction 2A/2B
	if (postcode.startsWith('20')) {
		const num = parseInt(postcode.substring(0, 5), 10);
		// 20000-20190 et 20200-20299 (Ajaccio) -> Corse-du-Sud (2A)
		// 20200-20299 (Bastia) -> Haute-Corse (2B)
		// Simplification: 20000-20199 = 2A, 20200-20999 = 2B
		if (num < 20200) return '2A';
		return '2B';
	}
	// DOM-TOM: codes postaux 97X
	if (postcode.startsWith('97')) {
		const domCode = postcode.substring(0, 3);
		return DEPARTMENTS_MAP[domCode] ? domCode : undefined;
	}
	// Métropole: 2 premiers chiffres
	const code = postcode.substring(0, 2);
	return DEPARTMENTS_MAP[code] ? code : undefined;
};

// ============================================================================
// Slug Maps for pSEO URLs
// ============================================================================

// Pre-computed slug maps for fast lookup
export const REGION_SLUGS = createSlugMap(REGIONS_MAP);
export const DEPARTMENT_SLUGS = createSlugMap(DEPARTMENTS_MAP);

// Reverse lookup: code -> slug
export const REGION_CODE_TO_SLUG = createCodeToSlugMap(REGIONS_MAP);
export const DEPARTMENT_CODE_TO_SLUG = createCodeToSlugMap(DEPARTMENTS_MAP);

/**
 * Get region data by its URL slug
 * @example getRegionBySlug("bretagne") // { code: "FR-BRE", name: "Bretagne", slug: "bretagne" }
 */
export const getRegionBySlug = (slug: string): { code: string; name: string; slug: string } | undefined => {
	return REGION_SLUGS.get(slug.toLowerCase());
};

/**
 * Get department data by its URL slug
 * @example getDepartmentBySlug("ille-et-vilaine") // { code: "35", name: "Ille-et-Vilaine", slug: "ille-et-vilaine" }
 */
export const getDepartmentBySlug = (slug: string): { code: string; name: string; slug: string } | undefined => {
	return DEPARTMENT_SLUGS.get(slug.toLowerCase());
};

/**
 * Get the slug for a region code
 * @example getRegionSlug("FR-BRE") // "bretagne"
 */
export const getRegionSlug = (code: string): string | undefined => {
	return REGION_CODE_TO_SLUG.get(code);
};

/**
 * Get the slug for a department code
 * @example getDepartmentSlug("35") // "ille-et-vilaine"
 */
export const getDepartmentSlug = (code: string): string | undefined => {
	return DEPARTMENT_CODE_TO_SLUG.get(code);
};

/**
 * Validate that a department belongs to a region
 */
export const validateDepartmentInRegion = (departmentCode: string, regionCode: string): boolean => {
	return DEPARTMENT_TO_REGION_MAP[departmentCode] === regionCode;
};

export interface NominatimResult {
	place_id: number;
	licence: string;
	osm_type: string;
	osm_id: number;
	boundingbox: string[];
	lat: string;
	lon: string;
	display_name: string;
	class: string;
	type: string;
	importance: number;
	address: {
		house_number?: string;
		road?: string;
		suburb?: string;
		city?: string;
		village?: string;
		county?: string;
		state?: string;
		postcode?: string;
		country?: string;
		country_code?: string;
		town?: string;
		municipality?: string;
		city_district?: string;
		office?: string;
		retail?: string;
		isolated_dwelling?: string;
		neighbourhood?: string;
		region?: string;
		'ISO3166-2-lvl6'?: string;
		'ISO3166-2-lvl4'?: string;
	};
}

const isValidAddress = (addr: NominatimResult['address']): boolean => {
	// Check if at least one important field is present
	const hasStreetInfo = addr.road || addr.house_number;
	const hasCityInfo = addr.city || addr.town || addr.municipality || addr.village;
	const hasPostalInfo = addr.postcode;
	const hasAreaInfo = addr.county || addr.state || addr.suburb || addr.neighbourhood || addr.city_district;
	const hasBusinessInfo = addr.office || addr.retail || addr.isolated_dwelling;

	// Return true if any of these groups has information
	return Boolean(hasStreetInfo || hasCityInfo || hasPostalInfo || hasAreaInfo || hasBusinessInfo);
};

export function useNominatim() {
	const suggestions = ref<NominatimResult[]>([]);
	const isLoading = ref(false);
	let debounceTimeout: number | null = null;

	const searchAddress = (query: string) => {
		if (!query || query.length < 3) {
			suggestions.value = [];
			return;
		}

		// Clear any existing timeout
		if (debounceTimeout) {
			clearTimeout(debounceTimeout);
		}

		// Set a new timeout
		debounceTimeout = window.setTimeout(() => {
			isLoading.value = true;

			// Normalize query: replace multiple spaces with single hyphen for better Nominatim matching
			// Example: "Bain De bretagne" -> "Bain-de-bretagne"
			const normalizedQuery = query.replace(/\s+/g, '-');

			fetch(
				`https://nominatim.openstreetmap.org/search?q=${encodeURIComponent(
					normalizedQuery
				)}&format=json&addressdetails=1&countrycodes=fr`,
				{
					headers: {
						'Accept-Language': 'fr'
					}
				}
			)
				.then(response => response.json())
				.then((data: NominatimResult[]) => {
					// Filter out invalid addresses
					const validResults = data.filter(item => isValidAddress(item.address));

					// Deduplicate by city name and county
					const seen = new Map<string, NominatimResult>();
					validResults.forEach(item => {
						const addr = item.address;
						const cityName = addr.city || addr.town || addr.village || addr.municipality;
						const county = addr.county;

						// Create a unique key based on city name and county
						const key = `${cityName}-${county}`;

						// Keep only the first occurrence of each city-county combination
						if (cityName && !seen.has(key)) {
							seen.set(key, item);
						}
					});

					suggestions.value = Array.from(seen.values());
				})
				.catch(error => {
					console.error('Error fetching address suggestions:', error);
					suggestions.value = [];
				})
				.finally(() => {
					isLoading.value = false;
				});
		}, 300); // 300ms delay
	};

	return {
		suggestions,
		isLoading,
		searchAddress,
		isValidAddress,
		getDepartmentCode,
		getDepartmentName,
		getDepartmentCodeFromPostcode
	};
}
