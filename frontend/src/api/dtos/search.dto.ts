import type { ProspectScoreDto, SignalDto } from './prospect.dto';

export interface SearchRequest {
	keywords: string;
	city: string;
	radius: number;
	lat?: number;
	lng?: number;
	page?: number;
	pageSize?: number;
}

export interface OpeningHoursDto {
	openNow: boolean;
	weekdayText: string[];
}

export interface ReviewDto {
	authorName: string;
	profilePhotoUrl?: string;
	rating: number;
	text?: string;
	relativeTimeDescription?: string;
}

export interface SearchResultDto {
	placeId: string;
	name: string;
	address: string;
	phone?: string;
	website?: string;
	googleMapsUrl?: string;
	instagram?: string;
	facebook?: string;
	linkedin?: string;
	twitter?: string;
	tiktok?: string;
	youtube?: string;
	rating?: number;
	userRatingsTotal?: number;
	lat: number;
	lng: number;
	types?: string[];
	businessStatus?: string;
	openingHours?: OpeningHoursDto;
	reviews?: ReviewDto[];
	/** Google's own one-line description of the business; absent on most small businesses */
	editorialSummary?: string;
	/** Backend-computed prospect score (0–100), higher is better; null until signals are detected */
	score?: number | null;
	/** Explainable score (stars + reasons), same shape as on prospects */
	scoreDetails?: ProspectScoreDto | null;
	signals?: SignalDto[];
}

export interface SearchResponseDto {
	results: SearchResultDto[];
	nextPageToken: string | null;
}
