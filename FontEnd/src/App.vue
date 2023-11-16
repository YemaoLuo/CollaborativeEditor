<template>
  <div class="container">
    <h1 class="topic">
      {{ language === 'zh-CN' ? '在线协作Markdown编辑器' : 'Collaborative Editor for Markdown' }}
    </h1>
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
    <div class="online">
      Online: {{ online }}
    </div>
    <MdEditor v-model="text" :theme="theme" :language="language" :toolbars-exclude="exclude"/>
  </div>
</template>

<script setup>
import {onMounted, ref, watch} from 'vue';
import {MdEditor} from 'md-editor-v3';
import 'md-editor-v3/lib/style.css';
import '@/App.css';

let theme = ref('light');
let language = ref('en-US');
let text = ref('Hello Editor!');
let online = ref('1');
const exclude = ref(['github', 'save']);

function toggleTheme() {
  theme.value = theme.value === 'light' ? 'dark' : 'light';
}

function toggleLanguage(lang) {
  language.value = lang;
}

onMounted(() => {
  document.title = 'Collaborative Editor';

  let socketURL = 'ws://';
  socketURL += window.location.host;
  socketURL += '/CollaborativeHandler';
  console.log('socketURL:', socketURL);
  const socket = new WebSocket(socketURL);

  watch(text, (newValue) => {
    socket.send(newValue.toString());
  });

  socket.addEventListener('message', (event) => {
    const message = JSON.parse(event.data);
    if (message.type === 1) {
      online.value = message.message;
      console.log('online:', online.value);
    } else if (message.type === 2) {
      text.value = message.message;
      console.log('text:', text.value);
    }
  });
});
</script>