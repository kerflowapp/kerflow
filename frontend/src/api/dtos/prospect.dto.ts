export enum KanbanStatus {
	NEW = 'NEW',
	CONTACTED = 'CONTACTED',
	IN_DISCUSSION = 'IN_DISCUSSION',
	WON = 'WON',
	LOST = 'LOST'
}

export enum ProspectSource {
	SEARCH = 'SEARCH',
	CSV = 'CSV',
	MANUAL = 'MANUAL'
}

export interface SocialLinks {
	instagram?: string;
	facebook?: string;
	linkedin?: string;
}

export type SignalImportance = 'HIGH' | 'MEDIUM' | 'LOW';

export interface SignalDto {
	type: string;
	importance: SignalImportance;
	source: string;
	params?: Record<string, unknown>;
	detectedAt: string;
}

export interface ScoreReasonDto {
	type: string;
	importance: SignalImportance;
	points: number;
	params?: Record<string, unknown>;
}

/** Explainable qualification score: 0-100 + 1-5 stars, always with its reasons */
export interface ProspectScoreDto {
	score: number;
	stars: number;
	reasons: ScoreReasonDto[];
}

export type SizeBucket = 'SOLO' | 'FROM_2_TO_5' | 'FROM_6_TO_9' | 'FROM_10_TO_19' | 'FROM_20_TO_49' | 'FIFTY_PLUS';

export type SizeConfidence = 'LOW' | 'MEDIUM' | 'HIGH';

export interface SizeReasonDto {
	/** i18n key under size.reasons, interpolated with params */
	key: string;
	params?: Record<string, unknown>;
}

/** Company size estimate: INSEE workforce bracket when declared, heuristics otherwise */
export interface SizeEstimateDto {
	bucket: SizeBucket;
	confidence: SizeConfidence;
	reasons: SizeReasonDto[];
	estimatedAt: string;
}

export type BusinessCategory =
	| 'TAXI_VTC'
	| 'AMBULANCE'
	| 'ROAD_FREIGHT'
	| 'PASSENGER_TRANSPORT'
	| 'VEHICLE_RENTAL'
	| 'AUTO_SERVICES'
	| 'DRIVING_SCHOOL'
	| 'CONSTRUCTION'
	| 'LANDSCAPING'
	| 'CLEANING'
	| 'OTHER';

/** Where the category comes from — the source is the confidence */
export type BusinessCategorySource = 'NAF' | 'GOOGLE_TYPES';

export interface BusinessFactDto {
	/** i18n key under business.facts, interpolated with params */
	key: string;
	params?: Record<string, unknown>;
}

/** Deterministic business profile: what the company does, sourced and auditable */
export interface BusinessProfileDto {
	category: BusinessCategory;
	categorySource?: BusinessCategorySource | null;
	nafCode?: string | null;
	nafSection?: string | null;
	googleTypes?: string[];
	facts: BusinessFactDto[];
	profiledAt: string;
}

/** Agent-written profile sheet (via MCP): a markdown briefing, overwritten on every save */
export interface ProfileSheetDto {
	content: string;
	generatedBy: string;
	generatedAt: string;
}

/** Agent-written analysis (via MCP): the interpretation the profile cannot compute */
export interface ProspectAnalysisDto {
	summary: string;
	detectedNeeds?: string[];
	suggestedApproach?: string;
	generatedBy: string;
	analyzedAt: string;
}

export interface ProspectDto {
	id: string;
	name: string;
	address: string;
	phone: string;
	email: string;
	website: string;
	lat?: number | null;
	lng?: number | null;
	socialLinks: SocialLinks;
	statusKey: string;
	/** Ordering position within its kanban column; null on legacy rows never reordered */
	position?: number | null;
	notes: string;
	tags: string[];
	signals?: SignalDto[];
	/** Computed by the backend from the signals; null until the prospect is enriched */
	score?: ProspectScoreDto | null;
	/** Null when neither the registry nor the heuristics could tell */
	sizeEstimate?: SizeEstimateDto | null;
	/** Null until enriched, or when nothing is known about the activity */
	businessProfile?: BusinessProfileDto | null;
	/** Written by an MCP agent; coexists with businessProfile, never merges into it */
	analysis?: ProspectAnalysisDto | null;
	/** Written by an MCP agent; markdown briefing, overwritten on every save */
	profileSheet?: ProfileSheetDto | null;
	siren?: string | null;
	googlePlaceId?: string;
	/** Kept from the search so enriching never needs a paid Google call */
	googleTypes?: string[];
	googleRating?: number | null;
	googleUserRatingsTotal?: number | null;
	googleEditorialSummary?: string | null;
	source: ProspectSource;
	searchQuery: string;
	createdAt: string;
	updatedAt: string;
}

export interface CreateProspectDto {
	name: string;
	address?: string;
	phone?: string;
	email?: string;
	website?: string;
	lat?: number;
	lng?: number;
	socialLinks?: SocialLinks;
	statusKey?: string;
	notes?: string;
	tags?: string[];
	signals?: SignalDto[];
	googlePlaceId?: string;
	/** Passed back from the search result so re-enriching never re-fetches Google */
	googleTypes?: string[];
	googleRating?: number | null;
	googleUserRatingsTotal?: number | null;
	googleEditorialSummary?: string | null;
	source: ProspectSource;
	searchQuery?: string;
}

export interface UpdateProspectDto {
	name?: string;
	address?: string;
	phone?: string;
	email?: string;
	website?: string;
	lat?: number;
	lng?: number;
	socialLinks?: SocialLinks;
	statusKey?: string;
	notes?: string;
	tags?: string[];
}

export interface ProspectPipelineColumnDto {
	id: string;
	key: string;
	name: string | null;
	sortOrder: number;
	color: string | null;
	icon: string | null;
	system: boolean;
}

export interface CreateProspectPipelineColumnDto {
	name: string;
	color?: string | null;
	icon?: string | null;
}

export interface UpdateProspectPipelineColumnDto {
	name: string;
	color?: string | null;
	icon?: string | null;
}

export interface ReorderProspectPipelineColumnsDto {
	orderedIds: string[];
}

export interface ReorderProspectsDto {
	statusKey: string;
	orderedIds: string[];
}
