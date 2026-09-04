import { defineStore } from 'pinia';
import { Observable, from, map, switchMap } from 'rxjs';
import type { Ref } from 'vue';
import { ref } from 'vue';

import { getUserInformation$, login$, signup$, updatePassword$, verifyAccount$ } from '~/api/auth.api';
import { SignupRequest, UpdatePasswordRequest } from '~/api/dtos/auth.dto';
import type { UserDto } from '~/api/dtos/users.dto';
import { removeAuthToken, removeRefreshToken, setAuthToken, setRefreshToken } from '~/utils/tokenStorage';

const LOGGED_IN = 'loggedIn';

export type AuthenticationStore = {
	user: Ref<UserDto>;
	isLoggedIn: Ref<boolean>;
	logout: () => void;
	login: (request: { username: string; password: string }) => Observable<unknown>;
	verifyAndLogin: (email: string, code: string, password: string) => Observable<unknown>;
	updatePassword: (request: UpdatePasswordRequest) => Observable<unknown>;
	signup: (request: SignupRequest) => Observable<unknown>;
	refreshUserInformation: () => Observable<unknown>;
};

export const useAuthStore = defineStore<'auth', AuthenticationStore>('auth', () => {
	const user: Ref<UserDto> = ref();
	const isLoggedIn = ref<boolean>(localStorage.getItem(LOGGED_IN) == 'true');

	const logout = async () => {
		isLoggedIn.value = false;
		user.value = undefined;
		localStorage.removeItem(LOGGED_IN);
		await removeAuthToken();
		await removeRefreshToken();
	};

	const login = (request: { username: string; password: string }) => {
		return login$(request).pipe(
			switchMap(response =>
				from(
					(async () => {
						const token = response.data.token;
						if (token) {
							await setAuthToken(token);
							localStorage.setItem(LOGGED_IN, 'true');
							isLoggedIn.value = true;
						}
						if (response.data.refreshToken) {
							await setRefreshToken(response.data.refreshToken);
						}
					})()
				).pipe(map(() => response))
			)
		);
	};

	const verifyAndLogin = (email: string, code: string, password: string) => {
		return verifyAccount$({ email, verificationCode: code }).pipe(switchMap(() => login({ username: email, password })));
	};

	const signup = (request: SignupRequest) => {
		return signup$(request).pipe(map(() => {}));
	};

	const updatePassword = (request: UpdatePasswordRequest) => {
		return updatePassword$(request);
	};

	const refreshUserInformation = () => {
		return getUserInformation$().pipe(
			map(response => {
				user.value = response.data;
			})
		);
	};

	return {
		logout,
		isLoggedIn,
		user,
		login,
		verifyAndLogin,
		signup,
		updatePassword,
		refreshUserInformation
	};
});
