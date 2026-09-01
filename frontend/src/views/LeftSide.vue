<template>
	<div class="login-left">
		<div class="left-content">
			<div class="brand-image">
				<img alt="Boxcore" class="logo-image" src="/logo.svg" />
			</div>
			<h1 class="welcome-text">{{ $t('auth.individual.features.landing-text-1') }}</h1>
			<p class="welcome-subtitle">{{ $t('auth.individual.features.landing-text-2') }}</p>
		</div>
	</div>
</template>

<style scoped>
.login-left {
	flex: 1;
	background-color: rgb(var(--v-theme-marine));
	display: flex;
	align-items: center;
	justify-content: center;
	padding: 2rem;
	color: white;
	position: relative;
	overflow: hidden;
}

.login-left::before {
	content: '';
	position: absolute;
	top: 0;
	left: 0;
	right: 0;
	bottom: 0;
	background: linear-gradient(135deg, rgba(13, 15, 18, 0.92), rgba(255, 106, 26, 0.35));
	z-index: 1;
}

.left-content {
	position: relative;
	z-index: 2;
	max-width: 500px;
	padding: 2rem;
}

.welcome-text {
	font-size: 2.5rem;
	font-weight: 700;
	margin-bottom: 1rem;
	color: white;
	text-align: center;
}

.welcome-subtitle {
	font-size: 2rem;
	margin-bottom: 3rem;
	text-align: center;
}

.brand-image {
	display: flex;
	justify-content: center;
	margin-bottom: 3rem;
}

.logo-image {
	width: 120px;
	height: auto;
}

.features {
	display: flex;
	flex-direction: column;
	gap: 1.5rem;
}

.feature-item {
	display: flex;
	align-items: center;
	gap: 1rem;
}

.feature-icon {
	background-color: rgba(255, 255, 255, 0.2);
	border-radius: 50%;
	padding: 0.5rem;
}

.feature-text {
	font-size: 1.1rem;
	opacity: 0.9;
}
</style>
<script setup lang="ts"></script>
