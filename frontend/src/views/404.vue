<template>
	<div class="d-flex flex-column align-center justify-content-center">
		<div>
			<v-img alt="404 Illustration" class="not-found-image mb-6" src="/404_illustration.svg">
				<template #error>
					<v-img alt="404 Illustration" src="https://placehold.co/280x280/EFEFEF/AAAAAA?text=404" />
				</template>
			</v-img>
		</div>
		<div>
			<h1 class="text-h3 font-weight-bold text-primary mb-2">
				{{ $t('common.page-not-found.title') || 'Page Not Found' }}
			</h1>
		</div>
		<div>
			<p class="text-subtitle-1 text-medium-emphasis mb-8">
				{{ $t('common.page-not-found.text') || "The page you are looking for doesn't exist." }}
			</p>
		</div>

		<div class="d-flex flex-column flex-sm-row">
			<v-btn color="primary" prepend-icon="mdi-home" rounded="lg" to="/dashboard" variant="elevated">
				{{ $t('common.button.back-to-dashboard') || 'Back to Dashboard' }}
			</v-btn>
		</div>
	</div>
</template>

<style scoped>
.not-found-image {
	margin: 0 auto;
}
</style>

<script setup lang="ts"></script>
