export interface AuthorizationRequestDto {
	clientName: string;
	scopes: string[];
	expiresAt: string;
}

export interface ConsentDecisionDto {
	/** External URL to navigate to: the OAuth client's callback, not an app route. */
	redirectUri: string;
}
