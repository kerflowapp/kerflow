export type SignupRequest = {
	login: string;
	password: string;
	firstName?: string;
	lastName?: string;
	phoneNumber?: string;
};

export type CheckEmailResponse = {
	exists: boolean;
	pendingConfirmation?: boolean;
};

export type UpdatePasswordRequest = {
	action: UpdatePasswordAction;
	email: string;
	password?: string;
	confirmationCode?: string;
};

export enum UpdatePasswordAction {
	RESET = 'RESET',
	CONFIRM = 'CONFIRM'
}

export type SigninResponse = {
	token: string;
	refreshToken?: string;
};
