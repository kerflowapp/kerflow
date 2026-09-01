import { Preferences } from '@capacitor/preferences';

const TOKEN_KEY = 'auth_token';

/**
 * Stocke le token d'authentification de manière sécurisée
 */
export async function setAuthToken(token: string): Promise<void> {
	await Preferences.set({
		key: TOKEN_KEY,
		value: token
	});
}

/**
 * Récupère le token d'authentification stocké
 */
export async function getAuthToken(): Promise<string | null> {
	const { value } = await Preferences.get({ key: TOKEN_KEY });
	return value;
}

/**
 * Supprime le token d'authentification stocké
 */
export async function removeAuthToken(): Promise<void> {
	await Preferences.remove({ key: TOKEN_KEY });
}

/**
 * Vérifie si un token est stocké
 */
export async function hasAuthToken(): Promise<boolean> {
	const token = await getAuthToken();
	return token !== null && token !== '';
}

const REFRESH_TOKEN_KEY = 'refresh_token';

export async function setRefreshToken(token: string): Promise<void> {
	await Preferences.set({
		key: REFRESH_TOKEN_KEY,
		value: token
	});
}

export async function getRefreshToken(): Promise<string | null> {
	const { value } = await Preferences.get({ key: REFRESH_TOKEN_KEY });
	return value;
}

export async function removeRefreshToken(): Promise<void> {
	await Preferences.remove({ key: REFRESH_TOKEN_KEY });
}
