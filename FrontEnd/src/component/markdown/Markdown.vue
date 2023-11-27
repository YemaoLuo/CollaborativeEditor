<template>
  <div class="container">
    <h1 class="topic">
      {{ language === 'zh-CN' ? '在线协作Markdown编辑器' : 'Collaborative Markdown Editor' }}
    </h1>
    <div class="button-group">
      <button class="theme-button" :class="{ dark: theme === 'dark' }" @click="toggleTheme">
        {{ theme === 'light' ? 'Dark' : 'Light' }}
      </button>
      <button class="language-button" :class="{ active: language === 'zh-CN' }" @click="toggleLanguage(language)">
        {{ language === 'zh-CN' ? 'English' : '中文' }}
      </button>
      <button class="language-button" onclick="window.location.href = '/';">
        {{ language === 'zh-CN' ? 'Back' : '返回' }}
      </button>
    </div>
    <div class="status-container">
      <div class="status-dot"
           :class="{ 'green-dot': isConnected, 'red-dot': !isConnected }"></div>
    </div>
    <div class="online">
      Online: {{ online }}
    </div>
    <MdEditor v-model="text" :theme="theme" :language="language" :toolbars="toolbars" no-upload-img
              @on-change="onChange"
              ref="editorRef">
      <template #defToolbars>
        <Emoji/>
        <ExportPDF :modelValue="text"/>
      </template>
    </MdEditor>
  </div>
</template>

<script setup lang="ts">
import {onMounted, ref} from 'vue';
import type {ExposeParam} from 'md-editor-v3';
import {MdEditor} from 'md-editor-v3';
import {Emoji, ExportPDF} from '@vavt/v3-extension';
import {toolbars} from './staticConfig';

import '@vavt/v3-extension/lib/asset/style.css';
import 'md-editor-v3/lib/style.css';
import './Markdown.css';

let theme = ref('light');
let language = ref('en-US');
let text = ref('');
let online = ref('0');
const editorRef = ref<ExposeParam>();

let socket = null;
let isConnected = ref(false);

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
  if (lang === 'en-US') {
    language.value = 'zh-CN';
  } else {
    language.value = 'en-US';
  }
}

let onChange = (change) => {

};

onMounted(() => {
  document.title = 'Markdown Editor';

  const urlParams = new URLSearchParams(window.location.search);
  let socketURL = 'ws://';
  socketURL += window.location.host;
  socketURL += '/MDHandler/';
  const id = urlParams.get('id');
  if (id == null) {
    alert('Please enter a valid id.');
  }
  socketURL += id;
  console.info('socketURL:', socketURL);
  socket = new WebSocket(socketURL);

  socket.addEventListener('message', (event) => {
    const message = JSON.parse(event.data);
    console.info('message:', message);
    if (message.type === 1) {
      online.value = message.message;
    } else if (message.type === 2) {
      // TODO

    }
  });

  socket.addEventListener('open', () => {
    isConnected.value = true;
  });

  socket.addEventListener('close', () => {
    isConnected.value = false;
  });
});
</script>

<script lang="ts">
export default {
  name: 'Markdown'
};
</script>