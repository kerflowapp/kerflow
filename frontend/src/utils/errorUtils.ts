/**
 * Backend error code carried by a failed request, when there is one.
 *
 * RxJS hands the callback a plain `Error`; axios attaches the response to it,
 * so the code has to be read defensively.
 */
export function getApiErrorCode(error: unknown): string | undefined {
	return (error as { response?: { data?: { errorCode?: string } } })?.response?.data?.errorCode;
}
