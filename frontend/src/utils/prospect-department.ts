import type { ProspectDto } from '~/api/dtos/prospect.dto';
import { getDepartmentCodeFromPostcode } from '~/composables/useNominatim';

const POSTCODE = /\b\d{5}\b/;

/** Department code deduced from the French postcode found in the address; undefined when there is none. */
export const departmentOf = (prospect: Pick<ProspectDto, 'address'>): string | undefined => {
	const postcode = prospect.address?.match(POSTCODE)?.[0];
	return postcode ? getDepartmentCodeFromPostcode(postcode) : undefined;
};
