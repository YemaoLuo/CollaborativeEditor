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

let theme = ref('light');
let language = ref('en-US');
let text = ref('');
let online = ref('0');
const exclude = ref(['github', 'save']);
const editorRef = ref<ExposeParam>();

let socket = null;
let socketURL = 'ws://';
let isConnected = ref(false);
socketURL += window.location.host;
socketURL += '/CollaborativeHandler';
console.info('socketURL:', socketURL);

let enableOnChange = true;

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

function updateCursorPosition(oldText, newText, cursorPosition) {
  if (newText.length > oldText.length) {
    let addPos = findAddedTextPositions(oldText, newText);
    let start = addPos[0];
    let end = addPos[1];
    if (end < cursorPosition) {
      return Math.min(cursorPosition + end - start, newText.length - 1);
    } else {
      return cursorPosition;
    }
  } else if (newText.length < oldText.length) {
    let delPos = findDeletedTextPositions(oldText, newText);
    let start = delPos[0];
    let end = delPos[1];
    console.info('start:', start, 'end:', end, 'cursorPosition:', cursorPosition);
    if (start >= cursorPosition) {
      return Math.min(cursorPosition, newText.length);
    } else if (end < cursorPosition) {
      return Math.max(cursorPosition - (end - start + 1), 0);
    } else {
      return Math.max(0, start);
    }
  } else {
    return cursorPosition;
  }
}

function findDeletedTextPositions(oldText, newText) {
  let deletedPositions = [oldText.length];

  for (let i = 0; i < newText.length; i++) {
    if (oldText[i] !== newText[i]) {
      deletedPositions[0] = i;
      break;
    }
  }
  deletedPositions[1] = deletedPositions[0] + oldText.length - newText.length - 1;

  return deletedPositions;
}

function findAddedTextPositions(oldText, newText) {
  let addedPositions = [];

  for (let i = 0; i < oldText.length; i++) {
    if (oldText[i] !== newText[i]) {
      addedPositions[0] = i;
      break;
    }
  }
  addedPositions[1] = addedPositions[0] + newText.length - oldText.length;

  return addedPositions;
}

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
  if (!enableOnChange) {
    return;
  }

  console.info('change:', change);
  if (change.trim() !== '' && socket != null) {
    socket.send(change);
  }
};

onMounted(() => {
  document.title = 'Collaborative Editor';

  socket = new WebSocket(socketURL);

  socket.addEventListener('message', (event) => {
    const message = JSON.parse(event.data);
    console.info('message:', message);
    if (message.type === 1) {
      online.value = message.message;
    } else if (message.type === 2) {
      enableOnChange = false;

      let preCursorPos = getCursorPos(text.value);
      let preText = text.value;

      text.value = message.message;

      if (preText.length === 0) {
        preCursorPos = message.message.length;
      }

      preCursorPos = updateCursorPosition(preText, message.message, preCursorPos);
      nextTick(() => {
        console.info('Configuring cursor position:', preCursorPos);
        const option = {
          cursorPos: preCursorPos,
        };
        editorRef.value?.focus(option);

        enableOnChange = true;
      });
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