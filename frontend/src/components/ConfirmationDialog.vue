<template>
	<v-dialog v-model="showDialog" max-width="500px">
		<v-card :class="`text-${level}`" :prepend-icon="icon" :title="title">
			<v-card-text class="pt-4 text-black">
				<slot name="message"></slot>
			</v-card-text>

			<v-card-actions class="text-black">
				<v-spacer></v-spacer>
				<slot name="actions"></slot>
			</v-card-actions>
		</v-card>
	</v-dialog>
</template>

<script setup lang="ts">
import { computed, toRefs } from 'vue';

/*************************************************** Component Setup ****************************************************/
// Props & Emits
const showDialog = defineModel<boolean>('modelValue', { required: true });
const props = defineProps<{
	title: string;
	level: string;
}>();
const { title, level } = toRefs(props);

const icon = computed(() => {
	return level.value === 'error' ? 'mdi-alert' : '';
});

/*************************************************** State Management ****************************************************/
</script>
