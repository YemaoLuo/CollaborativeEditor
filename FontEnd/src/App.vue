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
    <MdEditor v-model="text" :theme="theme" :language="language" :toolbars-exclude='exclude'
              @on-change="onChange"
              ref="editorRef"/>
  </div>
</template>

<script setup lang="ts">
import {nextTick, onMounted, ref} from 'vue';
import type {ExposeParam} from 'md-editor-v3';
import {MdEditor} from 'md-editor-v3';
import 'md-editor-v3/lib/style.css';
import '@/App.css';

let theme = ref('light');
let language = ref('en-US');
let text = ref('')
let online = ref('1');
const exclude = ref(['github', 'save']);
const editorRef = ref<ExposeParam>();

let socket;
let socketURL = 'ws://';
socketURL += window.location.host;
socketURL += '/CollaborativeHandler';
console.log('socketURL:', socketURL);

function toggleTheme() {
  theme.value = theme.value === 'light' ? 'dark' : 'light';
}

function toggleLanguage(lang) {
  language.value = lang;
}

const onChange = (change) => {
  console.log('change:', change);
  socket.send(change);
};

function getCursorPositionInDivElement(divElement) {
  const selection = window.getSelection();
  const anchorNode = selection.anchorNode;
  if (anchorNode && divElement.contains(anchorNode)) {
    const textContent = divElement.textContent;
    const anchorOffset = selection.anchorOffset;
    let cursorIndex = 0;
    for (let i = 0; i < textContent.length; i++) {
      if (anchorNode === divElement && anchorOffset === i) {
        break;
      }

      const node = divElement.childNodes[i];
      const nodeTextLength = node.textContent.length;

      if (anchorNode === node) {
        cursorIndex += anchorOffset;
        break;
      }
      cursorIndex += nodeTextLength;
    }
    return cursorIndex;
  }
  return -1;
}

function getCursorPos() {
  const selection = window.getSelection();
  let cursorElementText = '';
  let position = 0;

  if (selection.rangeCount > 0) {
    const range = selection.getRangeAt(0);
    const cursorElement = range.startContainer.parentElement;
    cursorElementText = cursorElement.innerText;
    if (cursorElement.classList.contains('cm-line')) {
      position = getCursorPositionInDivElement(cursorElement);
    }
  }
  let lines = text.value.split('\n');
  let count = 0;
  lines.some((line) => {
    if (line === cursorElementText) {
      return true;
    } else {
      count += line.length + 1;
    }
    return false;
  });

  return count + position;
}

onMounted(() => {
  document.title = 'Collaborative Editor';

  socket = new WebSocket(socketURL);

  socket.addEventListener('message', (event) => {
    const message = JSON.parse(event.data);
    console.log('message:', message);
    if (message.type === 1) {
      online.value = message.message;
    } else if (message.type === 2) {
      let preCursorPos = getCursorPos();
      let preText = text.value;

      text.value = message.message;

      if (preText.length === 0) {
        preCursorPos = message.message.length;
      }

      nextTick(() => {
        console.log('Configuring cursor position:', preCursorPos);
        const option = {
          cursorPos: preCursorPos,
        };
        editorRef.value?.focus(option);
      });
    }
  });
})
</script>