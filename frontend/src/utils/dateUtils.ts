export const formatDate = (date: string): string => {
	if (!date) return '';
	return new Date(date).toLocaleDateString('fr-FR', { year: 'numeric', month: 'long', day: 'numeric' });
};
