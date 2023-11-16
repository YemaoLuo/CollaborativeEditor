<template>
  <div class="container">
    <h1 class="topic">{{ tittle }}</h1>
    <div class="button-group">
      <button class="theme-button" :class="{ dark: theme === 'dark' }" @click="toggleTheme">
        {{ theme === 'light' ? 'Dark Theme' : 'Light Theme' }}
      </button>
      <button class="language-button" :class="{ active: language === 'zh-CN' }" @click="toggleLanguage('zh-CN')">中文
      </button>
      <button class="language-button" :class="{ active: language === 'en-US' }" @click="toggleLanguage('en-US')">
        English
      </button>
    </div>
    <MdEditor v-model="text" :theme="theme" :language="language"/>
  </div>
</template>

<script setup>
import {onMounted, ref, watch} from 'vue';
import {MdEditor} from 'md-editor-v3';
import 'md-editor-v3/lib/style.css';
import '@/App.css';

const theme = ref('light');
const language = ref('en-US');
const tittle = ref("Collaborative Editor for Markdown")
const text = ref('Hello Editor!');

function toggleTheme() {
  theme.value = theme.value === 'light' ? 'dark' : 'light';
}

function toggleLanguage(lang) {
  language.value = lang;
  if (lang === "en-US") {
    this.tittle = "Collaborative Editor for Markdown";
  } else {
    this.tittle = "在线协作Markdown编辑器";
  }
}

onMounted(() => {
  document.title = 'Collaborative Editor';
});

watch(text, (newValue) => {
  console.log('modified text:', newValue);
});
</script>