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
    <div class="status-container">
      <div class="status-dot"
           :class="{ 'green-dot': isConnected, 'red-dot': !isConnected }"></div>
      <div class="status-tooltip">{{ getStatusTooltip }}</div>
    </div>
    <div class="online">
      Online: {{ online }}
    </div>
    <MdEditor v-model="text" :theme="theme" :language="language" :toolbars-exclude='exclude' no-upload-img
              @on-change="onChange"
              ref="editorRef"/>
  </div>
</template>

<script setup lang="ts">
import {computed, nextTick, onMounted, ref} from 'vue';
import type {ExposeParam} from 'md-editor-v3';
import {MdEditor} from 'md-editor-v3';
import 'md-editor-v3/lib/style.css';
import '@/App.css';
import {getCursorPos, updateCursorPosition} from '@/utils/cursor';

let theme = ref('light');
let language = ref('en-US');
let text = ref('')
let online = ref('0');
const exclude = ref(['github', 'save']);
const editorRef = ref<ExposeParam>();

let socket = null;
let socketURL = 'ws://';
let isConnected = ref(false);
socketURL += window.location.host;
socketURL += '/CollaborativeHandler';
console.debug('socketURL:', socketURL);

function toggleTheme() {
  theme.value = theme.value === 'light' ? 'dark' : 'light';
  if (theme.value === 'dark') {
    document.body.style.backgroundColor = 'black';
    document.body.style.color = 'white';
  } else {
    document.body.style.backgroundColor = '';
    document.body.style.color = '';
  }
}

function toggleLanguage(lang) {
  language.value = lang;
}

const getStatusTooltip = computed(() => {
  if (isConnected.value) {
    return language.value === 'zh-CN' ? '服务器连接成功' : 'Connected to server';
  } else {
    return language.value === 'zh-CN' ? '服务器断开连接' : 'Disconnected from server';
  }
});

let onChange = (change) => {
  console.debug('change:', change);
  if (socket != null) {
    socket.send(change);
  }
};

onMounted(() => {
  document.title = 'Collaborative Editor';

  socket = new WebSocket(socketURL);

  socket.addEventListener('message', (event) => {
    const message = JSON.parse(event.data);
    console.debug('message:', message);
    if (message.type === 1) {
      online.value = message.message;
    } else if (message.type === 2) {
      let preCursorPos = getCursorPos();
      let preText = text.value;

      text.value = message.message;

      if (preText.length === 0) {
        preCursorPos = message.message.length;
      }

      preCursorPos = updateCursorPosition(preText, message.message, preCursorPos);
      nextTick(() => {
        console.debug('Configuring cursor position:', preCursorPos);
        const option = {
          cursorPos: preCursorPos,
        };
        editorRef.value?.focus(option);
      });
    }
  });

  socket.addEventListener('open', () => {
    isConnected.value = true;
  });

  socket.addEventListener('close', () => {
    isConnected.value = false;
  });
})
</script>