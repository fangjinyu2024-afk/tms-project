<template>
  <Teleport to="body">
    <div v-if="state.visible" class="toast" :class="state.tone">{{ state.text }}</div>
  </Teleport>
</template>

<script setup lang="ts">
import { useToast } from './useToast'

const { state } = useToast()
</script>
