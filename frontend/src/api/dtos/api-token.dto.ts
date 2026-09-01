export interface ApiTokenDto {
	id: string;
	name: string;
	tokenPrefix: string;
	creationDate: string;
	lastUsedAt?: string;
	revokedAt?: string;
}

export interface CreatedApiTokenDto {
	id: string;
	name: string;
	tokenPrefix: string;
	token: string;
}
